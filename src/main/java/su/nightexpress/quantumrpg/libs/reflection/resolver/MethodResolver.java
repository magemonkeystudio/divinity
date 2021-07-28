package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.MethodWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.WrapperAbstract;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;

public class MethodResolver extends MemberResolver<Method> {
  public MethodResolver(Class<?> clazz) {
    super(clazz);
  }
  
  public MethodResolver(String className) throws ClassNotFoundException {
    super(className);
  }
  
  public Method resolveSignature(String... signatures) throws ReflectiveOperationException {
    byte b;
    int i;
    Method[] arrayOfMethod;
    for (i = (arrayOfMethod = this.clazz.getDeclaredMethods()).length, b = 0; b < i; ) {
      Method method = arrayOfMethod[b];
      String methodSignature = MethodWrapper.getMethodSignature(method);
      byte b1;
      int j;
      String[] arrayOfString;
      for (j = (arrayOfString = signatures).length, b1 = 0; b1 < j; ) {
        String s = arrayOfString[b1];
        if (s.equals(methodSignature))
          return AccessUtil.setAccessible(method); 
        b1++;
      } 
      b++;
    } 
    return null;
  }
  
  public Method resolveSignatureSilent(String... signatures) {
    try {
      return resolveSignature(signatures);
    } catch (ReflectiveOperationException reflectiveOperationException) {
      return null;
    } 
  }
  
  public MethodWrapper resolveSignatureWrapper(String... signatures) {
    return new MethodWrapper(resolveSignatureSilent(signatures));
  }
  
  public Method resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
    return AccessUtil.setAccessible(this.clazz.getDeclaredMethods()[index]);
  }
  
  public Method resolveIndexSilent(int index) {
    try {
      return resolveIndex(index);
    } catch (IndexOutOfBoundsException|ReflectiveOperationException indexOutOfBoundsException) {
      return null;
    } 
  }
  
  public MethodWrapper resolveIndexWrapper(int index) {
    return new MethodWrapper(resolveIndexSilent(index));
  }
  
  public MethodWrapper resolveWrapper(String... names) {
    return new MethodWrapper(resolveSilent(names));
  }
  
  public MethodWrapper resolveWrapper(ResolverQuery... queries) {
    return new MethodWrapper(resolveSilent(queries));
  }
  
  public Method resolveSilent(String... names) {
    try {
      return resolve(names);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Method resolveSilent(ResolverQuery... queries) {
    return super.resolveSilent(queries);
  }
  
  public Method resolve(String... names) throws NoSuchMethodException {
    ResolverQuery.Builder builder = ResolverQuery.builder();
    byte b;
    int i;
    String[] arrayOfString;
    for (i = (arrayOfString = names).length, b = 0; b < i; ) {
      String name = arrayOfString[b];
      builder.with(name);
      b++;
    } 
    return resolve(builder.build());
  }
  
  public Method resolve(ResolverQuery... queries) throws NoSuchMethodException {
    try {
      return super.resolve(queries);
    } catch (ReflectiveOperationException e) {
      throw (NoSuchMethodException)e;
    } 
  }
  
  protected Method resolveObject(ResolverQuery query) throws ReflectiveOperationException {
    byte b;
    int i;
    Method[] arrayOfMethod;
    for (i = (arrayOfMethod = this.clazz.getDeclaredMethods()).length, b = 0; b < i; ) {
      Method method = arrayOfMethod[b];
      if (method.getName().equals(query.getName()) && ((query.getTypes()).length == 0 || ClassListEqual(query.getTypes(), method.getParameterTypes())))
        return AccessUtil.setAccessible(method); 
      b++;
    } 
    throw new NoSuchMethodException();
  }
  
  protected NoSuchMethodException notFoundException(String joinedNames) {
    return new NoSuchMethodException("Could not resolve method for " + joinedNames + " in class " + this.clazz);
  }
  
  static boolean ClassListEqual(Class[] l1, Class[] l2) {
    boolean equal = true;
    if (l1.length != l2.length)
      return false; 
    for (int i = 0; i < l1.length; i++) {
      if (l1[i] != l2[i]) {
        equal = false;
        break;
      } 
    } 
    return equal;
  }
}
