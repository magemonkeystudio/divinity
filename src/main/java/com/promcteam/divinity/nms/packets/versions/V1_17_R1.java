package com.promcteam.divinity.nms.packets.versions;

import com.mojang.datafixers.util.Pair;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.hooks.Hooks;
import com.promcteam.codex.nms.packets.IPacketHandler;
import com.promcteam.codex.nms.packets.events.EnginePlayerPacketEvent;
import com.promcteam.codex.nms.packets.events.EngineServerPacketEvent;
import com.promcteam.codex.utils.Reflex;
import com.promcteam.divinity.Divinity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.api.event.EntityEquipmentChangeEvent;
import com.promcteam.divinity.config.EngineCfg;
import com.promcteam.divinity.data.api.DivinityUser;
import com.promcteam.divinity.data.api.UserEntityNamesMode;
import com.promcteam.divinity.data.api.UserProfile;
import com.promcteam.divinity.manager.EntityManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class V1_17_R1 extends UniversalPacketHandler implements IPacketHandler {

    private static final String PACKET_LOCATION = "net.minecraft.network.protocol.game";

    public V1_17_R1(@NotNull Divinity plugin) {
        super(plugin);
    }

    @Override
    public void managePlayerPacket(@NotNull EnginePlayerPacketEvent e) {
        Class playoutParticles        = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutWorldParticles");
        Class playoutUpdateAttributes = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutUpdateAttributes");
        Class playoutEntityMetadata   = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutEntityMetadata");
        Class playOutEntityEquipment  = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutEntityEquipment");

        Object packet = e.getPacket();

        if (EngineCfg.PACKETS_REDUCE_COMBAT_PARTICLES && playoutParticles.isInstance(packet)) {
            this.manageDamageParticle(e, packet);
            return;
        }

        if (playoutUpdateAttributes.isInstance(packet)) {
            this.manageEquipmentChanges(e, packet);
            return;
        }
        if (playoutEntityMetadata.isInstance(packet)) {
            this.manageEntityNames(e, packet);
            return;
        }
        if (playOutEntityEquipment.isInstance(packet)) {
            this.managePlayerHelmet(e, packet);
        }
    }

    @Override
    public void manageServerPacket(@NotNull EngineServerPacketEvent e) {
    }

    @Override
    public void manageEquipmentChanges(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Class playoutUpdateAttributes = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutUpdateAttributes");
            Class craftServerClass        = Reflex.getCraftClass("CraftServer");
            Class nmsEntityClass          = Reflex.getClass("net.minecraft.world.entity", "Entity");
            Class worldServerClass        = Reflex.getClass("net.minecraft.server.level", "WorldServer");

            Object equip = playoutUpdateAttributes.cast(packet);

            Integer entityId = (Integer) Reflex.getFieldValue(equip, "a");
            if (entityId == null) return;

            Object server    = craftServerClass.cast(Bukkit.getServer());
            Object nmsEntity = null;

            Object dedicatedServer = Reflex.invokeMethod(
                    Reflex.getMethod(craftServerClass, "getServer"),
                    server
            );

            Iterable<?> worlds = (Iterable<?>) Reflex.invokeMethod(
                    Reflex.getMethod(dedicatedServer.getClass(), "getWorlds"),
                    dedicatedServer
            );

            Method getEntity = Reflex.getMethod(worldServerClass, "getEntity", int.class);
            for (Object worldServer : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, worldServer, entityId.intValue());
                if (nmsEntity != null) {
                    break;
                }
            }

            if (nmsEntity == null) return;


            Method getUniqueId = Reflex.getMethod(nmsEntityClass, "getUniqueID");

            Entity bukkitEntity =
                    CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            if (!(bukkitEntity instanceof LivingEntity)) return;
            if (EntityManager.isPacketDuplicatorFixed(bukkitEntity)
                    || !EntityManager.isEquipmentNew((LivingEntity) bukkitEntity)) return;

            EntityEquipmentChangeEvent event = new EntityEquipmentChangeEvent((LivingEntity) bukkitEntity);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void manageDamageParticle(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Class packetParticlesClass = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutWorldParticles");
        Class particleParamClass   = Reflex.getClass("net.minecraft.core.particles", "ParticleParam");

        Object p = packetParticlesClass.cast(packet);

        Object j = Reflex.getFieldValue(p, "j");
        if (j == null) return;

        Method a = Reflex.getMethod(particleParamClass, "a");

        String name = (String) Reflex.invokeMethod(a, j);
        if (name.contains("damage_indicator")) {
            Reflex.setFieldValue(p, "h", 20);
        }
    }

    @Override
    protected void manageEntityNames(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        DivinityUser user = plugin.getUserManager().getOrLoadUser(e.getReciever());
        if (user == null) return;

        UserProfile         profile   = user.getActiveProfile();
        UserEntityNamesMode namesMode = profile.getNamesMode();
        if (namesMode == UserEntityNamesMode.DEFAULT) return;

        Class pClass = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutEntityMetadata");

        Object p = pClass.cast(packet);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) Reflex.getFieldValue(p, "b");
        if (list == null) return;

        // Hide or show custom entity names
        if (list.size() > 13) {
            Object index3 = list.get(13);

            Method bMethod = Reflex.getMethod(index3.getClass(), "b");

            Object b = Reflex.invokeMethod(bMethod, index3);
            if (b == null || !b.getClass().equals(Boolean.class)) return;
            //Object nameVisible = Reflex.getFieldValue(index3, "b");

            boolean visibility = namesMode == UserEntityNamesMode.ALWAYS_VISIBLE;
            Reflex.setFieldValue(index3, "b", visibility);
        }
    }

    @Override
    protected void managePlayerHelmet(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(Divinity.getInstance(), () -> {
            Class playOutEntityEquipment = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutEntityEquipment");
            Class enumItemSlotClass      = Reflex.getClass("net.minecraft.world.entity", "EnumItemSlot");

            Object p = playOutEntityEquipment.cast(packet);

            @SuppressWarnings("unchecked")
            List<Pair<Object, Object>> slots = (List<Pair<Object, Object>>) Reflex.getFieldValue(p, "c");
            boolean contains = false;
            for (Pair<Object, Object> pair : slots) {
                Enum head = (Enum) Reflex.invokeMethod(
                        Reflex.getMethod(enumItemSlotClass, "fromName", String.class),
                        null, "head");
                if (pair.getFirst() == head) {
                    contains = true;
                    break;
                }
            }
            if (slots == null || !contains) return;

            Integer entityId = (Integer) Reflex.getFieldValue(p, "b");
            if (entityId == null) return;

            Class craftServerClass = Reflex.getCraftClass("CraftServer");
            Class nmsEntityClass   = Reflex.getClass("net.minecraft.world.entity", "Entity");
            Class worldServerClass = Reflex.getClass("net.minecraft.server.level", "WorldServer");

            Object server    = craftServerClass.cast(Bukkit.getServer());
            Object nmsEntity = null;
            Object dedicatedServer = Reflex.invokeMethod(
                    Reflex.getMethod(craftServerClass, "getServer"),
                    server
            );

            Iterable<?> worlds = (Iterable<?>) Reflex.invokeMethod(
                    Reflex.getMethod(dedicatedServer.getClass(), "getWorlds"),
                    dedicatedServer
            );

            Method getEntity = Reflex.getMethod(worldServerClass, "getEntity", int.class);
            for (Object worldServer : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, worldServer, entityId.intValue());
                if (nmsEntity != null) {
                    break;
                }
            }

            if (nmsEntity == null) return;


            Method getUniqueId = Reflex.getMethod(nmsEntityClass, "getUniqueID");

            Entity bukkitEntity =
                    CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            if (bukkitEntity == null || Hooks.isNPC(bukkitEntity) || !(bukkitEntity instanceof Player)) return;

            Player       player = (Player) bukkitEntity;
            DivinityUser user   = plugin.getUserManager().getOrLoadUser(player);
            if (user == null) return;

            UserProfile profile = user.getActiveProfile();
            if (profile.isHideHelmet()) {
                Reflex.setFieldValue(p,
                        "c",
                        Reflex.getFieldValue(Reflex.getClass("net.minecraft.world.item", "ItemStack"), "a"));
            }
        });
    }
}
