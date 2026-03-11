package local.simpleshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Shop GUI – rendering driven by ShopConfig / shop.yml.
 */
public class ShopGUI implements Listener {

    private static final String MAIN_TITLE = "§6§lSimpleShop";
    private static final String CATEGORY_TITLE_PREFIX = "§8§l[ §6§l";
    private static final int ITEMS_PER_PAGE = 36; // slots 9-44

    private final SimpleShopPlugin plugin;
    private final Economy economy;
    private final ShopConfig shopConfig;

    // Track which category and page each player is viewing
    private final Map<UUID, String>  playerCategory = new HashMap<>();
    private final Map<UUID, Integer> playerPage     = new HashMap<>();

    public ShopGUI(SimpleShopPlugin plugin, Economy economy, ShopConfig shopConfig) {
        this.plugin      = plugin;
        this.economy     = economy;
        this.shopConfig  = shopConfig;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // =========================================================================
    //  Main market screen
    // =========================================================================

    public void openMainShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);

        // Gray interior fill
        ItemStack gray  = grayPane();
        ItemStack black = blackPane();
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        // Black top and bottom border rows
        for (int i = 0;  i < 9;  i++) inv.setItem(i, black); // row 1
        for (int i = 45; i < 54; i++) inv.setItem(i, black); // row 6

        // Collect category items
        List<ShopConfig.ShopCategory> ordered = shopConfig.getCategoriesOrdered();
        boolean hasSpawners = !shopConfig.getSpawnerEntries().isEmpty();
        List<ItemStack> items = new ArrayList<>();
        for (ShopConfig.ShopCategory cat : ordered) {
            items.add(createCategoryIcon(cat.icon, cat.color + cat.displayName, cat.tagline, cat.detail));
        }
        if (hasSpawners) {
            items.add(createCategoryIcon(Material.SPAWNER, "§b§lSpawners", "Mob spawner shop", "Buy custom spawners"));
        }

        // Black column borders (left col=0,9,18,27,36 and right col=8,17,26,35,44)
        // to give a framed look
        for (int row = 1; row <= 4; row++) {
            inv.setItem(row * 9,     black);
            inv.setItem(row * 9 + 8, black);
        }

        // Place items across inner rows (rows 2+3 if >7, otherwise centred in row 3)
        if (items.size() <= 7) {
            List<Integer> slots = spacedSlots(items.size(), 19, 7);
            for (int i = 0; i < items.size(); i++) inv.setItem(slots.get(i), items.get(i));
        } else {
            int half = (items.size() + 1) / 2;
            List<Integer> row1 = spacedSlots(half,               10, 7);
            List<Integer> row2 = spacedSlots(items.size() - half, 19, 7);
            for (int i = 0; i < row1.size(); i++) inv.setItem(row1.get(i), items.get(i));
            for (int i = 0; i < row2.size(); i++) inv.setItem(row2.get(i), items.get(half + i));
        }

        // Bottom bar: back-arrow at 45, balance at 49 centred, sell at 53
        inv.setItem(45, grayPane());
        inv.setItem(49, createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
        inv.setItem(53, createIcon(Material.EMERALD, "§a§lSell Inventory",
                "§7Instantly sells all eligible",
                "§7items in your inventory.",
                "§8────────────────────",
                "§b§l▸ §eClick to sell all!"));

        player.openInventory(inv);
    }

    // =========================================================================
    //  Category page
    // =========================================================================

    public void openCategory(Player player, String categoryId) {
        openCategory(player, categoryId, 0);
    }

    public void openCategory(Player player, String categoryId, int page) {
        if ("spawners".equals(categoryId)) {
            openSpawnersCategory(player);
            return;
        }

        ShopConfig.ShopCategory cat = shopConfig.getCategory(categoryId);
        if (cat == null) return;

        int totalItems = cat.items.size();
        int totalPages = Math.max(1, (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int safePage   = Math.max(0, Math.min(page, totalPages - 1));

        playerCategory.put(player.getUniqueId(), categoryId);
        playerPage.put(player.getUniqueId(), safePage);

        Inventory inv = Bukkit.createInventory(null, 54, CATEGORY_TITLE_PREFIX + cat.displayName + " §8§l]");

        // Gray interior, black top/bottom border rows
        ItemStack gray  = grayPane();
        ItemStack black = blackPane();
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);
        for (int i = 0;  i < 9;  i++) inv.setItem(i, black);
        for (int i = 45; i < 54; i++) inv.setItem(i, black);

        // Category header centred in top border row
        inv.setItem(4, createCategoryHeaderIcon(cat));

        // Items fill rows 2-5 (slots 9-44) for current page
        int startIdx = safePage * ITEMS_PER_PAGE;
        int slot = 9;
        for (int i = startIdx; i < Math.min(startIdx + ITEMS_PER_PAGE, totalItems); i++) {
            if (slot >= 45) break;
            inv.setItem(slot++, createShopItemIcon(cat.items.get(i)));
        }

        renderCategoryFooter(inv, player, safePage, totalPages);
        player.openInventory(inv);
    }

    // =========================================================================
    //  Spawners page
    // =========================================================================

    private void openSpawnersCategory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, CATEGORY_TITLE_PREFIX + "Spawners §8§l]");

