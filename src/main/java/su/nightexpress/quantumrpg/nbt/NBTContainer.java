package su.nightexpress.quantumrpg.nbt;

public class NBTContainer extends NBTCompound {
  private Object nbt;
  
  public NBTContainer() {
    super(null, null);
    this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
  }
  
  protected NBTContainer(Object nbt) {
    super(null, null);
    this.nbt = nbt;
  }
  
  public NBTContainer(String nbtString) throws IllegalArgumentException {
    super(null, null);
    try {
      this.nbt = ReflectionMethod.PARSE_NBT.run(null, new Object[] { nbtString });
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new IllegalArgumentException("Malformed Json: " + ex.getMessage());
    } 
  }
  
  protected Object getCompound() {
    return this.nbt;
  }
  
  protected void setCompound(Object tag) {
    this.nbt = tag;
  }
}
