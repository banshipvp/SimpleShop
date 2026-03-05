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
 * Shop GUI manager
 */
public class ShopGUI implements Listener {

    private final SimpleShopPlugin plugin;
    private final Economy economy;
    private final Map<String, List<ShopItem>> categories = new LinkedHashMap<>();
    private final List<SpawnerEntry> spawnerEntries = new ArrayList<>();

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
        raiding.add(new ShopItem(Material.DISPENSER, "Dispenser", 1_000, 400));
        raiding.add(new ShopItem(Material.OBSERVER, "Observer", 1_000, 400));
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

        // SPAWNERS category – buy-only items; sorted lowest → highest price
        spawnerEntries.clear();
        //                                                            buyPrice    factionValue
        spawnerEntries.add(new SpawnerEntry(EntityType.CHICKEN,          "Chicken",              40_000,    40_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.PIG,              "Pig",                  50_000,    50_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.SHEEP,            "Sheep",                50_000,    50_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.WOLF,             "Wolf",                 60_000,    60_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.COW,              "Cow",                  65_000,    65_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.CAVE_SPIDER,      "Cave Spider",          80_000,    80_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.SPIDER,           "Spider",               90_000,    90_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.ZOMBIE,           "Zombie",              100_000,   100_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.SKELETON,         "Skeleton",            120_000,   120_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.SLIME,            "Slime",               175_000,   175_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.ZOMBIFIED_PIGLIN, "Zombified Piglin",    185_000,   185_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.BLAZE,            "Blaze",               500_000,   500_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.CREEPER,          "Creeper",             500_000,   500_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.MAGMA_CUBE,       "Magma Cube",        1_800_000, 1_250_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.GHAST,            "Ghast",             2_000_000, 1_500_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.IRON_GOLEM,       "Iron Golem",        2_000_000, 2_000_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.SNOWMAN,          "Snowman",          10_000_000,10_000_000));
        spawnerEntries.add(new SpawnerEntry(EntityType.WARDEN,           "Warden",           15_000_000,15_000_000));
    }

    /**
     * Open the main shop menu
     */
    public void openMainShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§lFACTION MARKET §5✦");

        // ── Row 0: Header bar ─────────────────────────────────────────────
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        ItemStack titlePiece = new ItemStack(Material.NETHER_STAR);
        ItemMeta tm = titlePiece.getItemMeta();
        tm.setDisplayName("§d§l✦ Faction Market §d§l✦");
        tm.setLore(List.of("§7Browse categories below", "§7to buy and sell items"));
        titlePiece.setItemMeta(tm);
        inv.setItem(4, titlePiece);
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Row 1: Spacer ─────────────────────────────────────────────────
        inv.setItem(9,  pane(Material.PURPLE_STAINED_GLASS_PANE));
        for (int s = 10; s <= 16; s++) inv.setItem(s, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(17, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Row 2: Categories 1–4 ─────────────────────────────────────────
        inv.setItem(18, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(19, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(20, createCategoryIcon(Material.STONE,       "§b§lBlocks",   "Building materials",  "Stone, Wood, Glass & more"));
        inv.setItem(21, createCategoryIcon(Material.DIAMOND,     "§e§lMinerals", "Ores and ingots",    "Coal, Iron, Diamond & more"));
        inv.setItem(22, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(23, createCategoryIcon(Material.WHEAT,       "§a§lFarming",  "Crops and seeds",    "Wheat, Carrots & more"));
        inv.setItem(24, createCategoryIcon(Material.COOKED_BEEF, "§6§lFood",     "Cooked food items",  "Bread, Steak & more"));
        inv.setItem(25, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(26, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Row 3: Categories 5–8 ─────────────────────────────────────────
        inv.setItem(27, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(28, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(29, createCategoryIcon(Material.DIAMOND_SWORD, "§c§lCombat",   "Arrows, Pearls, TNT",     "PvP essentials"));
        inv.setItem(30, createCategoryIcon(Material.DISPENSER,     "§4§lRaiding",  "Cannon components",       "TNT, Repeaters & more"));
        inv.setItem(31, pane(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(32, createCategoryIcon(Material.ENDER_EYE,     "§d§lMisc",     "Special items",           "Rare drops, XP & more"));
        inv.setItem(33, createCategoryIcon(Material.SPAWNER,       "§5§lSpawners", "All spawner types",       "Value ramps to 100% at 48h"));
        inv.setItem(34, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(35, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Row 4: Utility bar ────────────────────────────────────────────
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

        // ── Row 5: Footer ─────────────────────────────────────────────────
        inv.setItem(45, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(46, pane(Material.PURPLE_STAINED_GLASS_PANE));
        for (int s = 47; s <= 51; s++) inv.setItem(s, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(52, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(53, pane(Material.PURPLE_STAINED_GLASS_PANE));

        player.openInventory(inv);
    }

    /**
     * Open a specific category
     */
    public void openCategory(Player player, String category) {
        if (category.equals("spawners")) {
            openSpawnersCategory(player);
            return;
        }

        List<ShopItem> items = categories.get(category);
        if (items == null) return;

        String categoryName = getCategoryDisplayName(category);
        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§l" + categoryName);

        // ── Row 0: Category header ────────────────────────────────────────
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(4, createCategoryHeaderIcon(category, categoryName));
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Rows 1–4: Items (slots 9–44, 36 slots) ───────────────────────
        int slot = 9;
        for (ShopItem item : items) {
            if (slot >= 45) break;
            ItemStack displayItem = new ItemStack(item.material);
            ItemMeta meta = displayItem.getItemMeta();
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
            displayItem.setItemMeta(meta);
            inv.setItem(slot++, displayItem);
        }

        // ── Row 5: Navigation footer ──────────────────────────────────────
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

        player.openInventory(inv);
    }

    /**
     * Open the Spawners browsing page.
     * Spawners are buy-only; their in-faction value ramps up over 48 hours.
     */
    private void openSpawnersCategory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§5✦ §d§lSpawners");

        // ── Row 0: Header ─────────────────────────────────────────────────
        inv.setItem(0, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(1, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(2, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(3, pane(Material.CYAN_STAINED_GLASS_PANE));
        ItemStack hdr = new ItemStack(Material.SPAWNER);
        ItemMeta hm = hdr.getItemMeta();
        hm.setDisplayName("§5§lSpawners");
        hm.setLore(List.of("§7Left-click §8→ §abuy 1", "§7Shift-click §8→ §abuy 16"));
        hdr.setItemMeta(hm);
        inv.setItem(4, hdr);
        inv.setItem(5, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(6, pane(Material.CYAN_STAINED_GLASS_PANE));
        inv.setItem(7, pane(Material.PURPLE_STAINED_GLASS_PANE));
        inv.setItem(8, pane(Material.PURPLE_STAINED_GLASS_PANE));

        // ── Rows 1–4: Spawner items (slots 9–44) ─────────────────────────
        int slot = 9;
        for (SpawnerEntry entry : spawnerEntries) {
            if (slot >= 45) break;
            inv.setItem(slot++, createSpawnerDisplayItem(entry));
        }

        // ── Row 5: Navigation footer ──────────────────────────────────────
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

        player.openInventory(inv);
    }

    /** Build the display ItemStack shown in the Spawners shop page. */
    private ItemStack createSpawnerDisplayItem(SpawnerEntry entry) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta rawMeta = item.getItemMeta();
        // Set the entity type so the spawner preview shows the correct mob
        if (rawMeta instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof CreatureSpawner cs) {
                cs.setSpawnedType(entry.entityType);
                bsm.setBlockState(cs);
            }
            double fv = entry.factionValue;
            bsm.setDisplayName("§6§l" + entry.displayName + " Spawner");
            List<String> lore = new ArrayList<>();
            lore.add("§7");
            lore.add("§aBuy Price: §e$" + formatMoney(entry.buyPrice));
            lore.add("§7");
            lore.add("§7Faction territory value over time:");
            lore.add("§e   0h §8→ §f$" + formatMoney(fv * 0.50) + "  §850%");
            lore.add("§e  24h §8→ §f$" + formatMoney(fv * 0.75) + "  §875%");
            lore.add("§e  48h §8→ §a$" + formatMoney(fv)        + "  §a100% §7(full)");
            lore.add("§7");
            lore.add("§e▸ Left Click: §aBuy 1");
            lore.add("§e▸ Shift + Left: §aBuy 16");
            bsm.setLore(lore);
            item.setItemMeta(bsm);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();

        // Gate: only handle Faction Market GUIs
        if (!title.startsWith("§5✦")) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        // ── Main market menu ─────────────────────────────────────────────
        if (title.contains("FACTION MARKET")) {
            if (displayName.contains("Blocks"))    { openCategory(player, "blocks");  return; }
            if (displayName.contains("Minerals"))  { openCategory(player, "ores");    return; }
            if (displayName.contains("Farming"))   { openCategory(player, "farming"); return; }
            if (displayName.contains("Food"))      { openCategory(player, "food");    return; }
            if (displayName.contains("Combat"))    { openCategory(player, "combat");  return; }
            if (displayName.contains("Raiding"))   { openCategory(player, "raiding"); return; }
            if (displayName.contains("Misc"))      { openCategory(player, "misc");    return; }
            if (displayName.contains("Spawners"))  { openCategory(player, "spawners"); return; }
            // Sell Inventory button
            if (displayName.contains("Sell Inventory")) {
                double total = 0;
                for (ItemStack stack : player.getInventory().getContents()) {
                    if (stack == null || stack.getType() == Material.AIR) continue;
                    double price = getSellPrice(stack.getType());
                    if (price > 0) {
                        total += price * stack.getAmount();
                        player.getInventory().remove(stack);
                    }
                }
                if (total > 0) {
                    economy.depositPlayer(player, total);
                    player.sendMessage("§a✓ Sold inventory for §e$" + formatMoney(total));
                } else {
                    player.sendMessage("§cNo sellable items found in your inventory.");
                }
                // Refresh balance display
                event.getView().getTopInventory().setItem(39, createIcon(
                        Material.GOLD_INGOT, "§e§lYour Balance",
                        "§a$" + formatMoney(economy.getBalance(player))));
                return;
            }
            return;
        }

        // ── Spawners category ────────────────────────────────────────────
        if (title.contains("Spawners")) {
            if (displayName.contains("Back to Market")) { openMainShop(player); return; }
            if (displayName.contains("Balance")) return;

            if (clicked.getType() == Material.SPAWNER && clicked.getItemMeta() instanceof BlockStateMeta bsm) {
                BlockState state = bsm.getBlockState();
                if (state instanceof CreatureSpawner cs && cs.getSpawnedType() != null) {
                    SpawnerEntry entry = findSpawnerEntry(cs.getSpawnedType());
                    if (entry != null) {
                        int amount = event.isShiftClick() ? 16 : 1;
                        buySpawner(player, entry, amount);
                        event.getView().getTopInventory().setItem(50, createIcon(
                                Material.GOLD_INGOT, "§e§lYour Balance",
                                "§a$" + formatMoney(economy.getBalance(player))));
                    }
                }
            }
            return;
        }

        // ── Item category pages ──────────────────────────────────────────
        if (displayName.contains("Back to Market")) { openMainShop(player); return; }
        if (displayName.contains("Balance")) return;
        // ignore glass pane decorations
        if (displayName.equals("§r")) return;

        Material itemType = clicked.getType();
        ShopItem shopItem = findShopItem(itemType);
        if (shopItem == null) return;

        int amount = event.isShiftClick() ? 64 : 1;
        if (event.isLeftClick()) {
            buyItem(player, shopItem, amount);
        } else {
            sellItem(player, shopItem, amount);
        }

        // Refresh balance in-place (slot 50 in category pages)
        event.getView().getTopInventory().setItem(50, createIcon(
                Material.GOLD_INGOT,
                "§e§lYour Balance",
                "§a$" + formatMoney(economy.getBalance(player))));
    }

    private void buySpawner(Player player, SpawnerEntry entry, int amount) {
        double totalCost = entry.buyPrice * amount;

        if (economy.getBalance(player) < totalCost) {
            player.sendMessage("§cYou don't have enough money! Need $" + formatMoney(totalCost));
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full!");
            return;
        }

        economy.withdrawPlayer(player, totalCost);

        // Build the spawner item with the correct entity type
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, amount);
        ItemMeta rawMeta = spawnerItem.getItemMeta();
        if (rawMeta instanceof BlockStateMeta bsm) {
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

        player.sendMessage("§a✓ Bought §e" + amount + "x " + entry.displayName
                + " Spawner §afor §e$" + formatMoney(totalCost));
        player.sendMessage("§7New balance: §a$" + formatMoney(economy.getBalance(player)));
        player.sendMessage("§7▸ Place in faction territory to start earning value!");
    }

    private SpawnerEntry findSpawnerEntry(EntityType type) {
        for (SpawnerEntry e : spawnerEntries) {
            if (e.entityType == type) return e;
        }
        return null;
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

    /** Public lookup used by /sellall — returns sell price or -1 if not sold */
    public double getSellPrice(Material material) {
        ShopItem item = findShopItem(material);
        return item == null ? -1 : item.sellPrice;
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

    // ── Decorative glass pane (invisible name so tooltips are clean) ──────────
    private ItemStack pane(Material material) {
        ItemStack p = new ItemStack(material);
        ItemMeta m = p.getItemMeta();
        m.setDisplayName("§r");
        p.setItemMeta(m);
        return p;
    }

    // ── Category button shown on the main market screen ──────────────────────
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

    // ── Header icon displayed at the top of category pages ───────────────────
    private ItemStack createCategoryHeaderIcon(String category, String displayName) {
        Material mat = switch (category) {
            case "blocks"  -> Material.STONE;
            case "ores"    -> Material.DIAMOND;
            case "farming" -> Material.WHEAT;
            case "food"    -> Material.COOKED_BEEF;
            case "combat"  -> Material.DIAMOND_SWORD;
            case "raiding" -> Material.DISPENSER;
            case "misc"    -> Material.ENDER_EYE;
            default        -> Material.CHEST;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§l" + displayName);
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

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "blocks"   -> "Blocks";
            case "ores"     -> "Ores & Minerals";
            case "farming"  -> "Farming";
            case "food"     -> "Food";
            case "combat"   -> "Combat";
            case "raiding"  -> "Raiding";
            case "misc"     -> "Miscellaneous";
            case "spawners" -> "Spawners";
            default         -> "Shop";
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
     * Spawner shop entry (buy-only; placed spawner values ramp up over 48h in faction territory).
     */
    private static class SpawnerEntry {
        final EntityType entityType;
        final String displayName;
        final double buyPrice;
        final double factionValue; // actual territory base value (from SpawnerType)

        SpawnerEntry(EntityType entityType, String displayName, double buyPrice, double factionValue) {
            this.entityType   = entityType;
            this.displayName  = displayName;
            this.buyPrice     = buyPrice;
            this.factionValue = factionValue;
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
