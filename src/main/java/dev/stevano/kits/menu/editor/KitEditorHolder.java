package dev.stevano.kits.menu.editor;

import dev.stevano.kits.kit.Kit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitEditorHolder implements InventoryHolder {

    private final Kit kit;
    private Inventory inventory;

    public KitEditorHolder(Kit kit) {
        this.kit = kit;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Kit getKit() {
        return this.kit;
    }
}
