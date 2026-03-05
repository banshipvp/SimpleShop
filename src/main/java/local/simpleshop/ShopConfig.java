package local.simpleshop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Persists shop categories and items to shop.yml.
 *
 * Category data layout in YAML:
 *   categories:
 *     blocks:
 *       position: 1
 *       display-name: "Blocks"
 *       icon: STONE
 *       color: "§b§l"
 *       tagline: "Building materials"
 *       detail: "Stone, Wood, Glass & more"
 *       items:
 *         stone:
 *           display-name: "Stone"
 *           buy: 2.0
 *           sell: 1.0
 *         ...
 *   spawners:
 *     chicken:
 *       display-name: "Chicken"
 *       buy: 40000.0
 *       faction-value: 40000.0
 */
public class ShopConfig {

    private final SimpleShopPlugin plugin;
    private File file;
    private FileConfiguration yaml;

    // Ordered list of category ids as defined by their position field
    private final List<String> categoryOrder = new ArrayList<>();

    // category id -> ShopCategory
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    // Spawner entries (separate section)
    private final List<ShopGUI.SpawnerEntry> spawnerEntries = new ArrayList<>();

    // -------------------------------------------------------------------------

    public ShopConfig(SimpleShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Load / Save ─────────────────────────────────────────────────────────

    public void load() {
        file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        parse();
    }

    public void save() {
        // Rewrite categories section
        yaml.set("categories", null);
        for (ShopCategory cat : categories.values()) {
            String base = "categories." + cat.id;
            yaml.set(base + ".position", cat.position);
            yaml.set(base + ".display-name", cat.displayName);
            yaml.set(base + ".icon", cat.icon.name());
            yaml.set(base + ".color", cat.color);
            yaml.set(base + ".tagline", cat.tagline);
            yaml.set(base + ".detail", cat.detail);
            yaml.set(base + ".items", null);
            for (ShopGUI.ShopItem item : cat.items) {
                String iBase = base + ".items." + item.material.name().toLowerCase();
                yaml.set(iBase + ".display-name", item.name);
                yaml.set(iBase + ".buy", item.buyPrice);
                yaml.set(iBase + ".sell", item.sellPrice);
            }
        }

        // Rewrite spawners section
        yaml.set("spawners", null);
        for (ShopGUI.SpawnerEntry e : spawnerEntries) {
            String base = "spawners." + e.entityType.name().toLowerCase();
            yaml.set(base + ".display-name", e.displayName);
            yaml.set(base + ".buy", e.buyPrice);
            yaml.set(base + ".faction-value", e.factionValue);
        }

        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save shop.yml: " + ex.getMessage());
        }
    }

    // ── Parse from YAML ──────────────────────────────────────────────────────

