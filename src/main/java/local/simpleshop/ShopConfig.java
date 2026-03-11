package local.simpleshop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        if (shouldNormalizeYamlFormat()) {
            save();
        }
        ensureDefaultCategories();
    }

    /** Adds wool/carpet and glass categories if they don't already exist. */
    private void ensureDefaultCategories() {
        boolean changed = false;

        if (!categories.containsKey("wool_carpet")) {
            ShopCategory cat = new ShopCategory();
            cat.id          = "wool_carpet";
            cat.displayName = "Wool & Carpet";
            cat.icon        = Material.valueOf("WHITE_WOOL");
            cat.color       = "§d§l";
            cat.tagline     = "Dye your world";
            cat.detail      = "All 16 colours of wool & carpet";
            cat.position    = nextPosition();
            String[][] wools = {
                {"WHITE_WOOL","White Wool"},{"ORANGE_WOOL","Orange Wool"},
                {"MAGENTA_WOOL","Magenta Wool"},{"LIGHT_BLUE_WOOL","Light Blue Wool"},
                {"YELLOW_WOOL","Yellow Wool"},{"LIME_WOOL","Lime Wool"},
                {"PINK_WOOL","Pink Wool"},{"GRAY_WOOL","Gray Wool"},
                {"LIGHT_GRAY_WOOL","Light Gray Wool"},{"CYAN_WOOL","Cyan Wool"},
                {"PURPLE_WOOL","Purple Wool"},{"BLUE_WOOL","Blue Wool"},
                {"BROWN_WOOL","Brown Wool"},{"GREEN_WOOL","Green Wool"},
                {"RED_WOOL","Red Wool"},{"BLACK_WOOL","Black Wool"},
                {"WHITE_CARPET","White Carpet"},{"ORANGE_CARPET","Orange Carpet"},
                {"MAGENTA_CARPET","Magenta Carpet"},{"LIGHT_BLUE_CARPET","Light Blue Carpet"},
                {"YELLOW_CARPET","Yellow Carpet"},{"LIME_CARPET","Lime Carpet"},
                {"PINK_CARPET","Pink Carpet"},{"GRAY_CARPET","Gray Carpet"},
                {"LIGHT_GRAY_CARPET","Light Gray Carpet"},{"CYAN_CARPET","Cyan Carpet"},
                {"PURPLE_CARPET","Purple Carpet"},{"BLUE_CARPET","Blue Carpet"},
                {"BROWN_CARPET","Brown Carpet"},{"GREEN_CARPET","Green Carpet"},
                {"RED_CARPET","Red Carpet"},{"BLACK_CARPET","Black Carpet"}
            };
            for (String[] w : wools) {
                try {
                    cat.items.add(new ShopGUI.ShopItem(Material.valueOf(w[0]), w[1], 5.0, 2.0));
                } catch (IllegalArgumentException ignored) {}
            }
            categories.put("wool_carpet", cat);
            changed = true;
        }

        if (!categories.containsKey("glass")) {
            ShopCategory cat = new ShopCategory();
            cat.id          = "glass";
            cat.displayName = "Glass";
            cat.icon        = Material.GLASS;
            cat.color       = "§b§l";
            cat.tagline     = "Transparent building";
            cat.detail      = "Glass, panes & all stained variants";
            cat.position    = nextPosition();
            String[][] glasses = {
                {"GLASS","Glass"},{"GLASS_PANE","Glass Pane"},
                {"WHITE_STAINED_GLASS","White Glass"},{"WHITE_STAINED_GLASS_PANE","White Glass Pane"},
                {"ORANGE_STAINED_GLASS","Orange Glass"},{"ORANGE_STAINED_GLASS_PANE","Orange Glass Pane"},
                {"MAGENTA_STAINED_GLASS","Magenta Glass"},{"MAGENTA_STAINED_GLASS_PANE","Magenta Glass Pane"},
                {"LIGHT_BLUE_STAINED_GLASS","Light Blue Glass"},{"LIGHT_BLUE_STAINED_GLASS_PANE","Light Blue Glass Pane"},
                {"YELLOW_STAINED_GLASS","Yellow Glass"},{"YELLOW_STAINED_GLASS_PANE","Yellow Glass Pane"},
                {"LIME_STAINED_GLASS","Lime Glass"},{"LIME_STAINED_GLASS_PANE","Lime Glass Pane"},
                {"PINK_STAINED_GLASS","Pink Glass"},{"PINK_STAINED_GLASS_PANE","Pink Glass Pane"},
                {"GRAY_STAINED_GLASS","Gray Glass"},{"GRAY_STAINED_GLASS_PANE","Gray Glass Pane"},
                {"LIGHT_GRAY_STAINED_GLASS","Light Gray Glass"},{"LIGHT_GRAY_STAINED_GLASS_PANE","Light Gray Glass Pane"},
                {"CYAN_STAINED_GLASS","Cyan Glass"},{"CYAN_STAINED_GLASS_PANE","Cyan Glass Pane"},
                {"PURPLE_STAINED_GLASS","Purple Glass"},{"PURPLE_STAINED_GLASS_PANE","Purple Glass Pane"},
                {"BLUE_STAINED_GLASS","Blue Glass"},{"BLUE_STAINED_GLASS_PANE","Blue Glass Pane"},
                {"BROWN_STAINED_GLASS","Brown Glass"},{"BROWN_STAINED_GLASS_PANE","Brown Glass Pane"},
                {"GREEN_STAINED_GLASS","Green Glass"},{"GREEN_STAINED_GLASS_PANE","Green Glass Pane"},
                {"RED_STAINED_GLASS","Red Glass"},{"RED_STAINED_GLASS_PANE","Red Glass Pane"},
                {"BLACK_STAINED_GLASS","Black Glass"},{"BLACK_STAINED_GLASS_PANE","Black Glass Pane"}
            };
            for (String[] g : glasses) {
                try {
                    cat.items.add(new ShopGUI.ShopItem(Material.valueOf(g[0]), g[1], 5.0, 2.0));
                } catch (IllegalArgumentException ignored) {}
            }
            categories.put("glass", cat);
            changed = true;
        }

        if (changed) save();
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
                cat.displayName = cs.getString("display-name",
                        cs.getString("displayName", capitalizeWords(id)));
                cat.icon        = parseMaterial(cs.getString("icon", "CHEST"), Material.CHEST);
                cat.color       = cs.getString("color", "§f§l");
                cat.tagline     = cs.getString("tagline", "");
                cat.detail      = cs.getString("detail", "");

                loadCategoryItems(cat, cs);
                loaded.add(cat);
            }

            loaded.sort(Comparator.comparingInt(c -> c.position));
            for (ShopCategory cat : loaded) {
                categories.put(cat.id, cat);
            }
        }

        ConfigurationSection spawnSection = yaml.getConfigurationSection("spawners");
        loadSpawners(spawnSection);

        if (spawnerEntries.isEmpty()) {
            loadFallbackSpawnersFromBundledConfig();
            if (!spawnerEntries.isEmpty()) {
                plugin.getLogger().warning("shop.yml has no spawners section; restored default spawner entries.");
                save();
            }
        }
    }

    private void loadFallbackSpawnersFromBundledConfig() {
        var in = plugin.getResource("shop.yml");
        if (in == null) {
            return;
        }

        FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(in, StandardCharsets.UTF_8));

        Object raw = defaults.get("spawners");
        if (raw instanceof ConfigurationSection section) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection ss = section.getConfigurationSection(key);
                if (ss == null) continue;
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    String fallbackName = capitalizeWords(key.replace('_', ' '));
                    String displayName = ss.getString("display-name",
                            ss.getString("displayName", fallbackName));
                    double buy = ss.getDouble("buy", ss.getDouble("buyPrice", 0.0));
                    double factionValue = ss.getDouble("faction-value",
                            ss.getDouble("factionValue", buy));
                    spawnerEntries.add(new ShopGUI.SpawnerEntry(type, displayName, buy, factionValue));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return;
        }

        if (!(raw instanceof List)) {
            return;
        }

        for (Map<?, ?> row : defaults.getMapList("spawners")) {
            String entityRaw = Objects.toString(row.get("entityType"), "");
            if (entityRaw.isEmpty()) continue;

            try {
                EntityType type = EntityType.valueOf(entityRaw.toUpperCase());
                String fallbackName = capitalizeWords(entityRaw.replace('_', ' '));
                Object displayNameRaw = valueOr(valueOf(row, "display-name"), valueOf(row, "displayName"));
                String displayName = Objects.toString(displayNameRaw, fallbackName);
                double buy = toDouble(valueOr(valueOf(row, "buy"), valueOf(row, "buyPrice")), 0.0);
                double factionValue = toDouble(valueOr(valueOf(row, "faction-value"), valueOf(row, "factionValue")), buy);
                spawnerEntries.add(new ShopGUI.SpawnerEntry(type, displayName, buy, factionValue));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void loadCategoryItems(ShopCategory cat, ConfigurationSection categorySection) {
        Object rawItems = categorySection.get("items");

        if (rawItems instanceof ConfigurationSection itemsSection) {
            // New format:
            // items:
            //   stone:
            //     display-name: "Stone"
            //     buy: 2.0
            //     sell: 1.0
            for (String matKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(matKey);
                if (itemSection == null) continue;

                Material material = parseMaterial(matKey.toUpperCase(), null);
                if (material == null) continue;

                String fallbackName = capitalizeWords(matKey.replace('_', ' '));
                String displayName = itemSection.getString("display-name",
                        itemSection.getString("name", fallbackName));
                double buy = itemSection.getDouble("buy", 0.0);
                double sell = itemSection.getDouble("sell", 0.0);
                cat.items.add(new ShopGUI.ShopItem(material, displayName, buy, sell));
            }
            return;
        }

        // Legacy format:
        // items:
        //   - material: "STONE"
        //     name: "Stone"
        //     buy: 2.0
        //     sell: 1.0
        for (Map<?, ?> entry : categorySection.getMapList("items")) {
            Object matObj = entry.get("material");
            if (matObj == null) continue;

            Material material = parseMaterial(matObj.toString(), null);
            if (material == null) continue;

            String fallbackName = capitalizeWords(material.name().replace('_', ' '));
                Object displayNameRaw = valueOr(valueOf(entry, "display-name"), valueOf(entry, "name"));
                String displayName = Objects.toString(displayNameRaw, fallbackName);

            double buy = toDouble(entry.get("buy"), 0.0);
            double sell = toDouble(entry.get("sell"), 0.0);

            cat.items.add(new ShopGUI.ShopItem(material, displayName, buy, sell));
        }
    }

    private void loadSpawners(ConfigurationSection spawnSection) {
        Object raw = yaml.get("spawners");
        if (raw instanceof ConfigurationSection section) {
            // New format:
            // spawners:
            //   zombie:
            //     display-name: "Zombie"
            //     buy: 100000
            //     faction-value: 100000
            for (String key : section.getKeys(false)) {
                ConfigurationSection ss = section.getConfigurationSection(key);
                if (ss == null) continue;
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    String fallbackName = capitalizeWords(key.replace('_', ' '));
                    String displayName = ss.getString("display-name",
                            ss.getString("displayName", fallbackName));
                    double buy = ss.getDouble("buy", ss.getDouble("buyPrice", 0.0));
                    double factionValue = ss.getDouble("faction-value",
                            ss.getDouble("factionValue", buy));
                    spawnerEntries.add(new ShopGUI.SpawnerEntry(type, displayName, buy, factionValue));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return;
        }

        if (!(raw instanceof List)) {
            return;
        }

        // Legacy list format
        for (Map<?, ?> row : yaml.getMapList("spawners")) {
            String entityRaw = Objects.toString(row.get("entityType"), "");
            if (entityRaw.isEmpty()) continue;

            try {
                EntityType type = EntityType.valueOf(entityRaw.toUpperCase());
                String fallbackName = capitalizeWords(entityRaw.replace('_', ' '));
                Object displayNameRaw = valueOr(valueOf(row, "display-name"), valueOf(row, "displayName"));
                String displayName = Objects.toString(displayNameRaw, fallbackName);
                double buy = toDouble(valueOr(valueOf(row, "buy"), valueOf(row, "buyPrice")), 0.0);
                double factionValue = toDouble(valueOr(valueOf(row, "faction-value"), valueOf(row, "factionValue")), buy);
                spawnerEntries.add(new ShopGUI.SpawnerEntry(type, displayName, buy, factionValue));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static Object valueOf(Map<?, ?> map, String key) {
        return map.get(key);
    }

    private static Object valueOr(Object primary, Object fallback) {
        return primary != null ? primary : fallback;
    }

    private static double toDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean shouldNormalizeYamlFormat() {
        ConfigurationSection categoriesSection = yaml.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String id : categoriesSection.getKeys(false)) {
                ConfigurationSection cs = categoriesSection.getConfigurationSection(id);
                if (cs == null) continue;

                if (cs.contains("displayName")) return true;

                Object rawItems = cs.get("items");
                if (rawItems instanceof List) return true;
            }
        }

        Object rawSpawners = yaml.get("spawners");
        return rawSpawners instanceof List;
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

    private static String capitalizeWords(String s) {
        if (s == null || s.isBlank()) return s;
        String[] parts = s.trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) out.append(' ');
            out.append(capitalize(parts[i]));
        }
        return out.toString();
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
