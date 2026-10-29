package dev.stevano.kits.menu.editor;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.menu.KitSelectorMenu;
import dev.stevano.kits.util.CC;
import dev.stevano.kits.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class KitEditorMenu {

    public static final int SIZE = 45;

    public static final int SLOT_ARMOR = 11;
    public static final int SLOT_INFO_ICON = 13;
    public static final int SLOT_CONTENTS = 15;

    public static final int SLOT_EFFECTS = 20;
    public static final int SLOT_IMPORT_ALL = 22;
    public static final int SLOT_TEST = 24;

    public static final int SLOT_CLOSE = 38;
    public static final int SLOT_SAVE = 40;
    public static final int SLOT_PREVIEW = 42;

    public static void open(Player player, Kit kit) {
        KitEditorHolder holder = new KitEditorHolder(kit);
        Inventory inv = Bukkit.createInventory(holder, SIZE, CC.translate("&8Kit Editor: &b" + kit.getName()));
        holder.setInventory(inv);

        render(inv, kit);
        player.openInventory(inv);
    }

    public static void render(Inventory inv, Kit kit) {
        ItemStack borderPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 15)
                .name("&7")
                .build();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, borderPane);
        }

        int armorCount = countArmor(kit.getArmor());
        inv.setItem(SLOT_ARMOR, new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .name("&b&lEdit Armor")
                .lore(
                        "&7Modify the armor worn with this kit.",
                        "",
                        "&7Configured Pieces: &e" + armorCount + "&7/4",
                        getArmorSummaryLine(kit),
                        "",
                        "&a» Click to open armor editor «"
                )
                .build());

        ItemStack iconItem = kit.getIcon() != null && kit.getIcon().getType() != Material.AIR
                ? kit.getIcon().clone()
                : new ItemStack(Material.DIAMOND_SWORD);

        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Information and display icon.");
        infoLore.add("");
        infoLore.add("&7Name: &f" + kit.getName());
        infoLore.add("&7Display Name: &f" + CC.translate(kit.getDisplayName()));
        infoLore.add("&7Permission: &f" + (kit.getPermission() != null ? kit.getPermission() : "None"));
        infoLore.add("&7Description: &f" + (kit.getDescription() != null && !kit.getDescription().isEmpty() ? kit.getDescription() : "None"));
        infoLore.add("");
        infoLore.add("&e» Click with item in hand to change icon «");

        inv.setItem(SLOT_INFO_ICON, new ItemBuilder(iconItem)
                .name("&e&lKit Info & Icon")
                .lore(infoLore)
                .build());

        int contentsCount = countContents(kit.getContents());
        inv.setItem(SLOT_CONTENTS, new ItemBuilder(Material.CHEST)
                .name("&6&lEdit Inventory Items")
                .lore(
                        "&7Modify the 36 inventory item slots.",
                        "",
                        "&7Configured Items: &e" + contentsCount + "&7/36",
                        "",
                        "&a» Click to open items editor «"
                )
                .build());

        List<String> effectLore = new ArrayList<>();
        effectLore.add("&7Potion effects granted upon kit equip.");
        effectLore.add("");
        if (kit.getPotionEffects() != null && !kit.getPotionEffects().isEmpty()) {
            effectLore.add("&7Active Effects:");
            for (PotionEffect effect : kit.getPotionEffects()) {
                effectLore.add(" &7* &d" + effect.getType().getName() + " &7(Amplifier: &e" + effect.getAmplifier()
                        + "&7, Duration: &e" + (effect.getDuration() / 20) + "s&7)");
            }
        } else {
            effectLore.add("&7Active Effects: &cNone");
        }
        effectLore.add("");
        effectLore.add("&b» Left-Click: Copy active effects from yourself");
        effectLore.add("&c» Right-Click: Clear all effects");

        inv.setItem(SLOT_EFFECTS, new ItemBuilder(Material.BREWING_STAND_ITEM)
                .name("&d&lPotion Effects")
                .lore(effectLore)
                .build());

        inv.setItem(SLOT_IMPORT_ALL, new ItemBuilder(Material.HOPPER)
                .name("&a&lCopy My Whole Inventory")
                .lore(
                        "&7Quickly overwrite all kit armor, contents,",
                        "&7and active potion effects with your current player state!",
                        "",
                        "&e» Click to import loadout «"
                )
                .build());

        inv.setItem(SLOT_TEST, new ItemBuilder(Material.GOLDEN_APPLE)
                .name("&e&lTest Kit (Equip)")
                .lore(
                        "&7Applies this kit directly to you so you can test",
                        "&7how it feels and plays.",
                        "",
                        "&a» Click to equip and test «"
                )
                .build());

        inv.setItem(SLOT_CLOSE, new ItemBuilder(Material.BARRIER)
                .name("&c&lClose Menu")
                .lore("&7Close the kit editor.")
                .build());

        inv.setItem(SLOT_SAVE, new ItemBuilder(Material.EMERALD)
                .name("&a&lSave Kit to Disk")
                .lore(
                        "&7Save all changes to &f" + kit.getName().toLowerCase() + ".yml&7.",
                        "",
                        "&a» Click to save «"
                )
                .build());

        inv.setItem(SLOT_PREVIEW, new ItemBuilder(Material.COMPASS)
                .name("&b&lPreview in /kit")
                .lore(
                        "&7Opens the player kit selector GUI so you can see",
                        "&7how this kit looks to players.",
                        "",
                        "&b» Click to view kit selector «"
                )
                .build());
    }

    private static String getArmorSummaryLine(Kit kit) {
        ItemStack[] armor = kit.getArmor();
        if (armor == null) return " &7Armor: &cNone";
        StringBuilder sb = new StringBuilder(" &7[");
        sb.append(armor.length > 3 && armor[3] != null ? "&aH" : "&7-");
        sb.append(" ");
        sb.append(armor.length > 2 && armor[2] != null ? "&aC" : "&7-");
        sb.append(" ");
        sb.append(armor.length > 1 && armor[1] != null ? "&aL" : "&7-");
        sb.append(" ");
        sb.append(armor.length > 0 && armor[0] != null ? "&aB" : "&7-");
        sb.append("&7]");
        return sb.toString();
    }

    private static int countArmor(ItemStack[] armor) {
        if (armor == null) return 0;
        int count = 0;
        for (ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) count++;
        }
        return count;
    }

    private static int countContents(ItemStack[] contents) {
        if (contents == null) return 0;
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) count++;
        }
        return count;
    }
}
