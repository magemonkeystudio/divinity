package su.nightexpress.quantumrpg.nbt;

import java.util.HashSet;
import java.util.Set;

public class NBTListCompound {
  private NBTList owner;
  
  private Object compound;
  
  protected NBTListCompound(NBTList parent, Object obj) {
    this.owner = parent;
    this.compound = obj;
  }
  
  public void setString(String key, String value) {
    if (value == null) {
      remove(key);
      return;
    } 
    try {
      this.compound.getClass().getMethod("setString", new Class[] { String.class, String.class }).invoke(this.compound, new Object[] { key, value });
      this.owner.save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public void setInteger(String key, int value) {
    try {
      this.compound.getClass().getMethod("setInt", new Class[] { String.class, int.class }).invoke(this.compound, new Object[] { key, Integer.valueOf(value) });
      this.owner.save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public int getInteger(String value) {
    try {
      return ((Integer)this.compound.getClass().getMethod("getInt", new Class[] { String.class }).invoke(this.compound, new Object[] { value })).intValue();
    } catch (Exception ex) {
      ex.printStackTrace();
      return 0;
    } 
  }
  
  public void setDouble(String key, double value) {
    try {
      this.compound.getClass().getMethod("setDouble", new Class[] { String.class, double.class }).invoke(this.compound, new Object[] { key, Double.valueOf(value) });
      this.owner.save();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public double getDouble(String key) {
    try {
      return ((Double)this.compound.getClass().getMethod("getDouble", new Class[] { String.class }).invoke(this.compound, new Object[] { key })).doubleValue();
    } catch (Exception ex) {
      ex.printStackTrace();
      return 0.0D;
    } 
  }
  
  public String getString(String key) {
    try {
      return (String)this.compound.getClass().getMethod("getString", new Class[] { String.class }).invoke(this.compound, new Object[] { key });
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    } 
  }
  
  public boolean hasKey(String key) {
    try {
      return ((Boolean)this.compound.getClass().getMethod("hasKey", new Class[] { String.class }).invoke(this.compound, new Object[] { key })).booleanValue();
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    } 
  }
  
  public Set<String> getKeys() {
    try {
      return (Set<String>)ReflectionMethod.LISTCOMPOUND_GET_KEYS.run(this.compound, new Object[0]);
    } catch (Exception ex) {
      ex.printStackTrace();
      return new HashSet<>();
    } 
  }
  
  public void remove(String key) {
    try {
      this.compound.getClass().getMethod("remove", new Class[] { String.class }).invoke(this.compound, new Object[] { key });
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
}
