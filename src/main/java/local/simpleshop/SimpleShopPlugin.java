package local.simpleshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SimpleShop - Buy and sell items via GUI
 */
public class SimpleShopPlugin extends JavaPlugin {

    private Economy economy;
    private ShopConfig shopConfig;
    private ShopGUI shopGUI;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling SimpleShop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopConfig = new ShopConfig(this);
        shopConfig.load();

        shopGUI = new ShopGUI(this, economy, shopConfig);

        getCommand("shopadd").setExecutor(new ShopAddCommand(this));
        getCommand("shopadd").setTabCompleter(new ShopAddCommand(this));
        getCommand("shopedit").setExecutor(new ShopEditCommand(this));
        getCommand("shopedit").setTabCompleter(new ShopEditCommand(this));

        getLogger().info("SimpleShop enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleShop disabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("shop")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            shopGUI.openMainShop(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("sellall")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            handleSellAll(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("shopadd") || command.getName().equalsIgnoreCase("shopedit")) {
            return false; // handled via setExecutor
        }

        return false;
    }

    private void handleSellAll(Player player) {
        double total = 0;
        int itemsSold = 0;

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            double price = shopGUI.getSellPrice(stack.getType());
            if (price < 0) continue; // not a shop item

            total += price * stack.getAmount();
            itemsSold += stack.getAmount();
            stack.setAmount(0); // removes the item
        }

        if (itemsSold == 0) {
            player.sendMessage("§cYou have no items that can be sold.");
            return;
        }

        economy.depositPlayer(player, total);
        player.sendMessage("§a✓ Sold §e" + itemsSold + "§a item(s) for §e$"
                + String.format("%.2f", total) + "§a!");
        player.sendMessage("§7New balance: §a$" + String.format("%.2f", economy.getBalance(player)));
    }

    public Economy getEconomy() {
        return economy;
    }

    public ShopConfig getShopConfig() {
        return shopConfig;
    }

    public ShopGUI getShopGUI() {
        return shopGUI;
    }
}
