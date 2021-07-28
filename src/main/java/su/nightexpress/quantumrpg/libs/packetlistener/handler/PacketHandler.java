package su.nightexpress.quantumrpg.libs.packetlistener.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.MethodResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.NMSClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;

public abstract class PacketHandler {
  private static final List<PacketHandler> handlers = new ArrayList<>();
  
  private boolean hasSendOptions;
  
  private boolean forcePlayerSend;
  
  private boolean forceServerSend;
  
  private boolean hasReceiveOptions;
  
  private boolean forcePlayerReceive;
  
  private boolean forceServerReceive;
  
  public static boolean addHandler(PacketHandler handler) {
    boolean b = handlers.contains(handler);
    if (!b) {
      try {
        PacketOptions options = handler.getClass().getMethod("onSend", new Class[] { SentPacket.class }).<PacketOptions>getAnnotation(PacketOptions.class);
        if (options != null) {
          handler.hasSendOptions = true;
          if (options.forcePlayer() && options.forceServer())
            throw new IllegalArgumentException("Cannot force player and server packets at the same time!"); 
          if (options.forcePlayer()) {
            handler.forcePlayerSend = true;
          } else if (options.forceServer()) {
            handler.forceServerSend = true;
          } 
        } 
      } catch (Exception e) {
        throw new RuntimeException("Failed to register handler (onSend)", e);
      } 
      try {
        PacketOptions options = handler.getClass().getMethod("onReceive", new Class[] { ReceivedPacket.class }).<PacketOptions>getAnnotation(PacketOptions.class);
        if (options != null) {
          handler.hasReceiveOptions = true;
          if (options.forcePlayer() && options.forceServer())
            throw new IllegalArgumentException("Cannot force player and server packets at the same time!"); 
          if (options.forcePlayer()) {
            handler.forcePlayerReceive = true;
          } else if (options.forceServer()) {
            handler.forceServerReceive = true;
          } 
        } 
      } catch (Exception e) {
        throw new RuntimeException("Failed to register handler (onReceive)", e);
      } 
    } 
    handlers.add(handler);
    return !b;
  }
  
  public static boolean removeHandler(PacketHandler handler) {
    return handlers.remove(handler);
  }
  
  public static void notifyHandlers(SentPacket packet) {
    for (PacketHandler handler : getHandlers()) {
      try {
        if (handler.hasSendOptions && (
          handler.forcePlayerSend ? 
          !packet.hasPlayer() : (
          
          handler.forceServerSend && 
          !packet.hasChannel())))
          continue; 
        handler.onSend(packet);
      } catch (Exception e) {
        System.err.println("[PacketListenerAPI] An exception occured while trying to execute 'onSend'" + ((handler.plugin != null) ? (" in plugin " + handler.plugin.getName()) : "") + ": " + e.getMessage());
        e.printStackTrace(System.err);
      } 
    } 
  }
  
  public static void notifyHandlers(ReceivedPacket packet) {
    for (PacketHandler handler : getHandlers()) {
      try {
        if (handler.hasReceiveOptions && (
          handler.forcePlayerReceive ? 
          !packet.hasPlayer() : (
          
          handler.forceServerReceive && 
          !packet.hasChannel())))
          continue; 
        handler.onReceive(packet);
      } catch (Exception e) {
        System.err.println("[PacketListenerAPI] An exception occured while trying to execute 'onReceive'" + ((handler.plugin != null) ? (" in plugin " + handler.plugin.getName()) : "") + ": " + e.getMessage());
        e.printStackTrace(System.err);
      } 
    } 
  }
  
