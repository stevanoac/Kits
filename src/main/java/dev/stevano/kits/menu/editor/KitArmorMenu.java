package dev.stevano.kits.menu.editor;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.util.CC;
import dev.stevano.kits.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitArmorMenu {

    public static final int SIZE = 27;
    public static final int SLOT_HELMET = 10;
    public static final int SLOT_CHESTPLATE = 12;
    public static final int SLOT_LEGGINGS = 14;
    public static final int SLOT_BOOTS = 16;

    public static final int SLOT_BACK = 18;
    public static final int SLOT_IMPORT = 20;
    public static final int SLOT_CLEAR = 22;
    public static final int SLOT_SAVE = 26;

    public static void open(Player player, Kit kit) {
        KitArmorHolder holder = new KitArmorHolder(kit);
        Inventory inv = Bukkit.createInventory(holder, SIZE, CC.translate("&8Edit Armor: &b" + kit.getName()));
        holder.setInventory(inv);

        ItemStack borderPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 15)
                .name("&7")
                .build();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, borderPane);
        }

        ItemStack[] armor = kit.getArmor();
        ItemStack boots = armor != null && armor.length > 0 ? armor[0] : null;
        ItemStack leggings = armor != null && armor.length > 1 ? armor[1] : null;
        ItemStack chestplate = armor != null && armor.length > 2 ? armor[2] : null;
        ItemStack helmet = armor != null && armor.length > 3 ? armor[3] : null;

        inv.setItem(SLOT_HELMET, helmet != null && helmet.getType() != Material.AIR ? helmet.clone() : createPlaceholder(Material.LEATHER_HELMET, "&bHelmet Slot", "&7Place a helmet here"));
        inv.setItem(SLOT_CHESTPLATE, chestplate != null && chestplate.getType() != Material.AIR ? chestplate.clone() : createPlaceholder(Material.LEATHER_CHESTPLATE, "&bChestplate Slot", "&7Place a chestplate here"));
        inv.setItem(SLOT_LEGGINGS, leggings != null && leggings.getType() != Material.AIR ? leggings.clone() : createPlaceholder(Material.LEATHER_LEGGINGS, "&bLeggings Slot", "&7Place leggings here"));
        inv.setItem(SLOT_BOOTS, boots != null && boots.getType() != Material.AIR ? boots.clone() : createPlaceholder(Material.LEATHER_BOOTS, "&bBoots Slot", "&7Place boots here"));

        inv.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .name("&c&l« Back to Kit Editor")
                .lore("&7Saves armor changes and returns", "&7to the main kit editor menu.")
                .build());

        inv.setItem(SLOT_IMPORT, new ItemBuilder(Material.HOPPER)
                .name("&e&lImport Worn Armor")
                .lore("&7Click to copy the armor you", "&7are currently wearing into this kit.")
                .build());

        inv.setItem(SLOT_CLEAR, new ItemBuilder(Material.LAVA_BUCKET)
                .name("&c&lClear Armor")
                .lore("&7Removes all armor pieces from the kit.")
                .build());

        inv.setItem(SLOT_SAVE, new ItemBuilder(Material.EMERALD)
                .name("&a&lSave & Return")
                .lore("&7Click to save kit armor to file", "&7and return to the editor.")
                .build());

        player.openInventory(inv);
    }

    public static boolean isArmorSlot(int slot) {
        return slot == SLOT_HELMET || slot == SLOT_CHESTPLATE || slot == SLOT_LEGGINGS || slot == SLOT_BOOTS;
    }

    public static boolean isPlaceholder(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name.contains("Slot");
    }

    private static ItemStack createPlaceholder(Material mat, String name, String lore) {
        return new ItemBuilder(mat)
                .name(name)
                .lore(lore, "&e» Click with an armor item to set «")
                .build();
    }

    public static void saveArmor(Inventory inv, Kit kit) {
        ItemStack[] armor = new ItemStack[4];

        ItemStack boots = inv.getItem(SLOT_BOOTS);
        ItemStack leggings = inv.getItem(SLOT_LEGGINGS);
        ItemStack chestplate = inv.getItem(SLOT_CHESTPLATE);
        ItemStack helmet = inv.getItem(SLOT_HELMET);

        armor[0] = boots != null && !isPlaceholder(boots) && boots.getType() != Material.AIR ? boots.clone() : null;
        armor[1] = leggings != null && !isPlaceholder(leggings) && leggings.getType() != Material.AIR ? leggings.clone() : null;
        armor[2] = chestplate != null && !isPlaceholder(chestplate) && chestplate.getType() != Material.AIR ? chestplate.clone() : null;
        armor[3] = helmet != null && !isPlaceholder(helmet) && helmet.getType() != Material.AIR ? helmet.clone() : null;

        kit.setArmor(armor);
        try {
            Kits.getInstance().getKitManager().saveKit(kit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
