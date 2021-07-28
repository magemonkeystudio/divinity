package su.nightexpress.quantumrpg.libs.reflection.resolver;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ResolverAbstract<T> {
  protected final Map<ResolverQuery, T> resolvedObjects = new ConcurrentHashMap<>();
  
  protected T resolveSilent(ResolverQuery... queries) {
    try {
      return resolve(queries);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  protected T resolve(ResolverQuery... queries) throws ReflectiveOperationException {
    if (queries == null || queries.length <= 0)
      throw new IllegalArgumentException("Given possibilities are empty"); 
    byte b;
    int i;
    ResolverQuery[] arrayOfResolverQuery;
    for (i = (arrayOfResolverQuery = queries).length, b = 0; b < i; ) {
      ResolverQuery query = arrayOfResolverQuery[b];
      if (this.resolvedObjects.containsKey(query))
        return this.resolvedObjects.get(query); 
      try {
        T resolved = resolveObject(query);
        this.resolvedObjects.put(query, resolved);
        return resolved;
      } catch (ReflectiveOperationException reflectiveOperationException) {}
      b++;
    } 
    throw notFoundException(Arrays.asList(queries).toString());
  }
  
  protected abstract T resolveObject(ResolverQuery paramResolverQuery) throws ReflectiveOperationException;
  
  protected ReflectiveOperationException notFoundException(String joinedNames) {
    return new ReflectiveOperationException("Objects could not be resolved: " + joinedNames);
  }
}
