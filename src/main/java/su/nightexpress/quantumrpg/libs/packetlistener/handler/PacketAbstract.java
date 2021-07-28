package su.nightexpress.quantumrpg.libs.packetlistener.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import su.nightexpress.quantumrpg.libs.packetlistener.channel.ChannelWrapper;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;

public abstract class PacketAbstract {
  private Player player;
  
  private ChannelWrapper channelWrapper;
  
  private Object packet;
  
  private Cancellable cancellable;
  
  protected FieldResolver fieldResolver;
  
  public PacketAbstract(Object packet, Cancellable cancellable, Player player) {
    this.player = player;
    this.packet = packet;
    this.cancellable = cancellable;
    this.fieldResolver = new FieldResolver(packet.getClass());
  }
  
  public PacketAbstract(Object packet, Cancellable cancellable, ChannelWrapper channelWrapper) {
    this.channelWrapper = channelWrapper;
    this.packet = packet;
    this.cancellable = cancellable;
    this.fieldResolver = new FieldResolver(packet.getClass());
  }
  
  public void setPacketValue(String field, Object value) {
    try {
      this.fieldResolver.resolve(new String[] { field }).set(getPacket(), value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void setPacketValueSilent(String field, Object value) {
    try {
      this.fieldResolver.resolve(new String[] { field }).set(getPacket(), value);
    } catch (Exception exception) {}
  }
  
  public void setPacketValue(int index, Object value) {
    try {
      this.fieldResolver.resolveIndex(index).set(getPacket(), value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void setPacketValueSilent(int index, Object value) {
    try {
      this.fieldResolver.resolveIndex(index).set(getPacket(), value);
    } catch (Exception exception) {}
  }
  
  public Object getPacketValue(String field) {
    try {
      return this.fieldResolver.resolve(new String[] { field }).get(getPacket());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public Object getPacketValueSilent(String field) {
    try {
      return this.fieldResolver.resolve(new String[] { field }).get(getPacket());
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public Object getPacketValue(int index) {
    try {
      return this.fieldResolver.resolveIndex(index).get(getPacket());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public Object getPacketValueSilent(int index) {
    try {
      return this.fieldResolver.resolveIndex(index).get(getPacket());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public FieldResolver getFieldResolver() {
    return this.fieldResolver;
  }
  
  public void setCancelled(boolean b) {
    this.cancellable.setCancelled(b);
  }
  
  public boolean isCancelled() {
    return this.cancellable.isCancelled();
  }
  
  public Player getPlayer() {
    return this.player;
  }
  
  public boolean hasPlayer() {
    return (this.player != null);
  }
  
  public ChannelWrapper<?> getChannel() {
    return this.channelWrapper;
  }
  
  public boolean hasChannel() {
    return (this.channelWrapper != null);
  }
  
  public String getPlayername() {
    if (!hasPlayer())
      return null; 
    return this.player.getName();
  }
  
  public void setPacket(Object packet) {
    this.packet = packet;
  }
  
  public Object getPacket() {
    return this.packet;
  }
  
  public String getPacketName() {
    return this.packet.getClass().getSimpleName();
  }
  
  public String toString() {
    return "Packet{ " + (getClass().equals(SentPacket.class) ? "[> OUT >]" : "[< IN <]") + " " + getPacketName() + " " + (hasPlayer() ? getPlayername() : (hasChannel() ? (String)getChannel().channel() : "#server#")) + " }";
  }
}
