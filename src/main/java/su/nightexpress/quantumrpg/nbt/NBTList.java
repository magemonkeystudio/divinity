package su.nightexpress.quantumrpg.nbt;

import java.lang.reflect.Method;
import su.nightexpress.quantumrpg.nbt.utils.MethodNames;

public class NBTList {
  private String listName;
  
  private NBTCompound parent;
  
  private NBTType type;
  
  private Object listObject;
  
  protected NBTList(NBTCompound owner, String name, NBTType type, Object list) {
    this.parent = owner;
    this.listName = name;
    this.type = type;
    this.listObject = list;
    if (type != NBTType.NBTTagString && type != NBTType.NBTTagCompound)
      System.err.println("List types != String/Compound are currently not implemented!"); 
  }
  
  protected void save() {
    this.parent.set(this.listName, this.listObject);
  }
  
  public NBTListCompound addCompound() {
    if (this.type != NBTType.NBTTagCompound) {
      (new Throwable("Using Compound method on a non Compound list!")).printStackTrace();
      return null;
    } 
    try {
      Method method = this.listObject.getClass().getMethod("add", new Class[] { ClassWrapper.NMS_NBTBASE.getClazz() });
      Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
      method.invoke(this.listObject, new Object[] { compound });
      return new NBTListCompound(this, compound);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public NBTListCompound getCompound(int id) {
    if (this.type != NBTType.NBTTagCompound) {
      (new Throwable("Using Compound method on a non Compound list!")).printStackTrace();
      return null;
    } 
    try {
      Method method = this.listObject.getClass().getMethod("get", new Class[] { int.class });
      Object compound = method.invoke(this.listObject, new Object[] { Integer.valueOf(id) });
      return new NBTListCompound(this, compound);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public String getString(int i) {
    if (this.type != NBTType.NBTTagString) {
      (new Throwable("Using String method on a non String list!")).printStackTrace();
      return null;
    } 
    try {
      Method method = this.listObject.getClass().getMethod("getString", new Class[] { int.class });
      return (String)method.invoke(this.listObject, new Object[] { Integer.valueOf(i) });
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public void addString(String s) {
    if (this.type != NBTType.NBTTagString) {
      (new Throwable("Using String method on a non String list!")).printStackTrace();
      return;
    } 
    try {
      Method method = this.listObject.getClass().getMethod("add", new Class[] { ClassWrapper.NMS_NBTBASE.getClazz() });
      method.invoke(this.listObject, new Object[] { ClassWrapper.NMS_NBTTAGSTRING.getClazz().getConstructor(new Class[] { String.class }).newInstance(new Object[] { s }) });
      save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public void setString(int i, String s) {
    if (this.type != NBTType.NBTTagString) {
      (new Throwable("Using String method on a non String list!")).printStackTrace();
      return;
    } 
    try {
      Method method = this.listObject.getClass().getMethod("a", new Class[] { int.class, ClassWrapper.NMS_NBTBASE.getClazz() });
      method.invoke(this.listObject, new Object[] { Integer.valueOf(i), ClassWrapper.NMS_NBTTAGSTRING.getClazz().getConstructor(new Class[] { String.class }).newInstance(new Object[] { s }) });
      save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public void remove(int i) {
    try {
      Method method = this.listObject.getClass().getMethod(MethodNames.getRemoveMethodName(), new Class[] { int.class });
      method.invoke(this.listObject, new Object[] { Integer.valueOf(i) });
      save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public int size() {
    try {
      Method method = this.listObject.getClass().getMethod("size", new Class[0]);
      return ((Integer)method.invoke(this.listObject, new Object[0])).intValue();
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    } 
  }
  
  public NBTType getType() {
    return this.type;
  }
}
