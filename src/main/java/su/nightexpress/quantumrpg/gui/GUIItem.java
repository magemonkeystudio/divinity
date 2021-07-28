package su.nightexpress.quantumrpg.gui;

import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.nbt.NBTItem;

public class GUIItem {
  private String id;
  
  private ContentType type;
  
  private ItemStack item;
  
  private int[] slot;
  
  public GUIItem(String id, ContentType type, ItemStack item, int[] slot) {
    setId(id);
    setType(type);
    setItem(item);
    setSlots(slot);
  }
  
  public GUIItem(GUIItem i2) {
    setId(i2.getId());
    setType(i2.getType());
    setItem(i2.getItem());
    setSlots(i2.getSlots());
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public ContentType getType() {
    return this.type;
  }
  
  public void setType(ContentType type) {
    this.type = type;
  }
  
  public ItemStack getItem() {
    return new ItemStack(this.item);
  }
  
  public void setItem(ItemStack item) {
    if (this.type == ContentType.NONE) {
      this.item = new ItemStack(item);
      return;
    } 
    NBTItem nbt = new NBTItem(item);
    nbt.setString(this.type.name(), this.type.name());
    this.item = new ItemStack(nbt.getItem());
  }
  
  public int[] getSlots() {
    return this.slot;
  }
  
  public void setSlots(int[] slot) {
    this.slot = slot;
  }
}
