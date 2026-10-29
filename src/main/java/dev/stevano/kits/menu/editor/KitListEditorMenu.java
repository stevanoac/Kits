package dev.stevano.kits.menu.editor;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.util.CC;
import dev.stevano.kits.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KitListEditorMenu {

    public static final int SIZE = 36;

    public static void open(Player player) {
        KitManager manager = Kits.getInstance().getKitManager();
        Collection<Kit> kits = manager.getKits();

        KitListEditorHolder holder = new KitListEditorHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, CC.translate("&8Select a Kit to Edit"));
        holder.setInventory(inv);

        ItemStack borderPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 15)
                .name("&7")
                .build();

        for (int i = 0; i < SIZE; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, borderPane);
            }
        }

        inv.setItem(31, new ItemBuilder(Material.BARRIER)
                .name("&c&lClose Menu")
                .lore("&7Click to close this menu.")
                .build());

        int[] availableSlots = new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        };

        int slotIndex = 0;
        for (Kit kit : kits) {
            if (slotIndex >= availableSlots.length) break;
            int slot = availableSlots[slotIndex++];
            holder.mapKit(slot, kit);

            ItemStack icon = kit.getIcon() != null && kit.getIcon().getType() != Material.AIR
                    ? kit.getIcon().clone()
                    : new ItemStack(Material.DIAMOND_SWORD);

            List<String> lore = new ArrayList<>();
            lore.add("&7-------------------------");
            lore.add("&7Name: &f" + kit.getName());
            lore.add("&7Display: &f" + CC.translate(kit.getDisplayName()));
            lore.add("&7Permission: &f" + (kit.getPermission() != null ? kit.getPermission() : "None"));
            lore.add("");
            lore.add("&a» Click to edit this kit «");
            lore.add("&7-------------------------");

            inv.setItem(slot, new ItemBuilder(icon)
                    .name("&b&l" + kit.getName())
                    .lore(lore)
                    .build());
        }

        if (kits.isEmpty()) {
            inv.setItem(13, new ItemBuilder(Material.COAL)
                    .name("&c&lNo Kits Found")
                    .lore("&7Use &e/kit create <name> &7to create one!")
                    .build());
        }

        player.openInventory(inv);
    }
}
