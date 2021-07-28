package su.nightexpress.quantumrpg.nbt;

import org.bukkit.entity.Entity;

public class NBTEntity extends NBTCompound {
  private final Entity ent;
  
  public NBTEntity(Entity entity) {
    super(null, null);
    this.ent = entity;
  }
  
  protected Object getCompound() {
    return NBTReflectionUtil.getEntityNBTTagCompound(NBTReflectionUtil.getNMSEntity(this.ent));
  }
  
  protected void setCompound(Object compound) {
    NBTReflectionUtil.setEntityNBTTag(compound, NBTReflectionUtil.getNMSEntity(this.ent));
  }
}
