package su.nightexpress.quantumrpg.libs.glowapi;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.libs.apihelper.API;
import su.nightexpress.quantumrpg.libs.apihelper.APIManager;
import su.nightexpress.quantumrpg.libs.packetlistener.PacketListenerAPI;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.PacketHandler;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.PacketOptions;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.ReceivedPacket;
import su.nightexpress.quantumrpg.libs.packetlistener.handler.SentPacket;
import su.nightexpress.quantumrpg.libs.reflection.minecraft.DataWatcher;
import su.nightexpress.quantumrpg.libs.reflection.minecraft.Minecraft;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ConstructorResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.FieldResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.MethodResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.ResolverQuery;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.NMSClassResolver;
import su.nightexpress.quantumrpg.libs.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class GlowAPI implements API, Listener {
    private static final NMSClassResolver NMS_CLASS_RESOLVER = new NMSClassResolver();
    private static final Map<UUID, GlowData> dataMap = new HashMap<>();
    public static String TEAM_TAG_VISIBILITY = "always";
    public static String TEAM_PUSH = "always";
    protected static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    protected static OBCClassResolver obcClassResolver = new OBCClassResolver();
    static Class<?> mcDataWatcher;
    static Class<?> DataWatcherItem;
    static FieldResolver DataWatcherItemFieldResolver;
    static MethodResolver DataWatcherItemMethodResolver;
    private static Class<?> PacketPlayOutEntityMetadata;
    private static Class<?> Entity;
    private static FieldResolver PacketPlayOutMetadataFieldResolver;
    private static FieldResolver EntityFieldResolver;
    private static FieldResolver DataWatcherFieldResolver;
    private static ConstructorResolver DataWatcherItemConstructorResolver;
    private static MethodResolver DataWatcherMethodResolver;
    private static MethodResolver EntityMethodResolver;
    private static Class<?> PacketPlayOutScoreboardTeam;
    private static FieldResolver PacketScoreboardTeamFieldResolver;
    private static FieldResolver EntityPlayerFieldResolver;
    private static MethodResolver PlayerConnectionMethodResolver;
    private static FieldResolver CraftWorldFieldResolver;
    private static FieldResolver WorldFieldResolver;
    private static MethodResolver IntHashMapMethodResolver;

    public static void setGlowing(Entity entity, Color color, String tagVisibility, String push, Player receiver) {
        GlowData glowData;
        if (receiver == null)
            return;
        boolean glowing = (color != null);
        if (entity == null)
            glowing = false;
        if (entity instanceof OfflinePlayer && !((OfflinePlayer) entity).isOnline())
            glowing = false;
        boolean wasGlowing = dataMap.containsKey((entity != null) ? entity.getUniqueId() : null);
        if (wasGlowing && entity != null) {
            glowData = dataMap.get(entity.getUniqueId());
        } else {
            glowData = new GlowData();
        }
        Color oldColor = wasGlowing ? glowData.colorMap.get(receiver.getUniqueId()) : null;
        if (glowing) {
            glowData.colorMap.put(receiver.getUniqueId(), color);
        } else {
            glowData.colorMap.remove(receiver.getUniqueId());
        }
        if (glowData.colorMap.isEmpty()) {
            dataMap.remove((entity != null) ? entity.getUniqueId() : null);
        } else if (entity != null) {
            dataMap.put(entity.getUniqueId(), glowData);
        }
        if (color != null && oldColor == color)
            return;
        if (entity == null)
            return;
        if (entity instanceof OfflinePlayer && !((OfflinePlayer) entity).isOnline())
            return;
        if (!receiver.isOnline())
            return;
        sendGlowPacket(entity, wasGlowing, glowing, receiver);
        if (oldColor != null && oldColor != Color.NONE)
            sendTeamPacket(entity, oldColor, false, false, tagVisibility, push, receiver);
        if (glowing)
            sendTeamPacket(entity, color, false, (color != Color.NONE), tagVisibility, push, receiver);
    }

    public static void setGlowing(Entity entity, Color color, Player receiver) {
        setGlowing(entity, color, "always", "always", receiver);
    }

    public static void setGlowing(Entity entity, boolean glowing, Player receiver) {
        setGlowing(entity, glowing ? Color.NONE : null, receiver);
    }

    public static void setGlowing(Entity entity, boolean glowing, Collection<? extends Player> receivers) {
        for (Player receiver : receivers)
            setGlowing(entity, glowing, receiver);
    }

    public static void setGlowing(Entity entity, Color color, Collection<? extends Player> receivers) {
        for (Player receiver : receivers)
            setGlowing(entity, color, receiver);
    }

    public static void setGlowing(Collection<? extends Entity> entities, Color color, Player receiver) {
        for (Entity entity : entities)
            setGlowing(entity, color, receiver);
    }

    public static void setGlowing(Collection<? extends Entity> entities, Color color, Collection<? extends Player> receivers) {
        for (Entity entity : entities)
            setGlowing(entity, color, receivers);
    }

    public static boolean isGlowing(Entity entity, Player receiver) {
        return (getGlowColor(entity, receiver) != null);
    }

    public static boolean isGlowing(Entity entity, Collection<? extends Player> receivers, boolean checkAll) {
        if (checkAll) {
            boolean glowing = true;
            for (Player receiver : receivers) {
                if (!isGlowing(entity, receiver))
                    glowing = false;
            }
            return glowing;
        }
        for (Player receiver : receivers) {
            if (isGlowing(entity, receiver))
                return true;
        }
        return false;
    }

    public static Color getGlowColor(Entity entity, Player receiver) {
        if (!dataMap.containsKey(entity.getUniqueId()))
            return null;
        GlowData data = dataMap.get(entity.getUniqueId());
        return data.colorMap.get(receiver.getUniqueId());
    }

    protected static void sendGlowPacket(Entity entity, boolean wasGlowing, boolean glowing, Player receiver) {
        try {
            if (PacketPlayOutEntityMetadata == null)
                PacketPlayOutEntityMetadata = NMS_CLASS_RESOLVER.resolve("PacketPlayOutEntityMetadata");
            if (mcDataWatcher == null)
                mcDataWatcher = NMS_CLASS_RESOLVER.resolve("DataWatcher");
            if (DataWatcherItem == null)
                DataWatcherItem = NMS_CLASS_RESOLVER.resolve("DataWatcher$Item");
            if (Entity == null)
                Entity = NMS_CLASS_RESOLVER.resolve("Entity");
            if (PacketPlayOutMetadataFieldResolver == null)
                PacketPlayOutMetadataFieldResolver = new FieldResolver(PacketPlayOutEntityMetadata);
            if (DataWatcherItemConstructorResolver == null)
                DataWatcherItemConstructorResolver = new ConstructorResolver(DataWatcherItem);
            if (EntityFieldResolver == null)
                EntityFieldResolver = new FieldResolver(Entity);
            if (DataWatcherMethodResolver == null)
                DataWatcherMethodResolver = new MethodResolver(mcDataWatcher);
            if (DataWatcherItemMethodResolver == null)
                DataWatcherItemMethodResolver = new MethodResolver(DataWatcherItem);
            if (EntityMethodResolver == null)
                EntityMethodResolver = new MethodResolver(Entity);
            if (DataWatcherFieldResolver == null)
                DataWatcherFieldResolver = new FieldResolver(mcDataWatcher);

            List<Object> list = new ArrayList();
            Object dataWatcher = EntityMethodResolver.resolve(new String[]{"getDataWatcher"}).invoke(Minecraft.getHandle(entity));
            Map<Integer, Object> dataWatcherItems = (Map<Integer, Object>) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher);
            Object dataWatcherObject = DataWatcher.V1_9.ValueType.ENTITY_FLAG.getType();

            byte prev = dataWatcherItems.isEmpty() ?
                    Integer.valueOf(0).byteValue() :
                    (Byte) DataWatcherItemMethodResolver.resolve(new String[]{"b"}).invoke(dataWatcherItems.get(Integer.valueOf(0)), new Object[0]);
            byte b = (byte) (glowing ? (prev | 0x40) : (prev & 0xFFFFFFBF));
            Object dataWatcherItem = DataWatcherItemConstructorResolver.resolveFirstConstructor().newInstance(dataWatcherObject, Byte.valueOf(b));
            list.add(dataWatcherItem);
            Object packetMetadata = PacketPlayOutEntityMetadata.newInstance();
            PacketPlayOutMetadataFieldResolver.resolve(new String[]{"a"}).set(packetMetadata, Integer.valueOf(-entity.getEntityId()));
            PacketPlayOutMetadataFieldResolver.resolve(new String[]{"b"}).set(packetMetadata, list);
            sendPacket(packetMetadata, receiver);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initTeam(Player receiver, String tagVisibility, String push) {
        byte b;
        int i;
        Color[] arrayOfColor;
        for (i = (arrayOfColor = Color.values()).length, b = 0; b < i; ) {
            Color color = arrayOfColor[b];
            sendTeamPacket(null, color, true, false, tagVisibility, push, receiver);
            b++;
        }
    }

    public static void initTeam(Player receiver) {
        initTeam(receiver, TEAM_TAG_VISIBILITY, TEAM_PUSH);
    }

    protected static void sendTeamPacket(Entity entity, Color color, boolean createNewTeam, boolean addEntity, String tagVisibility, String push, Player receiver) {
        try {
            if (PacketPlayOutScoreboardTeam == null)
                PacketPlayOutScoreboardTeam = NMS_CLASS_RESOLVER.resolve("PacketPlayOutScoreboardTeam");
            if (PacketScoreboardTeamFieldResolver == null)
                PacketScoreboardTeamFieldResolver = new FieldResolver(PacketPlayOutScoreboardTeam);
            Object packetScoreboardTeam = PacketPlayOutScoreboardTeam.newInstance();
            PacketScoreboardTeamFieldResolver.resolve(new String[]{"i"}).set(packetScoreboardTeam, Integer.valueOf(createNewTeam ? 0 : (addEntity ? 3 : 4)));
            PacketScoreboardTeamFieldResolver.resolve(new String[]{"a"}).set(packetScoreboardTeam, color.getTeamName());
            PacketScoreboardTeamFieldResolver.resolve(new String[]{"e"}).set(packetScoreboardTeam, tagVisibility);
            PacketScoreboardTeamFieldResolver.resolve(new String[]{"f"}).set(packetScoreboardTeam, push);
            if (createNewTeam) {
                if (color == Color.NONE)
                    return;
                String cc = color.name();
                if (cc.equalsIgnoreCase("PURPLE"))
                    cc = "LIGHT_PURPLE";
                PacketScoreboardTeamFieldResolver.resolve(new String[]{"g"}).set(packetScoreboardTeam, Integer.valueOf(color.packetValue));
                PacketScoreboardTeamFieldResolver.resolve(new String[]{"c"}).set(packetScoreboardTeam, "§" + color.colorCode);
                PacketScoreboardTeamFieldResolver.resolve(new String[]{"b"}).set(packetScoreboardTeam, color.getTeamName());
                PacketScoreboardTeamFieldResolver.resolve(new String[]{"d"}).set(packetScoreboardTeam, "");
                PacketScoreboardTeamFieldResolver.resolve(new String[]{"j"}).set(packetScoreboardTeam, Integer.valueOf(0));
            }
            if (!createNewTeam) {
                Collection<String> collection = (Collection<String>) PacketScoreboardTeamFieldResolver.resolve(new String[]{"h"}).get(packetScoreboardTeam);
                if (entity instanceof OfflinePlayer) {
                    collection.add(entity.getName());
                } else {
                    collection.add(entity.getUniqueId().toString());
                }
            }
            sendPacket(packetScoreboardTeam, receiver);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void sendPacket(Object packet, Player p) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        if (EntityPlayerFieldResolver == null)
            EntityPlayerFieldResolver = new FieldResolver(NMS_CLASS_RESOLVER.resolve("EntityPlayer"));
        if (PlayerConnectionMethodResolver == null)
            PlayerConnectionMethodResolver = new MethodResolver(NMS_CLASS_RESOLVER.resolve("PlayerConnection"));
        try {
            Object handle = Minecraft.getHandle(p);
            Object connection = EntityPlayerFieldResolver.resolve(new String[]{"playerConnection"}).get(handle);
            PlayerConnectionMethodResolver.resolve(new String[]{"sendPacket"}).invoke(connection, packet);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entity getEntityById(World world, int entityId) {
        try {
            if (CraftWorldFieldResolver == null)
                CraftWorldFieldResolver = new FieldResolver(obcClassResolver.resolve("CraftWorld"));
            if (WorldFieldResolver == null)
                WorldFieldResolver = new FieldResolver(nmsClassResolver.resolve("World"));
            if (IntHashMapMethodResolver == null)
                IntHashMapMethodResolver = new MethodResolver(nmsClassResolver.resolve("IntHashMap"));
            if (EntityMethodResolver == null)
                EntityMethodResolver = new MethodResolver(nmsClassResolver.resolve("Entity"));
            Object entitiesById = WorldFieldResolver.resolve(new String[]{"entitiesById"}).get(CraftWorldFieldResolver.resolve(new String[]{"world"}).get(world));
            Object entity = IntHashMapMethodResolver.resolve(new ResolverQuery[]{new ResolverQuery("get", int.class)}).invoke(entitiesById, Integer.valueOf(entityId));
            if (entity == null)
                return null;
            return (Entity) EntityMethodResolver.resolve(new String[]{"getBukkitEntity"}).invoke(entity, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        APIManager.require(PacketListenerAPI.class, QuantumRPG.instance);
    }

    public void init(Plugin plugin) {
        APIManager.initAPI(PacketListenerAPI.class);
        APIManager.registerEvents(this, this);
        PacketHandler.addHandler(new PacketHandler((QuantumRPG.instance != null) ? QuantumRPG.instance : plugin) {
            @PacketOptions(forcePlayer = true)
            public void onSend(SentPacket sentPacket) {
                if ("PacketPlayOutEntityMetadata".equals(sentPacket.getPacketName())) {
                    int a = ((Integer) sentPacket.getPacketValue("a")).intValue();
                    if (a < 0) {
                        sentPacket.setPacketValue("a", Integer.valueOf(-a));
                        return;
                    }
                    List b = (List) sentPacket.getPacketValue("b");
                    if (b == null || b.isEmpty())
                        return;
                    Entity entity = GlowAPI.getEntityById(sentPacket.getPlayer().getWorld(), a);
                    if (entity != null)
                        if (GlowAPI.isGlowing(entity, sentPacket.getPlayer())) {
                            if (GlowAPI.DataWatcherItemMethodResolver == null)
                                GlowAPI.DataWatcherItemMethodResolver = new MethodResolver(GlowAPI.DataWatcherItem);
                            if (GlowAPI.DataWatcherItemFieldResolver == null)
                                GlowAPI.DataWatcherItemFieldResolver = new FieldResolver(GlowAPI.DataWatcherItem);
                            try {
                                for (Object prevItem : b) {
                                    Object prevObj = GlowAPI.DataWatcherItemMethodResolver.resolve(new String[]{"b"}).invoke(prevItem);
                                    if (prevObj instanceof Byte) {
                                        byte prev = ((Byte) prevObj).byteValue();
                                        byte bte = (byte) (prev | 0x40);
                                        GlowAPI.DataWatcherItemFieldResolver.resolve(new String[]{"b"}).set(prevItem, Byte.valueOf(bte));
                                    }
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                }
            }

            public void onReceive(ReceivedPacket receivedPacket) {
            }
        });
    }

    public void disable(Plugin plugin) {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        initTeam(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (isGlowing(event.getPlayer(), receiver))
                setGlowing(event.getPlayer(), null, receiver);
        }
    }

    public enum Color {
        BLACK(0, "0"),
        DARK_BLUE(1, "1"),
        DARK_GREEN(2, "2"),
        DARK_AQUA(3, "3"),
        DARK_RED(4, "4"),
        DARK_PURPLE(5, "5"),
        GOLD(6, "6"),
        GRAY(7, "7"),
        DARK_GRAY(8, "8"),
        BLUE(9, "9"),
        GREEN(10, "a"),
        AQUA(11, "b"),
        RED(12, "c"),
        PURPLE(13, "d"),
        YELLOW(14, "e"),
        WHITE(15, "f"),
        NONE(-1, "");

        int packetValue;

        String colorCode;

        Color(int packetValue, String colorCode) {
            this.packetValue = packetValue;
            this.colorCode = colorCode;
        }

        String getTeamName() {
            String name = String.format("GAPI#%s", name());
            if (name.length() > 16)
                name = name.substring(0, 16);
            return name;
        }
    }
}
