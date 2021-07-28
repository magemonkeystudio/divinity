package su.nightexpress.quantumrpg.libs.reflection.resolver.wrapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodWrapper<R> extends WrapperAbstract {
  private final Method method;
  
  public MethodWrapper(Method method) {
    this.method = method;
  }
  
  public boolean exists() {
    return (this.method != null);
  }
  
  public String getName() {
    return this.method.getName();
  }
  
  public R invoke(Object object, Object... args) {
    try {
      return (R)this.method.invoke(object, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public R invokeSilent(Object object, Object... args) {
    try {
      return (R)this.method.invoke(object, args);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Method getMethod() {
    return this.method;
  }
  
  public boolean equals(Object object) {
    if (this == object)
      return true; 
    if (object == null || getClass() != object.getClass())
      return false; 
    MethodWrapper<?> that = (MethodWrapper)object;
    return (this.method != null) ? this.method.equals(that.method) : ((that.method == null));
  }
  
  public int hashCode() {
    return (this.method != null) ? this.method.hashCode() : 0;
  }
  
  public static String getMethodSignature(Method method, boolean fullClassNames) {
    return MethodSignature.of(method, fullClassNames).getSignature();
  }
  
  public static String getMethodSignature(Method method) {
    return getMethodSignature(method, false);
  }
  
  public static class MethodSignature {
    static final Pattern SIGNATURE_STRING_PATTERN = Pattern.compile("(.+) (.*)\\((.*)\\)");
    
    private final String returnType;
    
    private final Pattern returnTypePattern;
    
    private final String name;
    
    private final Pattern namePattern;
    
    private final String[] parameterTypes;
    
    private final String signature;
    
    public MethodSignature(String returnType, String name, String[] parameterTypes) {
      this.returnType = returnType;
      this.returnTypePattern = Pattern.compile(returnType
          .replace("?", "\\w")
          .replace("*", "\\w*")
          .replace("[", "\\[")
          .replace("]", "\\]"));
      this.name = name;
      this.namePattern = Pattern.compile(name.replace("?", "\\w").replace("*", "\\w*"));
      this.parameterTypes = parameterTypes;
      StringBuilder builder = new StringBuilder();
      builder.append(returnType).append(" ").append(name).append("(");
      boolean first = true;
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = parameterTypes).length, b = 0; b < i; ) {
        String parameterType = arrayOfString[b];
        if (!first)
          builder.append(","); 
        builder.append(parameterType);
        first = false;
        b++;
      } 
      this.signature = builder.append(")").toString();
    }
    
    public static MethodSignature of(Method method, boolean fullClassNames) {
      String returnTypeString;
      Class<?> returnType = method.getReturnType();
      Class[] parameterTypes = method.getParameterTypes();
      if (returnType.isPrimitive()) {
        returnTypeString = returnType.toString();
      } else {
        returnTypeString = fullClassNames ? returnType.getName() : returnType.getSimpleName();
      } 
      String methodName = method.getName();
      String[] parameterTypeStrings = new String[parameterTypes.length];
      for (int i = 0; i < parameterTypeStrings.length; i++) {
        if (parameterTypes[i].isPrimitive()) {
          parameterTypeStrings[i] = parameterTypes[i].toString();
        } else {
          parameterTypeStrings[i] = fullClassNames ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName();
        } 
      } 
      return new MethodSignature(returnTypeString, methodName, parameterTypeStrings);
    }
    
    public static MethodSignature fromString(String signatureString) {
      if (signatureString == null)
        return null; 
      Matcher matcher = SIGNATURE_STRING_PATTERN.matcher(signatureString);
      if (matcher.find()) {
        if (matcher.groupCount() != 3)
          throw new IllegalArgumentException("invalid signature"); 
        return new MethodSignature(matcher.group(1), matcher.group(2), matcher.group(3).split(","));
      } 
      throw new IllegalArgumentException("invalid signature");
    }
    
    public String getReturnType() {
      return this.returnType;
    }
    
    public boolean isReturnTypeWildcard() {
      return !(!"?".equals(this.returnType) && !"*".equals(this.returnType));
    }
    
    public String getName() {
      return this.name;
    }
    
    public boolean isNameWildcard() {
      return !(!"?".equals(this.name) && !"*".equals(this.name));
    }
    
    public String[] getParameterTypes() {
      return this.parameterTypes;
    }
    
    public String getParameterType(int index) throws IndexOutOfBoundsException {
      return this.parameterTypes[index];
    }
    
    public boolean isParameterWildcard(int index) throws IndexOutOfBoundsException {
      return !(!"?".equals(getParameterType(index)) && !"*".equals(getParameterType(index)));
    }
    
    public String getSignature() {
      return this.signature;
    }
    
    public boolean matches(MethodSignature other) {
      if (other == null)
        return false; 
      if (!this.returnTypePattern.matcher(other.returnType).matches())
        return false; 
      if (!this.namePattern.matcher(other.name).matches())
        return false; 
      if (this.parameterTypes.length != other.parameterTypes.length)
        return false; 
      for (int i = 0; i < this.parameterTypes.length; i++) {
        if (!Pattern.compile(getParameterType(i).replace("?", "\\w").replace("*", "\\w*")).matcher(other.getParameterType(i)).matches())
          return false; 
      } 
      return true;
    }
    
    public boolean equals(Object o) {
      if (this == o)
        return true; 
      if (o == null || getClass() != o.getClass())
        return false; 
      MethodSignature signature1 = (MethodSignature)o;
      if (!this.returnType.equals(signature1.returnType))
        return false; 
      if (!this.name.equals(signature1.name))
        return false; 
      if (!Arrays.equals((Object[])this.parameterTypes, (Object[])signature1.parameterTypes))
        return false; 
      return this.signature.equals(signature1.signature);
    }
    
    public int hashCode() {
      int result = this.returnType.hashCode();
      result = 31 * result + this.name.hashCode();
      result = 31 * result + Arrays.hashCode((Object[])this.parameterTypes);
      result = 31 * result + this.signature.hashCode();
      return result;
    }
    
    public String toString() {
      return getSignature();
    }
  }
}
