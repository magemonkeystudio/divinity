package su.nightexpress.quantumrpg.nbt;

import java.lang.reflect.Constructor;

public enum ObjectCreator {
  NMS_NBTTAGCOMPOUND(ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz(), new Class[0]),
  NMS_BLOCKPOSITION(ClassWrapper.NMS_BLOCKPOSITION.getClazz(), new Class[] { int.class, int.class, int.class });
  
  private Constructor<?> construct;
  
  ObjectCreator(Class<?> clazz, Class[] args) {
    try {
      this.construct = clazz.getConstructor(args);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public Object getInstance(Object... args) {
    try {
      return this.construct.newInstance(args);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
}
