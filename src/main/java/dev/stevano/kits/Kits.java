package dev.stevano.kits;

import dev.stevano.kits.command.KitCommand;
import dev.stevano.kits.kit.KitManager;
import dev.stevano.kits.menu.MenuManager;
import dev.stevanoac.commando.Commando;
import io.github.nosequel.menu.MenuHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Kits extends JavaPlugin {
    private static Kits instance;
    private Commando commando;
    private KitManager kitManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
        new MenuHandler(this);
        this.commando = new Commando(this);
        load();
    }

    @Override
    public void onDisable() {
        if (this.kitManager != null) {
            this.kitManager.save();
        }
        instance = null;
    }

    private void load() {
        registerManagers();
        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new dev.stevano.kits.menu.editor.KitEditorListener(), this);
    }

    private void registerManagers() {
        this.kitManager = new KitManager(this);
        this.kitManager.load();
        this.menuManager = new MenuManager(this);
    }

    private void registerCommands() {
        this.commando.register(new KitCommand(this));
    }

    public static Kits getInstance() {
        return instance;
    }

    public Commando getCommando() {
        return commando;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}
