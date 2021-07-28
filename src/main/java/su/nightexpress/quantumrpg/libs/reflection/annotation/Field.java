package su.nightexpress.quantumrpg.libs.reflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
  String className();
  
  String[] value();
  
  Minecraft.Version[] versions() default {};
  
  boolean ignoreExceptions() default true;
}
