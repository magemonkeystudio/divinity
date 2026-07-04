package studio.magemonkey.divinity.nms.packets.versions;

import com.mojang.datafixers.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.api.events.EnginePlayerPacketEvent;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.hooks.Hooks;
import studio.magemonkey.codex.nms.packets.IPacketHandler;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.EntityEquipmentChangeEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.data.api.DivinityUser;
import studio.magemonkey.divinity.data.api.UserEntityNamesMode;
import studio.magemonkey.divinity.data.api.UserProfile;
import studio.magemonkey.divinity.manager.EntityManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 26.x scaffold using deobfuscated member names.
 * <p>
 * This is intentionally detached from the 1.21.7 chain so 26.x can be
 * implemented with Mojang-mapped names directly.
 */
public class V26_R1 extends UniversalPacketHandler implements IPacketHandler {
    protected static final String PACKET_LOCATION = "net.minecraft.network.protocol.game";
    protected static final String ENTITY_LOCATION = "net.minecraft.world.entity";
    protected static final String NMS_SERVER      = "net.minecraft.server";
    protected static final String NMS_LEVEL       = "net.minecraft.server.level";
    protected static final String NMS_CORE        = "net.minecraft.core";
    protected static final String NMS_PARTICLES   = "net.minecraft.core.particles";
    protected static final String NMS_RESOURCES   = "net.minecraft.resources";

    // Candidate 26.x packet class names (Mojang mapped):
    protected static final String CLIENTBOUND_PARTICLES_PACKET  = "ClientboundLevelParticlesPacket";
    protected static final String CLIENTBOUND_ATTRIBUTES_PACKET = "ClientboundUpdateAttributesPacket";
    protected static final String CLIENTBOUND_METADATA_PACKET   = "ClientboundSetEntityDataPacket";
    protected static final String CLIENTBOUND_EQUIPMENT_PACKET  = "ClientboundSetEquipmentPacket";

    // Candidate deobfuscated method names:
    protected static final String METHOD_GET_ALL_LEVELS = "getAllLevels";
    protected static final String METHOD_GET_ENTITY     = "getEntity";
    protected static final String METHOD_FROM_NAME      = "fromName";
    protected static final String METHOD_GET_TYPE       = "getType";

    // Candidate packet field names:
    protected static final String FIELD_COUNT = "count";

    protected final Class<?> craftServerClass   = Reflex.getCraftClass("CraftServer");
    protected final Class<?> dedicatedServerCls = Reflex.getClass(NMS_SERVER + ".dedicated.DedicatedServer");
    protected final Class<?> serverLevelClass   = Reflex.getClass(NMS_LEVEL, "ServerLevel");
    protected final Class<?> nmsEntityClass     = Reflex.getClass(ENTITY_LOCATION, "Entity");

    protected final Class<?> equipmentPacketClass  = Reflex.getClass(PACKET_LOCATION, CLIENTBOUND_EQUIPMENT_PACKET);
    protected final Class<?> attributesPacketClass = Reflex.getClass(PACKET_LOCATION, CLIENTBOUND_ATTRIBUTES_PACKET);
    protected final Class<?> metadataPacketClass   = Reflex.getClass(PACKET_LOCATION, CLIENTBOUND_METADATA_PACKET);
    protected final Class<?> particlesPacketClass  = Reflex.getClass(PACKET_LOCATION, CLIENTBOUND_PARTICLES_PACKET);

    public V26_R1(@NotNull Divinity plugin) {super(plugin);}

    @Override
    public void managePlayerPacket(@NotNull EnginePlayerPacketEvent event) {
        Object packet = event.getPacket();

        if (particlesPacketClass != null && EngineCfg.PACKETS_REDUCE_COMBAT_PARTICLES
                && particlesPacketClass.isInstance(packet)) {
            this.manageDamageParticle(event, packet);
            return;
        }

        if (attributesPacketClass != null && attributesPacketClass.isInstance(packet)) {
            this.manageEquipmentChanges(event, packet);
            return;
        }

        if (metadataPacketClass != null && metadataPacketClass.isInstance(packet)) {
            this.manageEntityNames(event, packet);
            return;
        }

        if (equipmentPacketClass != null && equipmentPacketClass.isInstance(packet)) {
            this.managePlayerHelmet(event, packet);
        }
    }

