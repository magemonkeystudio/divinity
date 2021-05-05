package su.nightexpress.quantumrpg.nms.packets.versions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.nms.packets.IPacketHandler;
import su.nexmedia.engine.nms.packets.events.EnginePlayerPacketEvent;
import su.nexmedia.engine.nms.packets.events.EngineServerPacketEvent;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.EntityEquipmentChangeEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.data.api.UserEntityNamesMode;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.manager.EntityManager;
import su.nightexpress.quantumrpg.modules.list.itemhints.ItemHintsManager;
import su.nightexpress.quantumrpg.nms.packets.PacketManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class UniversalPacketHandler implements IPacketHandler {

    protected QuantumRPG plugin;

    public UniversalPacketHandler(@NotNull QuantumRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public void managePlayerPacket(@NotNull EnginePlayerPacketEvent e) {
        Class playoutParticles = Reflex.getNMSClass("PacketPlayOutWorldParticles");
        Class playoutSpawnEntity = Reflex.getNMSClass("PacketPlayOutSpawnEntity");
        Class playoutUpdateAttributes = Reflex.getNMSClass("PacketPlayOutUpdateAttributes");
        Class playoutEntityMetadata = Reflex.getNMSClass("PacketPlayOutEntityMetadata");
        Class playOutEntityEquipment = Reflex.getNMSClass("PacketPlayOutEntityEquipment");

        Object packet = e.getPacket();

        if (EngineCfg.PACKETS_REDUCE_COMBAT_PARTICLES && playoutParticles.isInstance(packet)) {
            this.manageDamageParticle(e, packet);
            return;
        }
        if (EngineCfg.PACKETS_MOD_GLOW_COLOR && playoutSpawnEntity.isInstance(packet)) {
            this.manageCustomGlow(e, packet);
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
            return;
        }
    }

    public void manageEquipmentChanges(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Class playoutUpdateAttributes = Reflex.getNMSClass("PacketPlayOutUpdateAttributes");
        Class craftServerClass = Reflex.getCraftClass("CraftServer");
        Class nmsEntityClass = Reflex.getNMSClass("Entity");
        Class worldServerClass = Reflex.getNMSClass("WorldServer");

        Object equip = playoutUpdateAttributes.cast(packet);

        Integer entityId = (Integer) Reflex.getFieldValue(equip, "a");
        if (entityId == null) return;

        Object server = craftServerClass.cast(Bukkit.getServer());
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

        Entity bukkitEntity = NexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
        if (!(bukkitEntity instanceof LivingEntity)) return;
        if (EntityManager.isPacketDuplicatorFixed(bukkitEntity)) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            EntityEquipmentChangeEvent event = new EntityEquipmentChangeEvent((LivingEntity) bukkitEntity);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    protected void manageDamageParticle(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Class packetParticlesClass = Reflex.getNMSClass("PacketPlayOutWorldParticles");
        Class particleParamClass = Reflex.getNMSClass("ParticleParam");

        Object p = packetParticlesClass.cast(packet);

        Object j = Reflex.getFieldValue(p, "j");
        if (j == null) return;

        Method a = Reflex.getMethod(particleParamClass, "a");

        String name = (String) Reflex.invokeMethod(a, j);
        if (name.contains("damage_indicator")) {
            Reflex.setFieldValue(p, "h", 20);
        }
    }

    protected void manageCustomGlow(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Object oId = Reflex.getFieldValue(packet, "b"); // Entity UUID
        if (oId == null) return;

        // Do a tick delay to let entity be spawned in the world before we can get it by UUID
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            UUID id = (UUID) oId;

            // Get entity and check if it's a dropped item
            Entity entity = plugin.getServer().getEntity(id);
            if (!(entity instanceof org.bukkit.entity.Item)) return;

            // Check if Glow setting is applicable to this item stack.
            Item item = (Item) entity;
            ItemHintsManager hintManager = plugin.getModuleCache().getItemHintsManager();
            if (hintManager == null || !hintManager.isGlow(item)) return;

            // Get list of fake team entities to add our item into it
            Object pTeam = Reflex.invokeConstructor(
                    Reflex.getConstructor(Reflex.getNMSClass("PacketPlayOutScoreboardTeam")));
            Object oEntities = Reflex.getFieldValue(pTeam, "h"); // List of team entities
            if (oEntities == null) return;

            @SuppressWarnings("unchecked")
            Collection<String> entities = (Collection<String>) oEntities;
            entities.add(id.toString());

            // Set item custom hint via HintManager before apply glowing
            //hintManager.setItemHint(item, 0);

            // Get glowing color depends on hint color.
            ChatColor cc = ChatColor.WHITE;
            String name = ItemUT.getItemName(item.getItemStack());
            if (name.length() > 2) {
                String ss = String.valueOf(cc.getChar());
                if (name.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                    ss = name.substring(1, 2);
                }
                ChatColor c2 = ChatColor.getByChar(ss);
                if (c2 != null && c2.isColor()) cc = c2;
            }
            Enum ec = Reflex.getEnum(Reflex.getNMSClass("EnumChatFormat"), cc.name());
//            EnumChatFormat ec = EnumChatFormat.valueOf(cc.name());

            Player p = e.getReciever();

            // Check if team for this color is already created
            // Also Check team per player in case of logout
            boolean newTeam = true;
            Set<ChatColor> hash = PacketManager.COLOR_CACHE.get(p);
            if (hash != null) {
                if (hash.contains(cc)) {
                    newTeam = false;
                }
            } else {
                hash = new HashSet<>();
            }
            hash.add(cc);
            PacketManager.COLOR_CACHE.put(p, hash);

            // Set team name for each color
            String teamId = "GLOW_" + ec.name();
            if (teamId.length() > 16) teamId = teamId.substring(0, 16);

            // Set team fields
            Reflex.setFieldValue(pTeam, "i", newTeam ? 0 : 3); // 0 = new team, 3 = add entity, 4 = remove entity
            Reflex.setFieldValue(pTeam, "a", teamId); // Internal team name


            Class chatComponentClass = Reflex.getNMSClass("ChatComponentText");
            Constructor ctor = Reflex.getConstructor(chatComponentClass, String.class);
            if (newTeam) {
                Reflex.setFieldValue(pTeam, "g", ec); // Team color
                Reflex.setFieldValue(pTeam, "b", Reflex.invokeConstructor(ctor, teamId)); // Team display name
                Reflex.setFieldValue(pTeam, "c", Reflex.invokeConstructor(ctor, "")); // Team prefix
            }

            // Send packet to a player
            plugin.getPacketManager().sendPacket(e.getReciever(), pTeam);
            // Activate colored glowing
            entity.setGlowing(true);
        });
    }

    protected void manageEntityNames(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        RPGUser user = plugin.getUserManager().getOrLoadUser(e.getReciever());
        if (user == null) return;

        UserProfile profile = user.getActiveProfile();
        UserEntityNamesMode namesMode = profile.getNamesMode();
        if (namesMode == UserEntityNamesMode.DEFAULT) return;

        Class pClass = Reflex.getNMSClass("PacketPlayOutentityMetadata");

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

    protected void managePlayerHelmet(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
        Class playOutEntityEquipment = Reflex.getNMSClass("PacketPlayOutEntityEquipment");
        Class enumItemSlotClass = Reflex.getNMSClass("EnumItemSlot");

        Object p = playOutEntityEquipment.cast(packet);

        @SuppressWarnings("unchecked")
        List<Object> slots = (List<Object>) Reflex.getFieldValue(p, "b");
        if (slots == null || !slots.contains(Reflex.getEnum(enumItemSlotClass, "HEAD"))) return;

        Integer entityId = (Integer) Reflex.getFieldValue(p, "a");
        if (entityId == null) return;

        Class craftServerClass = Reflex.getCraftClass("CraftServer");
        Class nmsEntityClass = Reflex.getNMSClass("Entity");
        Class worldServerClass = Reflex.getNMSClass("WorldServer");

        Object server = craftServerClass.cast(Bukkit.getServer());
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

        Entity bukkitEntity = NexEngine.get().getServer().getEntity((UUID) Reflex.invokeMethod(getUniqueId, nmsEntity));
        if (bukkitEntity == null || Hooks.isNPC(bukkitEntity) || !(bukkitEntity instanceof Player)) return;

        Player player = (Player) bukkitEntity;
        RPGUser user = plugin.getUserManager().getOrLoadUser(player);
        if (user == null) return;

        UserProfile profile = user.getActiveProfile();
        if (profile.isHideHelmet()) {
            Reflex.setFieldValue(p, "c", Reflex.getFieldValue(Reflex.getNMSClass("ItemStack"), "a"));
        }
    }

    @Override
    public void manageServerPacket(@NotNull EngineServerPacketEvent e) {
    }
}
