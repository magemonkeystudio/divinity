package su.nightexpress.quantumrpg.nbt;

import org.bukkit.block.BlockState;

public class NBTTileEntity extends NBTCompound {
  private final BlockState tile;
  
  public NBTTileEntity(BlockState tile) {
    super(null, null);
    this.tile = tile;
  }
  
  protected Object getCompound() {
    return NBTReflectionUtil.getTileEntityNBTTagCompound(this.tile);
  }
  
  protected void setCompound(Object compound) {
    NBTReflectionUtil.setTileEntityNBTTagCompound(this.tile, compound);
  }
}
