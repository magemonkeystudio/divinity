package su.nightexpress.quantumrpg.nbt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Stack;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.nbt.utils.GsonWrapper;
import su.nightexpress.quantumrpg.nbt.utils.MethodNames;
import su.nightexpress.quantumrpg.nbt.utils.MinecraftVersion;

public class NBTReflectionUtil {
  public static Object getNMSEntity(Entity entity) {
    Class<?> clazz = ClassWrapper.CRAFT_ENTITY.getClazz();
    try {
      Method method = clazz.getMethod("getHandle", new Class[0]);
      return method.invoke(clazz.cast(entity), new Object[0]);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object readNBTFile(FileInputStream stream) {
    Class<?> clazz = ClassWrapper.NMS_NBTCOMPRESSEDSTREAMTOOLS.getClazz();
    try {
      Method method = clazz.getMethod("a", new Class[] { InputStream.class });
      return method.invoke(clazz, new Object[] { stream });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object saveNBTFile(Object nbt, FileOutputStream stream) {
    Class<?> clazz = ClassWrapper.NMS_NBTCOMPRESSEDSTREAMTOOLS.getClazz();
    try {
      Method method = clazz.getMethod("a", new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz(), OutputStream.class });
      return method.invoke(clazz, new Object[] { nbt, stream });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object getItemRootNBTTagCompound(Object nmsitem) {
    Class<? extends Object> clazz = (Class)nmsitem.getClass();
    try {
      Method method = clazz.getMethod("getTag", new Class[0]);
      Object answer = method.invoke(nmsitem, new Object[0]);
      return answer;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object convertNBTCompoundtoNMSItem(NBTCompound nbtcompound) {
    Class<?> clazz = ClassWrapper.NMS_ITEMSTACK.getClazz();
    try {
      Object nmsstack = clazz.getConstructor(new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() }).newInstance(new Object[] { nbtcompound.getCompound() });
      return nmsstack;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static NBTContainer convertNMSItemtoNBTCompound(Object nmsitem) {
    Class<? extends Object> clazz = (Class)nmsitem.getClass();
    try {
      Method method = clazz.getMethod("save", new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      Object answer = method.invoke(nmsitem, new Object[] { ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]) });
      return new NBTContainer(answer);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object getEntityNBTTagCompound(Object nmsitem) {
    Class<? extends Object> c = (Class)nmsitem.getClass();
    try {
      Method method = c.getMethod(MethodNames.getEntityNbtGetterMethodName(), new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      Object nbt = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
      Object answer = method.invoke(nmsitem, new Object[] { nbt });
      if (answer == null)
        answer = nbt; 
      return answer;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static Object setEntityNBTTag(Object NBTTag, Object NMSItem) {
    try {
      Method method = NMSItem.getClass().getMethod(MethodNames.getEntityNbtSetterMethodName(), new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      method.invoke(NMSItem, new Object[] { NBTTag });
      return NMSItem;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public static Object getTileEntityNBTTagCompound(BlockState tile) {
    try {
      Object pos = ObjectCreator.NMS_BLOCKPOSITION.getInstance(new Object[] { Integer.valueOf(tile.getX()), Integer.valueOf(tile.getY()), Integer.valueOf(tile.getZ()) });
      Object cworld = ClassWrapper.CRAFT_WORLD.getClazz().cast(tile.getWorld());
      Object nmsworld = cworld.getClass().getMethod("getHandle", new Class[0]).invoke(cworld, new Object[0]);
      Object o = nmsworld.getClass().getMethod("getTileEntity", new Class[] { pos.getClass() }).invoke(nmsworld, new Object[] { pos });
      Method method = ClassWrapper.NMS_TILEENTITY.getClazz().getMethod(MethodNames.getTileDataMethodName(), new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      Object tag = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
      Object answer = method.invoke(o, new Object[] { tag });
      if (answer == null)
        answer = tag; 
      return answer;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static void setTileEntityNBTTagCompound(BlockState tile, Object comp) {
    try {
      Object pos = ObjectCreator.NMS_BLOCKPOSITION.getInstance(new Object[] { Integer.valueOf(tile.getX()), Integer.valueOf(tile.getY()), Integer.valueOf(tile.getZ()) });
      Object cworld = ClassWrapper.CRAFT_WORLD.getClazz().cast(tile.getWorld());
      Object nmsworld = cworld.getClass().getMethod("getHandle", new Class[0]).invoke(cworld, new Object[0]);
      Object o = nmsworld.getClass().getMethod("getTileEntity", new Class[] { pos.getClass() }).invoke(nmsworld, new Object[] { pos });
      Method method = ClassWrapper.NMS_TILEENTITY.getClazz().getMethod("a", new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      method.invoke(o, new Object[] { comp });
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public static Object getSubNBTTagCompound(Object compound, String name) {
    Class<? extends Object> c = (Class)compound.getClass();
    try {
      Method method = c.getMethod("getCompound", new Class[] { String.class });
      Object answer = method.invoke(compound, new Object[] { name });
      return answer;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static void addNBTTagCompound(NBTCompound comp, String name) {
    if (name == null) {
      remove(comp, name);
      return;
    } 
    Object nbttag = comp.getCompound();
    if (nbttag == null)
      nbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return; 
    Object workingtag = gettoCompount(nbttag, comp);
    try {
      Method method = workingtag.getClass().getMethod("set", new Class[] { String.class, ClassWrapper.NMS_NBTBASE.getClazz() });
      method.invoke(workingtag, new Object[] { name, ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance() });
      comp.setCompound(nbttag);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public static Boolean valideCompound(NBTCompound comp) {
    Object root = comp.getCompound();
    if (root == null)
      root = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    return (gettoCompount(root, comp) != null) ? Boolean.valueOf(true) : Boolean.valueOf(false);
  }
  
  static Object gettoCompount(Object nbttag, NBTCompound comp) {
    Stack<String> structure = new Stack<>();
    while (comp.getParent() != null) {
      structure.add(comp.getName());
      comp = comp.getParent();
    } 
    while (!structure.isEmpty()) {
      nbttag = getSubNBTTagCompound(nbttag, structure.pop());
      if (nbttag == null)
        return null; 
    } 
    return nbttag;
  }
  
  public static void addOtherNBTCompound(NBTCompound comp, NBTCompound nbtcompound) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    try {
      Method method = workingtag.getClass().getMethod("a", new Class[] { ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz() });
      method.invoke(workingtag, new Object[] { nbtcompound.getCompound() });
      comp.setCompound(rootnbttag);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public static String getContent(NBTCompound comp, String key) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return null; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    try {
      Method method = workingtag.getClass().getMethod("get", new Class[] { String.class });
      return method.invoke(workingtag, new Object[] { key }).toString();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public static void set(NBTCompound comp, String key, Object val) {
    if (val == null) {
      remove(comp, key);
      return;
    } 
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue()) {
      (new Throwable("InvalideCompound")).printStackTrace();
      return;
    } 
    Object workingtag = gettoCompount(rootnbttag, comp);
    try {
      Method method = workingtag.getClass().getMethod("set", new Class[] { String.class, ClassWrapper.NMS_NBTBASE.getClazz() });
      method.invoke(workingtag, new Object[] { key, val });
      comp.setCompound(rootnbttag);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public static NBTList getList(NBTCompound comp, String key, NBTType type) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return null; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    try {
      Method method = workingtag.getClass().getMethod("getList", new Class[] { String.class, int.class });
      return new NBTList(comp, key, type, method.invoke(workingtag, new Object[] { key, Integer.valueOf(type.getId()) }));
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } 
  }
  
  public static void setObject(NBTCompound comp, String key, Object value) {
    if (!MinecraftVersion.hasGsonSupport())
      return; 
    try {
      String json = GsonWrapper.getString(value);
      setData(comp, ReflectionMethod.COMPOUND_SET_STRING, key, json);
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public static <T> T getObject(NBTCompound comp, String key, Class<T> type) {
    if (!MinecraftVersion.hasGsonSupport())
      return null; 
    String json = (String)getData(comp, ReflectionMethod.COMPOUND_GET_STRING, key);
    if (json == null)
      return null; 
    return (T)GsonWrapper.deserializeJson(json, type);
  }
  
  public static void remove(NBTCompound comp, String key) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    ReflectionMethod.COMPOUND_REMOVE_KEY.run(workingtag, new Object[] { key });
    comp.setCompound(rootnbttag);
  }
  
  public static Set<String> getKeys(NBTCompound comp) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return null; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    return (Set<String>)ReflectionMethod.COMPOUND_GET_KEYS.run(workingtag, new Object[0]);
  }
  
  public static void setData(NBTCompound comp, ReflectionMethod type, String key, Object data) {
    if (data == null) {
      remove(comp, key);
      return;
    } 
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]); 
    if (!valideCompound(comp).booleanValue())
      return; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    type.run(workingtag, new Object[] { key, data });
    comp.setCompound(rootnbttag);
  }
  
  public static Object getData(NBTCompound comp, ReflectionMethod type, String key) {
    Object rootnbttag = comp.getCompound();
    if (rootnbttag == null)
      return null; 
    if (!valideCompound(comp).booleanValue())
      return null; 
    Object workingtag = gettoCompount(rootnbttag, comp);
    return type.run(workingtag, new Object[] { key });
  }
}
