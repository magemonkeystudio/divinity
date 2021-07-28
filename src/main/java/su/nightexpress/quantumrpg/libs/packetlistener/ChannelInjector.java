package su.nightexpress.quantumrpg.libs.packetlistener;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.libs.packetlistener.channel.ChannelAbstract;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ConstructorResolver;

public class ChannelInjector {
  private static final ClassResolver CLASS_RESOLVER = new ClassResolver();
  
  private ChannelAbstract channel;
  
  public boolean inject(IPacketListener iPacketListener) {
    try {
      Class.forName("io.netty.channel.Channel");
      this.channel = newChannelInstance(iPacketListener, "su.nightexpress.quantumrpg.libs.packetlistener.channel.INCChannel");
      return true;
    } catch (Exception e1) {
      e1.printStackTrace();
      return false;
    } 
  }
  
  protected ChannelAbstract newChannelInstance(IPacketListener iPacketListener, String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    return (ChannelAbstract) (new ConstructorResolver(CLASS_RESOLVER.resolve(new String[] { clazzName }))).resolve(new Class[][] { { IPacketListener.class } }).newInstance(new Object[] { iPacketListener });
  }
  
  public void addChannel(Player p) {
    this.channel.addChannel(p);
  }
  
  public void removeChannel(Player p) {
    this.channel.removeChannel(p);
  }
  
  public void addServerChannel() {
    this.channel.addServerChannel();
  }
}
