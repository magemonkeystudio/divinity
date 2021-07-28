package su.nightexpress.quantumrpg.libs.packetlistener;

import org.bukkit.event.Cancellable;

public interface IPacketListener {
  Object onPacketSend(Object paramObject1, Object paramObject2, Cancellable paramCancellable);
  
  Object onPacketReceive(Object paramObject1, Object paramObject2, Cancellable paramCancellable);
}
