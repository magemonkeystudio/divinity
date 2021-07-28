package su.nightexpress.quantumrpg.types;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ItemGroup {
  WEAPON("Weapon"),
  ARMOR("Armor"),
  TOOL("Tool");
  
  private String name;
  
  private List<String> mats;
  
  ItemGroup(String name) {
    this.name = name;
    this.mats = new ArrayList<>();
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = ChatColor.translateAlternateColorCodes('&', name);
  }
  
  public List<String> getMaterials() {
    return this.mats;
  }
  
  public void setMaterials(List<String> mats) {
    this.mats = mats;
  }
  
  public boolean isItemOfThis(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return false; 
    return isItemOfThis(item.getType());
  }
  
  public boolean isItemOfThis(Material mat) {
    return isItemOfThis(mat.name());
  }
  
  public boolean isItemOfThis(String mat) {
    String n = mat.toUpperCase();
    return this.mats.contains(n);
  }
  
  public static ItemGroup getItemGroup(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return null; 
    return getItemGroup(item.getType());
  }
  
  public static ItemGroup getItemGroup(Material m) {
    return getItemGroup(m.name());
  }
  
  public static ItemGroup getItemGroup(String mat) {
    byte b;
    int i;
    ItemGroup[] arrayOfItemGroup;
    for (i = (arrayOfItemGroup = values()).length, b = 0; b < i; ) {
      ItemGroup ig = arrayOfItemGroup[b];
      if (ig.isItemOfThis(mat))
        return ig; 
      b++;
    } 
    return null;
  }
}