        // Gray interior, black top/bottom border rows
        ItemStack gray  = grayPane();
        ItemStack black = blackPane();
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);
        for (int i = 0;  i < 9;  i++) inv.setItem(i, black);
        for (int i = 45; i < 54; i++) inv.setItem(i, black);

        // Spawner header centred in top border row
        inv.setItem(4, createIcon(Material.SPAWNER, "§b§lSpawners",
                "§7Left-click §8→ §abuy 1", "§7Shift-click §8→ §abuy 16"));

        // Items fill rows 2-5 (slots 9-44)
        int slot = 9;
        for (SpawnerEntry entry : shopConfig.getSpawnerEntries()) {
            if (slot >= 45) break;
            inv.setItem(slot++, createSpawnerDisplayItem(entry));
        }

        renderCategoryFooter(inv, player);
        player.openInventory(inv);
    }

    // =========================================================================
    //  Click handler
    // =========================================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        boolean isMainMenu = MAIN_TITLE.equals(title);
        boolean isCategoryMenu = !isMainMenu && title.startsWith(CATEGORY_TITLE_PREFIX);
        if (!isMainMenu && !isCategoryMenu) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        // ── ignore decorations ───────────────────────────────────────────────
        if (displayName.equals("§r")) return;

        // ── Main market ──────────────────────────────────────────────────────
        if (isMainMenu) {
            if (displayName.contains("Your Balance")) return;

            // Sell Inventory
            if (displayName.contains("Sell Inventory")) {
                double total = performSellAll(player);
                if (total > 0) {
                    player.sendMessage("§a✓ Sold inventory for §e$" + formatMoney(total));
                } else {
                    player.sendMessage("§cNo sellable items found.");
                }
                event.getView().getTopInventory().setItem(49, createIcon(
                        Material.GOLD_INGOT, "§e§lYour Balance",
                        "§a$" + formatMoney(economy.getBalance(player))));
                return;

            }

            if (displayName.contains("Spawners") || displayName.contains("§b§lSpawners")) {
                openSpawnersCategory(player);
                return;
            }

            // Category click — match by display name against known categories
            for (ShopConfig.ShopCategory cat : shopConfig.getCategoriesOrdered()) {
                if (displayName.contains(cat.displayName)) {
                    openCategory(player, cat.id);
                    return;
                }
            }
            return;
        }

        // ── Spawners page ─────────────────────────────────────────────────────
        if (title.contains("Spawners")) {
            if (displayName.contains("Back to Market")) { openMainShop(player); return; }
            if (displayName.contains("Your Balance")) return;

            if (clicked.getType() == Material.SPAWNER
                    && clicked.getItemMeta() instanceof BlockStateMeta bsm) {
                BlockState state = bsm.getBlockState();
                if (state instanceof CreatureSpawner cs && cs.getSpawnedType() != null) {
                    SpawnerEntry entry = findSpawnerEntry(cs.getSpawnedType());
                    if (entry != null) {
                        buySpawner(player, entry, event.isShiftClick() ? 16 : 1);
                        event.getView().getTopInventory().setItem(49, createIcon(
                                Material.GOLD_INGOT, "§e§lYour Balance",
                                "§a$" + formatMoney(economy.getBalance(player))));
                    }
                }
            }
            return;
        }

        // ── Category item pages ───────────────────────────────────────────────
        if (displayName.contains("Back to Market")) { openMainShop(player); return; }
        if (displayName.contains("Your Balance")) return;
        // Pagination buttons
        if (displayName.contains("« Previous Page")) {
            String catId = playerCategory.get(player.getUniqueId());
            int pg = playerPage.getOrDefault(player.getUniqueId(), 0);
            if (catId != null && pg > 0) openCategory(player, catId, pg - 1);
            return;
        }
        if (displayName.contains("Next Page »")) {
            String catId = playerCategory.get(player.getUniqueId());
            int pg = playerPage.getOrDefault(player.getUniqueId(), 0);
            if (catId != null) openCategory(player, catId, pg + 1);
            return;
        }
        ShopItem shopItem = findShopItem(clicked.getType());
        if (shopItem == null) return;

        int amount = event.isShiftClick() ? 64 : 1;
        if (event.isLeftClick()) {
            buyItem(player, shopItem, amount);
        } else {
            sellItem(player, shopItem, amount);
        }
        event.getView().getTopInventory().setItem(49, createIcon(
                Material.GOLD_INGOT, "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
    }

    // =========================================================================
    //  Buy / Sell logic
    // =========================================================================

    private void buyItem(Player player, ShopItem item, int amount) {
        double cost = item.buyPrice * amount;
        if (economy.getBalance(player) < cost) {
            player.sendMessage("§cNot enough money! Need §e$" + formatMoney(cost)); return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cInventory full!"); return;
        }
        economy.withdrawPlayer(player, cost);
        player.getInventory().addItem(new ItemStack(item.material, amount));
        player.sendMessage("§a✓ Bought §e" + amount + "x " + item.name + " §afor §e$" + formatMoney(cost));
        player.sendMessage("§7Balance: §a$" + formatMoney(economy.getBalance(player)));
    }

    private void sellItem(Player player, ShopItem item, int amount) {
        ItemStack check = new ItemStack(item.material, amount);
        if (!player.getInventory().containsAtLeast(check, amount)) {
            player.sendMessage("§cYou don't have §e" + amount + "x " + item.name); return;
        }
        double price = item.sellPrice * amount;
        player.getInventory().removeItem(new ItemStack(item.material, amount));
        economy.depositPlayer(player, price);
        player.sendMessage("§a✓ Sold §e" + amount + "x " + item.name + " §afor §e$" + formatMoney(price));
        player.sendMessage("§7Balance: §a$" + formatMoney(economy.getBalance(player)));
    }

    private void buySpawner(Player player, SpawnerEntry entry, int amount) {
        double cost = entry.buyPrice * amount;
        if (economy.getBalance(player) < cost) {
            player.sendMessage("§cNot enough money! Need §e$" + formatMoney(cost)); return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cInventory full!"); return;
        }
        economy.withdrawPlayer(player, cost);
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, amount);
        if (spawnerItem.getItemMeta() instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof CreatureSpawner cs) {
                cs.setSpawnedType(entry.entityType);
                bsm.setBlockState(cs);
            }
            bsm.setDisplayName("§6" + entry.displayName + " Spawner");
            bsm.setLore(List.of("§7Place to spawn §f" + entry.displayName));
            spawnerItem.setItemMeta(bsm);
        }
        player.getInventory().addItem(spawnerItem);
        player.sendMessage("§a✓ Bought §e" + amount + "x " + entry.displayName + " Spawner §afor §e$" + formatMoney(cost));
        player.sendMessage("§7Balance: §a$" + formatMoney(economy.getBalance(player)));
    }

    /** Sell all eligible items from player's inventory; returns total earned. */
    double performSellAll(Player player) {
        double total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            double price = getSellPrice(stack.getType());
            if (price > 0) {
                total += price * stack.getAmount();
                player.getInventory().remove(stack);
            }
        }
        if (total > 0) economy.depositPlayer(player, total);
        return total;
    }

    // =========================================================================
    //  Public helpers (used by SimpleShopPlugin & commands)
    // =========================================================================

    /** Returns sell price for a material, or -1 if not in shop. */
    public double getSellPrice(Material material) {
        ShopItem item = findShopItem(material);
        return item == null ? -1 : item.sellPrice;
    }

    // =========================================================================
    //  Private lookup helpers
    // =========================================================================

    private ShopItem findShopItem(Material material) {
        for (ShopConfig.ShopCategory cat : shopConfig.getAllCategories()) {
            for (ShopItem item : cat.items) {
                if (item.material == material) return item;
            }
        }
        return null;
    }

    private SpawnerEntry findSpawnerEntry(EntityType type) {
        for (SpawnerEntry e : shopConfig.getSpawnerEntries()) {
            if (e.entityType == type) return e;
        }
        return null;
    }

    // =========================================================================
    //  GUI helpers
    // =========================================================================

    private void renderCategoryFooter(Inventory inv, Player player) {
        renderCategoryFooter(inv, player, 0, 1);
    }

    private void renderCategoryFooter(Inventory inv, Player player, int page, int totalPages) {
        inv.setItem(45, createIcon(Material.SPECTRAL_ARROW, "§c§l« Back to Market",
                "§7Return to the main market"));
        inv.setItem(49, createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
        if (page > 0) {
            inv.setItem(47, createIcon(Material.ARROW, "§7« Previous Page",
                    "§7Page " + page + " of " + totalPages));
        }
        if (page < totalPages - 1) {
            inv.setItem(51, createIcon(Material.ARROW, "§7Next Page »",
                    "§7Page " + (page + 2) + " of " + totalPages));
        }
    }

    /** Gray filler pane — ignored by the click handler (name = §r). */
    private ItemStack grayPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("§r");
        pane.setItemMeta(meta);
        return pane;
    }

    /** Black border pane — ignored by the click handler (name = §r). */
    private ItemStack blackPane() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("§r");
        pane.setItemMeta(meta);
        return pane;
    }

    /**
     * Distributes {@code count} items evenly across 9 slots in a row.
     * Returns absolute inventory slot numbers (rowStart = first slot in the row).
     */
    /**
     * Distributes {@code count} items evenly within {@code width} slots starting at {@code rowStart}.
     */
    private List<Integer> spacedSlots(int count, int rowStart, int width) {
        if (count <= 0) return List.of();
        List<Integer> result = new ArrayList<>();
        int n = Math.min(count, width);
        if (n == 1) {
            result.add(rowStart + width / 2);
        } else {
            double step = (width - 1.0) / (n - 1);
            for (int i = 0; i < n; i++) {
                result.add(rowStart + (int) Math.round(i * step));
            }
        }
        return result;
    }

    private List<Integer> spacedSlots(int count, int rowStart) {
        return spacedSlots(count, rowStart, 9);
    }

    private ItemStack createShopItemIcon(ShopItem item) {
        ItemStack stack = new ItemStack(item.material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§f§l" + item.name);
        meta.setLore(List.of(
            "§8────────────────────",
            "§aBuy:  §e$" + formatMoney(item.buyPrice) + " §8each",
            "§cSell: §e$" + formatMoney(item.sellPrice) + " §8each",
            "§8────────────────────",
            "§7▸ §eLeft-click   §7→ §abuy 1",
            "§7▸ §eShift+Left   §7→ §abuy 64",
            "§7▸ §eRight-click  §7→ §csell 1",
            "§7▸ §eShift+Right  §7→ §csell 64"
        ));
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createSpawnerDisplayItem(SpawnerEntry entry) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        if (item.getItemMeta() instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof CreatureSpawner cs) {
                cs.setSpawnedType(entry.entityType);
                bsm.setBlockState(cs);
            }
            double fv = entry.factionValue;
            bsm.setDisplayName("§6§l" + entry.displayName + " Spawner");
            bsm.setLore(List.of(
                "§8────────────────────",
                "§aBuy Price: §e$" + formatMoney(entry.buyPrice),
                "§8────────────────────",
                "§7Faction territory value over time:",
                "§e   0h §8→ §f$" + formatMoney(fv * 0.50) + "  §850%",
                "§e  24h §8→ §f$" + formatMoney(fv * 0.75) + "  §875%",
                "§e  48h §8→ §a$" + formatMoney(fv) +        "  §a100% §7(full)",
                "§8────────────────────",
                "§7▸ §eLeft-click   §7→ §abuy 1",
                "§7▸ §eShift-click  §7→ §abuy 16"
            ));
            item.setItemMeta(bsm);
        }
        return item;
    }

    private ItemStack createCategoryIcon(Material material, String name, String tagline, String detail) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(
            "§8━━━━━━━━━━━━━━━━━━━━",
            "§7" + tagline,
            "§f" + detail,
            "§8━━━━━━━━━━━━━━━━━━━━",
            "§b§l▸ §fClick to browse"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCategoryHeaderIcon(ShopConfig.ShopCategory cat) {
        ItemStack item = new ItemStack(cat.icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(cat.color + cat.displayName);
        meta.setLore(List.of("§7Left-click §8→ §abuy", "§7Right-click §8→ §csell"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createIcon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack pane(Material material) {
        ItemStack p = new ItemStack(material);
        ItemMeta m = p.getItemMeta();
        m.setDisplayName("§r");
        p.setItemMeta(m);
        return p;
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000);
        if (amount >= 1_000_000)     return String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000)         return String.format("%.1fK", amount / 1_000);
        return String.format("%.2f", amount);
    }

    // =========================================================================
    //  Public inner classes (referenced by ShopConfig)
    // =========================================================================

    public static class SpawnerEntry {
        public final EntityType entityType;
        public final String displayName;
        public final double buyPrice;
        public final double factionValue;

        public SpawnerEntry(EntityType entityType, String displayName,
                            double buyPrice, double factionValue) {
            this.entityType   = entityType;
            this.displayName  = displayName;
            this.buyPrice     = buyPrice;
            this.factionValue = factionValue;
        }
    }

    public static class ShopItem {
        public Material material;
        public String name;
        public double buyPrice;
        public double sellPrice;

        public ShopItem(Material material, String name, double buyPrice, double sellPrice) {
            this.material  = material;
            this.name      = name;
            this.buyPrice  = buyPrice;
            this.sellPrice = sellPrice;
        }
    }
}
