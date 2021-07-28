package su.nightexpress.quantumrpg.types;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSubType {
  private String id;
  
  private String name;
  
  private List<String> mats;
  
  public ItemSubType(String id, String name, List<String> mats) {
    this.id = id.toLowerCase();
    this.name = name;
    validateMats(mats);
  }
  
  public String getId() {
    return this.id;
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
  
  private void validateMats(List<String> list) {
    for (int i = 0; i < list.size(); i++) {
      String m = ((String)list.get(i)).toUpperCase();
      list.set(i, m);
    } 
    this.mats = list;
  }
}
