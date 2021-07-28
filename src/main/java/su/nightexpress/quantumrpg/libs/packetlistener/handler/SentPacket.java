package su.nightexpress.quantumrpg.libs.packetlistener.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import su.nightexpress.quantumrpg.libs.packetlistener.channel.ChannelWrapper;

public class SentPacket extends PacketAbstract {
  public SentPacket(Object packet, Cancellable cancellable, Player player) {
    super(packet, cancellable, player);
  }
  
  public SentPacket(Object packet, Cancellable cancellable, ChannelWrapper channelWrapper) {
    super(packet, cancellable, channelWrapper);
  }
}
