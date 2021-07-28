package su.nightexpress.quantumrpg.libs.packetlistener.channel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import su.nightexpress.quantumrpg.libs.packetlistener.IPacketListener;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.MethodResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.NMSClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.util.AccessUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class ChannelAbstract {
    protected static final NMSClassResolver nmsClassResolver = new NMSClassResolver();
    protected static final MethodResolver craftServerFieldResolver = new MethodResolver(Bukkit.getServer().getClass());
    static final Class<?> EntityPlayer = nmsClassResolver.resolveSilent("EntityPlayer");
    protected static final FieldResolver entityPlayerFieldResolver = new FieldResolver(EntityPlayer);
    static final Class<?> PlayerConnection = nmsClassResolver.resolveSilent("PlayerConnection");
    protected static final FieldResolver playerConnectionFieldResolver = new FieldResolver(PlayerConnection);
    static final Class<?> NetworkManager = nmsClassResolver.resolveSilent("NetworkManager");
    protected static final FieldResolver networkManagerFieldResolver = new FieldResolver(NetworkManager);
    static final Class<?> Packet = nmsClassResolver.resolveSilent("Packet");
    static final Class<?> ServerConnection = nmsClassResolver.resolveSilent("ServerConnection");
    protected static final FieldResolver serverConnectionFieldResolver = new FieldResolver(ServerConnection);
    static final Class<?> MinecraftServer = nmsClassResolver.resolveSilent("MinecraftServer");
    protected static final FieldResolver minecraftServerFieldResolver = new FieldResolver(MinecraftServer);
    static final Field networkManager = playerConnectionFieldResolver.resolveSilent("networkManager");
    static final Field playerConnection = entityPlayerFieldResolver.resolveSilent("playerConnection");
    static final Field serverConnection = minecraftServerFieldResolver.resolveByFirstTypeSilent(ServerConnection);
    static final Field connectionList = serverConnectionFieldResolver.resolveByLastTypeSilent(List.class);
    static final Method getServer = craftServerFieldResolver.resolveSilent("getServer");
    static final String KEY_HANDLER = "packet_handler";
    static final String KEY_PLAYER = "packet_listener_player";
    static final String KEY_SERVER = "packet_listener_server";
    final Executor addChannelExecutor = Executors.newSingleThreadExecutor();
    final Executor removeChannelExecutor = Executors.newSingleThreadExecutor();
    private final IPacketListener iPacketListener;

    public ChannelAbstract(IPacketListener iPacketListener) {
        this.iPacketListener = iPacketListener;
    }

    public abstract void addChannel(Player paramPlayer);

    public abstract void removeChannel(Player paramPlayer);

    public void addServerChannel() {
        try {
            Object dedicatedServer = getServer.invoke(Bukkit.getServer());
            if (dedicatedServer == null)
                return;
            Object serverConnection = ChannelAbstract.serverConnection.get(dedicatedServer);
            if (serverConnection == null)
                return;
            List<?> currentList = (List) connectionList.get(serverConnection);
            Field superListField = AccessUtil.setAccessible(currentList.getClass().getSuperclass().getDeclaredField("list"));
            Object list = superListField.get(currentList);
            if (IListenerList.class.isAssignableFrom(list.getClass()))
                return;
            List<Object> newList = Collections.synchronizedList(newListenerList());
            for (Object o : currentList)
                newList.add(o);
            connectionList.set(serverConnection, newList);
        } catch (Exception exception) {
        }
    }

    public abstract IListenerList<Object> newListenerList();

    protected final Object onPacketSend(Object receiver, Object packet, Cancellable cancellable) {
        return this.iPacketListener.onPacketSend(receiver, packet, cancellable);
    }

    protected final Object onPacketReceive(Object sender, Object packet, Cancellable cancellable) {
        return this.iPacketListener.onPacketReceive(sender, packet, cancellable);
    }

    interface IChannelHandler {
    }

    interface IChannelWrapper {
    }

    interface IListenerList<E> extends List<E> {
    }
}
