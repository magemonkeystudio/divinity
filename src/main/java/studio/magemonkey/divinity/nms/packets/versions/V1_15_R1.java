package studio.magemonkey.divinity.nms.packets.versions;

import studio.magemonkey.divinity.Divinity;
import org.jetbrains.annotations.NotNull;

public class V1_15_R1 extends UniversalPacketHandler {

    public V1_15_R1(@NotNull Divinity plugin) {
        super(plugin);
    }

//	@Override
//	public void managePlayerPacket(@NotNull EnginePlayerPacketEvent e) {
//		Object packet = e.getPacket();
//
//		if (EngineCfg.PACKETS_REDUCE_COMBAT_PARTICLES && packet instanceof PacketPlayOutWorldParticles) {
//			this.manageDamageParticle(e, packet);
//		}
//
//		if (EngineCfg.PACKETS_MOD_GLOW_COLOR && packet instanceof PacketPlayOutSpawnEntity) {
//			this.manageCustomGlow(e, packet);
////			Object oId = Reflex.getFieldValue(packet, "b"); // Entity UUID
////			if (oId == null) return;
////
////			// Do a tick delay to let entity be spawned in the world before we can get it by UUID
////			new BukkitRunnable() {
////				@SuppressWarnings("unchecked")
////				public void run() {
////					UUID id = (UUID) oId;
////
////					// Get entity and check if it's a dropped item
////					Entity entity = plugin.getServer().getEntity(id);
////					if (!(entity instanceof org.bukkit.entity.Item)) return;
////
////					// Check if Glow setting is applicable to this item stack.
////					org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
////					ItemHintsManager hintManager = plugin.getModuleCache().getItemHintsManager();
////					if (hintManager == null || !hintManager.isGlow(item)) return;
////
////					// Get list of fake team entities to add our item into it
////					PacketPlayOutScoreboardTeam pTeam = new PacketPlayOutScoreboardTeam();
////					Object oEntities = Reflex.getFieldValue(pTeam, "h"); // List of team entities
////					if (oEntities == null) return;
////					Collection<String> entities = (Collection<String>) oEntities;
////					entities.add(id.toString());
////
////					// Set item custom hint via HintManager before apply glowing
////					//hintManager.setItemHint(item, 0);
////
////					// Get glowing color depends on hint color.
////					ChatColor cc = ChatColor.WHITE;
////					String name = ItemUT.getItemName(item.getItemStack());
////					if (name.length() > 2) {
////						String ss = String.valueOf(cc.getChar());
////						if (name.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
////							ss = name.substring(1, 2);
////						}
////						ChatColor c2 = ChatColor.getByChar(ss);
////						if (c2 != null && c2.isColor()) cc = c2;
////					}
////					EnumChatFormat ec = EnumChatFormat.valueOf(cc.name());
////
////					Player p = e.getReciever();
////
////					// Check if team for this color is already created
////					// Also Check team per player in case of logout
////					boolean newTeam = true;
////					Set<ChatColor> hash = PacketManager.COLOR_CACHE.get(p);
////					if (hash != null) {
////						if (hash.contains(cc)) {
////							newTeam = false;
////						}
////					}
////					else {
////						hash = new HashSet<>();
////					}
////					hash.add(cc);
////					PacketManager.COLOR_CACHE.put(p, hash);
////
////					// Set team name for each color
////					String teamId = "GLOW_" + ec.name();
////					if (teamId.length() > 16) teamId = teamId.substring(0, 16);
////
////					// Set team fields
////					Reflex.setFieldValue(pTeam, "i", newTeam ? 0 : 3); // 0 = new team, 3 = add entity, 4 = remove entity
////					Reflex.setFieldValue(pTeam, "a", teamId); // Internal team name
////
////					if (newTeam) {
////						Reflex.setFieldValue(pTeam, "g", ec); // Team color
////						Reflex.setFieldValue(pTeam, "b", new ChatComponentText(teamId)); // Team display name
////						Reflex.setFieldValue(pTeam, "c", new ChatComponentText("")); // Team prefix
////					}
////
////					// Send packet to a player
////					plugin.getPacketManager().sendPacket(e.getReciever(), pTeam);
////					// Activate colored glowing
////					entity.setGlowing(true);
////				}
////			}.runTask(plugin);
//			return;
//		}
//
//		if (packet instanceof PacketPlayOutUpdateAttributes) {
//			this.manageEquipmentChanges(e, packet);
////			PacketPlayOutUpdateAttributes equip = (PacketPlayOutUpdateAttributes) packet;
////
////			Integer entityId = (Integer) Reflex.getFieldValue(equip, "a");
////			if (entityId == null) return;
////
////			CraftServer server = (CraftServer) Bukkit.getServer();
////			net.minecraft.server.v1_15_R1.Entity nmsEntity = null;
////			for (WorldServer worldServer : server.getServer().getWorlds()) {
////				nmsEntity = worldServer.getEntity(entityId.intValue());
////				if (nmsEntity != null) {
////					break;
////				}
////			}
////
////			if (nmsEntity == null) return;
////
////			Entity bukkitEntity = plugin.getServer().getEntity(nmsEntity.getUniqueID());
////			if (!(bukkitEntity instanceof LivingEntity)) return;
////			if (EntityManager.isPacketDuplicatorFixed(bukkitEntity)) return;
////
////			plugin.getServer().getScheduler().runTask(plugin, () -> {
////				EntityEquipmentChangeEvent event = new EntityEquipmentChangeEvent((LivingEntity) bukkitEntity);
////				plugin.getServer().getPluginManager().callEvent(event);
////			});
////
//			return;
//		}
//
//		if (packet instanceof PacketPlayOutEntityMetadata) {
//			this.manageEntityNames(e, packet);
//			return;
//		}
//		if (packet instanceof PacketPlayOutEntityEquipment) {
//			this.managePlayerHelmet(e, packet);
//			return;
//		}
//	}

//	protected void manageEntityNames(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
//		RPGUser user = plugin.getUserManager().getOrLoadUser(e.getReciever());
//		if (user == null) return;
//
//		UserProfile profile = user.getActiveProfile();
//		UserEntityNamesMode namesMode = profile.getNamesMode();
//		if (namesMode == UserEntityNamesMode.DEFAULT) return;
//
//		PacketPlayOutEntityMetadata p = (PacketPlayOutEntityMetadata) packet;
//		@SuppressWarnings("unchecked")
//		List<Item<?>> list = (List<Item<?>>) Reflex.getFieldValue(p, "b");
//		if (list == null) return;
//
//		// Hide or show custom entity names
//		if (list.size() > 13) {
//			Item<?> index3 = list.get(13);
//			Object b = index3.b();
//			if (b == null || !b.getClass().equals(Boolean.class)) return;
//			//Object nameVisible = Reflex.getFieldValue(index3, "b");
//
//			boolean visibility = namesMode == UserEntityNamesMode.ALWAYS_VISIBLE;
//			Reflex.setFieldValue(index3, "b", visibility);
//		}
//	}

//	@Override
//	protected void managePlayerHelmet(@NotNull EnginePlayerPacketEvent e, @NotNull Object packet) {
//		PacketPlayOutEntityEquipment p = (PacketPlayOutEntityEquipment) packet;
//
//		EnumItemSlot slot = (EnumItemSlot) Reflex.getFieldValue(p, "b");
//		if (slot != EnumItemSlot.HEAD) return;
//
//		Integer entityId = (Integer) Reflex.getFieldValue(p, "a");
//		if (entityId == null) return;
//
//		CraftServer server = (CraftServer) Bukkit.getServer();
//		net.minecraft.server.v1_15_R1.Entity nmsEntity = null;
//		for (WorldServer worldServer : server.getServer().getWorlds()) {
//			nmsEntity = worldServer.getEntity(entityId.intValue());
//			if (nmsEntity != null) {
//				break;
//			}
//		}
//
//		if (nmsEntity == null) return;
//
//		Entity bukkitEntity = plugin.getServer().getEntity(nmsEntity.getUniqueID());
//		if (bukkitEntity == null || Hooks.isNPC(bukkitEntity) || !(bukkitEntity instanceof Player)) return;
//
//		Player player = (Player) bukkitEntity;
//		RPGUser user = plugin.getUserManager().getOrLoadUser(player);
//		if (user == null) return;
//
//		UserProfile profile = user.getActiveProfile();
//		if (profile.isHideHelmet()) {
//			Reflex.setFieldValue(p, "c", ItemStack.a);
//		}
//	}
//
//	@Override
//	public void manageServerPacket(@NotNull EngineServerPacketEvent e) {
//
//	}
}
