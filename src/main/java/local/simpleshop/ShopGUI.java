package local.simpleshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Shop GUI manager
 */
public class ShopGUI implements Listener {

    private final SimpleShopPlugin plugin;
    private final Economy economy;
    private final Map<String, List<ShopItem>> categories = new LinkedHashMap<>();

    public ShopGUI(SimpleShopPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        setupShopItems();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Setup all shop items with categories
     */
    private void setupShopItems() {
        categories.clear();

        // BLOCKS category
        List<ShopItem> blocks = new ArrayList<>();
        blocks.add(new ShopItem(Material.STONE, "Stone", 2, 1));
        blocks.add(new ShopItem(Material.COBBLESTONE, "Cobblestone", 1, 0.5));
        blocks.add(new ShopItem(Material.DIRT, "Dirt", 1, 0.3));
        blocks.add(new ShopItem(Material.GRASS_BLOCK, "Grass Block", 3, 1));
        blocks.add(new ShopItem(Material.SAND, "Sand", 3, 1.5));
        blocks.add(new ShopItem(Material.GRAVEL, "Gravel", 3, 1.5));
        blocks.add(new ShopItem(Material.SANDSTONE, "Sandstone", 5, 2));
        blocks.add(new ShopItem(Material.OAK_LOG, "Oak Log", 5, 2));
        blocks.add(new ShopItem(Material.SPRUCE_LOG, "Spruce Log", 5, 2));
        blocks.add(new ShopItem(Material.BIRCH_LOG, "Birch Log", 5, 2));
        blocks.add(new ShopItem(Material.JUNGLE_LOG, "Jungle Log", 5, 2));
        blocks.add(new ShopItem(Material.ACACIA_LOG, "Acacia Log", 5, 2));
        blocks.add(new ShopItem(Material.DARK_OAK_LOG, "Dark Oak Log", 5, 2));
        blocks.add(new ShopItem(Material.OAK_PLANKS, "Oak Planks", 2, 1));
        blocks.add(new ShopItem(Material.SPRUCE_PLANKS, "Spruce Planks", 2, 1));
        blocks.add(new ShopItem(Material.BIRCH_PLANKS, "Birch Planks", 2, 1));
        blocks.add(new ShopItem(Material.BRICKS, "Bricks", 8, 4));
        blocks.add(new ShopItem(Material.STONE_BRICKS, "Stone Bricks", 6, 3));
        blocks.add(new ShopItem(Material.COBBLED_DEEPSLATE, "Cobbled Deepslate", 4, 2));
        blocks.add(new ShopItem(Material.DEEPSLATE_BRICKS, "Deepslate Bricks", 7, 3));
        blocks.add(new ShopItem(Material.GLASS, "Glass", 5, 2));
        blocks.add(new ShopItem(Material.GLASS_PANE, "Glass Pane", 4, 1.5));
        blocks.add(new ShopItem(Material.LADDER, "Ladder", 10, 4));
        blocks.add(new ShopItem(Material.CHEST, "Chest", 20, 8));
        blocks.add(new ShopItem(Material.BARREL, "Barrel", 25, 10));
        blocks.add(new ShopItem(Material.WATER_BUCKET, "Water Bucket", 100, 40));
        blocks.add(new ShopItem(Material.LAVA_BUCKET, "Lava Bucket", 120, 50));
        blocks.add(new ShopItem(Material.OBSIDIAN, "Obsidian", 50, 20));
        blocks.add(new ShopItem(Material.NETHERRACK, "Netherrack", 2, 1));
        blocks.add(new ShopItem(Material.END_STONE, "End Stone", 10, 5));
        blocks.add(new ShopItem(Material.QUARTZ_BLOCK, "Quartz Block", 40, 15));
        categories.put("blocks", blocks);

        // ORES & MINERALS category
        List<ShopItem> ores = new ArrayList<>();
        ores.add(new ShopItem(Material.COAL, "Coal", 5, 3));
        ores.add(new ShopItem(Material.IRON_INGOT, "Iron Ingot", 20, 10));
        ores.add(new ShopItem(Material.GOLD_INGOT, "Gold Ingot", 50, 25));
        ores.add(new ShopItem(Material.DIAMOND, "Diamond", 200, 100));
        ores.add(new ShopItem(Material.EMERALD, "Emerald", 300, 150));
        ores.add(new ShopItem(Material.NETHERITE_INGOT, "Netherite Ingot", 2000, 1000));
        ores.add(new ShopItem(Material.IRON_BLOCK, "Iron Block", 180, 90));
        ores.add(new ShopItem(Material.GOLD_BLOCK, "Gold Block", 450, 220));
        ores.add(new ShopItem(Material.DIAMOND_BLOCK, "Diamond Block", 1800, 900));
        ores.add(new ShopItem(Material.EMERALD_BLOCK, "Emerald Block", 2600, 1300));
        ores.add(new ShopItem(Material.REDSTONE, "Redstone", 10, 5));
        ores.add(new ShopItem(Material.LAPIS_LAZULI, "Lapis Lazuli", 15, 7));
        ores.add(new ShopItem(Material.QUARTZ, "Quartz", 20, 10));
        ores.add(new ShopItem(Material.COPPER_INGOT, "Copper Ingot", 15, 7));
        ores.add(new ShopItem(Material.IRON_NUGGET, "Iron Nugget", 3, 1));
        ores.add(new ShopItem(Material.GOLD_NUGGET, "Gold Nugget", 5, 2));
        categories.put("ores", ores);

        // FARMING category
        List<ShopItem> farming = new ArrayList<>();
        farming.add(new ShopItem(Material.WHEAT, "Wheat", 5, 2));
        farming.add(new ShopItem(Material.WHEAT_SEEDS, "Wheat Seeds", 2, 1));
        farming.add(new ShopItem(Material.CARROT, "Carrot", 4, 2));
        farming.add(new ShopItem(Material.POTATO, "Potato", 4, 2));
        farming.add(new ShopItem(Material.BEETROOT, "Beetroot", 4, 2));
        farming.add(new ShopItem(Material.PUMPKIN_SEEDS, "Pumpkin Seeds", 3, 1));
        farming.add(new ShopItem(Material.MELON_SEEDS, "Melon Seeds", 3, 1));
        farming.add(new ShopItem(Material.BEETROOT_SEEDS, "Beetroot Seeds", 3, 1));
        farming.add(new ShopItem(Material.NETHER_WART, "Nether Wart", 10, 5));
        farming.add(new ShopItem(Material.SUGAR_CANE, "Sugar Cane", 5, 3));
        farming.add(new ShopItem(Material.CACTUS, "Cactus", 6, 3));
        farming.add(new ShopItem(Material.MELON_SLICE, "Melon Slice", 3, 1));
        farming.add(new ShopItem(Material.PUMPKIN, "Pumpkin", 10, 5));
        farming.add(new ShopItem(Material.BAMBOO, "Bamboo", 2, 1));
        farming.add(new ShopItem(Material.KELP, "Kelp", 3, 1));
        farming.add(new ShopItem(Material.SWEET_BERRIES, "Sweet Berries", 5, 2));
        farming.add(new ShopItem(Material.COCOA_BEANS, "Cocoa Beans", 7, 3));
        farming.add(new ShopItem(Material.APPLE, "Apple", 8, 4));
        categories.put("farming", farming);

        // FOOD category
        List<ShopItem> food = new ArrayList<>();
        food.add(new ShopItem(Material.BREAD, "Bread", 10, 5));
        food.add(new ShopItem(Material.COOKED_BEEF, "Cooked Beef", 15, 7));
        food.add(new ShopItem(Material.COOKED_PORKCHOP, "Cooked Porkchop", 15, 7));
        food.add(new ShopItem(Material.COOKED_CHICKEN, "Cooked Chicken", 12, 6));
        food.add(new ShopItem(Material.COOKED_MUTTON, "Cooked Mutton", 12, 6));
        food.add(new ShopItem(Material.COOKED_RABBIT, "Cooked Rabbit", 14, 7));
        food.add(new ShopItem(Material.COOKED_SALMON, "Cooked Salmon", 13, 6));
        food.add(new ShopItem(Material.COOKED_COD, "Cooked Cod", 12, 5));
        food.add(new ShopItem(Material.BAKED_POTATO, "Baked Potato", 9, 4));
        food.add(new ShopItem(Material.CAKE, "Cake", 120, 50));
        food.add(new ShopItem(Material.GOLDEN_APPLE, "Golden Apple", 500, 250));
        food.add(new ShopItem(Material.ENCHANTED_GOLDEN_APPLE, "Enchanted Golden Apple", 5000, 2500));
        food.add(new ShopItem(Material.GOLDEN_CARROT, "Golden Carrot", 100, 50));
        categories.put("food", food);

        // COMBAT category
        List<ShopItem> combat = new ArrayList<>();
        combat.add(new ShopItem(Material.ARROW, "Arrow", 2, 1));
        combat.add(new ShopItem(Material.BOW, "Bow", 100, 50));
        combat.add(new ShopItem(Material.CROSSBOW, "Crossbow", 180, 90));
        combat.add(new ShopItem(Material.TRIDENT, "Trident", 2000, 1000));
        combat.add(new ShopItem(Material.SHIELD, "Shield", 180, 90));
        combat.add(new ShopItem(Material.ENDER_PEARL, "Ender Pearl", 150, 75));
        combat.add(new ShopItem(Material.IRON_SWORD, "Iron Sword", 180, 90));
        combat.add(new ShopItem(Material.DIAMOND_SWORD, "Diamond Sword", 1200, 500));
        combat.add(new ShopItem(Material.IRON_AXE, "Iron Axe", 160, 80));
        combat.add(new ShopItem(Material.DIAMOND_AXE, "Diamond Axe", 1000, 400));
        combat.add(new ShopItem(Material.IRON_HELMET, "Iron Helmet", 220, 100));
        combat.add(new ShopItem(Material.IRON_CHESTPLATE, "Iron Chestplate", 360, 170));
        combat.add(new ShopItem(Material.IRON_LEGGINGS, "Iron Leggings", 320, 150));
        combat.add(new ShopItem(Material.IRON_BOOTS, "Iron Boots", 180, 90));
        combat.add(new ShopItem(Material.DIAMOND_HELMET, "Diamond Helmet", 1400, 700));
        combat.add(new ShopItem(Material.DIAMOND_CHESTPLATE, "Diamond Chestplate", 2200, 1100));
        combat.add(new ShopItem(Material.DIAMOND_LEGGINGS, "Diamond Leggings", 2000, 1000));
        combat.add(new ShopItem(Material.DIAMOND_BOOTS, "Diamond Boots", 1200, 600));
        combat.add(new ShopItem(Material.TOTEM_OF_UNDYING, "Totem of Undying", 10000, 5000));
        categories.put("combat", combat);

        // RAIDING category
        List<ShopItem> raiding = new ArrayList<>();
        raiding.add(new ShopItem(Material.TNT, "TNT", 200, 100));
        raiding.add(new ShopItem(Material.SAND, "Sand", 3, 1.5));
        raiding.add(new ShopItem(Material.REDSTONE, "Redstone", 10, 5));
        raiding.add(new ShopItem(Material.REDSTONE_TORCH, "Redstone Torch", 20, 8));
        raiding.add(new ShopItem(Material.REPEATER, "Repeater", 35, 15));
        raiding.add(new ShopItem(Material.COMPARATOR, "Comparator", 45, 20));
        raiding.add(new ShopItem(Material.DISPENSER, "Dispenser", 85, 40));
        raiding.add(new ShopItem(Material.OBSERVER, "Observer", 90, 45));
        raiding.add(new ShopItem(Material.PISTON, "Piston", 60, 25));
        raiding.add(new ShopItem(Material.STICKY_PISTON, "Sticky Piston", 100, 45));
        raiding.add(new ShopItem(Material.SLIME_BLOCK, "Slime Block", 220, 100));
        raiding.add(new ShopItem(Material.LEVER, "Lever", 8, 3));
        raiding.add(new ShopItem(Material.STONE_BUTTON, "Stone Button", 5, 2));
        raiding.add(new ShopItem(Material.OAK_BUTTON, "Oak Button", 5, 2));
        raiding.add(new ShopItem(Material.REDSTONE_BLOCK, "Redstone Block", 90, 45));
        raiding.add(new ShopItem(Material.OBSIDIAN, "Obsidian", 50, 20));
        raiding.add(new ShopItem(Material.WATER_BUCKET, "Water Bucket", 100, 40));
        raiding.add(new ShopItem(Material.FLINT_AND_STEEL, "Flint and Steel", 60, 25));
        raiding.add(new ShopItem(Material.LADDER, "Ladder", 10, 4));
        categories.put("raiding", raiding);

        // MISC category
        List<ShopItem> misc = new ArrayList<>();
        misc.add(new ShopItem(Material.STRING, "String", 5, 2));
        misc.add(new ShopItem(Material.BONE, "Bone", 5, 2));
        misc.add(new ShopItem(Material.GUNPOWDER, "Gunpowder", 10, 5));
        misc.add(new ShopItem(Material.ENDER_EYE, "Ender Eye", 300, 150));
        misc.add(new ShopItem(Material.BLAZE_ROD, "Blaze Rod", 50, 25));
        misc.add(new ShopItem(Material.SLIME_BALL, "Slime Ball", 30, 15));
        misc.add(new ShopItem(Material.GHAST_TEAR, "Ghast Tear", 100, 50));
        misc.add(new ShopItem(Material.SHULKER_SHELL, "Shulker Shell", 800, 350));
        misc.add(new ShopItem(Material.ENDER_CHEST, "Ender Chest", 750, 350));
        misc.add(new ShopItem(Material.ANVIL, "Anvil", 300, 120));
        misc.add(new ShopItem(Material.ENCHANTING_TABLE, "Enchanting Table", 500, 250));
        misc.add(new ShopItem(Material.BOOKSHELF, "Bookshelf", 120, 60));
        misc.add(new ShopItem(Material.EXPERIENCE_BOTTLE, "XP Bottle", 50, 25));
        misc.add(new ShopItem(Material.NETHER_STAR, "Nether Star", 20000, 10000));
        misc.add(new ShopItem(Material.DRAGON_BREATH, "Dragon Breath", 1000, 500));
        categories.put("misc", misc);
    }

    /**
     * Open the main shop menu
     */
    public void openMainShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§6§l⚡ Shop Categories");

        // Blocks category
        ItemStack blocksIcon = createIcon(Material.STONE, "§a§lBlocks", 
            "§7Building materials", "§7Stone, Wood, Glass, etc.", "§e§lClick to browse!");
        inv.setItem(10, blocksIcon);

        // Ores category
        ItemStack oresIcon = createIcon(Material.DIAMOND, "§b§lOres & Minerals",
            "§7Ores and ingots", "§7Coal, Iron, Diamond, etc.", "§e§lClick to browse!");
        inv.setItem(11, oresIcon);

        // Farming category
        ItemStack farmIcon = createIcon(Material.WHEAT, "§2§lFarming",
            "§7Crops and seeds", "§7Wheat, Carrots, etc.", "§e§lClick to browse!");
        inv.setItem(12, farmIcon);

        // Food category
        ItemStack foodIcon = createIcon(Material.COOKED_BEEF, "§6§lFood",
            "§7Cooked food items", "§7Bread, Meat, etc.", "§e§lClick to browse!");
        inv.setItem(13, foodIcon);

        // Combat category
        ItemStack combatIcon = createIcon(Material.DIAMOND_SWORD, "§c§lCombat",
            "§7Arrows, Pearls, TNT", "§7Combat essentials", "§e§lClick to browse!");
        inv.setItem(14, combatIcon);

        // Misc category
        ItemStack miscIcon = createIcon(Material.ENDER_EYE, "§d§lMiscellaneous",
            "§7Special items", "§7Rare drops, XP, etc.", "§e§lClick to browse!");
        inv.setItem(15, miscIcon);

        // Raiding category
        ItemStack raidingIcon = createIcon(Material.DISPENSER, "§4§lRaiding",
            "§7Cannon essentials", "§7TNT, Redstone, Repeaters, etc.", "§e§lClick to browse!");
        inv.setItem(16, raidingIcon);

        // Balance display
        ItemStack balanceIcon = createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
            "§a$" + formatMoney(economy.getBalance(player)));
        inv.setItem(31, balanceIcon);

