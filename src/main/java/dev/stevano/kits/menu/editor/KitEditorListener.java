package dev.stevano.kits.menu.editor;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.menu.KitSelectorMenu;
import dev.stevano.kits.util.CC;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;

public class KitEditorListener implements Listener {

    private void playSound(Player player, String soundName) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0F, 1.0F);
        } catch (Throwable ignored) {}
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null) return;

        if (topInv.getHolder() instanceof KitEditorHolder) {
            KitEditorHolder holder = (KitEditorHolder) topInv.getHolder();
            Kit kit = holder.getKit();

            if (event.getRawSlot() < KitEditorMenu.SIZE) {
                event.setCancelled(true);
                int slot = event.getRawSlot();

                if (slot == KitEditorMenu.SLOT_ARMOR) {
                    playSound(player, "CLICK");
                    KitArmorMenu.open(player, kit);
                } else if (slot == KitEditorMenu.SLOT_INFO_ICON) {
                    ItemStack inHand = player.getItemInHand();
                    ItemStack onCursor = event.getCursor();

                    if (onCursor != null && onCursor.getType() != Material.AIR) {
                        kit.setIcon(onCursor.clone());
                        playSound(player, "ORB_PICKUP");
                        KitEditorMenu.render(topInv, kit);
                        CC.message(player, "&aKit icon set to cursor item: &f" + onCursor.getType().name());
                    } else if (inHand != null && inHand.getType() != Material.AIR) {
                        kit.setIcon(inHand.clone());
                        playSound(player, "ORB_PICKUP");
                        KitEditorMenu.render(topInv, kit);
                        CC.message(player, "&aKit icon set to hand item: &f" + inHand.getType().name());
                    } else {
                        playSound(player, "VILLAGER_NO");
                        CC.message(player, "&cHold an item in hand or on cursor to set the kit icon!");
                    }
                } else if (slot == KitEditorMenu.SLOT_CONTENTS) {
                    playSound(player, "CHEST_OPEN");
                    KitContentsMenu.open(player, kit);
                } else if (slot == KitEditorMenu.SLOT_EFFECTS) {
                    if (event.isRightClick()) {
                        kit.setPotionEffects(new HashSet<>());
                        playSound(player, "SPLASH");
                        KitEditorMenu.render(topInv, kit);
                        CC.message(player, "&cCleared all potion effects for kit &f" + kit.getName() + "&c.");
                    } else {
                        kit.setPotionEffects(new HashSet<>(player.getActivePotionEffects()));
                        playSound(player, "DRINK");
                        KitEditorMenu.render(topInv, kit);
                        CC.message(player, "&aCopied " + kit.getPotionEffects().size() + " active potion effect(s) to kit &f" + kit.getName() + "&a!");
                    }
                } else if (slot == KitEditorMenu.SLOT_IMPORT_ALL) {
                    kit.setArmor(player.getInventory().getArmorContents().clone());
                    kit.setContents(player.getInventory().getContents().clone());
                    kit.setPotionEffects(new HashSet<>(player.getActivePotionEffects()));
                    playSound(player, "LEVEL_UP");
                    KitEditorMenu.render(topInv, kit);
                    CC.message(player, "&aImported entire inventory loadout into kit &f" + kit.getName() + "&a!");
                } else if (slot == KitEditorMenu.SLOT_TEST) {
                    kit.apply(player);
                    playSound(player, "LEVEL_UP");
                    CC.message(player, "&aEquipped kit &f" + kit.getName() + " &afor testing!");
                } else if (slot == KitEditorMenu.SLOT_CLOSE) {
                    playSound(player, "CHEST_CLOSE");
                    player.closeInventory();
                } else if (slot == KitEditorMenu.SLOT_SAVE) {
                    try {
                        Kits.getInstance().getKitManager().saveKit(kit);
                        playSound(player, "LEVEL_UP");
                        CC.message(player, "&aSuccessfully saved kit &f" + kit.getName() + " &ato file!");
                    } catch (Exception e) {
                        playSound(player, "VILLAGER_NO");
                        CC.message(player, "&cFailed to save kit: &f" + e.getMessage());
                    }
                } else if (slot == KitEditorMenu.SLOT_PREVIEW) {
                    playSound(player, "CLICK");
                    new KitSelectorMenu(player).updateMenu();
                }
            }
            return;
        }

        if (topInv.getHolder() instanceof KitContentsHolder) {
            KitContentsHolder holder = (KitContentsHolder) topInv.getHolder();
            Kit kit = holder.getKit();
            int rawSlot = event.getRawSlot();

            if (rawSlot >= 0 && rawSlot < 36) {
                return;
            }

            if (rawSlot >= 36 && rawSlot < KitContentsMenu.SIZE) {
                event.setCancelled(true);

                if (rawSlot == KitContentsMenu.SLOT_BACK) {
                    playSound(player, "CLICK");
                    KitContentsMenu.saveContents(topInv, kit);
                    KitEditorMenu.open(player, kit);
                } else if (rawSlot == KitContentsMenu.SLOT_IMPORT) {
                    ItemStack[] playerItems = player.getInventory().getContents();
                    for (int i = 0; i < 36; i++) {
                        topInv.setItem(i, i < playerItems.length && playerItems[i] != null ? playerItems[i].clone() : null);
                    }
                    playSound(player, "ORB_PICKUP");
                    CC.message(player, "&aImported current inventory items into slots 0-35!");
                } else if (rawSlot == KitContentsMenu.SLOT_CLEAR) {
                    for (int i = 0; i < 36; i++) {
                        topInv.setItem(i, null);
                    }
                    playSound(player, "ITEM_BREAK");
                    CC.message(player, "&cCleared all 36 items!");
                } else if (rawSlot == KitContentsMenu.SLOT_SAVE) {
                    KitContentsMenu.saveContents(topInv, kit);
                    playSound(player, "LEVEL_UP");
                    CC.message(player, "&aSaved kit contents!");
                    KitEditorMenu.open(player, kit);
                }
            }
            return;
        }

        if (topInv.getHolder() instanceof KitArmorHolder) {
            KitArmorHolder holder = (KitArmorHolder) topInv.getHolder();
            Kit kit = holder.getKit();
            int rawSlot = event.getRawSlot();

            if (rawSlot < KitArmorMenu.SIZE) {
                event.setCancelled(true);

                if (KitArmorMenu.isArmorSlot(rawSlot)) {
                    ItemStack onCursor = event.getCursor();
                    ItemStack current = topInv.getItem(rawSlot);

                    if (onCursor != null && onCursor.getType() != Material.AIR) {
                        topInv.setItem(rawSlot, onCursor.clone());
                        event.getView().setCursor(null);
                        playSound(player, "ITEM_PICKUP");
                    } else if (current != null && !KitArmorMenu.isPlaceholder(current)) {
                        event.getView().setCursor(current.clone());
                        topInv.setItem(rawSlot, null);
                        playSound(player, "ITEM_PICKUP");
                    }
                } else if (rawSlot == KitArmorMenu.SLOT_BACK) {
                    playSound(player, "CLICK");
                    KitArmorMenu.saveArmor(topInv, kit);
                    KitEditorMenu.open(player, kit);
                } else if (rawSlot == KitArmorMenu.SLOT_IMPORT) {
                    ItemStack[] playerArmor = player.getInventory().getArmorContents();
                    topInv.setItem(KitArmorMenu.SLOT_BOOTS, playerArmor != null && playerArmor.length > 0 ? playerArmor[0] : null);
                    topInv.setItem(KitArmorMenu.SLOT_LEGGINGS, playerArmor != null && playerArmor.length > 1 ? playerArmor[1] : null);
                    topInv.setItem(KitArmorMenu.SLOT_CHESTPLATE, playerArmor != null && playerArmor.length > 2 ? playerArmor[2] : null);
                    topInv.setItem(KitArmorMenu.SLOT_HELMET, playerArmor != null && playerArmor.length > 3 ? playerArmor[3] : null);
                    playSound(player, "ORB_PICKUP");
                    CC.message(player, "&aImported currently equipped armor into kit!");
                } else if (rawSlot == KitArmorMenu.SLOT_CLEAR) {
                    topInv.setItem(KitArmorMenu.SLOT_BOOTS, null);
                    topInv.setItem(KitArmorMenu.SLOT_LEGGINGS, null);
                    topInv.setItem(KitArmorMenu.SLOT_CHESTPLATE, null);
                    topInv.setItem(KitArmorMenu.SLOT_HELMET, null);
                    playSound(player, "ITEM_BREAK");
                    CC.message(player, "&cCleared kit armor!");
                } else if (rawSlot == KitArmorMenu.SLOT_SAVE) {
                    KitArmorMenu.saveArmor(topInv, kit);
                    playSound(player, "LEVEL_UP");
                    CC.message(player, "&aSaved kit armor!");
                    KitEditorMenu.open(player, kit);
                }
            }
            return;
        }

        if (topInv.getHolder() instanceof KitListEditorHolder) {
            KitListEditorHolder holder = (KitListEditorHolder) topInv.getHolder();
            int rawSlot = event.getRawSlot();

            if (rawSlot < KitListEditorMenu.SIZE) {
                event.setCancelled(true);

                if (rawSlot == 31) {
                    playSound(player, "CLICK");
                    player.closeInventory();
                    return;
                }

                Kit kit = holder.getKitAt(rawSlot);
                if (kit != null) {
                    playSound(player, "CLICK");
                    KitEditorMenu.open(player, kit);
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null) return;

        if (topInv.getHolder() instanceof KitEditorHolder || topInv.getHolder() instanceof KitListEditorHolder) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topInv.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (topInv.getHolder() instanceof KitContentsHolder) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot >= 36 && rawSlot < KitContentsMenu.SIZE) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (topInv.getHolder() instanceof KitArmorHolder) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < KitArmorMenu.SIZE && !KitArmorMenu.isArmorSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null) return;

        if (inv.getHolder() instanceof KitContentsHolder) {
            KitContentsHolder holder = (KitContentsHolder) inv.getHolder();
            KitContentsMenu.saveContents(inv, holder.getKit());
        } else if (inv.getHolder() instanceof KitArmorHolder) {
            KitArmorHolder holder = (KitArmorHolder) inv.getHolder();
            KitArmorMenu.saveArmor(inv, holder.getKit());
        }
    }
}
