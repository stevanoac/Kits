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

public class KitContentsMenu {

    public static final int SIZE = 54;
    public static final int BORDER_START = 36;
    public static final int BORDER_END = 44;
    public static final int SLOT_BACK = 45;
    public static final int SLOT_IMPORT = 48;
    public static final int SLOT_CLEAR = 50;
    public static final int SLOT_SAVE = 53;

    public static void open(Player player, Kit kit) {
        KitContentsHolder holder = new KitContentsHolder(kit);
        Inventory inv = Bukkit.createInventory(holder, SIZE, CC.translate("&8Edit Contents: &b" + kit.getName()));
        holder.setInventory(inv);

        ItemStack[] contents = kit.getContents();
        if (contents != null) {
            for (int i = 0; i < 36 && i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    inv.setItem(i, contents[i].clone());
                }
            }
        }

        ItemStack borderPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 15)
                .name("&7")
                .build();
        for (int i = BORDER_START; i <= BORDER_END; i++) {
            inv.setItem(i, borderPane);
        }

        for (int i = 45; i < 54; i++) {
            if (i != SLOT_BACK && i != SLOT_IMPORT && i != SLOT_CLEAR && i != SLOT_SAVE) {
                inv.setItem(i, borderPane);
            }
        }

        inv.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .name("&c&l« Back to Kit Editor")
                .lore("&7Saves changes and returns to", "&7the main kit editor menu.")
                .build());

        inv.setItem(SLOT_IMPORT, new ItemBuilder(Material.HOPPER)
                .name("&e&lImport My Inventory")
                .lore("&7Click to overwrite slots 0-35", "&7with your current inventory items.")
                .build());

        inv.setItem(SLOT_CLEAR, new ItemBuilder(Material.LAVA_BUCKET)
                .name("&c&lClear All Items")
                .lore("&7Clears all 36 kit items.")
                .build());

        inv.setItem(SLOT_SAVE, new ItemBuilder(Material.EMERALD)
                .name("&a&lSave & Return")
                .lore("&7Click to save kit contents to file", "&7and return to the editor.")
                .build());

        player.openInventory(inv);
    }

    public static void saveContents(Inventory inv, Kit kit) {
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                contents[i] = item.clone();
            } else {
                contents[i] = null;
            }
        }
        kit.setContents(contents);
        try {
            Kits.getInstance().getKitManager().saveKit(kit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
