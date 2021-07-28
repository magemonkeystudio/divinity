package su.nightexpress.quantumrpg.libs.packetlistener.channel;

import java.net.SocketAddress;

public class ChannelWrapper<T> {
  private T channel;
  
  public ChannelWrapper(T channel) {
    this.channel = channel;
  }
  
  public T channel() {
    return this.channel;
  }
  
  public SocketAddress getRemoteAddress() {
    return null;
  }
  
  public SocketAddress getLocalAddress() {
    return null;
  }
}
