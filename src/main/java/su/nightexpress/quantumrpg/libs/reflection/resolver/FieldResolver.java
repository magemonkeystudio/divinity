package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.FieldWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.WrapperAbstract;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;

public class FieldResolver extends MemberResolver<Field> {
  public FieldResolver(Class<?> clazz) {
    super(clazz);
  }
  
  public FieldResolver(String className) throws ClassNotFoundException {
    super(className);
  }
  
  public Field resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
    return AccessUtil.setAccessible(this.clazz.getDeclaredFields()[index]);
  }
  
  public Field resolveIndexSilent(int index) {
    try {
      return resolveIndex(index);
    } catch (IndexOutOfBoundsException|ReflectiveOperationException indexOutOfBoundsException) {
      return null;
    } 
  }
  
  public FieldWrapper resolveIndexWrapper(int index) {
    return new FieldWrapper(resolveIndexSilent(index));
  }
  
  public FieldWrapper resolveWrapper(String... names) {
    return new FieldWrapper(resolveSilent(names));
  }
  
  public Field resolveSilent(String... names) {
    try {
      return resolve(names);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Field resolve(String... names) throws NoSuchFieldException {
    ResolverQuery.Builder builder = ResolverQuery.builder();
    byte b;
    int i;
    String[] arrayOfString;
    for (i = (arrayOfString = names).length, b = 0; b < i; ) {
      String name = arrayOfString[b];
      builder.with(name);
      b++;
    } 
    try {
      return super.resolve(builder.build());
    } catch (ReflectiveOperationException e) {
      throw (NoSuchFieldException)e;
    } 
  }
  
  public Field resolveSilent(ResolverQuery... queries) {
    try {
      return resolve(queries);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Field resolve(ResolverQuery... queries) throws NoSuchFieldException {
    try {
      return super.resolve(queries);
    } catch (ReflectiveOperationException e) {
      throw (NoSuchFieldException)e;
    } 
  }
  
  protected Field resolveObject(ResolverQuery query) throws ReflectiveOperationException {
    if (query.getTypes() == null || (query.getTypes()).length == 0)
      return AccessUtil.setAccessible(this.clazz.getDeclaredField(query.getName())); 
    byte b;
    int i;
    Field[] arrayOfField;
    for (i = (arrayOfField = this.clazz.getDeclaredFields()).length, b = 0; b < i; ) {
      Field field = arrayOfField[b];
      if (field.getName().equals(query.getName())) {
        byte b1;
        int j;
        Class[] arrayOfClass;
        for (j = (arrayOfClass = query.getTypes()).length, b1 = 0; b1 < j; ) {
          Class type = arrayOfClass[b1];
          if (field.getType().equals(type))
            return field; 
          b1++;
        } 
      } 
      b++;
    } 
    return null;
  }
  
  public Field resolveByFirstType(Class<?> type) throws ReflectiveOperationException {
    byte b;
    int i;
    Field[] arrayOfField;
    for (i = (arrayOfField = this.clazz.getDeclaredFields()).length, b = 0; b < i; ) {
      Field field = arrayOfField[b];
      if (field.getType().equals(type))
        return AccessUtil.setAccessible(field); 
      b++;
    } 
    throw new NoSuchFieldException("Could not resolve field of type '" + type.toString() + "' in class " + this.clazz);
  }
  
  public Field resolveByFirstTypeSilent(Class<?> type) {
    try {
      return resolveByFirstType(type);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Field resolveByLastType(Class<?> type) throws ReflectiveOperationException {
    Field field = null;
    byte b;
    int i;
    Field[] arrayOfField;
    for (i = (arrayOfField = this.clazz.getDeclaredFields()).length, b = 0; b < i; ) {
      Field field1 = arrayOfField[b];
      if (field1.getType().equals(type))
        field = field1; 
      b++;
    } 
    if (field == null)
      throw new NoSuchFieldException("Could not resolve field of type '" + type.toString() + "' in class " + this.clazz); 
    return AccessUtil.setAccessible(field);
  }
  
  public Field resolveByLastTypeSilent(Class<?> type) {
    try {
      return resolveByLastType(type);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  protected NoSuchFieldException notFoundException(String joinedNames) {
    return new NoSuchFieldException("Could not resolve field for " + joinedNames + " in class " + this.clazz);
  }
}
