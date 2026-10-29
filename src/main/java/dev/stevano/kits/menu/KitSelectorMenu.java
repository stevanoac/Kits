package dev.stevano.kits.menu;

import dev.stevano.kits.Kits;
import dev.stevano.kits.kit.Kit;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.util.CC;
import dev.stevano.kits.util.ItemBuilder;
import io.github.nosequel.menu.Menu;
import io.github.nosequel.menu.buttons.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitSelectorMenu extends Menu {

    private final MenuManager menuManager;

    public KitSelectorMenu(Player player, MenuManager menuManager) {
        super(player,
                CC.translate(menuManager.getMenusConfig().getString("kit-selector.title", "&8Kit Selector")),
                getValidSize(menuManager.getMenusConfig().getInt("kit-selector.size", 36)),
                null);
        this.menuManager = menuManager;
    }

    public KitSelectorMenu(Player player) {
        this(player, Kits.getInstance().getMenuManager());
    }

    private static int getValidSize(int rawSize) {
        if (rawSize <= 9) return 9;
        if (rawSize <= 18) return 18;
        if (rawSize <= 27) return 27;
        if (rawSize <= 36) return 36;
        if (rawSize <= 45) return 45;
        return 54;
    }

    @Override
    public void updateMenu() {
        this.updateMenu(this.buttons);
    }

    @Override
    public void updateMenu(Button[] buttons) {
        this.tick();

        int size = getSize();
        org.bukkit.inventory.Inventory inv = getInventory();
        if (inv == null || inv.getSize() != size) {
            inv = org.bukkit.Bukkit.createInventory(this.getHolder(), size, getTitle());
            setInventory(inv);
        } else {
            inv.clear();
        }

        for (int i = 0; i < size && i < this.buttons.length; i++) {
            if (this.buttons[i] != null) {
                inv.setItem(i, this.buttons[i].toItemStack());
            } else {
                inv.setItem(i, null);
            }
        }

        getPlayer().openInventory(inv);
        registerMenu();
    }

    @Override
    public void tick() {
        FileConfiguration config = this.menuManager.getMenusConfig();
        int size = getSize();
        if (this.buttons == null || this.buttons.length != size) {
            this.buttons = new Button[size];
        } else {
            Arrays.fill(this.buttons, null);
        }

        Player player = getPlayer();
        KitManager kitManager = Kits.getInstance().getKitManager();

        boolean borderEnabled = config.getBoolean("kit-selector.border.enabled", true);
        if (borderEnabled && size >= 27) {
            ItemStack borderItem = createConfigItem(config.getConfigurationSection("kit-selector.border"), Material.STAINED_GLASS_PANE, (short) 15, " ");
            Button borderButton = new Button(borderItem);

            int rows = size / 9;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < 9; c++) {
                    if (r == 0 || r == rows - 1 || c == 0 || c == 8) {
                        int slot = r * 9 + c;
                        if (slot < size) {
                            this.buttons[slot] = borderButton;
                        }
                    }
                }
            }
        }

        ConfigurationSection itemsSection = config.getConfigurationSection("kit-selector.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec != null) {
                    int slot = itemSec.getInt("slot", -1);
                    if (slot >= 0 && slot < size) {
                        ItemStack item = createConfigItem(itemSec, Material.STONE, (short) 0, key);
                        Button btn = new Button(item);
                        if (key.equalsIgnoreCase("close")) {
                            btn.setClickAction(event -> event.getWhoClicked().closeInventory());
                        }
                        this.buttons[slot] = btn;
                    }
                }
            }
        }

        ConfigurationSection kitSlotsSec = config.getConfigurationSection("kit-selector.kit-slots");
        Map<String, Integer> customSlots = new HashMap<>();
        if (kitSlotsSec != null) {
            for (String kitKey : kitSlotsSec.getKeys(false)) {
                customSlots.put(kitKey.toLowerCase(), kitSlotsSec.getInt(kitKey));
            }
        }

        List<Kit> kits = new ArrayList<>(kitManager.getKits());
        Set<Integer> occupiedSlots = new HashSet<>();
        for (int i = 0; i < size; i++) {
            if (this.buttons[i] != null) {
                occupiedSlots.add(i);
            }
        }

        Iterator<Kit> iterator = kits.iterator();
        while (iterator.hasNext()) {
            Kit kit = iterator.next();
            String nameLower = kit.getName().toLowerCase();
            if (customSlots.containsKey(nameLower)) {
                int slot = customSlots.get(nameLower);
                if (slot >= 0 && slot < size) {
                    this.buttons[slot] = createKitButton(kit, player, config);
                    occupiedSlots.add(slot);
                    iterator.remove();
                }
            }
        }

        int nextSlot = 0;
        for (Kit kit : kits) {
            while (nextSlot < size && occupiedSlots.contains(nextSlot)) {
                nextSlot++;
            }
            if (nextSlot < size) {
                this.buttons[nextSlot] = createKitButton(kit, player, config);
                occupiedSlots.add(nextSlot);
                nextSlot++;
            }
        }

        boolean fillEmpty = config.getBoolean("kit-selector.fill-empty-slots", true);
        if (fillEmpty) {
            ItemStack fillerItem = createConfigItem(config.getConfigurationSection("kit-selector.filler"), Material.STAINED_GLASS_PANE, (short) 7, " ");
            Button fillerButton = new Button(fillerItem);
            for (int i = 0; i < size; i++) {
                if (this.buttons[i] == null) {
                    this.buttons[i] = fillerButton;
                }
            }
        }
    }

    private Button createKitButton(Kit kit, Player player, FileConfiguration config) {
        boolean hasPerm = kit.getPermission() == null || kit.getPermission().trim().isEmpty() || player.hasPermission(kit.getPermission());

        String displayPath = hasPerm ? "kit-selector.kit-display.available" : "kit-selector.kit-display.locked";
        String nameFormat = config.getString(displayPath + ".name", (hasPerm ? "&a" : "&c") + "{displayName}");
        List<String> loreFormat = config.getStringList(displayPath + ".lore");

        int itemsCount = countItems(kit.getContents());
        int armorCount = countItems(kit.getArmor());
        int effectsCount = kit.getPotionEffects() != null ? kit.getPotionEffects().size() : 0;
        String desc = kit.getDescription() != null ? kit.getDescription() : "";

        String formattedName = CC.translate(nameFormat
                .replace("{name}", kit.getName())
                .replace("{displayName}", kit.getDisplayName())
                .replace("{description}", desc));

        List<String> formattedLore = new ArrayList<>();
        if (loreFormat != null && !loreFormat.isEmpty()) {
            for (String line : loreFormat) {
                formattedLore.add(CC.translate(line
                        .replace("{name}", kit.getName())
                        .replace("{displayName}", kit.getDisplayName())
                        .replace("{description}", desc)
                        .replace("{items_count}", String.valueOf(itemsCount))
                        .replace("{armor_count}", String.valueOf(armorCount))
                        .replace("{effects_count}", String.valueOf(effectsCount))
                        .replace("{permission}", kit.getPermission() != null ? kit.getPermission() : "None")));
            }
        }

        ItemStack icon = kit.getIcon() != null && kit.getIcon().getType() != Material.AIR ? kit.getIcon().clone() : new ItemStack(Material.DIAMOND_SWORD);
        Button button = new Button(icon);
        button.setDisplayName(formattedName);
        button.setLore(formattedLore.toArray(new String[0]));

        button.setClickAction(event -> {
            Player clicker = (Player) event.getWhoClicked();

            if (!hasPerm) {
                try {
                    clicker.playSound(clicker.getLocation(), Sound.valueOf("VILLAGER_NO"), 1.0F, 1.0F);
                } catch (Exception ignored) {}
                CC.message(clicker, "&cYou do not have permission to equip the &f" + CC.translate(kit.getDisplayName()) + " &ckit!");
                return;
            }

            clicker.closeInventory();
            kit.apply(clicker);

            try {
                clicker.playSound(clicker.getLocation(), Sound.valueOf("LEVEL_UP"), 1.0F, 1.0F);
            } catch (Exception ignored) {}

            CC.message(clicker, "&aYou have successfully equipped the &f" + CC.translate(kit.getDisplayName()) + " &akit!");
        });

        return button;
    }

    private ItemStack createConfigItem(ConfigurationSection sec, Material defaultMat, short defaultData, String defaultName) {
        Material mat = defaultMat;
        short data = defaultData;
        String name = defaultName;
        List<String> lore = new ArrayList<>();

        if (sec != null) {
            String matStr = sec.getString("material");
            if (matStr != null) {
                Material parsed = ItemBuilder.parseMaterial(matStr);
                if (parsed != null) mat = parsed;
            }
            data = (short) sec.getInt("data", defaultData);
            name = sec.getString("name", defaultName);
            lore = sec.getStringList("lore");
        }

        ItemBuilder builder = new ItemBuilder(mat, 1, data);
        if (name != null) builder.name(name);
        if (lore != null && !lore.isEmpty()) builder.lore(lore);

        return builder.build();
    }

    private int countItems(ItemStack[] items) {
        if (items == null) return 0;
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) count++;
        }
        return count;
    }
}
