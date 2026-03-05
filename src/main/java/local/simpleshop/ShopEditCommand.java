package local.simpleshop;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /shopedit – edit an existing category's metadata or item list.
 *
 * Sub-commands:
 *   /shopedit <category> position <n>             – move category to position n
 *   /shopedit <category> name <displayName>       – rename category
 *   /shopedit <category> icon <MATERIAL>          – change icon
 *   /shopedit <category> tagline <text>           – change tagline
 *   /shopedit <category> color <colorCode>        – change color prefix (e.g. §a§l)
 *   /shopedit <category> remove <MATERIAL>        – remove an item from the category
 */
public class ShopEditCommand implements CommandExecutor, TabCompleter {

    private final SimpleShopPlugin plugin;

    private static final List<String> SUB_COMMANDS =
            List.of("position", "name", "icon", "tagline", "color", "remove");

    public ShopEditCommand(SimpleShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simpleshop.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sendUsage(sender);
            return true;
        }

        ShopConfig cfg = plugin.getShopConfig();
        String catId  = args[0].toLowerCase();
        String sub    = args[1].toLowerCase();
        String value  = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        ShopConfig.ShopCategory cat = cfg.getCategory(catId);
        if (cat == null) {
            sender.sendMessage("§cCategory §e" + catId + " §cnot found.");
            return true;
        }

        switch (sub) {
            case "position" -> {
                int pos;
                try {
                    pos = Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cPosition must be a number.");
                    return true;
                }
                cfg.setCategoryPosition(catId, pos);
                cfg.save();
                sender.sendMessage("§a✓ Category §e" + catId + " §amoved to position §e" + pos + "§a.");
            }
            case "name" -> {
                cfg.setCategoryName(catId, value);
                cfg.save();
                sender.sendMessage("§a✓ Category §e" + catId + " §arenamed to §e" + value + "§a.");
            }
            case "icon" -> {
                Material mat = Material.matchMaterial(value.toUpperCase());
                if (mat == null) {
                    sender.sendMessage("§cUnknown material: §e" + value);
                    return true;
                }
                cfg.setCategoryIcon(catId, mat);
                cfg.save();
                sender.sendMessage("§a✓ Category §e" + catId + " §aicon set to §e" + mat.name() + "§a.");
            }
            case "tagline" -> {
                // value is the new tagline; keep existing detail
                cfg.setCategoryTagline(catId, value, cat.detail);
                cfg.save();
                sender.sendMessage("§a✓ Category §e" + catId + " §atagline set to §e" + value + "§a.");
            }
            case "color" -> {
                // Accept raw §-codes or replace & with §
                String colorCode = value.replace("&", "§");
                cfg.setCategoryColor(catId, colorCode);
                cfg.save();
                sender.sendMessage("§a✓ Category §e" + catId + " §acolor updated.");
            }
            case "remove" -> {
                Material mat = Material.matchMaterial(value.toUpperCase());
                if (mat == null) {
                    sender.sendMessage("§cUnknown material: §e" + value);
                    return true;
                }
                boolean removed = cfg.removeItem(catId, mat);
                cfg.save();
                if (removed) {
                    sender.sendMessage("§a✓ Removed §e" + mat.name() + " §afrom §e" + catId + "§a.");
                } else {
                    sender.sendMessage("§cItem §e" + mat.name() + " §cnot found in §e" + catId + "§c.");
                }
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("simpleshop.admin")) return List.of();

        if (args.length == 1) {
            return plugin.getShopConfig().getCategoriesOrdered().stream()
                    .map(c -> c.id)
                    .filter(id -> id.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (sub.equals("icon") || sub.equals("remove")) {
                return java.util.Arrays.stream(Material.values())
                        .map(m -> m.name().toLowerCase())
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§7  /shopedit <category> position <n>");
        sender.sendMessage("§7  /shopedit <category> name <displayName>");
        sender.sendMessage("§7  /shopedit <category> icon <MATERIAL>");
        sender.sendMessage("§7  /shopedit <category> tagline <text>");
        sender.sendMessage("§7  /shopedit <category> color <colorCode>");
        sender.sendMessage("§7  /shopedit <category> remove <MATERIAL>");
    }
}
