package su.nightexpress.quantumrpg.modules.list.repair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.citizensnpcs.api.trait.TraitInfo;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.manager.types.ClickType;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LimitedItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.repair.RepairManager.RepairItem;
import su.nightexpress.quantumrpg.modules.list.repair.command.RepairOpenCmd;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;

public class RepairManager extends QModuleDrop<RepairItem> {

	private AnvilGUI gui;
	
	private ClickType anvilUseAction;
	
	private boolean materialsIgnoreMeta;
	private Map<String, MaterialGroup> materialGroups;
	private Map<String, MaterialTable> materialTables;
	
	ActionManipulator actionsComplete;
	ActionManipulator actionsError;
	
	private VaultHK vaultHook;
	DurabilityStat duraStat;
	
	public RepairManager(@NotNull QuantumRPG plugin) {
		super(plugin, RepairItem.class);
	}
	
	@Override
	@NotNull
	public String getId() {
		return EModule.REPAIR;
	}

	@Override
	@NotNull
	public String version() {
		return "1.7.0";
	}

	@Override
	public void setup() {
		this.duraStat = ItemStats.getStat(DurabilityStat.class);
		if (this.duraStat == null) {
			this.interruptLoad();
			this.error("Durability Stat is not registered. Module will be disabled.");
			return;
		}
		this.vaultHook = plugin.getVault();
		
		this.actionsComplete = new ActionManipulator(plugin, cfg, "repair.actions-complete");
		this.actionsError = new ActionManipulator(plugin, cfg, "repair.actions-error");
		
		String path = "anvil.";
		if (cfg.getBoolean(path + "enabled", true)) {
			try {
				this.anvilUseAction = ClickType.valueOf(cfg.getString(path + "open-action", "SHIFT_RIGHT"));
			}
			catch (IllegalArgumentException ex) {
				this.anvilUseAction = ClickType.SHIFT_RIGHT;
			}
		}
		
		for (RepairType repairType : RepairType.values()) {
			if (!cfg.getBoolean("repair.types." + repairType.name() + ".enabled")) {
				repairType.setEnabled(false);
				continue;
			}
			if (repairType == RepairType.VAULT) {
				if (this.vaultHook == null || this.vaultHook.getEconomy() == null) continue;
			}
			
			double typeCost = cfg.getDouble("repair.types." + repairType.name() + ".cost-per-unit", 1);
			repairType.setEnabled(true);
			repairType.setCost(typeCost);
		}
		
		if (RepairType.MATERIAL.isEnabled()) {
			path = "repair.materials.";
			this.materialsIgnoreMeta = cfg.getBoolean(path + "ignore-items-with-meta");
			this.materialGroups = new HashMap<>();
			for (String id : cfg.getSection(path + "materials-group")) {
				String path2 = path + "materials-group." + id;
				Set<Material> mats = new HashSet<>();
				for (String matName : cfg.getStringList(path2)) {
					Material material = Material.getMaterial(matName.toUpperCase());
					if (material == null) {
						error("Invalid material '" + matName + "' in material group '" + id + "' !");
						continue;
					}
					mats.add(material);
				}
				MaterialGroup mg = new MaterialGroup(id, mats);
				this.materialGroups.put(mg.getId(), mg);
			}
			
			this.materialTables = new HashMap<>();
			for (String id : cfg.getSection(path + "materials-table")) {
				String path2 = path + "materials-table." + id + ".";
				if (!materialGroups.containsKey(id.toLowerCase())) {
					error("Invalid material group '" + id + "' in materials table!");
					continue;
				}
				
				String name = StringUT.color(cfg.getString(path2 + "name", id));
				Set<String> mats = new HashSet<>(cfg.getStringList(path2 + "can-repair"));
				MaterialTable mt = new MaterialTable(id, name, mats);
				this.materialTables.put(mt.getId(), mt);
			}
		}
		
		this.gui = new AnvilGUI(this);
		this.moduleCommand.addSubCommand(new RepairOpenCmd(this));
		
		CitizensHK citi = plugin.getCitizens();
		if (citi != null) {
			TraitInfo trait = TraitInfo.create(RepairTrait.class).withName("repair");
			citi.registerTrait(plugin, trait);
		}
	}

