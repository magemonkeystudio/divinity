package su.nightexpress.quantumrpg.nbt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NBTFile extends NBTCompound {
  private final File file;
  
  private Object nbt;
  
  public NBTFile(File file) throws IOException {
    super(null, null);
    this.file = file;
    if (file.exists()) {
      FileInputStream inputsteam = new FileInputStream(file);
      this.nbt = NBTReflectionUtil.readNBTFile(inputsteam);
    } else {
      this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
      save();
    } 
  }
  
  public void save() throws IOException {
    if (!this.file.exists()) {
      this.file.getParentFile().mkdirs();
      this.file.createNewFile();
    } 
    FileOutputStream outStream = new FileOutputStream(this.file);
    NBTReflectionUtil.saveNBTFile(this.nbt, outStream);
  }
  
  public File getFile() {
    return this.file;
  }
  
  protected Object getCompound() {
    return this.nbt;
  }
  
  protected void setCompound(Object compound) {
    this.nbt = compound;
  }
}
