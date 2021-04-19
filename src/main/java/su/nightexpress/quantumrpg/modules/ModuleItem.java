package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.engine.utils.eval.Evaluator;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.identify.IdentifyManager.UnidentifiedItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.item.ItemLevelRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.item.ItemModuleRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.item.ItemTierRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.item.ItemTypeRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.UntradeableRequirement;
import su.nightexpress.quantumrpg.stats.tiers.Tier;
import su.nightexpress.quantumrpg.utils.LoreUT;

public abstract class ModuleItem extends LoadableItem {

	protected QuantumRPG plugin;
	
	protected Tier tier;
	protected String name;
	protected Material material;
	protected List<String> lore;
	protected int modelData;
	protected int durability;
	protected int[] color;
	protected boolean enchanted;
	protected String hash;
	protected Set<ItemFlag> flags;
	protected boolean isUnbreakable;
	
	protected int levelMin;
	protected int levelMax;
	
	protected TreeMap<Integer, String[]> targetItemLevels;
	protected String[] targetItemTypes;
	protected String[] targetItemModules;
	protected String targetTier;
	
	protected final QModuleDrop<?> module;
	
	// Creating new config
	@Deprecated
	public ModuleItem(@NotNull QuantumRPG plugin, String path, QModuleDrop<?> module) {
		super(plugin, path);
		this.module = module;
		this.plugin = plugin;
	}
	
	private void updateConfig(@NotNull JYML cfg) {
		cfg.addMissing("tier", JStrings.DEFAULT);
		
		cfg.addMissing("level.min", 1);
		cfg.addMissing("level.max", 1);
		
		if (!cfg.contains("target-requirements")) {
			cfg.addMissing("target-requirements.type", Arrays.asList(JStrings.MASK_ANY));
			cfg.addMissing("target-requirements.level.1", 0);
			cfg.addMissing("target-requirements.module", Arrays.asList(JStrings.MASK_ANY));
			if (this instanceof SocketItem) {
				cfg.addMissing("target-requirements.socket", JStrings.DEFAULT);
			}
		}
		cfg.saveChanges();
	}
	
