package su.nightexpress.quantumrpg.modules.list.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.ChatColor;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.api.QModule;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.fortify.FortifyManager;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.EntityStatsTask;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.utils.LoreUT;

public class SetManager extends QModule {

	private String formatElementActive;
	private String formatElementInactive;
	private List<String> formatLore;
	
	private Map<String, ItemSet> sets;
	
	private static final String SET_LORE_TAG = "qrpg_set_cache";

	public SetManager(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public String getId() {
		return EModule.SETS;
	}

	@Override
	@NotNull
	public String version() {
		return "1.62";
	}

	@Override
	public void setup() {
		String path = "format.";
		this.formatElementActive = StringUT.color(cfg.getString(path + "element.active", "%c%• %name% &a✓"));
		this.formatElementInactive = StringUT.color(cfg.getString(path + "element.inactive", "%c%• %name% &c✗"));
		this.formatLore = StringUT.color(cfg.getStringList(path + "lore"));
		
		this.sets = new HashMap<>();
		this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "items");
		
		for (JYML cfg : JYML.loadAll(this.getFullPath() + "items", true)) {
			try {
				ItemSet set = new ItemSet(plugin, cfg);
				this.sets.put(set.getId(), set);
			}
			catch (Exception ex) {
				error("Could not load Set: " + cfg.getFile().getName());
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void shutdown() {
		if (this.sets != null) {
			this.sets.clear();
			this.sets = null;
		}
	}

	public void addSetPotionEffects(@NotNull LivingEntity entity) {
		EntityStats stats = EntityStats.get(entity);
		
		for (Map.Entry<ItemSet, Integer> e : this.getEquippedSets(entity).entrySet()) {
			int have = e.getValue();
			if (have <= 0) continue;
			
			ItemSet set = e.getKey();
			for (SetElementBonus setBonus : set.getElementBonuses()) {
				int need = setBonus.getMinElementsAmount();
				if (have < need) continue;
				
				setBonus.getPotionEffects().forEach(eff -> stats.addPermaPotionEffect(eff));
			}
		}
	}
	
	@NotNull
	public Map<ItemSet, Integer> getEquippedSets(@NotNull LivingEntity entity) {
		Map<ItemSet, Integer> sets = new HashMap<>();
		
		Collection<ItemSet> sets2 = this.getSets();
		for (ItemStack item : EntityStats.get(entity).getEquipment()) {
			for (ItemSet set : sets2) {
				if (set.isValidElement(item)) {
					sets.merge(set, 1, Integer::sum);
				}
			}
		}
		
		return sets;
	}
	
	@NotNull
	public List<BonusMap> getActiveSetBonuses(@NotNull LivingEntity entity) {
		List<BonusMap> bonuses = new ArrayList<>();
			
		for (Map.Entry<ItemSet, Integer> e : this.getEquippedSets(entity).entrySet()) {
			int have = e.getValue();
			if (have <= 0) continue;
			
			ItemSet set = e.getKey();
			for (SetElementBonus setElement : set.getElementBonuses()) {
				int need = setElement.getMinElementsAmount();
				if (have < need) continue;
				
				bonuses.add(setElement.getBonusMap());
			}
		}
		return bonuses;
	}
	
	@NotNull
	private String getClearName(@NotNull ItemStack item) {
		String name = ItemUT.getItemName(item);
		
		RefineManager refineManager = plugin.getModuleCache().getRefineManager();
		if (refineManager != null) {
			name = refineManager.getNameWithoutLevel(item, name);
		}
		
		FortifyManager fortifyManager = plugin.getModuleCache().getFortifyManager();
		if (fortifyManager != null) {
			name = fortifyManager.getNameDeformatted(item, name);
		}
		
		return name.trim();
	}
	
	public boolean hasSet(@NotNull ItemStack item) {
		return this.getItemSet(item) != null;
	}
	
	@Nullable
	public ItemSet getItemSet(@NotNull ItemStack item) {
		for (ItemSet set : this.getSets()) {
			if (set.isValidElement(item)) {
				return set;
			}
		}
		return null;
	}
	
	private void updateSets(@NotNull Player player) {
		EntityStats entityStats = EntityStats.get(player);
		entityStats.updateAll();
		
		for (ItemStack item : entityStats.getEquipment()) {
			this.updateItemSet(item, player);
		}
		player.updateInventory();
	}
	
	public void updateItemSet(@NotNull ItemStack item, @Nullable Player player) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
		
		StringBuilder loreTag = new StringBuilder();
		String storedTag = ItemUT.getLoreTag(item, SET_LORE_TAG);
		String[] storedLines = storedTag != null ? storedTag.split(LoreUT.TAG_SPLITTER) : new String[] {};
		
		int pos = lore.indexOf(ItemTags.PLACEHOLDER_ITEM_SET);
		if (pos < 0) {
			
			// Delete old set lines
			if (storedLines.length > 0) {
				int firstText = -1;
				for (String stored : storedLines) {
					firstText++;
					if (!StringUT.colorOff(stored).isEmpty()) {
						break;
					}
				}
				
				int index = lore.indexOf(storedLines[firstText]) - firstText;
				
				if (index >= 0) {
					if (pos < 0) pos = index;
					for (int count = 0; count < storedLines.length; count++) {
						lore.remove(index);
					}
				}
			}
			if (pos < 0) return; // If no set info then exit
		}
		else lore.remove(pos);
		
		ItemSet set = this.getItemSet(item);
		if (set != null) {
			for (String lineFormat : this.formatLore) {
				if (lineFormat.contains("%elements%")) {
					for (SetElement setPart : set.getElements()) {
						String name = StringUT.colorOff(setPart.getName());
						String color = set.getColorInactive();
						String format = this.formatElementInactive;
						
						if (player != null && setPart.isEquipped(player)) {
							format = this.formatElementActive;
							color = set.getColorActive();
						}
						
						String name3 = format.replace("%c%", color).replace("%name%", name);
						pos = LoreUT.addToLore(lore, pos, name3);
						
						if (loreTag.length() > 0) loreTag.append(LoreUT.TAG_SPLITTER);
						loreTag.append(name3);
					}
				}
				else if (lineFormat.contains("%effects%")) {
					int partHave = (player != null) ? set.getEquippedElements(player) : 0;
					
					for (SetElementBonus setPartBonus : set.getElementBonuses()) {
						int partNeed = setPartBonus.getMinElementsAmount();
						
						String color = set.getColorInactive();
						if (partHave >= partNeed) {
							color = set.getColorActive();
						}
						
						List<String> loreBonus = setPartBonus.getLore();
						for (String lineBonus : loreBonus) {
							String lineX = lineBonus.replace("%c%", color);
							pos = LoreUT.addToLore(lore, pos, lineX);
							
							if (loreTag.length() > 0) loreTag.append(LoreUT.TAG_SPLITTER);
							loreTag.append(lineX);
						}
					}
				}
				else {
					String lineX = lineFormat.replace("%set%", set.getName());
					if (lineX.isEmpty()) lineX = ChatColor.GRAY + lineX;
					
					pos = LoreUT.addToLore(lore, pos, lineX);
					
					if (loreTag.length() > 0) loreTag.append(LoreUT.TAG_SPLITTER);
					loreTag.append(lineX);
				}
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		ItemUT.addLoreTag(item, SET_LORE_TAG, loreTag.toString());
	}
	
	@NotNull
	public Collection<ItemSet> getSets() {
		return this.sets.values();
	}
	
	@Nullable
	public ItemSet getSetById(@NotNull String id) {
		if (id.equalsIgnoreCase(QModuleDrop.RANDOM_ID)) {
			return Rnd.get(new ArrayList<>(this.getSets()));
		}
		return this.sets.get(id.toLowerCase());
	}
	
	@NotNull
	public List<String> getSetNames() {
		return new ArrayList<>(sets.keySet());
	}
	
	// -------------------------------------------------------------------- //
	// EVENTS

	@EventHandler(ignoreCancelled = true)
	public void onSetUpdateClick(InventoryClickEvent e) {
		if (e.getInventory().getType() != InventoryType.CRAFTING) return;
		if (e.getSlotType() == SlotType.CRAFTING) return;
		final Player p = (Player) e.getWhoClicked();
		if (p.getGameMode() == GameMode.CREATIVE) return;
		
		boolean update = false;
		
		ItemStack target = e.getCurrentItem();
		
		if (target != null && this.hasSet(target)) {
			this.updateItemSet(target, null);
			if (e.getSlotType() == SlotType.ARMOR || e.isShiftClick()) {
				update = true;
			}
		}
		
		if (e.getSlotType() == SlotType.ARMOR) {
			ItemStack cur = e.getCursor();
			if (cur != null && this.hasSet(cur)) {
				update = true;
			}
		}
		
		if (update) {
			this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.updateSets(p));
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSetUpdateHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE) return;
		
		ItemStack target = p.getInventory().getItem(e.getNewSlot());
		if (target != null && this.hasSet(target)) {
			this.updateItemSet(target, null);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSetUpdateSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();
		ItemStack stack = item.getItemStack();
		
		this.updateItemSet(stack, null);
		item.setItemStack(stack);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSetNameHackAnvil(PrepareAnvilEvent e) {
		ItemStack result = e.getResult();
		if (result == null) return;
		
		ItemStack before = e.getInventory().getItem(0);
		if (before == null) return;
		
		ItemMeta metaBefore = before.getItemMeta();
		ItemMeta metaResult = result.getItemMeta();
		
		String nameBefore = metaBefore == null ? "" : StringUT.colorOff(metaBefore.getDisplayName());
		String nameResult = metaResult == null ? "" : StringUT.colorOff(metaResult.getDisplayName());
		
		if (nameBefore.equalsIgnoreCase(nameResult)) return;
		
		if (this.hasSet(result) || this.hasSet(before)) {
			e.setResult(null);
		}
	}
	
	// -------------------------------------------------------------------- //
	// CLASSES
	
	public class ItemSet extends LoadableItem {
		
		private String name;
		private String prefix;
		private String suffix;
		private String colorHave;
		private String colorMiss;
		private Map<String, SetElement> elements;
		private TreeMap<Integer, SetElementBonus> elementBonus;
		
		public ItemSet(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
			super(plugin, cfg);
			
			this.name = StringUT.color(cfg.getString("name", id));
			this.prefix = StringUT.color(cfg.getString("prefix", ""));
			this.suffix = StringUT.color(cfg.getString("suffix", ""));
			this.colorHave = StringUT.color(cfg.getString("color.active", "&a"));
			this.colorMiss = StringUT.color(cfg.getString("color.inactive", "&8"));
			
			this.elements = new LinkedHashMap<>();
			for (String eId : cfg.getSection("elements")) {
				String path2 = "elements." + eId + ".";
				
				Set<Material> eMaterials = new HashSet<>();
				for (String matName : cfg.getStringList(path2 + "materials")) {
					Material eMat = Material.getMaterial(matName.toUpperCase());
					if (eMat != null) {
						eMaterials.add(eMat);
					}
				}
				if (eMaterials.isEmpty()) {
					error("Invalid material for '" + eId + "' element of '" + id + "' set!");
					continue;
				}
				
				String eRawName = cfg.getString(path2 + "name");
				if (eRawName == null) {
					error("No Set Element name provided in: " + cfg.getFile().getName());
					continue;
				}
				
				String eName = StringUT.oneSpace(StringUT.color(eRawName).replace("%suffix%", suffix).replace("%prefix%", prefix));
				
				SetElement setElement = new SetElement(eId, eMaterials, eName);
				this.elements.put(setElement.getId(), setElement);
			}
			
			this.elementBonus = new TreeMap<>();
			for (String sAmount : cfg.getSection("bonuses.by-elements-amount")) {
				int amount = StringUT.getInteger(sAmount, -1);
				if (amount < 1) continue;
				
				String path2 = "bonuses.by-elements-amount." + sAmount + ".";
				
				List<String> bonusLore = new ArrayList<>();
				for (String s3 : cfg.getStringList(path2 + "lore")) {
					bonusLore.add(StringUT.color(s3));
				}
				
				// Setup potion effects.
				// With good map = better performance.
				Map<PotionEffectType, Integer> potions = new HashMap<>();
				for (String potionName : cfg.getSection(path2 + "potion-effects")) {
					PotionEffectType pet = PotionEffectType.getByName(potionName.toUpperCase());
					if (pet == null) {
						error("Invalid potion effect " + potionName + " for set " + id);
						continue;
					}
					int level = cfg.getInt(path2 + "potion-effects." + potionName);
					if (level > 0) {
						potions.put(pet, level);
					}
				}
				
				BonusMap bonusMap = new BonusMap();
				bonusMap.loadStats(cfg, path2 + "item-stats");
				bonusMap.loadDamages(cfg, path2 + "damage-types");
				bonusMap.loadDefenses(cfg, path2 + "defense-types");
				
				SetElementBonus spe = new SetElementBonus(amount, bonusLore, potions, bonusMap);
				this.elementBonus.put(amount, spe);
			}
		}
		
		@NotNull
		public String getName() {
			return this.name;
		}
		
		@NotNull
		public String getPrefix() {
			return this.prefix;
		}
		
		@NotNull
		public String getSuffix() {
			return this.suffix;
		}
		
		@NotNull
		public String getColorActive() {
			return this.colorHave;
		}
		
		@NotNull
		public String getColorInactive() {
			return this.colorMiss;
		}
		
		@NotNull
		public Collection<SetElement> getElements() {
			return this.elements.values();
		}
		
		@Nullable
		public SetElement getElement(@NotNull String id) {
			return this.elements.get(id.toLowerCase());
		}
		
		@NotNull
		public Collection<SetElementBonus> getElementBonuses() {
			return this.elementBonus.values();
		}
		
		public boolean isValidElement(@NotNull ItemStack item) {
			for (SetElement element : this.getElements()) {
				if (element.isValidElement(item)) {
					return true;
				}
			}
			return false;
		}
		
		public int getEquippedElements(@NotNull LivingEntity entity) {
			return (int) this.getElements().stream()
					.filter(element -> element.isEquipped(entity)).count();
		}

		@Override
		protected void save(@NotNull JYML cfg) {
			
		}
	}
	
	public class SetElement {
		
		private String id;
		private Set<Material> materials;
		private String name;
		
		public SetElement(
				@NotNull String id,
				@NotNull Set<Material> materials,
				@NotNull String name
				) {
			this.id = id.toLowerCase();
			this.materials = materials;
			this.setName(name);
		}
		
		@NotNull
		public String getId() {
			return this.id;
		}
		
		@NotNull
		public Set<Material> getMaterials() {
			return this.materials;
		}
		
		@NotNull
		public String getName() {
			return this.name;
		}
		
		public void setName(@NotNull String name) {
			this.name = name.trim(); // Removes spaces from name
		}
		
		public boolean isEquipped(@NotNull LivingEntity p) {
			for (ItemStack item : EntityStats.get(p).getEquipment()) {
				if (this.isValidElement(item)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isValidElement(@NotNull ItemStack item) {
			if (!this.getMaterials().contains(item.getType())) return false;
			
			String itemName = StringUT.colorOff(getClearName(item));
			String elemName = StringUT.colorOff(this.getName());
			return elemName.equalsIgnoreCase(itemName);
		}
	}
	
	public class SetElementBonus {
		
		private int minAmount;
		private List<String> lore;
		private Set<PotionEffect> potions;
		private BonusMap bonusMap;
		
		public SetElementBonus(
				int amount,
				@NotNull List<String> lore,
				@NotNull Map<PotionEffectType, Integer> potions,
				@NotNull BonusMap bonusMap
				) {
			this.minAmount = amount;
			this.lore = lore;
			this.potions = new HashSet<>();
			for (Map.Entry<PotionEffectType, Integer> e : potions.entrySet()) {
				PotionEffectType type = e.getKey();
				int dur = EntityStatsTask.POTION_DURATION;
				if (type.equals(PotionEffectType.NIGHT_VISION) || type.getName().equalsIgnoreCase(PotionEffectType.NIGHT_VISION.getName())) {
					dur *= 5;
				}
				
				PotionEffect pe = new PotionEffect(type, dur, Math.max(0, e.getValue() - 1));
				this.potions.add(pe);
			}
			
			this.bonusMap = bonusMap;
		}
		
		public int getMinElementsAmount() {
			return this.minAmount;
		}
		
		@NotNull
		public List<String> getLore() {
			return this.lore;
		}
		
		@NotNull
		public Set<PotionEffect> getPotionEffects() {
			return this.potions;
		}
		
		@NotNull
		public BonusMap getBonusMap() {
			return this.bonusMap;
		}
	}
}
