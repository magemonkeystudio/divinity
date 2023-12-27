package su.nightexpress.quantumrpg.nms.packets.versions;

import com.mojang.datafixers.util.Pair;
import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.nms.packets.events.EnginePlayerPacketEvent;
import mc.promcteam.engine.utils.Reflex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.EntityEquipmentChangeEvent;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.manager.EntityManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class V1_20_R2 extends V1_20_R1 {
    private Class  playoutUpdateAttributes = Reflex.getClass(PACKET_LOCATION, "PacketPlayOutUpdateAttributes");
    private Class  craftServerClass        = Reflex.getCraftClass("CraftServer");
    private Class  nmsEntityClass          = Reflex.getClass("net.minecraft.world.entity", "Entity");
    private Class  worldServerClass        = Reflex.getClass("net.minecraft.server.level", "WorldServer");
    private Method getEntity               = Reflex.getMethod(worldServerClass, "a", int.class);
    private Method getServer               = Reflex.getMethod(craftServerClass, "getServer");

    public V1_20_R2(@NotNull QuantumRPG plugin) {super(plugin);}

    @Override
    public void manageEquipmentChanges(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Bukkit.getScheduler().runTask(plugin, () -> {

            Object equip = playoutUpdateAttributes.cast(packet);

            Integer entityId = (Integer) Reflex.getFieldValue(equip, "a");
            if (entityId == null) return;

            Object server    = craftServerClass.cast(Bukkit.getServer());
            Object nmsEntity = null;

            Object dedicatedServer = Reflex.invokeMethod(
                    getServer,
                    server
            );

            Iterable<?> worlds = (Iterable<?>) Reflex.invokeMethod(
                    Reflex.getMethod(dedicatedServer.getClass(), "F"), // Get worlds (getAllLevels)
                    dedicatedServer
            );

            for (Object worldServer : worlds) {
                nmsEntity = Reflex.invokeMethod(getEntity, worldServer, entityId.intValue());
                if (nmsEntity != null) {
                    break;
                }
            }

            if (nmsEntity == null) return;

            Method getUniqueId = Reflex.getMethod(nmsEntityClass, "cv");
            Entity bukkitEntity =
                    NexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));

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
                    Reflex.getMethod(dedicatedServer.getClass(), "F"), //Get worlds (getAllLevels)
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


            Method getUniqueId = Reflex.getMethod(nmsEntityClass, "cv");
            Entity bukkitEntity =
                    NexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));

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
