package dev.stevano.kits.menu;

import dev.stevano.kits.Kits;
import dev.stevano.kits.util.CC;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

@Getter
public class MenuManager {

    private final Kits plugin;
    private FileConfiguration menusConfig;
    private File menusFile;

    public MenuManager(Kits plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdirs();
        }

        this.menusFile = new File(this.plugin.getDataFolder(), "menus.yml");
        if (!this.menusFile.exists()) {
            this.plugin.saveResource("menus.yml", false);
        }

        this.menusConfig = YamlConfiguration.loadConfiguration(this.menusFile);
        CC.log("&b[Kits] &aSuccessfully loaded menus.yml configuration.");
    }

    public void reload() {
        load();
    }

    public void openKitSelector(Player player) {
        new KitSelectorMenu(player, this).updateMenu();
    }

    public FileConfiguration getMenusConfig() {
        return this.menusConfig;
    }
}