  public boolean equals(Object object) {
    if (this == object)
      return true; 
    if (object == null || getClass() != object.getClass())
      return false; 
    PacketHandler that = (PacketHandler)object;
    if (this.hasSendOptions != that.hasSendOptions)
      return false; 
    if (this.forcePlayerSend != that.forcePlayerSend)
      return false; 
    if (this.forceServerSend != that.forceServerSend)
      return false; 
    if (this.hasReceiveOptions != that.hasReceiveOptions)
      return false; 
    if (this.forcePlayerReceive != that.forcePlayerReceive)
      return false; 
    if (this.forceServerReceive != that.forceServerReceive)
      return false; 
    return !((this.plugin != null) ? !this.plugin.equals(that.plugin) : (that.plugin != null));
  }
  
  public int hashCode() {
    int result = this.hasSendOptions ? 1 : 0;
    result = 31 * result + (this.forcePlayerSend ? 1 : 0);
    result = 31 * result + (this.forceServerSend ? 1 : 0);
    result = 31 * result + (this.hasReceiveOptions ? 1 : 0);
    result = 31 * result + (this.forcePlayerReceive ? 1 : 0);
    result = 31 * result + (this.forceServerReceive ? 1 : 0);
    result = 31 * result + ((this.plugin != null) ? this.plugin.hashCode() : 0);
    return result;
  }
  
  public String toString() {
    return "PacketHandler{hasSendOptions=" + 
      this.hasSendOptions + 
      ", forcePlayerSend=" + this.forcePlayerSend + 
      ", forceServerSend=" + this.forceServerSend + 
      ", hasReceiveOptions=" + this.hasReceiveOptions + 
      ", forcePlayerReceive=" + this.forcePlayerReceive + 
      ", forceServerReceive=" + this.forceServerReceive + 
      ", plugin=" + this.plugin + 
      '}';
  }
  
  public static List<PacketHandler> getHandlers() {
    return new ArrayList<>(handlers);
  }
  
  public static List<PacketHandler> getForPlugin(Plugin plugin) {
    List<PacketHandler> handlers = new ArrayList<>();
    if (plugin == null)
      return handlers; 
    for (PacketHandler h : getHandlers()) {
      if (plugin.equals(h.getPlugin()))
        handlers.add(h); 
    } 
    return handlers;
  }
  
  static NMSClassResolver nmsClassResolver = new NMSClassResolver();
  
  static FieldResolver EntityPlayerFieldResolver = new FieldResolver(nmsClassResolver.resolveSilent(new String[] { "EntityPlayer" }));
  
  static MethodResolver PlayerConnectionMethodResolver = new MethodResolver(nmsClassResolver.resolveSilent(new String[] { "PlayerConnection" }));
  
  private Plugin plugin;
  
  public void sendPacket(Player p, Object packet) {
    if (p == null || packet == null)
      throw new NullPointerException(); 
    try {
      Object handle = Minecraft.getHandle(p);
      Object connection = EntityPlayerFieldResolver.resolve(new String[] { "playerConnection" }).get(handle);
      PlayerConnectionMethodResolver.resolve(new String[] { "sendPacket" }).invoke(connection, new Object[] { packet });
    } catch (Exception e) {
      System.err.println("[PacketListenerAPI] Exception while sending " + packet + " to " + p);
      e.printStackTrace();
    } 
  }
  
  public Object cloneObject(Object obj) throws Exception {
    if (obj == null)
      return obj; 
    Object clone = obj.getClass().newInstance();
    byte b;
    int i;
    Field[] arrayOfField;
    for (i = (arrayOfField = obj.getClass().getDeclaredFields()).length, b = 0; b < i; ) {
      Field f = arrayOfField[b];
      f = AccessUtil.setAccessible(f);
      f.set(clone, f.get(obj));
      b++;
    } 
    return clone;
  }
  
  @Deprecated
  public PacketHandler() {}
  
  public PacketHandler(Plugin plugin) {
    this.plugin = plugin;
  }
  
  public Plugin getPlugin() {
    return this.plugin;
  }
  
  public abstract void onSend(SentPacket paramSentPacket);
  
  public abstract void onReceive(ReceivedPacket paramReceivedPacket);
}
