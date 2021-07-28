package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.lang.reflect.Member;
import su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper.WrapperAbstract;

public abstract class MemberResolver<T extends Member> extends ResolverAbstract<T> {
  protected Class<?> clazz;
  
  public MemberResolver(Class<?> clazz) {
    if (clazz == null)
      throw new IllegalArgumentException("class cannot be null"); 
    this.clazz = clazz;
  }
  
  public MemberResolver(String className) throws ClassNotFoundException {
    this((new ClassResolver()).resolve(new String[] { className }));
  }
  
  public abstract T resolveIndex(int paramInt) throws IndexOutOfBoundsException, ReflectiveOperationException;
  
  public abstract T resolveIndexSilent(int paramInt);
  
  public abstract WrapperAbstract resolveIndexWrapper(int paramInt);
}