	@Override
	public void shutdown() {
		if (this.gui != null) {
			this.gui.shutdown();
			this.gui = null;
		}
		if (this.materialGroups != null) {
			this.materialGroups.clear();
			this.materialGroups = null;
		}
		if (this.materialTables != null) {
			this.materialTables.clear();
			this.materialTables = null;
		}
		this.actionsComplete = null;
		this.actionsError = null;
	}
	
	// -------------------------------------------------------------------- //
	// METHODS
	
	public boolean openAnvilGUI(
			@NotNull Player player, 
			@Nullable ItemStack target, 
			@Nullable ItemStack src,
			@Nullable RepairType type,
			boolean isForce) {
		
		if (!isForce && !player.hasPermission(Perms.REPAIR_GUI)) {
			plugin.lang().Error_NoPerm.send(player);
			return false;
		}
		
		if (target != null && target.getType() != Material.AIR) {
			if (!ItemStats.hasStat(target, AbstractStat.Type.DURABILITY)) {
				plugin.lang().Repair_Error_NoDurability
					.replace("%item%", ItemUT.getItemName(target))
					.send(player);
				return false;
			}
			if (!this.duraStat.isDamaged(target)) {
				plugin.lang().Repair_Error_NotDamaged
					.replace("%item%", ItemUT.getItemName(target))
					.send(player);
				return false;
			}
			
			this.splitDragItem(player, null, target);
		}
		
		this.gui.open(player, target, src, type);
		return true;
	}
	
	@NotNull
	ItemStack getResult(@NotNull ItemStack target, @NotNull Player player) {
		double[] arr = duraStat.getRaw(target);
		if (arr == null) return target;
		
		double max = arr[1];
		ItemStack result = new ItemStack(target);
		this.duraStat.add(result, new double[] {max, max}, -1);
		
		return result;
	}
	
	@Nullable
	private MaterialGroup getMaterialGroup(@NotNull ItemStack item) {
		String itemType = item.getType().name();
		
		for (MaterialTable matTable : this.getMaterialTables().values()) {
			Set<String> tableMaterials = matTable.getMaterials();
			for (String tableMaterial : tableMaterials) {
				if (tableMaterial.endsWith(JStrings.MASK_ANY)) {
					String materialPrefix = tableMaterial.replace(JStrings.MASK_ANY, "").toUpperCase();
					if (itemType.startsWith(materialPrefix)) {
						return this.getMaterialGroup(matTable.getId());
					}
				}
				else if (tableMaterial.startsWith(JStrings.MASK_ANY)) {
					String materialPrefix = tableMaterial.replace(JStrings.MASK_ANY, "").toUpperCase();
					if (itemType.endsWith(materialPrefix)) {
						return this.getMaterialGroup(matTable.getId());
					}
				}
				else {
					if (itemType.equalsIgnoreCase(tableMaterial.toUpperCase())) {
						return this.getMaterialGroup(matTable.getId());
					}
				}
			}
		}
		return null;
	}
	
	@NotNull
	public String getMaterialName(@NotNull ItemStack item) {
		if (!RepairType.MATERIAL.isEnabled()) return plugin.lang().getEnum(item.getType());
		
		MaterialGroup materialGroup = this.getMaterialGroup(item);
		if (materialGroup != null) {
			MaterialTable materialTable = this.getMaterialTable(materialGroup.getId());
			if (materialTable != null) {
				return materialTable.getName();
			}
		}
		return ItemUT.getItemName(item);
	}
	
	private int getPlayerMaterials(@NotNull Player player, @NotNull MaterialGroup group) {
		int have = 0;
		
		Set<Material> mats = group.getMaterials();
		for (ItemStack item : player.getInventory().getContents()) {
			if (ItemUT.isAir(item)) continue;
			if (this.isMaterialMetaIgnored() && item.hasItemMeta()) continue;
			if (mats.contains(item.getType())) {
				have += item.getAmount();
			}
		}
		return have;
	}
	
