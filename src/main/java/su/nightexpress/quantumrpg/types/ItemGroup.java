package su.nightexpress.quantumrpg.types;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mc.promcteam.engine.utils.StringUT;

public enum ItemGroup {

	WEAPON("Weapon"),
	ARMOR("Armor"),
	TOOL("Tool"),
	;
	
	private String name;
	private Set<String> mats;
	
	private ItemGroup(@NotNull String name) {
		this.setName(name);
		this.mats = new HashSet<>();
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
	
	public void setMaterials(@NotNull Set<String> mats) {
		this.mats.clear();
		this.mats.addAll(mats);
		this.mats.forEach(mat -> mat.toUpperCase());
	}
	
	public boolean isItemOfThis(@NotNull ItemStack item) {
		return this.isItemOfThis(item.getType());
	}
	
	public boolean isItemOfThis(@NotNull Material mat) {
		return this.isItemOfThis(mat.name());
	}
	
	public boolean isItemOfThis(@NotNull String mat) {
		String n = mat.toUpperCase();
		return this.mats.contains(n);
	}
	
	@Nullable
	public static ItemGroup getItemGroup(@NotNull ItemStack item) {
		return getItemGroup(item.getType());
	}
	
	@Nullable
	public static ItemGroup getItemGroup(@NotNull Material material) {
		return getItemGroup(material.name());
	}
	
	@Nullable
	public static ItemGroup getItemGroup(@NotNull String mat) {
		for (ItemGroup ig : ItemGroup.values()) {
			if (ig.isItemOfThis(mat)) {
				return ig;
			}
		}
		return null;
	}
}
