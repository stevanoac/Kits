package dev.stevano.kits.kit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Set;

@Getter @Setter
public class Kit {
    private String name;
    private String displayName;
    private String permission;
    private ItemStack icon;
    private ItemStack[] armor;
    private ItemStack[] contents;
    private Set<PotionEffect> potionEffects;
    private String description;

    public Kit(String name) {
        this.name = name;
        this.displayName = name.replace("_", " ");
        this.icon = new ItemStack(Material.WOOD_SWORD, 1);
    }

    public void apply(Player player) {
        if (player == null) return;

        player.getInventory().clear();

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        if (this.armor != null) {
            ItemStack[] clonedArmor = new ItemStack[this.armor.length];
            for (int i = 0; i < this.armor.length; i++) {
                clonedArmor[i] = this.armor[i] != null ? this.armor[i].clone() : null;
            }
            player.getInventory().setArmorContents(clonedArmor);
        } else {
            player.getInventory().setArmorContents(new ItemStack[4]);
        }

        if (this.contents != null) {
            ItemStack[] clonedContents = new ItemStack[this.contents.length];
            for (int i = 0; i < this.contents.length; i++) {
                clonedContents[i] = this.contents[i] != null ? this.contents[i].clone() : null;
            }
            player.getInventory().setContents(clonedContents);
        }

        if (this.potionEffects != null) {
            for (PotionEffect effect : this.potionEffects) {
                player.addPotionEffect(effect, true);
            }
        }

        player.updateInventory();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public Set<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(Set<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
