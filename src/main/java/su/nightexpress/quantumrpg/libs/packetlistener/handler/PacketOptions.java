package su.nightexpress.quantumrpg.libs.packetlistener.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketOptions {
  boolean forcePlayer() default false;
  
  boolean forceServer() default false;
}
