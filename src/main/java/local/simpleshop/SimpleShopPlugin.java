package local.simpleshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SimpleShop - Buy and sell items via GUI
 */
public class SimpleShopPlugin extends JavaPlugin {

    private Economy economy;
    private ShopGUI shopGUI;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling SimpleShop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopGUI = new ShopGUI(this, economy);
        
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
        return false;
    }

    public Economy getEconomy() {
        return economy;
    }
}