	private void takeMaterials(@NotNull Player player, @NotNull MaterialGroup group, int cost) {
		Set<Material> mats = group.getMaterials();
		
		for (ItemStack item : player.getInventory().getContents()) {
			if (ItemUT.isAir(item)) continue;
			if (this.isMaterialMetaIgnored() && item.hasItemMeta()) continue;
			if (mats.contains(item.getType())) {
				int a = item.getAmount();
				if (a > cost) {
					item.setAmount(a - cost);
					return;
				}
				
				item.setAmount(0);
				cost = cost - a;
				if (cost <= 0) {
					return;
				}
			}
		}
	}
	
	public int calcCost(@NotNull ItemStack item, @Nullable RepairType type) {
		int cost = 0;
		if (type == null) return cost;
		
		double[] arr = this.duraStat.getRaw(item);
		if (arr == null) return cost;
		
		double d1 = arr[0];
		double d2 = arr[1];
		double value = type.getCostPerUnit();
		double get = value * (d2 - d1);
		cost = (int) get;
		
		return Math.max(1, cost);
	}
	
	boolean payForRepair(@NotNull Player player, @NotNull RepairType type, @NotNull ItemStack item) {
		int pay = this.calcCost(item, type);
		
		if (this.getPlayerBalance(player, type, item) < pay) {
			return false;
		}
		
		if (type == RepairType.EXP) {
			PlayerUT.setExp(player, -pay);
		}
		else if (type == RepairType.MATERIAL) {
			MaterialGroup mg = this.getMaterialGroup(item);
			if (mg == null) return false;
			
			this.takeMaterials(player, mg, pay);
		}
		else if (type == RepairType.VAULT) {
			this.vaultHook.take(player, pay);
		}
		
		return true;
	}
	
	public int getPlayerBalance(@NotNull Player player, @Nullable RepairType type, @NotNull ItemStack item) {
		if (type == null || !type.isEnabled()) return 0;
		
		if (type == RepairType.EXP) {
			return PlayerUT.getTotalExperience(player);
		}
		else if (type == RepairType.MATERIAL) {
			MaterialGroup mg = this.getMaterialGroup(item);
			if (mg != null) {
				return this.getPlayerMaterials(player, mg);
			}
		}
		else if (type == RepairType.VAULT) {
			return (int) this.vaultHook.getBalance(player);
		}
		
		return 0;
	}

