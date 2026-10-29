package dev.stevano.kits.kit;

import dev.stevano.kits.Kits;
import dev.stevano.kits.util.CC;
import dev.stevano.kits.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class KitManager {

    private final Kits plugin;
    private final File kitsDirectory;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    public KitManager() {
        this(Kits.getInstance());
    }

    public KitManager(Kits plugin) {
        this.plugin = plugin;
        this.kitsDirectory = plugin != null ? new File(plugin.getDataFolder(), "kits") : new File("kits");
    }

    public boolean ensureDirectoryExists() {
        if (!this.kitsDirectory.exists()) {
            CC.log("&e[Kits] &eThe kits directory does not exist! Creating directory: &f" + this.kitsDirectory.getPath());
            boolean created = this.kitsDirectory.mkdirs();
            if (created) {
                CC.log("&a[Kits] &aSuccessfully created kits directory at: &f" + this.kitsDirectory.getPath());
                saveDefaultKit();
                return true;
            } else {
                CC.log("&c[Kits] &cFailed to create kits directory at: &f" + this.kitsDirectory.getPath());
                return false;
            }
        }
        return true;
    }

    public void saveDefaultKit() {
        if (this.plugin != null) {
            File archerFile = new File(this.kitsDirectory, "archer.yml");
            if (!archerFile.exists()) {
                try {
                    this.plugin.saveResource("kits/archer.yml", false);
                    CC.log("&a[Kits] &aExtracted default kit file: &farcher.yml");
                } catch (Exception ignored) {}
            }
        }
    }

    public void load() {
        long startTime = System.currentTimeMillis();
        this.kits.clear();

        CC.log(CC.CHAT_BAR);
        CC.log("&b[Kits] &7Starting kits loading process...");

        if (!ensureDirectoryExists()) {
            CC.log("&c[Kits] &cAborting kit loading because the kits directory could not be created.");
            CC.log(CC.CHAT_BAR);
            return;
        }

        File[] files = this.kitsDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            saveDefaultKit();
            files = this.kitsDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        }

        if (files == null || files.length == 0) {
            CC.log("&e[Kits] &7No kit configuration files (.yml) found in &f" + this.kitsDirectory.getName() + "&7.");
            CC.log("&b[Kits] &aCompleted kit loading &7(0 kits loaded) in &b" + (System.currentTimeMillis() - startTime) + "ms&7.");
            CC.log(CC.CHAT_BAR);
            return;
        }

        CC.log("&b[Kits] &7Found &b" + files.length + " &7kit file(s) in &f" + this.kitsDirectory.getName() + "&7. Loading...");

        int loadedCount = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                Kit kit = loadKit(file);
                if (kit != null) {
                    this.kits.put(kit.getName().toLowerCase(), kit);
                    loadedCount++;

                    int itemCount = countItems(kit.getContents());
                    int armorCount = countItems(kit.getArmor());
                    int effectCount = kit.getPotionEffects() != null ? kit.getPotionEffects().size() : 0;

                    CC.log("&b[Kits] &7[" + (i + 1) + "/" + files.length + "] &aLoaded kit &f" + kit.getName()
                            + " &7(&f" + CC.translate(kit.getDisplayName()) + "&7) &8[&eItems: " + itemCount
                            + " &7| &eArmor: " + armorCount
                            + " &7| &eEffects: " + effectCount + "&8]");
                }
            } catch (Exception e) {
                CC.log("&c[Kits] &cFailed to load kit from file &f" + file.getName() + "&c: " + e.getMessage());
                e.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        CC.log("&a[Kits] &aSuccessfully loaded &b" + loadedCount + "&a/&b" + files.length + " &akit(s) in &b" + duration + "ms&a.");
        CC.log(CC.CHAT_BAR);
    }

    public Kit loadKit(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String fallbackName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        String name = config.getString("name", fallbackName);
        Kit kit = new Kit(name);

        kit.setDisplayName(config.getString("displayName", name.replace("_", " ")));
        kit.setPermission(config.getString("permission", "kit." + name.toLowerCase()));
        kit.setDescription(config.getString("description", ""));

        if (config.contains("icon")) {
            kit.setIcon(ItemBuilder.deserialize(config.get("icon")));
        }

        ItemStack[] armor = new ItemStack[4];
        if (config.contains("armor")) {
            if (config.isConfigurationSection("armor")) {
                ConfigurationSection sec = config.getConfigurationSection("armor");
                if (sec.contains("boots")) armor[0] = ItemBuilder.deserialize(sec.get("boots"));
                if (sec.contains("leggings")) armor[1] = ItemBuilder.deserialize(sec.get("leggings"));
                if (sec.contains("chestplate")) armor[2] = ItemBuilder.deserialize(sec.get("chestplate"));
                if (sec.contains("helmet")) armor[3] = ItemBuilder.deserialize(sec.get("helmet"));

                for (int i = 0; i < 4; i++) {
                    if (armor[i] == null && sec.contains(String.valueOf(i))) {
                        armor[i] = ItemBuilder.deserialize(sec.get(String.valueOf(i)));
                    }
                }
            } else if (config.isList("armor")) {
                List<?> list = config.getList("armor");
                for (int i = 0; i < list.size() && i < 4; i++) {
                    armor[i] = ItemBuilder.deserialize(list.get(i));
                }
            } else if (config.isString("armor")) {
                ItemStack[] base64Items = ItemBuilder.fromBase64(config.getString("armor"));
                if (base64Items != null) {
                    System.arraycopy(base64Items, 0, armor, 0, Math.min(base64Items.length, 4));
                }
            }
        }
        kit.setArmor(armor);

        ItemStack[] contents = new ItemStack[36];
        if (config.contains("contents")) {
            if (config.isConfigurationSection("contents")) {
                ConfigurationSection sec = config.getConfigurationSection("contents");
                for (String key : sec.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        if (slot >= 0 && slot < contents.length) {
                            contents[slot] = ItemBuilder.deserialize(sec.get(key));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if (config.isList("contents")) {
                List<?> list = config.getList("contents");
                for (int i = 0; i < list.size() && i < contents.length; i++) {
                    contents[i] = ItemBuilder.deserialize(list.get(i));
                }
            } else if (config.isString("contents")) {
                ItemStack[] base64Items = ItemBuilder.fromBase64(config.getString("contents"));
                if (base64Items != null) {
                    System.arraycopy(base64Items, 0, contents, 0, Math.min(base64Items.length, 36));
                }
            }
        }
        kit.setContents(contents);

        if (config.contains("potionEffects")) {
            Set<PotionEffect> effects = new HashSet<>();
            List<?> list = config.getList("potionEffects");
            if (list != null) {
                for (Object item : list) {
                    PotionEffect effect = ItemBuilder.parsePotionEffect(item);
                    if (effect != null) {
                        effects.add(effect);
                    }
                }
            }
            kit.setPotionEffects(effects);
        }

        return kit;
    }

    public void save() {
        long startTime = System.currentTimeMillis();

        CC.log(CC.CHAT_BAR);
        CC.log("&b[Kits] &7Starting kits saving process...");

        if (!ensureDirectoryExists()) {
            CC.log("&c[Kits] &cAborting kit saving because the kits directory could not be created.");
            CC.log(CC.CHAT_BAR);
            return;
        }

        if (this.kits.isEmpty()) {
            CC.log("&e[Kits] &7No kits in memory to save.");
            CC.log("&b[Kits] &aCompleted kit saving &7(0 kits saved) in &b" + (System.currentTimeMillis() - startTime) + "ms&7.");
            CC.log(CC.CHAT_BAR);
            return;
        }

        CC.log("&b[Kits] &7Saving &b" + this.kits.size() + " &7kit(s) to &f" + this.kitsDirectory.getName() + "&7...");

        int savedCount = 0;
        int total = this.kits.size();
        int index = 0;

        for (Kit kit : this.kits.values()) {
            index++;
            try {
                saveKit(kit);
                savedCount++;
                CC.log("&b[Kits] &7[" + index + "/" + total + "] &aSaved kit &f" + kit.getName()
                        + " &7to &f" + kit.getName().toLowerCase() + ".yml");
            } catch (Exception e) {
                CC.log("&c[Kits] &cFailed to save kit &f" + kit.getName() + "&c: " + e.getMessage());
                e.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        CC.log("&a[Kits] &aSuccessfully saved &b" + savedCount + "&a/&b" + total + " &akit(s) in &b" + duration + "ms&a.");
        CC.log(CC.CHAT_BAR);
    }

    public void saveKit(Kit kit) throws IOException {
        ensureDirectoryExists();

        File file = new File(this.kitsDirectory, kit.getName().toLowerCase() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", kit.getName());
        config.set("displayName", kit.getDisplayName() != null ? CC.deTranslate(kit.getDisplayName()) : kit.getName());
        config.set("permission", kit.getPermission());
        config.set("description", kit.getDescription() != null ? CC.deTranslate(kit.getDescription()) : "");

        if (kit.getIcon() != null && kit.getIcon().getType() != Material.AIR) {
            config.set("icon", ItemBuilder.serialize(kit.getIcon()));
        }

        ItemStack[] armor = kit.getArmor();
        if (armor != null) {
            if (armor.length > 3 && armor[3] != null && armor[3].getType() != Material.AIR) {
                config.set("armor.helmet", ItemBuilder.serialize(armor[3]));
            }
            if (armor.length > 2 && armor[2] != null && armor[2].getType() != Material.AIR) {
                config.set("armor.chestplate", ItemBuilder.serialize(armor[2]));
            }
            if (armor.length > 1 && armor[1] != null && armor[1].getType() != Material.AIR) {
                config.set("armor.leggings", ItemBuilder.serialize(armor[1]));
            }
            if (armor.length > 0 && armor[0] != null && armor[0].getType() != Material.AIR) {
                config.set("armor.boots", ItemBuilder.serialize(armor[0]));
            }
        }

        ItemStack[] contents = kit.getContents();
        if (contents != null) {
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (item != null && item.getType() != Material.AIR) {
                    config.set("contents." + slot, ItemBuilder.serialize(item));
                }
            }
        }

        if (kit.getPotionEffects() != null && !kit.getPotionEffects().isEmpty()) {
            List<Map<String, Object>> effectsList = new ArrayList<>();
            for (PotionEffect effect : kit.getPotionEffects()) {
                if (effect != null) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("type", effect.getType().getName());
                    map.put("duration", effect.getDuration());
                    map.put("amplifier", effect.getAmplifier());
                    effectsList.add(map);
                }
            }
            config.set("potionEffects", effectsList);
        }

        config.save(file);
    }

    public void save(Kit kit, boolean log) {
        try {
            saveKit(kit);
            if (log) {
                CC.log("&b[Kits] &aSuccessfully saved kit &f" + kit.getName() + " &7to &f" + kit.getName().toLowerCase() + ".yml");
            }
        } catch (Exception e) {
            CC.log("&c[Kits] &cFailed to save kit &f" + kit.getName() + "&c: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save(Kit kit) {
        save(kit, true);
    }

    public boolean applyKit(Player player, Kit kit) {
        if (player == null || kit == null) return false;
        kit.apply(player);
        return true;
    }

    public boolean applyKit(Player player, String kitName) {
        Kit kit = getKit(kitName);
        if (kit == null) return false;
        return applyKit(player, kit);
    }

    public Kit getKit(String name) {
        if (name == null) return null;
        return this.kits.get(name.toLowerCase());
    }

    public void registerKit(Kit kit) {
        if (kit != null && kit.getName() != null) {
            this.kits.put(kit.getName().toLowerCase(), kit);
        }
    }

    public void unregisterKit(String name) {
        if (name != null) {
            this.kits.remove(name.toLowerCase());
        }
    }

    public void unregisterKit(Kit kit) {
        if (kit != null) {
            unregisterKit(kit.getName());
        }
    }

    public boolean deleteKit(String name) {
        Kit kit = getKit(name);
        if (kit == null) return false;
        unregisterKit(name);
        File file = new File(this.kitsDirectory, name.toLowerCase() + ".yml");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                CC.log("&b[Kits] &eDeleted kit file &f" + file.getName());
            }
            return deleted;
        }
        return true;
    }

    public Collection<Kit> getKits() {
        return Collections.unmodifiableCollection(this.kits.values());
    }

    private int countItems(ItemStack[] items) {
        if (items == null) return 0;
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                count++;
            }
        }
        return count;
    }
}
