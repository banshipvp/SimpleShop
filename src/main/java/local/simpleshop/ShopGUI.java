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

    private final SimpleShopPlugin plugin;
    private final Economy economy;
    private final ShopConfig shopConfig;

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
        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§lFACTION MARKET §5✦");

        // Row 0: header bar
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        ItemStack title = new ItemStack(Material.NETHER_STAR);
        ItemMeta tm = title.getItemMeta();
        tm.setDisplayName("§d§l✦ Faction Market §d§l✦");
        tm.setLore(List.of("§7Browse categories below", "§7to buy and sell items"));
        title.setItemMeta(tm);
        inv.setItem(4, title);
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // Row 1: spacer
        inv.setItem(9,  pane(Material.PURPLE_STAINED_GLASS_PANE));
        for (int s = 10; s <= 16; s++) inv.setItem(s, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(17, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // Rows 2-3: category buttons (slots 18-26, 27-35) with gray divider at col 4
        List<ShopConfig.ShopCategory> ordered = shopConfig.getCategoriesOrdered();

        // We fill slots: row2=19,20,21,  23,24,25   row3=28,29,30,  32,33,34
        // Max 8 visible categories (2 rows × 4 cols, skipping centre col)
        int[] contentSlots = {20, 21, 23, 24, 29, 30, 32, 33};

        inv.setItem(18, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(19, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(22, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(25, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(26, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(27, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(28, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(31, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(34, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(35, pane(Material.PURPLE_STAINED_GLASS_PANE));

        for (int i = 0; i < contentSlots.length && i < ordered.size(); i++) {
            ShopConfig.ShopCategory cat = ordered.get(i);
            inv.setItem(contentSlots[i],
                    createCategoryIcon(cat.icon, cat.color + cat.displayName, cat.tagline, cat.detail));
        }

        // Always show spawners in the last slot (slot 33 or next available)
        // already handled above via getCategoriesOrdered() which includes spawners if defined

        // Row 4: utility bar
        inv.setItem(36, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(37, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(38, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(39, createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
        inv.setItem(40, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(41, createIcon(Material.EMERALD, "§a§lSell Inventory",
                "§7Instantly sells all eligible",
                "§7items in your inventory.",
                "§8────────────────────",
                "§d§l▸ §eClick to sell all!"));
        inv.setItem(42, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(43, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(44, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // Row 5: footer
        inv.setItem(45, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(46, pane(Material.PURPLE_STAINED_GLASS_PANE));
        for (int s = 47; s <= 51; s++) inv.setItem(s, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(52, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(53, pane(Material.PURPLE_STAINED_GLASS_PANE));

        player.openInventory(inv);
    }

    // =========================================================================
    //  Category page
    // =========================================================================

    public void openCategory(Player player, String categoryId) {
        if ("spawners".equals(categoryId)) {
            openSpawnersCategory(player);
            return;
        }

        ShopConfig.ShopCategory cat = shopConfig.getCategory(categoryId);
        if (cat == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§l" + cat.displayName);

        // Row 0: header
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(4, createCategoryHeaderIcon(cat));
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // Rows 1-4: items (slots 9-44 = 36 slots)
        int slot = 9;
        for (ShopItem item : cat.items) {
            if (slot >= 45) break;
            inv.setItem(slot++, createShopItemIcon(item));
        }

        renderCategoryFooter(inv, player);
        player.openInventory(inv);
    }

    // =========================================================================
    //  Spawners page
    // =========================================================================

    private void openSpawnersCategory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§lSpawners");

        // Row 0: header
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        ItemStack hdr = createIcon(Material.SPAWNER, "§5§lSpawners",
                "§7Left-click §8→ §abuy 1", "§7Shift-click §8→ §abuy 16");
        inv.setItem(4, hdr);
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // Rows 1-4: spawner items
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
        if (!title.startsWith("§5✦")) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        // ── ignore decorations ───────────────────────────────────────────────
        if (displayName.equals("§r")) return;

        // ── Main market ──────────────────────────────────────────────────────
        if (title.contains("FACTION MARKET")) {
            if (displayName.contains("Your Balance")) return;

            // Sell Inventory
            if (displayName.contains("Sell Inventory")) {
                double total = performSellAll(player);
                if (total > 0) {
                    player.sendMessage("§a✓ Sold inventory for §e$" + formatMoney(total));
                } else {
                    player.sendMessage("§cNo sellable items found.");
                }
                event.getView().getTopInventory().setItem(39, createIcon(
                        Material.GOLD_INGOT, "§e§lYour Balance",
                        "§a$" + formatMoney(economy.getBalance(player))));
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
                        event.getView().getTopInventory().setItem(50, createIcon(
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

        ShopItem shopItem = findShopItem(clicked.getType());
        if (shopItem == null) return;

        int amount = event.isShiftClick() ? 64 : 1;
        if (event.isLeftClick()) {
            buyItem(player, shopItem, amount);
        } else {
            sellItem(player, shopItem, amount);
        }
        event.getView().getTopInventory().setItem(50, createIcon(
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
        inv.setItem(45, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(46, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(47, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(48, createIcon(Material.SPECTRAL_ARROW, "§c§l« Back to Market",
                "§7Return to the main market"));
        inv.setItem(49, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(50, createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
        inv.setItem(51, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(52, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(53, pane(Material.PURPLE_STAINED_GLASS_PANE));
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
            "§8────────────────────",
            "§7" + tagline,
            "§7" + detail,
            "§8────────────────────",
            "§d§l▸ §eClick to browse!"
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
