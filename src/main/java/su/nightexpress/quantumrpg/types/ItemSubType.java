package su.nightexpress.quantumrpg.types;

import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ItemSubType {

    private String      id;
    private String      name;
    private Set<String> mats;

    public ItemSubType(@NotNull String id, @NotNull String name, @NotNull Set<String> mats) {
        this.id = id.toLowerCase();
        this.setName(name);

        this.mats = new HashSet<>(mats);
        this.mats.forEach(mat -> mat.toUpperCase());
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        this.name = StringUT.color(name);
    }

    @NotNull
    public Set<String> getMaterials() {
        return this.mats;
    }

    public boolean isItemOfThis(@NotNull ItemStack item) {
        return this.isItemOfThis(item.getType());
    }

    public boolean isItemOfThis(@NotNull Material mat) {
        return this.isItemOfThis(mat.name());
    }

    public boolean isItemOfThis(@NotNull String mat) {
        return this.mats.contains(mat.toUpperCase());
    }
}
