package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class QModuleDrop extends QModule {
  protected HashMap<String, ModuleItem> items;
  
  public QModuleDrop(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public ModuleItem getItemById(String id) {
    if (id.equalsIgnoreCase("random"))
      return (new ArrayList<>(this.items.values())).get(Utils.r.nextInt(this.items.size())); 
    return this.items.get(id.toLowerCase());
  }
  
  public <T extends ModuleItem> T getItemById(String id, Class<T> c) {
    if (!this.items.containsKey(id.toLowerCase()))
      return null; 
    if (id.equalsIgnoreCase("random"))
      return c.cast((new ArrayList(this.items.values())).get(Utils.r.nextInt(this.items.size()))); 
    return c.cast(this.items.get(id.toLowerCase()));
  }
  
  public Collection<ModuleItem> getItems() {
    return this.items.values();
  }
  
  public <T extends ModuleItem> Collection<T> getItems(Class<T> ic) {
    Collection<T> list = new ArrayList<>();
    list.add(ic.cast(this.items.values()));
    return list;
  }
  
  public List<String> getItemIds() {
    List<String> list = new ArrayList<>();
    for (ModuleItem g : getItems())
      list.add(g.getId()); 
    list.add("random");
    return list;
  }
  
  public boolean isItemOfThisModule(ItemStack item) {
    EModule e = ItemAPI.getItemModule(item);
    return (e != null && e == type());
  }
  
  public String getItemId(ItemStack item) {
    if (item == null)
      return "null"; 
    NBTItem nbt = new NBTItem(item);
    if (nbt.hasKey("E_ITEM_ID").booleanValue())
      return nbt.getString("E_ITEM_ID"); 
    return "null";
  }
  
  public void enable() {
    this.items = new HashMap<>();
    super.enable();
  }
  
  public void unload() {
    super.unload();
    this.items = null;
  }
}
