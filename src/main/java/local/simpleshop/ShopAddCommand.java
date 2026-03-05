package local.simpleshop;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /shopadd – add items or categories to the shop.
 *
 * Usage:
 *   /shopadd <MATERIAL> <categoryId> <buyPrice> <sellPrice>
 *   /shopadd category <id> <MATERIAL_icon> [position]
 */
public class ShopAddCommand implements CommandExecutor, TabCompleter {

    private final SimpleShopPlugin plugin;

    public ShopAddCommand(SimpleShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simpleshop.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        ShopConfig cfg = plugin.getShopConfig();

        // ── /shopadd category <id> <icon> [position] ─────────────────────
        if (args.length >= 3 && args[0].equalsIgnoreCase("category")) {
            String id = args[1].toLowerCase();
            Material icon = Material.matchMaterial(args[2].toUpperCase());
            if (icon == null) {
                sender.sendMessage("§cUnknown material: §e" + args[2]);
                return true;
            }
            int position = -1; // -1 = append at end
            if (args.length >= 4) {
                try {
                    position = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cPosition must be a number.");
                    return true;
                }
            }

            // Derive a readable display name from the id
            String displayName = toTitleCase(id);
            if (cfg.getCategory(id) != null) {
                sender.sendMessage("§cCategory §e" + id + " §calready exists. Use §e/shopedit §cto modify it.");
                return true;
            }
            cfg.addCategory(id, displayName, icon, position);
            cfg.save();
            sender.sendMessage("§a✓ Category §e" + id + " §acreated"
                    + (position > 0 ? " at position §e" + position : "") + "§a.");
            return true;
        }

        // ── /shopadd <MATERIAL> <categoryId> <buy> <sell> ────────────────
        if (args.length >= 4) {
            Material mat = Material.matchMaterial(args[0].toUpperCase());
            if (mat == null) {
                sender.sendMessage("§cUnknown material: §e" + args[0]);
                return true;
            }
            String categoryId = args[1].toLowerCase();
            double buy, sell;
            try {
                buy  = Double.parseDouble(args[2]);
                sell = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cBuy/sell prices must be numbers.");
                return true;
            }

            String displayName = toTitleCase(mat.name().replace('_', ' '));
            cfg.addItem(categoryId, mat, displayName, buy, sell);
            cfg.save();
            sender.sendMessage("§a✓ Added §e" + displayName
                    + " §ato §e" + categoryId
                    + " §a(buy §e$" + buy + " §a/ sell §e$" + sell + "§a).");
            return true;
        }

        sender.sendMessage("§eUsage:");
        sender.sendMessage("§7  /shopadd <MATERIAL> <category> <buyPrice> <sellPrice>");
        sender.sendMessage("§7  /shopadd category <id> <MATERIAL_icon> [position]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("simpleshop.admin")) return List.of();

        if (args.length == 1) {
            List<String> base = Arrays.stream(Material.values())
                    .map(m -> m.name().toLowerCase())
                    .collect(Collectors.toList());
            base.add(0, "category");
            return base.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("category")) return List.of(); // free-text id
            return plugin.getShopConfig().getCategoriesOrdered().stream()
                    .map(c -> c.id)
                    .filter(id -> id.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("category")) {
            return Arrays.stream(Material.values())
                    .map(m -> m.name().toLowerCase())
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private static String toTitleCase(String s) {
        String[] words = s.replace('_', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
