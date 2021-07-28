package su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft;

import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ClassResolver;

public class NMSClassResolver extends ClassResolver {
  public Class<?> resolve(String... names) throws ClassNotFoundException {
    for (int i = 0; i < names.length; i++) {
      if (!names[i].startsWith("net.minecraft.server"))
        names[i] = "net.minecraft.server." + Minecraft.getVersion() + names[i]; 
    } 
    return super.resolve(names);
  }
}