	@Override
	protected boolean onDragDrop(
			@NotNull Player player, 
			@NotNull ItemStack src, 
			@NotNull ItemStack target, 
			@NotNull RepairItem rTool, 
			@NotNull InventoryClickEvent e) {
		
		double arr[] = this.duraStat.getRaw(target);
		if (arr == null || !ItemStats.hasStat(target, AbstractStat.Type.DURABILITY)) return false;
		
		if (!this.duraStat.isDamaged(target)) {
			plugin.lang().Repair_Error_NotDamaged.replace("%item%", ItemUT.getItemName(target))
			.send(player);
			return false;
		}
		
		this.takeChargeClickEvent(player, src, e);
		
		// Save other items in stack
		// and then return them back to a player
		ItemStack lost = null;
		if (target.getAmount() > 1) {
			lost = new ItemStack(target);
			lost.setAmount(target.getAmount() - 1);
			target.setAmount(1);
		}
		
		int rLvl = ItemStats.getLevel(src);
		int rPerc = rTool.getRepairPercent(rLvl);
		
		double durMax = arr[1];
		double durNow = arr[0];
		
		durNow = (int) Math.min(durMax, durNow + durMax * 1D * (rPerc * 1D / 100D));
		
		this.duraStat.add(target, new double[] {durNow, durMax}, -1);
		e.setCurrentItem(target);
		
		if (lost != null) {
			ItemUT.addItem(player, lost);
		}
		
		return true;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onRepairAnvilUse(PlayerInteractEvent e) {
		if (this.anvilUseAction == null) return;
		if (e.useItemInHand() == Result.DENY) return;
		if (e.useInteractedBlock() == Result.DENY) return;
		
		Block block = e.getClickedBlock();
		if (block == null || block.getType() != Material.ANVIL) return;
		
		Action action = e.getAction();
		final Player player = e.getPlayer();
		ClickType type = ClickType.from(action, player.isSneaking());
		
		if (type == this.anvilUseAction) {
			final ItemStack i = player.getInventory().getItemInMainHand();
			e.setUseInteractedBlock(Result.DENY);
			e.setUseItemInHand(Result.DENY);
			e.setCancelled(true);

			if (e.getHand() == EquipmentSlot.OFF_HAND) return;
			this.openAnvilGUI(player, i, new ItemStack(Material.ANVIL), null, false);
		}
	}
	
	// -------------------------------------------------------------------- //
	// CLASSES
	
	public class MaterialGroup {
		
		private String id;
		private Set<Material> materials;
		
		public MaterialGroup(@NotNull String id, @NotNull Set<Material> mats) {
			this.id = id.toLowerCase();
			this.materials = mats;
		}
		
		@NotNull
		public String getId() {
			return this.id;
		}
		
		@NotNull
		public Set<Material> getMaterials() {
			return this.materials;
		}
	}
	
	public class MaterialTable {
		
		private String id;
		private String name;
		private Set<String> materials;
		
		public MaterialTable(
				@NotNull String id,
				@NotNull String name,
				@NotNull Set<String> mats
				) {
			this.id = id.toLowerCase();
			this.name = name;
			this.materials = mats;
		}
		
		@NotNull
		public String getId() {
			return this.id;
		}
		
		@NotNull
		public String getName() {
			return this.name;
		}
		
		@NotNull
		public Set<String> getMaterials() {
			return this.materials;
		}
	}
	
	public class RepairItem extends LimitedItem {
		
		private TreeMap<Integer, Integer> repairLvl;
		
		public RepairItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
			super(plugin, cfg, RepairManager.this);
			
			this.repairLvl = new TreeMap<>();
			for (String sLvl : cfg.getSection("repair-by-level")) {
				int lvl = StringUT.getInteger(sLvl, -1);
				if (lvl < 1) continue;
				
				int amount = cfg.getInt("repair-by-level." + sLvl);
				this.repairLvl.put(lvl, amount);
			}
		}
		
		@Override @NotNull
		protected ItemStack build(int lvl, int uses) {
			ItemStack item = super.build(lvl, uses);
			if (item.getType() == Material.AIR) return item;
			
			String strRepair = String.valueOf(this.getRepairPercent(lvl));
			
			ItemMeta meta = item.getItemMeta();
			if (meta == null) return item;
			
			if (meta.hasDisplayName()) {
				meta.setDisplayName(meta.getDisplayName().replace("%repair%", strRepair));
			}
			
			List<String> lore = meta.getLore();
			if (lore != null) {
				for (int i = 0; i < lore.size(); i++) {
					lore.set(i, lore.get(i).replace("%repair%", strRepair));
				}
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
			
			return item;
		}
		
		/**
		 * @param toolLvl
		 * @return Returns amount (in percent) to repair. Returns -1 if nothing found.
		 */
		public int getRepairPercent(int toolLvl) {
			Map.Entry<Integer, Integer> e = this.repairLvl.floorEntry(toolLvl);
			if (e == null) return 0;
			
			return e.getValue();
		}
	}
	
	public boolean isMaterialMetaIgnored() {
		return this.materialsIgnoreMeta;
	}
	
	@NotNull
	public Map<String, MaterialGroup> getMaterialGroups() {
		return this.materialGroups;
	}
	
	@NotNull
	public Map<String, MaterialTable> getMaterialTables() {
		return this.materialTables;
	}
	
	@Nullable
	public MaterialGroup getMaterialGroup(@NotNull String id) {
		return this.materialGroups.get(id.toLowerCase());
	}
	
	@Nullable
	public MaterialTable getMaterialTable(@NotNull String id) {
		return this.materialTables.get(id.toLowerCase());
	}
	
	public enum RepairType {
		EXP,
		MATERIAL,
		VAULT,
		;
		
		private boolean enabled = false;
		private double cost = 0D;
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public boolean isEnabled() {
			return this.enabled;
		}
		
		public void setCost(double cost) {
			this.cost = cost;
		}
		
		public double getCostPerUnit() {
			return this.cost;
		}
	}
}