    private void parse() {
        categories.clear();
        spawnerEntries.clear();

        ConfigurationSection catSection = yaml.getConfigurationSection("categories");
        if (catSection != null) {
            // Load each category, then sort by position
            List<ShopCategory> loaded = new ArrayList<>();
            for (String id : catSection.getKeys(false)) {
                ConfigurationSection cs = catSection.getConfigurationSection(id);
                if (cs == null) continue;

                ShopCategory cat = new ShopCategory();
                cat.id          = id;
                cat.position    = cs.getInt("position", 99);
                cat.displayName = cs.getString("display-name", id);
                cat.icon        = parseMaterial(cs.getString("icon", "CHEST"), Material.CHEST);
                cat.color       = cs.getString("color", "§f§l");
                cat.tagline     = cs.getString("tagline", "");
                cat.detail      = cs.getString("detail", "");

                ConfigurationSection items = cs.getConfigurationSection("items");
                if (items != null) {
                    for (String matKey : items.getKeys(false)) {
                        ConfigurationSection is = items.getConfigurationSection(matKey);
                        if (is == null) continue;
                        Material mat = parseMaterial(matKey.toUpperCase(), null);
                        if (mat == null) continue;
                        String displayName = is.getString("display-name", matKey);
                        double buy  = is.getDouble("buy", 0);
                        double sell = is.getDouble("sell", 0);
                        cat.items.add(new ShopGUI.ShopItem(mat, displayName, buy, sell));
                    }
                }
                loaded.add(cat);
            }

            loaded.sort(Comparator.comparingInt(c -> c.position));
            for (ShopCategory cat : loaded) {
                categories.put(cat.id, cat);
            }
        }

        ConfigurationSection spawnSection = yaml.getConfigurationSection("spawners");
        if (spawnSection != null) {
            for (String key : spawnSection.getKeys(false)) {
                ConfigurationSection ss = spawnSection.getConfigurationSection(key);
                if (ss == null) continue;
                try {
                    EntityType et = EntityType.valueOf(key.toUpperCase());
                    String displayName = ss.getString("display-name", key);
                    double buy   = ss.getDouble("buy", 0);
                    double fv    = ss.getDouble("faction-value", buy);
                    spawnerEntries.add(new ShopGUI.SpawnerEntry(et, displayName, buy, fv));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // ── Public accessors ─────────────────────────────────────────────────────

    /** All categories in display order. */
    public List<ShopCategory> getCategoriesOrdered() {
        List<ShopCategory> list = new ArrayList<>(categories.values());
        list.sort(Comparator.comparingInt(c -> c.position));
        return list;
    }

    public ShopCategory getCategory(String id) {
        return categories.get(id.toLowerCase());
    }

    public Collection<ShopCategory> getAllCategories() {
        return categories.values();
    }

    public List<ShopGUI.SpawnerEntry> getSpawnerEntries() {
        return spawnerEntries;
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    /**
     * Add or update an item in a category.
     * Creates the category with sensible defaults if it doesn't exist.
     */
    public boolean addItem(String categoryId, Material material, String displayName,
                           double buy, double sell) {
        ShopCategory cat = categories.computeIfAbsent(categoryId.toLowerCase(), id -> {
            ShopCategory newCat = new ShopCategory();
            newCat.id          = id;
            newCat.displayName = capitalize(id);
            newCat.icon        = material;
            newCat.color       = "§f§l";
            newCat.tagline     = newCat.displayName;
            newCat.detail      = "";
            newCat.position    = nextPosition();
            return newCat;
        });

        // Replace if exists
        cat.items.removeIf(i -> i.material == material);
        cat.items.add(new ShopGUI.ShopItem(material, displayName, buy, sell));
        save();
        return true;
    }

    /**
     * Add a new category. Returns false if it already exists.
     */
    public boolean addCategory(String id, String displayName, Material icon, int position) {
        id = id.toLowerCase();
        if (categories.containsKey(id)) return false;

        // Shift existing categories at or after requested position up by 1
        if (position > 0) {
            for (ShopCategory c : categories.values()) {
                if (c.position >= position) c.position++;
            }
        } else {
            position = nextPosition();
        }

        ShopCategory cat = new ShopCategory();
        cat.id          = id;
        cat.displayName = displayName;
        cat.icon        = icon;
        cat.color       = "§f§l";
        cat.tagline     = displayName;
        cat.detail      = "";
        cat.position    = position;
        categories.put(id, cat);
        save();
        return true;
    }

    /**
     * Move a category to a new position, shifting others out of the way.
     * Returns false if category not found.
     */
    public boolean setCategoryPosition(String id, int newPos) {
        ShopCategory target = categories.get(id.toLowerCase());
        if (target == null) return false;

        int oldPos = target.position;
        if (oldPos == newPos) return true;

        // Shift everything in between
        for (ShopCategory c : categories.values()) {
            if (c == target) continue;
            if (newPos < oldPos) {
                // Moving up: shift down anything in [newPos, oldPos)
                if (c.position >= newPos && c.position < oldPos) c.position++;
            } else {
                // Moving down: shift up anything in (oldPos, newPos]
                if (c.position > oldPos && c.position <= newPos) c.position--;
            }
        }
        target.position = newPos;
        save();
        return true;
    }

    /** Update a category's display name. */
    public boolean setCategoryName(String id, String name) {
        ShopCategory cat = categories.get(id.toLowerCase());
        if (cat == null) return false;
        cat.displayName = name;
        save();
        return true;
    }

    /** Update a category's icon material. */
    public boolean setCategoryIcon(String id, Material icon) {
        ShopCategory cat = categories.get(id.toLowerCase());
        if (cat == null) return false;
        cat.icon = icon;
        save();
        return true;
    }

    /** Update a category's tagline/detail. */
    public boolean setCategoryTagline(String id, String tagline, String detail) {
        ShopCategory cat = categories.get(id.toLowerCase());
        if (cat == null) return false;
        cat.tagline = tagline;
        cat.detail  = detail;
        save();
        return true;
    }

    /** Update a category's color prefix. */
    public boolean setCategoryColor(String id, String color) {
        ShopCategory cat = categories.get(id.toLowerCase());
        if (cat == null) return false;
        cat.color = color;
        save();
        return true;
    }

    /** Remove an item from a category by material. Returns false if not found. */
    public boolean removeItem(String categoryId, Material material) {
        ShopCategory cat = categories.get(categoryId.toLowerCase());
        if (cat == null) return false;
        boolean removed = cat.items.removeIf(i -> i.material == material);
        if (removed) save();
        return removed;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int nextPosition() {
        return categories.values().stream().mapToInt(c -> c.position).max().orElse(0) + 1;
    }

    private static Material parseMaterial(String name, Material fallback) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    public static class ShopCategory {
        public String id;
        public int position;
        public String displayName;
        public Material icon;
        public String color;
        public String tagline;
        public String detail;
        public final List<ShopGUI.ShopItem> items = new ArrayList<>();
    }
}