    @Override
    public void manageEquipmentChanges(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Integer entityId = (Integer) Reflex.invokeMethod(
                    Reflex.getMethod(packet.getClass(), "getEntityId"),
                    packet
            );
            if (entityId == null) return;

            Object      dedicatedServer = getDedicatedServer();
            Iterable<?> worlds          = getAllLevels(dedicatedServer);
            Object      nmsEntity       = null;
            Method      getEntity       = Reflex.getMethod(serverLevelClass, METHOD_GET_ENTITY, int.class);
            if (getEntity == null) return;
            for (Object level : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, level, entityId);
                if (nmsEntity != null) break;
            }

            if (nmsEntity == null) return;

            org.bukkit.entity.Entity bukkitEntity = (org.bukkit.entity.Entity) Reflex.invokeMethod(
                    Reflex.getMethod(nmsEntityClass, "getBukkitEntity"),
                    nmsEntity
            );
            if (!(bukkitEntity instanceof LivingEntity)) return;
            if (EntityManager.isPacketDuplicatorFixed(bukkitEntity)
                    || !EntityManager.isEquipmentNew((LivingEntity) bukkitEntity)) return;

            EntityEquipmentChangeEvent event = new EntityEquipmentChangeEvent((LivingEntity) bukkitEntity);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void manageEntityNames(@NotNull EnginePlayerPacketEvent event, @NotNull Object packet) {
        DivinityUser user = plugin.getUserManager().getOrLoadUser(event.getReciever());
        if (user == null) return;

        UserProfile         profile   = user.getActiveProfile();
        UserEntityNamesMode namesMode = profile.getNamesMode();
        if (namesMode == UserEntityNamesMode.DEFAULT) return;

        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) Reflex.invokeMethod(
                Reflex.getMethod(packet.getClass(), "packedItems"),
                packet
        );
        if (items == null || items.isEmpty()) return;

