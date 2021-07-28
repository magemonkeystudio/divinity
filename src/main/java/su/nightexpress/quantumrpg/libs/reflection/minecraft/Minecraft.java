package su.nightexpress.quantumrpg.libs.reflection.minecraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ConstructorResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.MethodResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.NMSClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.OBCClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;
import sun.reflect.ConstructorAccessor;

public class Minecraft {
  static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9]*)_R([0-9])");
  
  public static final Version VERSION;
  
  private static NMSClassResolver nmsClassResolver = new NMSClassResolver();
  
  private static OBCClassResolver obcClassResolver = new OBCClassResolver();
  
  private static Class<?> NmsEntity;
  
  private static Class<?> CraftEntity;
  
  static {
    VERSION = Version.getVersion();
    System.out.println("[ReflectionHelper] Version is " + VERSION);
    try {
      NmsEntity = nmsClassResolver.resolve(new String[] { "Entity" });
      CraftEntity = obcClassResolver.resolve(new String[] { "entity.CraftEntity" });
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static String getVersion() {
    return String.valueOf(VERSION.name()) + ".";
  }
  
  public static Object getHandle(Object object) throws ReflectiveOperationException {
    Method method;
    try {
      method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle", new Class[0]));
    } catch (ReflectiveOperationException e) {
      method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle", new Class[0]));
    } 
    return method.invoke(object, new Object[0]);
  }
  
  public static Entity getBukkitEntity(Object object) throws ReflectiveOperationException {
    Method method;
    try {
      method = AccessUtil.setAccessible(NmsEntity.getDeclaredMethod("getBukkitEntity", new Class[0]));
    } catch (ReflectiveOperationException e) {
      method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle", new Class[0]));
    } 
    return (Entity)method.invoke(object, new Object[0]);
  }
  
  public static Object getHandleSilent(Object object) {
    try {
      return getHandle(object);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public enum Version {
    UNKNOWN(-1) {
      public boolean matchesPackageName(String packageName) {
        return false;
      }
    },
    v1_7_R1(10701),
    v1_7_R2(10702),
    v1_7_R3(10703),
    v1_7_R4(10704),
    v1_8_R1(10801),
    v1_8_R2(10802),
    v1_8_R3(10803),
    v1_8_R4(
      10804),
    v1_9_R1(10901),
    v1_9_R2(10902),
    v1_10_R1(11001),
    v1_11_R1(11101),
    v1_12_R1(11201),
    v1_13_R1(11301),
    v1_13_R2(11302);
    
    private int version;
    
    Version(int version) {
      this.version = version;
    }
    
    public int version() {
      return this.version;
    }
    
    public boolean olderThan(Version version) {
      return (version() < version.version());
    }
    
    public boolean newerThan(Version version) {
      return (version() >= version.version());
    }
    
    public boolean inRange(Version oldVersion, Version newVersion) {
      return (newerThan(oldVersion) && olderThan(newVersion));
    }
    
    public boolean matchesPackageName(String packageName) {
      return packageName.toLowerCase().contains(name().toLowerCase());
    }
    
    public static Version getVersion() {
      String name = Bukkit.getServer().getClass().getPackage().getName();
      String versionPackage = String.valueOf(name.substring(name.lastIndexOf('.') + 1)) + ".";
      byte b;
      int i;
      Version[] arrayOfVersion;
      for (i = (arrayOfVersion = values()).length, b = 0; b < i; ) {
        Version version = arrayOfVersion[b];
        if (version.matchesPackageName(versionPackage))
          return version; 
        b++;
      } 
      System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");
      System.out.println("[ReflectionHelper] Generating dynamic constant...");
      Matcher matcher = Minecraft.NUMERIC_VERSION_PATTERN.matcher(versionPackage);
      while (matcher.find()) {
        if (matcher.groupCount() < 3)
          continue; 
        String majorString = matcher.group(1);
        String minorString = matcher.group(2);
        if (minorString.length() == 1)
          minorString = "0" + minorString; 
        String patchString = matcher.group(3);
        if (patchString.length() == 1)
          patchString = "0" + patchString; 
        String numVersionString = String.valueOf(majorString) + minorString + patchString;
        int numVersion = Integer.parseInt(numVersionString);
        String packge = versionPackage.substring(0, versionPackage.length() - 1);
        try {
          Field valuesField = (new FieldResolver(Version.class)).resolve(new String[] { "$VALUES" });
          Version[] oldValues = (Version[])valuesField.get(null);
          Version[] newValues = new Version[oldValues.length + 1];
          System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
          Version dynamicVersion = (Version)Minecraft.newEnumInstance(Version.class, new Class[] { String.class, 
                int.class, 
                int.class }, new Object[] { packge, 
                Integer.valueOf(newValues.length - 1), 
                Integer.valueOf(numVersion) });
          newValues[newValues.length - 1] = dynamicVersion;
          valuesField.set(null, newValues);
          System.out.println("[ReflectionHelper] Injected dynamic version " + packge + " (#" + numVersion + ").");
          System.out.println("[ReflectionHelper] Please inform inventivetalent about the outdated version, as this is not guaranteed to work.");
          return dynamicVersion;
        } catch (ReflectiveOperationException e) {
          e.printStackTrace();
        } 
      } 
      return UNKNOWN;
    }
    
    public String toString() {
      return String.valueOf(name()) + " (" + version() + ")";
    }
  }
  
  public static Object newEnumInstance(Class clazz, Class[] types, Object[] values) throws ReflectiveOperationException {
    Constructor constructor = (new ConstructorResolver(clazz)).resolve(new Class[][] { types });
    Field accessorField = (new FieldResolver(Constructor.class)).resolve(new String[] { "constructorAccessor" });
    ConstructorAccessor constructorAccessor = (ConstructorAccessor)accessorField.get(constructor);
    if (constructorAccessor == null) {
      (new MethodResolver(Constructor.class)).resolve(new String[] { "acquireConstructorAccessor" }).invoke(constructor, new Object[0]);
      constructorAccessor = (ConstructorAccessor)accessorField.get(constructor);
    } 
    return constructorAccessor.newInstance(values);
  }
}
