package dev.stevano.kits.menu.editor;

import dev.stevano.kits.kit.Kit;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

@Getter
public class KitListEditorHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Kit> kitSlotMap = new HashMap<>();

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void mapKit(int slot, Kit kit) {
        this.kitSlotMap.put(slot, kit);
    }

    public Kit getKitAt(int slot) {
        return this.kitSlotMap.get(slot);
    }
}
