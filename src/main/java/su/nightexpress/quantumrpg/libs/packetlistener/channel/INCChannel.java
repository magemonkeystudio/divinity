package su.nightexpress.quantumrpg.libs.packetlistener.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.libs.packetlistener.Cancellable;
import su.nightexpress.quantumrpg.libs.packetlistener.IPacketListener;
import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;

public class INCChannel extends ChannelAbstract {
  private static final Field channelField = networkManagerFieldResolver.resolveByFirstTypeSilent(Channel.class);
  
  public INCChannel(IPacketListener iPacketListener) {
    super(iPacketListener);
  }
  
  public void addChannel(final Player player) {
    try {
      final Channel channel = getChannel(player);
      this.addChannelExecutor.execute(new Runnable() {
            public void run() {
              if (channel.pipeline().get("packet_listener_server") == null)
                try {
                  channel.pipeline().addBefore("packet_handler", "packet_listener_player", (io.netty.channel.ChannelHandler)new INCChannel.ChannelHandler(player));
                } catch (Exception exception) {} 
            }
          });
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to add channel for " + player, e);
    } 
  }
  
  public void removeChannel(Player player) {
    try {
      final Channel channel = getChannel(player);
      this.removeChannelExecutor.execute(new Runnable() {
            public void run() {
              try {
                if (channel.pipeline().get("packet_listener_player") != null)
                  channel.pipeline().remove("packet_listener_player"); 
              } catch (Exception e) {
                throw new RuntimeException(e);
              } 
            }
          });
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to remove channel for " + player, e);
    } 
  }
  
  Channel getChannel(Player player) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(player);
    Object connection = playerConnection.get(handle);
    return (Channel)channelField.get(networkManager.get(connection));
  }
  
  public ChannelAbstract.IListenerList newListenerList() {
    return new ListenerList();
  }
  
  class ListenerList<E> extends ArrayList<E> implements ChannelAbstract.IListenerList<E> {
    public boolean add(E paramE) {
      try {
        final E a = paramE;
        INCChannel.this.addChannelExecutor.execute(new Runnable() {
              public void run() {
                try {
                  Channel channel = null;
                  while (channel == null)
                    channel = (Channel)INCChannel.channelField.get(a); 
                  if (channel.pipeline().get("packet_listener_server") == null)
                    channel.pipeline().addBefore("packet_handler", "packet_listener_server", (io.netty.channel.ChannelHandler)new INCChannel.ChannelHandler(new INCChannel.INCChannelWrapper(channel))); 
                } catch (Exception exception) {}
              }
            });
      } catch (Exception exception) {}
      return super.add(paramE);
    }
    
    public boolean remove(Object arg0) {
      try {
        final Object a = arg0;
        INCChannel.this.removeChannelExecutor.execute(new Runnable() {
              public void run() {
                try {
                  Channel channel = null;
                  while (channel == null)
                    channel = (Channel)INCChannel.channelField.get(a); 
                  channel.pipeline().remove("packet_listener_server");
                } catch (Exception exception) {}
              }
            });
      } catch (Exception exception) {}
      return super.remove(arg0);
    }
  }
  
  class ChannelHandler extends ChannelDuplexHandler implements ChannelAbstract.IChannelHandler {
    private Object owner;
    
    public ChannelHandler(Player player) {
      this.owner = player;
    }
    
    public ChannelHandler(ChannelWrapper channelWrapper) {
      this.owner = channelWrapper;
    }
    
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      Cancellable cancellable = new Cancellable();
      Object pckt = msg;
      if (INCChannel.Packet.isAssignableFrom(msg.getClass()))
        pckt = INCChannel.this.onPacketSend(this.owner, msg, cancellable); 
      if (cancellable.isCancelled())
        return; 
      super.write(ctx, pckt, promise);
    }
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      Cancellable cancellable = new Cancellable();
      Object pckt = msg;
      if (INCChannel.Packet.isAssignableFrom(msg.getClass()))
        pckt = INCChannel.this.onPacketReceive(this.owner, msg, cancellable); 
      if (cancellable.isCancelled())
        return; 
      super.channelRead(ctx, pckt);
    }
    
    public String toString() {
      return "INCChannel$ChannelHandler@" + hashCode() + " (" + this.owner + ")";
    }
  }
  
  class INCChannelWrapper extends ChannelWrapper<Channel> implements ChannelAbstract.IChannelWrapper {
    public INCChannelWrapper(Channel channel) {
      super(channel);
    }
    
    public SocketAddress getRemoteAddress() {
      return channel().remoteAddress();
    }
    
    public SocketAddress getLocalAddress() {
      return channel().localAddress();
    }
  }
}
