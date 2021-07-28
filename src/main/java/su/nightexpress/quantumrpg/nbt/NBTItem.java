package su.nightexpress.quantumrpg.nbt;

import org.bukkit.inventory.ItemStack;

public class NBTItem extends NBTCompound {
  private ItemStack bukkitItem;
  
  public NBTItem(ItemStack item) {
    super(null, null);
    if (item == null)
      throw new NullPointerException("ItemStack can't be null!"); 
    this.bukkitItem = item.clone();
  }
  
  protected Object getCompound() {
    return NBTReflectionUtil.getItemRootNBTTagCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, new Object[] { this.bukkitItem }));
  }
  
  protected void setCompound(Object compound) {
    Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, new Object[] { this.bukkitItem });
    ReflectionMethod.ITEMSTACK_SET_TAG.run(stack, new Object[] { compound });
    this.bukkitItem = (ItemStack)ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, new Object[] { stack });
  }
  
  public ItemStack getItem() {
    return this.bukkitItem;
  }
  
  protected void setItem(ItemStack item) {
    this.bukkitItem = item;
  }
  
  public boolean hasNBTData() {
    return (getCompound() != null);
  }
  
  public static NBTContainer convertItemtoNBT(ItemStack item) {
    return NBTReflectionUtil.convertNMSItemtoNBTCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, new Object[] { item }));
  }
  
  public static ItemStack convertNBTtoItem(NBTCompound comp) {
    return (ItemStack)ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, new Object[] { NBTReflectionUtil.convertNBTCompoundtoNMSItem(comp) });
  }
}
