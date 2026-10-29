package dev.stevano.kits.command;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.menu.KitSelectorMenu;
import dev.stevano.kits.util.CC;
import dev.stevanoac.commando.annotation.Command;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class KitCommand {

    private final Kits plugin;

    public KitCommand() {
        this(Kits.getInstance());
    }

    public KitCommand(Kits plugin) {
        this.plugin = plugin;
    }

    private KitManager getKitManager() {
        if (this.plugin != null && this.plugin.getKitManager() != null) {
            return this.plugin.getKitManager();
        }
        return Kits.getInstance().getKitManager();
    }

    @Command(name = "kit", description = "Select or view kits", permission = "kit.command")
    public void kit(Player player, String[] args) {
        KitManager manager = getKitManager();

        if (args.length == 0) {
            new KitSelectorMenu(player).updateMenu();
            return;
        }

        String kitName = args[0];
        Kit kit = manager.getKit(kitName);

        if (kit == null) {
            CC.message(player, "&cKit '&f" + kitName + "&c' does not exist! Use &e/kit &cto open the kit menu.");
            return;
        }

        String permission = kit.getPermission();
        if (permission != null && !permission.trim().isEmpty() && !player.hasPermission(permission)) {
            CC.message(player, "&cYou do not have permission to equip the &f" + CC.translate(kit.getDisplayName()) + " &ckit!");
            return;
        }

        kit.apply(player);
        CC.message(player, "&aYou have successfully equipped the &f" + CC.translate(kit.getDisplayName()) + " &akit!");
    }

    @Command(name = "kit list", description = "List all available kits", permission = "kit.command", console = true)
    public void list(CommandSender sender) {
        KitManager manager = getKitManager();
        Collection<Kit> kits = manager.getKits();

        CC.sender(sender, CC.CHAT_BAR);
        CC.sender(sender, "&b&lAvailable Kits &7(" + kits.size() + "):");

        if (kits.isEmpty()) {
            CC.sender(sender, " &cNo kits are currently loaded.");
        } else {
            for (Kit kit : kits) {
                boolean hasPerm = kit.getPermission() == null || kit.getPermission().trim().isEmpty() || sender.hasPermission(kit.getPermission());
                String status = hasPerm ? "&a[Available]" : "&c[Locked]";
                CC.sender(sender, " &7* " + (hasPerm ? "&a" : "&c") + kit.getName()
                        + " &7- &f" + CC.translate(kit.getDisplayName()) + " " + status);
            }
        }

        CC.sender(sender, "&7Use &b/kit &7to open the GUI, or &b/kit <name> &7to equip.");
        CC.sender(sender, CC.CHAT_BAR);
    }

    @Command(name = "kit create", description = "Create a new kit from your inventory", permission = "kit.admin")
    public void create(Player player, String kitName) {
        KitManager manager = getKitManager();

        Kit kit = new Kit(kitName);
        kit.setDisplayName("&a" + kitName.replace("_", " "));
        kit.setPermission("kit." + kitName.toLowerCase());
        kit.setDescription("Custom created kit");

        kit.setContents(player.getInventory().getContents().clone());
        kit.setArmor(player.getInventory().getArmorContents().clone());

        ItemStack inHand = player.getItemInHand();
        if (inHand != null && inHand.getType() != Material.AIR) {
            kit.setIcon(inHand.clone());
        } else {
            kit.setIcon(new ItemStack(Material.DIAMOND_SWORD, 1));
        }

        kit.setPotionEffects(new HashSet<>(player.getActivePotionEffects()));

        manager.registerKit(kit);
        manager.save(kit);

        CC.message(player, "&aSuccessfully created and saved kit &f" + kit.getName() + " &a(&f" + CC.translate(kit.getDisplayName()) + "&a)!");
    }

    @Command(name = "kit edit", description = "Edit an existing kit", permission = "kit.admin")
    public void edit(Player player, String[] args) {
        KitManager manager = getKitManager();

        if (args == null || args.length == 0) {
            dev.stevano.kits.menu.editor.KitListEditorMenu.open(player);
            return;
        }

        String kitName = args[0];
        Kit kit = manager.getKit(kitName);

        if (kit == null) {
            CC.message(player, "&cKit '&f" + kitName + "&c' does not exist! Use &e/kit edit &cto select from existing kits.");
            return;
        }

        dev.stevano.kits.menu.editor.KitEditorMenu.open(player, kit);
    }

    @Command(name = "kit delete", description = "Delete an existing kit", permission = "kit.admin", console = true)
    public void delete(CommandSender sender, String kitName) {
        KitManager manager = getKitManager();
        boolean deleted = manager.deleteKit(kitName);

        if (deleted) {
            CC.sender(sender, "&aSuccessfully deleted kit &f" + kitName + "&a.");
        } else {
            CC.sender(sender, "&cKit '&f" + kitName + "&c' not found.");
        }
    }

    @Command(name = "kit give", description = "Give a kit to a player", permission = "kit.admin", console = true)
    public void give(CommandSender sender, Player target, String kitName) {
        if (target == null || !target.isOnline()) {
            CC.sender(sender, "&cTarget player is not online.");
            return;
        }

        KitManager manager = getKitManager();
        Kit kit = manager.getKit(kitName);

        if (kit == null) {
            CC.sender(sender, "&cKit '&f" + kitName + "&c' does not exist.");
            return;
        }

        kit.apply(target);

        CC.message(target, "&aYou were given the &f" + CC.translate(kit.getDisplayName()) + " &akit by &f" + sender.getName() + "&a!");
        CC.sender(sender, "&aSuccessfully gave &f" + CC.translate(kit.getDisplayName()) + " &akit to &f" + target.getName() + "&a.");
    }

    @Command(name = "kit reload", description = "Reload all kit and menu configurations", permission = "kit.admin", console = true)
    public void reload(CommandSender sender) {
        KitManager manager = getKitManager();
        manager.load();

        if (this.plugin != null && this.plugin.getMenuManager() != null) {
            this.plugin.getMenuManager().reload();
        } else if (Kits.getInstance().getMenuManager() != null) {
            Kits.getInstance().getMenuManager().reload();
        }

        CC.sender(sender, "&aSuccessfully reloaded all kits and menus configurations!");
    }

    @Command(name = "kit help", description = "View kits command help", permission = "kit.command", console = true)
    public void help(CommandSender sender) {
        CC.sender(sender, CC.CHAT_BAR);
        CC.sender(sender, "&b&lKits Help & Commands:");
        CC.sender(sender, " &b/kit &7- Open kit selector GUI");
        CC.sender(sender, " &b/kit <name> &7- Equip a kit directly");
        CC.sender(sender, " &b/kit list &7- List all available kits");
        if (sender.hasPermission("kit.admin")) {
            CC.sender(sender, " &e/kit edit [kit] &7- Open the interactive kit editor GUI");
            CC.sender(sender, " &e/kit create <name> &7- Create a kit from current inventory");
            CC.sender(sender, " &e/kit delete <name> &7- Delete an existing kit");
            CC.sender(sender, " &e/kit give <player> <kit> &7- Give a kit to a player");
            CC.sender(sender, " &e/kit reload &7- Reload all kits and menus.yml");
        }
        CC.sender(sender, CC.CHAT_BAR);
    }
}