        if (items.size() > 3) {
            // Custom names is index 3: see https://c4k3.github.io/wiki.vg/Entity_metadata.html#Entity
            Object nameItem = items.get(3);
            Object value = Reflex.invokeMethod(
                    Reflex.getMethod(nameItem.getClass(), "value"),
                    nameItem
            );
            if (value == null) return;
            if (!value.getClass().equals(Boolean.class)) return;

            boolean visibility = namesMode == UserEntityNamesMode.ALWAYS_VISIBLE;
            Reflex.setFieldValue(nameItem, "value", visibility);
        }
    }

    @Override
    protected void managePlayerHelmet(@NotNull EnginePlayerPacketEvent event, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            @SuppressWarnings("unchecked")
            List<Pair<Object, Object>> slots = (List<Pair<Object, Object>>) Reflex.invokeMethod(
                    Reflex.getMethod(packet.getClass(), "getSlots"),
                    packet
            );
            if (slots == null || slots.isEmpty()) return;

            Class<?> equipmentSlotClass = Reflex.getClass(ENTITY_LOCATION, "EquipmentSlot");
            if (equipmentSlotClass == null) return;
            Object headSlot = Reflex.invokeMethod(
                    Reflex.getMethod(equipmentSlotClass, METHOD_FROM_NAME, String.class),
                    null,
                    "head"
            );
            if (headSlot == null) return;

            Pair<Object, Object> helmet = null;
            for (Pair<Object, Object> pair : slots) {
                if (pair.getFirst() == headSlot) {
                    helmet = pair;
                    break;
                }
            }
            if (helmet == null) return;

            Integer entityId = (Integer) Reflex.invokeMethod(
                    Reflex.getMethod(packet.getClass(), "getEntity"),
                    packet
            );
            if (entityId == null) return;

            Object      dedicatedServer = getDedicatedServer();
            Iterable<?> worlds          = getAllLevels(dedicatedServer);
            Object      nmsEntity       = null;
            Method      getEntity       = Reflex.getMethod(serverLevelClass, METHOD_GET_ENTITY, int.class);
            if (getEntity == null) return;
            for (Object level : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, level, entityId);
                if (nmsEntity != null) break;
            }

            if (nmsEntity == null) return;

            org.bukkit.entity.Entity bukkitEntity = (org.bukkit.entity.Entity) Reflex.invokeMethod(
                    Reflex.getMethod(nmsEntityClass, "getBukkitEntity"),
                    nmsEntity
            );
            if (bukkitEntity == null || Hooks.isNPC(bukkitEntity) || !(bukkitEntity instanceof Player)) return;

            Player       player = (Player) bukkitEntity;
            DivinityUser user   = plugin.getUserManager().getOrLoadUser(player);
            if (user == null) return;

            UserProfile profile = user.getActiveProfile();
            if (profile.isHideHelmet()) {
                slots.remove(helmet);
                slots.add(new Pair<>(helmet.getFirst(),
                        VersionManager.getNms()
                                .getNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))));
            }
        });
    }

    @Override
    protected void manageDamageParticle(@NotNull EnginePlayerPacketEvent event, @NotNull Object packet) {
        Object particle = Reflex.invokeMethod(
                Reflex.getMethod(packet.getClass(), "getParticle"),
                packet
        );
        if (particle == null) return;

        Class<?> particleOptionsClass = Reflex.getClass(NMS_PARTICLES, "ParticleOptions");
        Class<?> particleTypeClass    = Reflex.getClass(NMS_PARTICLES, "ParticleType");
        Class<?> registryClass        = Reflex.getClass(NMS_CORE, "Registry");
        Class<?> builtInRegistries    = Reflex.getClass(NMS_CORE + ".registries.BuiltInRegistries");
        Class<?> resourceKeyClass     = Reflex.getClass(NMS_RESOURCES, "ResourceLocation");
        if (particleOptionsClass == null
                || particleTypeClass == null
                || registryClass == null
                || builtInRegistries == null
                || resourceKeyClass == null) {
            return;
        }

        Object particleType = Reflex.invokeMethod(
                Reflex.getMethod(particleOptionsClass, METHOD_GET_TYPE),
                particle
        );
        Object particleTypeRegistry = Reflex.getFieldValue(builtInRegistries, "PARTICLE_TYPE");
        Object key = Reflex.invokeMethod(
                Reflex.getMethod(registryClass, "getKey", Object.class),
                particleTypeRegistry,
                particleType
        );
        String path = (String) Reflex.invokeMethod(
                Reflex.getMethod(resourceKeyClass, "getPath"),
                key
        );
        boolean isDamageParticle = Objects.equals(path, "damage_indicator");
        if (isDamageParticle) {
            Integer count = (Integer) Reflex.invokeMethod(
                    Reflex.getMethod(packet.getClass(), "getCount"),
                    packet
            );
            if (count == null) return;
            if (count > 20) {
                Reflex.setFieldValue(packet, FIELD_COUNT, 20);
            }
        }
    }

    private Object getDedicatedServer() {
        if (craftServerClass == null || dedicatedServerCls == null) {
            return null;
        }
        Object server = craftServerClass.cast(Bukkit.getServer());
        return Reflex.invokeMethod(
                Reflex.getMethod(craftServerClass, "getServer"),
                server
        );
    }

    private Iterable<?> getAllLevels(Object dedicatedServer) {
        if (dedicatedServer == null) {
            return List.of();
        }
        Method getAllLevels = Reflex.getMethod(dedicatedServerCls, METHOD_GET_ALL_LEVELS);
        Object result       = Reflex.invokeMethod(getAllLevels, dedicatedServer);
        return result instanceof Iterable<?> ? (Iterable<?>) result : List.of();
    }
}
