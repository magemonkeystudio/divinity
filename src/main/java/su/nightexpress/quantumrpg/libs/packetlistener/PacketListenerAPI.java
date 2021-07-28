package su.nightexpress.quantumrpg.libs.packetlistener;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.libs.apihelper.API;
import su.nightexpress.quantumrpg.libs.apihelper.APIManager;
import su.nightexpress.quantumrpg.libs.packetlistener.channel.ChannelWrapper;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.PacketHandler;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.ReceivedPacket;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.SentPacket;

public class PacketListenerAPI implements IPacketListener, Listener, API {
  private ChannelInjector channelInjector;
  
  protected boolean injected = false;
  
  Logger logger = Logger.getLogger("PacketListenerAPI");
  
  public void load() {
    this.channelInjector = new ChannelInjector();
    if (this.injected = this.channelInjector.inject(this)) {
      this.channelInjector.addServerChannel();
      this.logger.info("Injected custom channel handlers.");
    } else {
      this.logger.severe("Failed to inject channel handlers");
    } 
  }
  
  public void init(Plugin plugin) {
    APIManager.registerEvents(this, this);
    this.logger.info("Adding channels for online players...");
    for (Player player : Bukkit.getOnlinePlayers())
      this.channelInjector.addChannel(player); 
  }
  
  public void disable(Plugin plugin) {
    if (!this.injected)
      return; 
    this.logger.info("Removing channels for online players...");
    for (Player player : Bukkit.getOnlinePlayers())
      this.channelInjector.removeChannel(player); 
    this.logger.info("Removing packet handlers (" + PacketHandler.getHandlers().size() + ")...");
    while (!PacketHandler.getHandlers().isEmpty())
      PacketHandler.removeHandler(PacketHandler.getHandlers().get(0)); 
  }
  
  public static boolean addPacketHandler(PacketHandler handler) {
    return PacketHandler.addHandler(handler);
  }
  
  public static boolean removePacketHandler(PacketHandler handler) {
    return PacketHandler.removeHandler(handler);
  }
  
  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    this.channelInjector.addChannel(e.getPlayer());
  }
  
  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    this.channelInjector.removeChannel(e.getPlayer());
  }
  
  public Object onPacketReceive(Object sender, Object packet, Cancellable cancellable) {
    ReceivedPacket receivedPacket;
    if (sender instanceof Player) {
      receivedPacket = new ReceivedPacket(packet, cancellable, (Player)sender);
    } else {
      receivedPacket = new ReceivedPacket(packet, cancellable, (ChannelWrapper)sender);
    } 
    PacketHandler.notifyHandlers(receivedPacket);
    if (receivedPacket.getPacket() != null)
      return receivedPacket.getPacket(); 
    return packet;
  }
  
  public Object onPacketSend(Object receiver, Object packet, Cancellable cancellable) {
    SentPacket sentPacket;
    if (receiver instanceof Player) {
      sentPacket = new SentPacket(packet, cancellable, (Player)receiver);
    } else {
      sentPacket = new SentPacket(packet, cancellable, (ChannelWrapper)receiver);
    } 
    PacketHandler.notifyHandlers(sentPacket);
    if (sentPacket.getPacket() != null)
      return sentPacket.getPacket(); 
    return packet;
  }
}
