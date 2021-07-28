package su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper;

public class ClassWrapper<R> extends WrapperAbstract {
  private final Class<R> clazz;
  
  public ClassWrapper(Class<R> clazz) {
    this.clazz = clazz;
  }
  
  public boolean exists() {
    return (this.clazz != null);
  }
  
  public Class<R> getClazz() {
    return this.clazz;
  }
  
  public String getName() {
    return this.clazz.getName();
  }
  
  public R newInstance() {
    try {
      return this.clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public R newInstanceSilent() {
    try {
      return this.clazz.newInstance();
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public boolean equals(Object object) {
    if (this == object)
      return true; 
    if (object == null || getClass() != object.getClass())
      return false; 
    ClassWrapper<?> that = (ClassWrapper)object;
    return (this.clazz != null) ? this.clazz.equals(that.clazz) : ((that.clazz == null));
  }
  
  public int hashCode() {
    return (this.clazz != null) ? this.clazz.hashCode() : 0;
  }
}
