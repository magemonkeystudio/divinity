package com.promcteam.divinity.nms.packets.versions;

import com.mojang.datafixers.util.Pair;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.hooks.Hooks;
import com.promcteam.codex.nms.packets.events.EnginePlayerPacketEvent;
import com.promcteam.codex.utils.Reflex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.api.event.EntityEquipmentChangeEvent;
import com.promcteam.divinity.data.api.RPGUser;
import com.promcteam.divinity.data.api.UserProfile;
import com.promcteam.divinity.manager.EntityManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class V1_19_R1 extends V1_18_R2 {
    public V1_19_R1(@NotNull QuantumRPG plugin) {
        super(plugin);
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
                    Reflex.getMethod(dedicatedServer.getClass(), "E"), // Get worlds (getAllLevels)
                    dedicatedServer
            );

            Method getEntity = Reflex.getMethod(worldServerClass, "a", int.class);
            for (Object worldServer : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, worldServer, entityId.intValue());
                if (nmsEntity != null) {
                    break;
                }
            }

            if (nmsEntity == null) return;

            Entity bukkitEntity;
            Method getUniqueId;

            try {
                getUniqueId = Reflex.getMethod(nmsEntityClass, "cp");
                bukkitEntity = CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            } catch (ClassCastException ex) {
                getUniqueId = Reflex.getMethod(nmsEntityClass, "co");
                bukkitEntity = CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            }

            if (!(bukkitEntity instanceof LivingEntity)) return;
            if (EntityManager.isPacketDuplicatorFixed(bukkitEntity) || !EntityManager.isEquipmentNew((LivingEntity) bukkitEntity)) return;

            EntityEquipmentChangeEvent event = new EntityEquipmentChangeEvent((LivingEntity) bukkitEntity);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void managePlayerHelmet(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(QuantumRPG.getInstance(), () -> {
            Class playOutEntityEquipment = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutEntityEquipment");
            Class enumItemSlotClass      = Reflex.getClass("net.minecraft.world.entity", "EnumItemSlot");

            Object p = playOutEntityEquipment.cast(packet);

            @SuppressWarnings("unchecked")
            List<Pair<Object, Object>> slots = (List<Pair<Object, Object>>) Reflex.getFieldValue(p, "c");
            Pair<Object, Object> helmet = null;
            for (Pair<Object, Object> pair : slots) {
                Enum head = (Enum) Reflex.invokeMethod(
                        Reflex.getMethod(enumItemSlotClass, "a", String.class), //fromName
                        null, "head");
                if (pair.getFirst() == head) {
                    helmet = pair;
                    break;
                }
            }
            if (slots == null || helmet == null) return;

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
                    Reflex.getMethod(dedicatedServer.getClass(), "E"), //Get worlds (getAllLevels)
                    dedicatedServer
            );

            Method getEntity = Reflex.getMethod(worldServerClass, "a", int.class);
            for (Object worldServer : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, worldServer, entityId.intValue());
                if (nmsEntity != null) {
                    break;
                }
            }

            if (nmsEntity == null) return;


            Entity bukkitEntity;
            Method getUniqueId;

            try {
                getUniqueId = Reflex.getMethod(nmsEntityClass, "cp");
                bukkitEntity = CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            } catch (ClassCastException ex) {
                getUniqueId = Reflex.getMethod(nmsEntityClass, "co");
                bukkitEntity = CodexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
            }

            if (bukkitEntity == null || Hooks.isNPC(bukkitEntity) || !(bukkitEntity instanceof Player)) return;

            Player  player = (Player) bukkitEntity;
            RPGUser user   = plugin.getUserManager().getOrLoadUser(player);
            if (user == null) return;

            UserProfile profile = user.getActiveProfile();
            if (profile.isHideHelmet()) {
                ItemStack air = new ItemStack(Material.AIR);
                slots.remove(helmet);
                slots.add(new Pair<>(helmet.getFirst(), reflectionUtil.getNMSCopy(air)));
            }
        });
    }
}