	// Load from existent config
	public ModuleItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg, @NotNull QModuleDrop<?> module) {
		super(plugin, cfg);
		this.plugin = plugin;
		this.module = module;
		this.updateConfig(cfg);
		
		this.tier = Config.getTier(cfg.getString("tier", JStrings.DEFAULT));
		if (this.tier == null) {
			throw new IllegalArgumentException("Invalid Tier provided! Module Item must have valid Tier!");
		}
		
		// Fix for loading Generator Items
		if (this instanceof GeneratorItem || this instanceof UnidentifiedItem) {
			this.material = Material.LEATHER_HELMET;
		}
		else {
			String[] matSplit = cfg.getString("material", "STONE").split(":");
			this.material = Material.getMaterial(matSplit[0].toUpperCase());
			if (this.material == null) {
				throw new IllegalArgumentException("Invalid item material!");
			}
		}
		
		// Make item name and lore with complete format
		this.name = StringUT.color(cfg.getString("name", this.getId()));
		this.name = module.getItemNameFormat().replace(ItemTags.PLACEHOLDER_ITEM_NAME, this.name);
		this.name = this.getTier().format(this.name);
		
		this.lore = new ArrayList<>();
		for (String mLore : module.getItemLoreFormat()) {
			if (mLore.equalsIgnoreCase(ItemTags.PLACEHOLDER_ITEM_LORE)) {
				for (String itemLore : StringUT.color(cfg.getStringList("lore"))) {
					this.lore.add(this.tier.format(itemLore));
				}
				continue;
			}
			this.lore.add(this.tier.format(mLore));
		}
		this.modelData = cfg.getInt("model-data", -1);
		this.durability = cfg.getInt("durability", -1);
		
		String color = cfg.getString("color");
		if (color != null) {
			String[] rgb = color.split(",");
			int r = StringUT.getInteger(rgb[0], -1);
			int g = rgb.length >= 2 ? StringUT.getInteger(rgb[1], -1) : 0;
			int b = rgb.length >= 3 ? StringUT.getInteger(rgb[2], -1) : 0;
			this.color = new int[] {r,g,b};
		}
		
		this.enchanted = cfg.getBoolean("enchanted");
		this.hash = cfg.getString("skull-hash");
		
		this.flags = new HashSet<>();
		for (String flag : cfg.getStringList("item-flags")) {
			if (flag.equals(JStrings.MASK_ANY)) {
				this.flags.addAll(Arrays.asList(ItemFlag.values()));
				break;
			}
			try {
				this.flags.add(ItemFlag.valueOf(flag.toUpperCase()));
			}
			catch (IllegalArgumentException ex) {
				continue;
			}
		}
		
		this.isUnbreakable = cfg.getBoolean("unbreakable");
		
		int levelMin = cfg.getInt("level.min", 1);
		int levelMax = cfg.getInt("level.max", 1);
		this.levelMin = Math.min(levelMin, levelMax);
		this.levelMax = Math.max(levelMin, levelMax);
		
		this.targetItemLevels = new TreeMap<>();
		for (String rLvl : cfg.getSection("target-requirements.level")) {
			int itemLvl = StringUT.getInteger(rLvl, -1);
			if (itemLvl <= 0) continue;
			
			String raw = cfg.getString("target-requirements.level." + rLvl);
			if (raw == null || raw.isEmpty()) continue;
			
			this.targetItemLevels.put(itemLvl, raw.split(":"));
		}
		
		List<String> types = cfg.getStringList("target-requirements.type");
		types.replaceAll(String::toLowerCase);
		// If wildcard is present then no need to require it
		if (!types.contains(JStrings.MASK_ANY)) {
			this.targetItemTypes = types.toArray(new String[types.size()]);
		}
		
		List<String> modules = cfg.getStringList("target-requirements.module");
		modules.replaceAll(String::toLowerCase);
		// If wildcard is present then no need to require it
		if (!modules.contains(JStrings.MASK_ANY)) {
			this.targetItemModules = modules.toArray(new String[modules.size()]);
		}
		
		this.targetTier = cfg.getString("target-requirements.tier", "");
		
		cfg.saveChanges();
	}
	
	@Override
	protected void save(@NotNull JYML cfg) {
		
	}
	
	@NotNull
	public Tier getTier() {
		return this.tier;
	}
	
	@NotNull
	public String getName() {
		return this.name;
	}
	
	@NotNull
	public Material getMaterial() {
		return this.material;
	}
	
	@NotNull
	public List<String> getLore() {
		return this.lore;
	}
	
	@NotNull
	public QModuleDrop<?> getModule() {
		return this.module;
	}
	
	public int getMinLevel() {
		return this.levelMin;
	}
	
	public int getMaxLevel() {
		return this.levelMax;
	}
	
	protected final int[] getTargetLevelRequirement(int itemLvl) {
		Map.Entry<Integer, String[]> e = this.targetItemLevels.floorEntry(itemLvl);
		if (e == null) return null;
		
		String[] both = e.getValue();
		
		int[] values = this.doMathExpression(itemLvl, both);
		// Fine min. value by min. possible level or null if both values not present.
		if (values[0] <= 0) {
			if (values[1] > 0) {
				values[0] = 1;
			}
			else {
				return null;
			}
		}
		return values;
	}
	
	protected final int[] doMathExpression(int itemLvl, @NotNull String[] both) {
		int[] values = new int[2];
		for (int i = 0; i < both.length; i++) {
			String str = both[i].replace(ItemTags.PLACEHOLDER_ITEM_LEVEL, String.valueOf(itemLvl)).trim();
			String[] intSplit = str.split("~");
			double val1 = Evaluator.eval(intSplit[0], 1);
			double val2 = intSplit.length >= 2 ? Evaluator.eval(intSplit[1], 1) : val1;
			
			double min = Math.min(val1, val2);
			double max = Math.max(val1, val2);
			
			values[i] = (int) Rnd.getDouble(min, max);
		}
		// Fine max. value by correcting to min if it's not present.
		if (values[1] <= 0) values[1] = values[0];
		
		return values;
	}
	
	@NotNull
	public final ItemStack create() {
		return this.create(-1);
	}

	@NotNull
	public ItemStack create(int lvl) {
		return this.build(this.validateLevel(lvl));
	}
	
	@NotNull
	protected ItemStack build(int lvl) {
		ItemStack item = new ItemStack(this.getMaterial());
		ItemUT.addSkullTexture(item, this.hash, this.getId());
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;
		
		meta.setDisplayName(this.name);
		meta.setLore(this.lore);
		
		if (this.modelData > 0) {
			meta.setCustomModelData(this.modelData);
		}
		if (this.durability >= 0 && meta instanceof Damageable) {
			Damageable dMeta = (Damageable) meta;
			dMeta.setDamage(this.durability);
		}
		
		if (!ArrayUtils.isEmpty(this.color)) {
			int r = this.color[0] >= 0 ? color[0] : Rnd.get(255);
			int g = this.color[1] >= 0 ? color[1] : Rnd.get(255);
			int b = this.color[2] >= 0 ? color[2] : Rnd.get(255);
			if (meta instanceof LeatherArmorMeta) {
				LeatherArmorMeta lm = (LeatherArmorMeta) meta;
				lm.setColor(Color.fromRGB(r, g, b));
			}
			else if (meta instanceof PotionMeta) {
				PotionMeta pm = (PotionMeta) meta;
				pm.setColor(Color.fromRGB(r, g, b));
			}
		}
		
		meta.addItemFlags(this.flags.toArray(new ItemFlag[this.flags.size()]));
		meta.setUnbreakable(this.isUnbreakable);
		if (this.enchanted) {
			meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
		}
		item.setItemMeta(meta);
		
		ItemStats.setId(item, this.getId());
		ItemStats.setModule(item, this.getModule().getId());
		ItemStats.setLevel(item, this.validateLevel(lvl));
		
		// Do not apply if super class can override
		if (!(this instanceof RatedItem)) {
			this.replacePlaceholders(item);
		}
		
		return item;
	}
	
	protected final int validateLevel(int lvl) {
		if (lvl == -1) {
			lvl = Rnd.get(this.getMinLevel(), this.getMaxLevel());
		}
		else {
			if (lvl > this.getMaxLevel()) lvl = this.getMaxLevel();
			else if (lvl < this.getMinLevel()) lvl = this.getMinLevel();
		}
		return lvl;
	}
	
	protected final void replacePlaceholders(@NotNull ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		int level = ItemStats.getLevel(item);
		int sucRate = ItemStats.getSocketRate(item);
		
		String sLevel = String.valueOf(level);
		String sLevelRom = NumberUT.toRoman(level);
		String sSucRate = String.valueOf(sucRate);
		
		String name = meta.getDisplayName()
				.replace(ItemTags.PLACEHOLDER_ITEM_SUCCESS_RATE, sSucRate)
				.replace(ItemTags.PLACEHOLDER_ITEM_LEVEL_ROMAN, sLevelRom)
				.replace(ItemTags.PLACEHOLDER_ITEM_LEVEL, sLevel);
		meta.setDisplayName(name);
		
		
		List<String> metaLore = meta.getLore();
		List<String> lore = metaLore != null ? metaLore : new ArrayList<>();
		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i)
					.replace(ItemTags.PLACEHOLDER_ITEM_SUCCESS_RATE, sSucRate)
					.replace(ItemTags.PLACEHOLDER_ITEM_LEVEL_ROMAN, sLevelRom)
					.replace(ItemTags.PLACEHOLDER_ITEM_LEVEL, sLevel);
			lore.set(i, StringUT.color(line));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		// Replace %TARGET_LEVEL% placeholder
		int[] reqLevels = this.getTargetLevelRequirement(level);
		if (reqLevels != null) {
			ItemLevelRequirement reqLevel = ItemRequirements.getItemRequirement(ItemLevelRequirement.class);
			if (reqLevel != null && reqLevel.hasPlaceholder(item)) {
				reqLevel.add(item, reqLevels, -1);
			}
		}
		
		// Replace %TARGET_TYPE% placeholder
		if (!ArrayUtils.isEmpty(this.targetItemTypes)) {
			ItemTypeRequirement reqType = ItemRequirements.getItemRequirement(ItemTypeRequirement.class);
			if (reqType != null && reqType.hasPlaceholder(item)) {
				reqType.add(item, this.targetItemTypes, -1);
			}
		}
		
		// Replace %TARGET_MODULE% placeholder
		if (!ArrayUtils.isEmpty(this.targetItemModules)) {
			ItemModuleRequirement reqMod = ItemRequirements.getItemRequirement(ItemModuleRequirement.class);
			if (reqMod != null && reqMod.hasPlaceholder(item)) {
				reqMod.add(item, this.targetItemModules, -1);
			}
		}
		
		// Replace %TARGET_TIER% placeholder
		if (this.targetTier != null && !this.targetTier.isEmpty()) {
			Tier tier = Config.getTier(this.targetTier);
			if (tier != null) {
				ItemTierRequirement tierRequirement = ItemRequirements.getItemRequirement(ItemTierRequirement.class);
				if (tierRequirement != null && tierRequirement.hasPlaceholder(item)) {
					tierRequirement.add(item, this.targetTier, -1);
				}
			}
		}
		
		// Replace %UNTRADEABLE% placeholder
		UntradeableRequirement reqUntrade = ItemRequirements.getUserRequirement(UntradeableRequirement.class);
		if (reqUntrade != null && reqUntrade.hasPlaceholder(item)) {
			reqUntrade.add(item, -1);
		}
		
		// Delete placeholders if requirements were not added or been disabled.
		LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_ITEM_LEVEL, null);
		LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_ITEM_TYPE, null);
		LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_ITEM_MODULE, null);
		LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_ITEM_UNTRADEABLE, null);
		LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_ITEM_TIER, null);
	}
}