        player.openInventory(inv);
    }

    /**
     * Open a specific category
     */
    public void openCategory(Player player, String category) {
        List<ShopItem> items = categories.get(category);
        if (items == null) return;

        String categoryName = getCategoryDisplayName(category);
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lShop - " + categoryName);

        int slot = 0;
        for (ShopItem item : items) {
            if (slot >= 45) break;

            ItemStack displayItem = new ItemStack(item.material);
            ItemMeta meta = displayItem.getItemMeta();
            meta.setDisplayName("§e§l" + item.name);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7");
            lore.add("§aBuy Price: §e$" + formatMoney(item.buyPrice));
            lore.add("§cSell Price: §e$" + formatMoney(item.sellPrice));
            lore.add("§7");
            lore.add("§e▸ Left Click: §aBuy 1");
            lore.add("§e▸ Shift + Left: §aBuy 64");
            lore.add("§e▸ Right Click: §cSell 1");
            lore.add("§e▸ Shift + Right: §cSell 64");
            meta.setLore(lore);
            
            displayItem.setItemMeta(meta);
            inv.setItem(slot++, displayItem);
        }

        // Back button
        ItemStack backButton = createIcon(Material.ARROW, "§c§lBack to Categories");
        inv.setItem(49, backButton);

        // Balance display
        ItemStack balanceIcon = createIcon(Material.GOLD_INGOT, "§e§lYour Balance",
            "§a$" + formatMoney(economy.getBalance(player)));
        inv.setItem(48, balanceIcon);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        if (!title.contains("Shop")) return;
        
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Main shop menu
        if (title.contains("Categories")) {
            if (displayName.contains("Blocks")) {
                openCategory(player, "blocks");
            } else if (displayName.contains("Ores")) {
                openCategory(player, "ores");
            } else if (displayName.contains("Farming")) {
                openCategory(player, "farming");
            } else if (displayName.contains("Food")) {
                openCategory(player, "food");
            } else if (displayName.contains("Combat")) {
                openCategory(player, "combat");
            } else if (displayName.contains("Miscellaneous")) {
                openCategory(player, "misc");
            } else if (displayName.contains("Raiding")) {
                openCategory(player, "raiding");
            }
            return;
        }
        
        // Category menus
        if (displayName.contains("Back to Categories")) {
            openMainShop(player);
            return;
        }
        
        if (displayName.contains("Balance")) {
            return;
        }
        
        // Handle buy/sell
        Material itemType = clicked.getType();
        ShopItem shopItem = findShopItem(itemType);
        
        if (shopItem == null) return;
        
        boolean isShiftClick = event.isShiftClick();
        boolean isLeftClick = event.isLeftClick();
        int amount = isShiftClick ? 64 : 1;
        
        if (isLeftClick) {
            // Buy
            buyItem(player, shopItem, amount);
        } else {
            // Sell
            sellItem(player, shopItem, amount);
        }
        
        // Update balance display in-place so cursor/crosshair position stays stable
        event.getView().getTopInventory().setItem(48, createIcon(
            Material.GOLD_INGOT,
            "§e§lYour Balance",
            "§a$" + formatMoney(economy.getBalance(player))
        ));
    }

    private void buyItem(Player player, ShopItem item, int amount) {
        double totalCost = item.buyPrice * amount;
        
        if (economy.getBalance(player) < totalCost) {
            player.sendMessage("§cYou don't have enough money! Need $" + formatMoney(totalCost));
            return;
        }
        
        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full!");
            return;
        }
        
        economy.withdrawPlayer(player, totalCost);
        ItemStack itemStack = new ItemStack(item.material, amount);
        player.getInventory().addItem(itemStack);
        
        player.sendMessage("§a✓ Bought §e" + amount + "x " + item.name + " §afor §e$" + formatMoney(totalCost));
        player.sendMessage("§7New balance: §a$" + formatMoney(economy.getBalance(player)));
    }

    private void sellItem(Player player, ShopItem item, int amount) {
        // Check if player has the items
        ItemStack toCheck = new ItemStack(item.material, amount);
        if (!player.getInventory().containsAtLeast(toCheck, amount)) {
            player.sendMessage("§cYou don't have §e" + amount + "x " + item.name);
            return;
        }
        
        double totalPrice = item.sellPrice * amount;
        
        // Remove items from inventory
        ItemStack toRemove = new ItemStack(item.material, amount);
        player.getInventory().removeItem(toRemove);
        
        economy.depositPlayer(player, totalPrice);
        
        player.sendMessage("§a✓ Sold §e" + amount + "x " + item.name + " §afor §e$" + formatMoney(totalPrice));
        player.sendMessage("§7New balance: §a$" + formatMoney(economy.getBalance(player)));
    }

    private ShopItem findShopItem(Material material) {
        for (List<ShopItem> items : categories.values()) {
            for (ShopItem item : items) {
                if (item.material == material) {
                    return item;
                }
            }
        }
        return null;
    }

    private String findCategoryByItem(Material material) {
        for (Map.Entry<String, List<ShopItem>> entry : categories.entrySet()) {
            for (ShopItem item : entry.getValue()) {
                if (item.material == material) {
                    return entry.getKey();
                }
            }
        }
        return "blocks";
    }

    private ItemStack createIcon(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "blocks" -> "Blocks";
            case "ores" -> "Ores & Minerals";
            case "farming" -> "Farming";
            case "food" -> "Food";
            case "combat" -> "Combat";
            case "raiding" -> "Raiding";
            case "misc" -> "Miscellaneous";
            default -> "Shop";
        };
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000);
        } else {
            return String.format("%.2f", amount);
        }
    }

    /**
     * Shop item with buy/sell prices
     */
    private static class ShopItem {
        Material material;
        String name;
        double buyPrice;
        double sellPrice;

        ShopItem(Material material, String name, double buyPrice, double sellPrice) {
            this.material = material;
            this.name = name;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
    }
}
