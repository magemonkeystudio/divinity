package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.ConstructorWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.WrapperAbstract;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;

public class ConstructorResolver extends MemberResolver<Constructor> {
  public ConstructorResolver(Class<?> clazz) {
    super(clazz);
  }
  
  public ConstructorResolver(String className) throws ClassNotFoundException {
    super(className);
  }
  
  public Constructor resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
    return AccessUtil.setAccessible(this.clazz.getDeclaredConstructors()[index]);
  }
  
  public Constructor resolveIndexSilent(int index) {
    try {
      return resolveIndex(index);
    } catch (IndexOutOfBoundsException|ReflectiveOperationException indexOutOfBoundsException) {
      return null;
    } 
  }
  
  public ConstructorWrapper resolveIndexWrapper(int index) {
    return new ConstructorWrapper(resolveIndexSilent(index));
  }
  
  public ConstructorWrapper resolveWrapper(Class[]... types) {
    return new ConstructorWrapper(resolveSilent(types));
  }
  
  public Constructor resolveSilent(Class[]... types) {
    try {
      return resolve(types);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Constructor resolve(Class[]... types) throws NoSuchMethodException {
    ResolverQuery.Builder builder = ResolverQuery.builder();
    byte b;
    int i;
    Class[][] arrayOfClass;
    for (i = (arrayOfClass = types).length, b = 0; b < i; ) {
      Class[] type = arrayOfClass[b];
      builder.with(type);
      b++;
    } 
    try {
      return (Constructor)resolve(builder.build());
    } catch (ReflectiveOperationException e) {
      throw (NoSuchMethodException)e;
    } 
  }
  
  protected Constructor resolveObject(ResolverQuery query) throws ReflectiveOperationException {
    return AccessUtil.setAccessible(this.clazz.getDeclaredConstructor(query.getTypes()));
  }
  
  public Constructor resolveFirstConstructor() throws ReflectiveOperationException {
    Constructor[] arrayOfConstructor;
    if ((arrayOfConstructor = (Constructor[])this.clazz.getDeclaredConstructors()).length != 0) {
      Constructor constructor = arrayOfConstructor[0];
      return AccessUtil.setAccessible(constructor);
    } 
    return null;
  }
  
  public Constructor resolveFirstConstructorSilent() {
    try {
      return resolveFirstConstructor();
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Constructor resolveLastConstructor() throws ReflectiveOperationException {
    Constructor constructor = null;
    byte b;
    int i;
    Constructor[] arrayOfConstructor;
    for (i = (arrayOfConstructor = (Constructor[])this.clazz.getDeclaredConstructors()).length, b = 0; b < i; ) {
      Constructor constructor1 = arrayOfConstructor[b];
      constructor = constructor1;
      b++;
    } 
    if (constructor != null)
      return AccessUtil.setAccessible(constructor); 
    return null;
  }
  
  public Constructor resolveLastConstructorSilent() {
    try {
      return resolveLastConstructor();
    } catch (Exception exception) {
      return null;
    } 
  }
  
  protected NoSuchMethodException notFoundException(String joinedNames) {
    return new NoSuchMethodException("Could not resolve constructor for " + joinedNames + " in class " + this.clazz);
  }
}
