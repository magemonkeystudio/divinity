package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.constants.JStrings;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LimitedItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ResourceManager.ResourceCategory;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.api.IAttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.command.EditorCommand;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AbilityGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.SingleAttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.sets.SetManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.user.BannedClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.ClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.LevelRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.SoulboundRequirement;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;

public class ItemGeneratorManager extends QModuleDrop<GeneratorItem> {

	private ResourceManager resourceManager;
	private ItemAbilityHandler abilityHandler;
	private EditorGUI activeEditor;
	
	private static final String PLACE_GEN_DAMAGE = "%GENERATOR_DAMAGE%";
	private static final String PLACE_GEN_DEFENSE = "%GENERATOR_DEFENSE%";
	private static final String PLACE_GEN_STATS = "%GENERATOR_STATS%";
	private static final String PLACE_GEN_SOCKETS = "%GENERATOR_SOCKETS_%TYPE%%";
	private static final String PLACE_GEN_ABILITY = "%GENERATOR_ABILITY%";
	
	public ItemGeneratorManager(@NotNull QuantumRPG plugin) {
		super(plugin, GeneratorItem.class);
	}

	@Override
	@NotNull
	public String getId() {
		return EModule.ITEM_GENERATOR;
	}

	@Override
	@NotNull
	public String version() {
		return "2.0.0";
	}
	
	@Override
	public void setup() {
		try (InputStream in = plugin.getClass().getResourceAsStream(this.getPath()+"settings.yml")) {
			YamlConfiguration configuration = new YamlConfiguration();
			configuration.loadFromString(new String(in.readAllBytes()));
			cfg.addMissing("editor-gui", configuration.get("editor-gui"));
			cfg.saveChanges();
		} catch (Exception e) { e.printStackTrace(); }

		this.resourceManager = new ResourceManager(this);
		
		this.abilityHandler = new ItemAbilityHandler(this);
		this.abilityHandler.setup();
	}

	@Override
	protected void onPostSetup() {
		super.onPostSetup();
		this.moduleCommand.addSubCommand(new EditorCommand(this));
	}

	@Override
	public void shutdown() {
		if (this.abilityHandler != null) {
			this.abilityHandler.shutdown();
			this.abilityHandler = null;
		}
		
		if (this.resourceManager != null) {
			this.resourceManager.shutdown();
			this.resourceManager = null;
		}

		this.activeEditor = null;
	}
	
	// loadItems is PostSetup method.
	@Override
	protected void loadItems() {
		super.loadItems();
		
		this.resourceManager.setup();
	}

	public boolean reload(String id) {
		GeneratorItem itemGenerator = items.get(id);
		if (id == null) { return false; }
		items.put(id, new GeneratorItem(plugin, itemGenerator.getConfig()));
		return true;
	}

	public void openEditor(String id, Player player) {
		if (!this.isEnabled()) { throw new IllegalStateException("Module is disabled!"); }
		GeneratorItem itemGenerator = items.get(id);
		if (itemGenerator == null) {
			plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidItem.send(player);
			return;
		}
		if (activeEditor != null) {
			for (Player viewer : activeEditor.getViewers()) {
				if (viewer.isOnline() && !viewer.equals(player)) {
					plugin.lang().ItemGenerator_Cmd_Editor_Error_AlreadyOpen.replace("%player%", viewer.getName());
					return;
				}
			}
		}

		(activeEditor = new EditorGUI(this, cfg, itemGenerator)).open(player, 1);
	}

	void onEditorClose(EditorGUI editorGUI) {
		if (this.activeEditor == editorGUI) { this.activeEditor = null; }
	}

	public class GeneratorItem extends LimitedItem {

		private double prefixChance;
		private double suffixChance;
		
		private boolean materialsWhitelist;
		private Set<Material> materialsList;
		
		private List<Integer> modelDataList;
		private Map<String, List<Integer>> modelDataSpecial;
		
		private Map<String, BonusMap> materialsModifiers;
		
		private TreeMap<Integer, String[]> reqUserLvl;
		private TreeMap<Integer, String[]> reqUserClass;
		private TreeMap<Integer, String[]> reqBannedUserClass;
		
		private int enchantsMinAmount;
		private int enchantsMaxAmount;
		private boolean enchantsSafeOnly;
		private boolean enchantsSafeLevels;
		private Map<Enchantment, String[]> enchantsList;
		
		private Set<IAttributeGenerator> attributeGenerators;
		private AbilityGenerator abilityGenerator;
		
		public GeneratorItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
			super(plugin, cfg, ItemGeneratorManager.this);
			
			String path = "generator.";
			this.prefixChance = cfg.getDouble(path + "prefix-chance");
			this.suffixChance = cfg.getDouble(path + "suffix-chance");
			
			// Pre-cache available materials for Generator.
			this.materialsWhitelist = cfg.getBoolean(path + "materials.reverse");
			this.materialsList = new HashSet<>(Config.getAllRegisteredMaterials());
			
			String mask = JStrings.MASK_ANY;
			Set<String> materials = new HashSet<>(cfg.getStringList(path + "materials.black-list"));

			this.materialsList.removeIf(matAll -> {
				
				String mAll = matAll.name();
				for (String mCfg : materials) {
					boolean isWildCard = mCfg.startsWith(mask) || mCfg.endsWith(mask);
					String mCfgRaw = isWildCard ? mCfg.replace(mask, "") : mCfg;
					boolean matches = isWildCard ? (mAll.startsWith(mCfgRaw) || mAll.endsWith(mCfgRaw)) : mAll.equalsIgnoreCase(mCfgRaw);
					
					if (matches) { // If matches then either keep item in list or remove it
						return !this.materialsWhitelist;
					}
				}
				
				// For whitelist it will remove all items not passed the match check.
				return this.materialsWhitelist;
			});
			
			// Load Model Data values for specified item groups.
			path = "generator.materials.model-data.";
			this.modelDataList = cfg.getIntegerList(path + "default");
			this.modelDataSpecial = new HashMap<>();
			for (String specGroup : cfg.getSection(path + "special")) {
				List<Integer> specList = cfg.getIntegerList(path + "special." + specGroup);
				this.modelDataSpecial.put(specGroup.toLowerCase(), specList);
			}
			
			// Load Attribute Bonus Maps depends on Item Group.
			path = "generator.materials.stat-modifiers.";
			this.materialsModifiers = new HashMap<>();
			for (String group : cfg.getSection("generator.materials.stat-modifiers")) {
				if (!ItemUtils.parseItemGroup(group)) {
					error("Invalid item group provided: '" + group + "' in '" + path + "'. File: " + cfg.getFile().getName());
					continue;
				}
				
				BonusMap bMap = new BonusMap();
				String path2 = path + group + ".";
				bMap.loadDamages(cfg, path2 + "damage-types");
				bMap.loadDefenses(cfg, path2 + "defense-types");
				bMap.loadStats(cfg, path2 + "item-stats");
				
				this.materialsModifiers.put(group.toLowerCase(), bMap);
			}
			
			// Load User Requirements.
			path = "generator.user-requirements-by-level.";
			if (ItemRequirements.isRegisteredUser(LevelRequirement.class)) {
				this.reqUserLvl = new TreeMap<>();
				for (String sLvl : cfg.getSection(path + "level")) {
					int itemLvl = StringUT.getInteger(sLvl, -1);
					if (itemLvl <= 0) continue;
					
					String reqRaw = cfg.getString(path + "level." + sLvl);
					if (reqRaw == null || reqRaw.isEmpty()) continue;
					
					this.reqUserLvl.put(itemLvl, reqRaw.split(":"));
				}
			}
			if (ItemRequirements.isRegisteredUser(ClassRequirement.class)) {
				this.reqUserClass = new TreeMap<>();
				for (String sLvl : cfg.getSection(path + "class")) {
					int itemLvl = StringUT.getInteger(sLvl, -1);
					if (itemLvl <= 0) continue;
					
					String reqRaw = cfg.getString(path + "class." + sLvl);
					if (reqRaw == null || reqRaw.isEmpty()) continue;
					
					this.reqUserClass.put(itemLvl, reqRaw.split(","));
				}
			}
			if (ItemRequirements.isRegisteredUser(BannedClassRequirement.class)) {
				this.reqBannedUserClass = new TreeMap<>();
				for (String sLvl : cfg.getSection(path + "banned-class")) {
					int itemLvl = StringUT.getInteger(sLvl, -1);
					if (itemLvl <= 0) continue;

					String reqRaw = cfg.getString(path + "banned-class." + sLvl);
					if (reqRaw == null || reqRaw.isEmpty()) continue;

					this.reqBannedUserClass.put(itemLvl, reqRaw.split(","));
				}
			}
			
			// Pre-cache enchantments.
			path = "generator.enchantments.";
			cfg.addMissing(path + "safe-levels", true);
			
			this.enchantsMinAmount = Math.max(0, cfg.getInt(path + "minimum"));
			this.enchantsMaxAmount = Math.max(0, cfg.getInt(path + "maximum"));
			this.enchantsSafeOnly = cfg.getBoolean(path + "safe-only");
			this.enchantsSafeLevels = cfg.getBoolean(path + "safe-levels");
			this.enchantsList = new HashMap<>();
			for (String sId : cfg.getSection(path + "list")) {
				Enchantment en = Enchantment.getByKey(NamespacedKey.minecraft(sId.toLowerCase()));
				if (en == null) {
					error("Invalid enchantment provided: " + sId + " (" + cfg.getFile().getName() + ")");
					continue;
				}
				
				String reqRaw = cfg.getString(path + "list." + sId);
				if (reqRaw == null || reqRaw.isEmpty()) continue;
				
				this.enchantsList.put(en, reqRaw.split(":"));
			}
			
			this.attributeGenerators = new HashSet<>();
			
			// Pre-cache Ammo Attributes
			this.addAttributeGenerator(new SingleAttributeGenerator<>(this.plugin, this, "generator.ammo-types.", ItemStats.getAmmos(), ItemTags.PLACEHOLDER_ITEM_AMMO));
			this.addAttributeGenerator(new SingleAttributeGenerator<>(this.plugin, this, "generator.hand-types.", ItemStats.getHands(), ItemTags.PLACEHOLDER_ITEM_HAND));
			
			this.addAttributeGenerator(new AttributeGenerator<>(this.plugin, this, "generator.damage-types.", ItemStats.getDamages(), ItemGeneratorManager.PLACE_GEN_DAMAGE));
			this.addAttributeGenerator(new AttributeGenerator<>(this.plugin, this, "generator.defense-types.", ItemStats.getDefenses(), ItemGeneratorManager.PLACE_GEN_DEFENSE));
			this.addAttributeGenerator(new AttributeGenerator<>(this.plugin, this, "generator.item-stats.", ItemStats.getStats(), ItemGeneratorManager.PLACE_GEN_STATS));
			
			// Pre-cache Socket Attributes
			for (SocketAttribute.Type socketType : SocketAttribute.Type.values()) {
				this.addAttributeGenerator(new AttributeGenerator<>(this.plugin, this, "generator.sockets." + socketType.name() + ".", ItemStats.getSockets(socketType), ItemGeneratorManager.PLACE_GEN_SOCKETS.replace("%TYPE%", socketType.name())));
			}
			
			this.addAttributeGenerator(this.abilityGenerator = new AbilityGenerator(this.plugin, this, PLACE_GEN_ABILITY));
			
			// --------------- END OF CONFIG ---------------------- //
			
			cfg.saveChanges();
		}
		
		protected final int[] getUserLevelRequirement(int itemLvl) {
			if (this.reqUserLvl == null) return new int[] {0};
			
			Map.Entry<Integer, String[]> e = this.reqUserLvl.floorEntry(itemLvl);
			if (e == null) return new int[] {0};
			
			return this.doMathExpression(itemLvl, e.getValue());
		}
		
		@Nullable
		protected final String[] getUserClassRequirement(int itemLvl) {
			if (this.reqUserClass == null) return null;
			
			Map.Entry<Integer, String[]> e = this.reqUserClass.floorEntry(itemLvl);
			if (e == null) return null;
			
			return e.getValue();
		}

		@Nullable
		protected final String[] getBannedUserClassRequirement(int itemLvl) {
			if (this.reqBannedUserClass == null) return null;

			Map.Entry<Integer, String[]> e = this.reqBannedUserClass.floorEntry(itemLvl);
			if (e == null) return null;

			return e.getValue();
		}
		
		public double getPrefixChance() {
			return this.prefixChance;
		}
		
		public double getSuffixChance() {
			return this.suffixChance;
		}
		
		public boolean isMaterialReversed() {
			return this.materialsWhitelist;
		}
		
		@NotNull
		public Set<Material> getMaterialsList() {
			return materialsList;
		}
		
		public int getMinEnchantments() {
			return this.enchantsMinAmount;
		}
		
		public int getMaxEnchantments() {
			return this.enchantsMaxAmount;
		}
		
		public boolean isSafeEnchant() {
			return enchantsSafeOnly;
		}
		
		public boolean isEnchantsSafeLevels() {
			return enchantsSafeLevels;
		}
		
		@NotNull
		public BiFunction<Boolean, Double, Double> getMaterialModifier(@NotNull ItemStack item, @NotNull ItemLoreStat<?> stat) {
			for (Map.Entry<String, BonusMap> e : this.materialsModifiers.entrySet()) {
				if (ItemUtils.compareItemGroup(item, e.getKey())) {
					BonusMap bMap = e.getValue();
					return bMap.getBonus(stat);
				}
			}
			return (isBonus, result) -> result;
		}
		
		@NotNull
		public Set<IAttributeGenerator> getAttributeGenerators() {
			return attributeGenerators;
		}
		
		public boolean addAttributeGenerator(@NotNull IAttributeGenerator generator) {
			return this.attributeGenerators.add(generator);
		}
		
		@NotNull
		public AbilityGenerator getAbilityGenerator() {
			return abilityGenerator;
		}
		
		public double getScaleOfLevel(double scale, int itemLevel) {
			return scale == 1D ? (scale) : ((scale * 100D - 100D) * (double) itemLevel / 100D + 1D);
		}
		
		@Override
		@NotNull
		public ItemStack create(int lvl, int uses) {
			return this.create(lvl, uses, null);
		}
		
		@NotNull
		public ItemStack create(int lvl, int uses, @Nullable Material mat) {
			lvl = this.validateLevel(lvl);
			if (uses < 1) uses = this.getCharges(lvl);
			
			return this.build(lvl, uses, mat);
		}
		
		@NotNull
		protected ItemStack build(int itemLvl, int uses, @Nullable Material mat) {
			ItemStack item = super.build(itemLvl, uses);
			
			// Set material
			if (this.materialsList.isEmpty()) return item;
			if (mat != null && this.materialsList.contains(mat)) {
				item.setType(mat);
			}
			else {
				Material type = Rnd.get(new ArrayList<>(this.materialsList));
				item.setType(type != null ? type : Material.AIR);
			}
			
			ItemMeta meta = item.getItemMeta(); 
			if (meta == null) return item;
			
			String itemMaterial = item.getType().name();
			
			List<Integer> dataValues = new ArrayList<>();
			for (Map.Entry<String, List<Integer>> e : this.modelDataSpecial.entrySet()) {
				if (ItemUtils.compareItemGroup(item, e.getKey())) {
					dataValues.addAll(e.getValue());
				}
			}
			if (dataValues.isEmpty()) {
				dataValues.addAll(this.modelDataList);
			}
			if (!dataValues.isEmpty()) {
				Integer mInt = Rnd.get(dataValues);
				if (mInt != null) {
					meta.setCustomModelData(mInt.intValue());
				}
			}
			
			// Prepare prefixes and suffixes
			String prefixTier = "";
			String suffixTier = "";
			
			String prefixMaterial = "";
			String suffixMaterial = "";
			
			String prefixItemType = "";
			String suffixItemType = "";
			
			String itemGroupId = ItemUtils.getItemGroupIdFor(item);
			String itemGroupName = ItemUtils.getItemGroupNameFor(item.getType());
			
			if (Rnd.get(true) <= prefixChance) {
				prefixTier = Rnd.get(resourceManager.getPrefix(ResourceCategory.TIER, this.getTier().getId()));
				prefixMaterial = Rnd.get(resourceManager.getPrefix(ResourceCategory.MATERIAL, itemMaterial));
				prefixItemType = Rnd.get(resourceManager.getPrefix(ResourceCategory.SUBTYPE, itemGroupId));
			}
			if (Rnd.get(true) <= suffixChance) {
				suffixTier = Rnd.get(resourceManager.getSuffix(ResourceCategory.TIER, this.getTier().getId()));
				suffixMaterial = Rnd.get(resourceManager.getSuffix(ResourceCategory.MATERIAL, itemMaterial));
				suffixItemType = Rnd.get(resourceManager.getSuffix(ResourceCategory.SUBTYPE, itemGroupId));
			}
			
			// Replace prefix and suffix
			if (meta.hasDisplayName()) {
				String metaName = meta.getDisplayName()
						.replace("%item_type%", itemGroupName)
						.replace("%suffix_tier%", suffixTier != null ? suffixTier : "")
						.replace("%prefix_tier%", prefixTier != null ? prefixTier : "")
						
						.replace("%prefix_type%", prefixItemType != null ? prefixItemType : "")
						.replace("%suffix_type%", suffixItemType != null ? suffixItemType : "")
						
						.replace("%prefix_material%", prefixMaterial != null ? prefixMaterial : "")
						.replace("%suffix_material%", suffixMaterial != null ? suffixMaterial : "");
				metaName = StringUT.oneSpace(metaName);
				meta.setDisplayName(metaName);
			}
			
			List<String> lore = meta.getLore();
			if (lore != null) {
				lore.replaceAll(line -> line.replace("%item_type%", itemGroupName));
				meta.setLore(lore);
			}
			
			// +-------------------------+
			//           COLORED
			//      LEATHER AND SHIELDS
			// +-------------------------+
			// TODO More options, mb generator?
			if (meta instanceof BlockStateMeta) {
	            BlockStateMeta bmeta = (BlockStateMeta) meta;
		        Banner banner = (Banner) bmeta.getBlockState();
		            
				DyeColor bBaseColor = Rnd.get(DyeColor.values());
				PatternType bPattern = Rnd.get(PatternType.values());
				DyeColor bPatternColor = Rnd.get(DyeColor.values());
				
		        banner.setBaseColor(bBaseColor);
		        banner.addPattern(new Pattern(bPatternColor, bPattern));
		        banner.update();
		        bmeta.setBlockState(banner);
			}
			item.setItemMeta(meta);
			
			// Add enchants
			int enchRoll = Rnd.get(this.getMinEnchantments(), this.getMaxEnchantments());
			int enchCount = 0;
			List<Map.Entry<Enchantment, String[]>> enchants = new ArrayList<>(this.enchantsList.entrySet());
			Collections.shuffle(enchants);
			
			for (Map.Entry<Enchantment, String[]> e : enchants) {
				if (enchCount >= enchRoll) {
					break;
				}
				Enchantment enchant = e.getKey();
				int[] enchLevels = this.doMathExpression(itemLvl, e.getValue());
				int enchLevel = Rnd.get(enchLevels[0], enchLevels[1]);
				if (enchLevel < 1) continue;
				
				if (this.isSafeEnchant()) {
					if (!enchant.canEnchantItem(item) || !ItemUtils.checkEnchantConflict(item, enchant)) {
						continue;
					}
				}
				if (this.isEnchantsSafeLevels()) {
					enchLevel = Math.min(enchant.getMaxLevel(), enchLevel);
				}
				item.addUnsafeEnchantment(enchant, enchLevel);
				enchCount++;
			}
			
			// Quick fix for skull textures of ModuleItem because of ItemGen materials.
			// TODO
			ItemUT.addSkullTexture(item, this.hash, this.getId());
			
			this.getAttributeGenerators().forEach(generator -> generator.generate(item, itemLvl));
			
			LoreUT.replacePlaceholder(item, PLACE_GEN_DAMAGE, null);
			LoreUT.replacePlaceholder(item, PLACE_GEN_DEFENSE, null);
			
			LevelRequirement reqLevel = ItemRequirements.getUserRequirement(LevelRequirement.class);
			if (reqLevel != null) {
				reqLevel.add(item, this.getUserLevelRequirement(itemLvl), -1);
			}
			
			String[] userClass = this.getUserClassRequirement(itemLvl);
			if (userClass != null) {
				ClassRequirement reqClass = ItemRequirements.getUserRequirement(ClassRequirement.class);
				if (reqClass != null) {
					reqClass.add(item, userClass, -1);
				}
			}

			String[] bannedUserClass = this.getBannedUserClassRequirement(itemLvl);
			if (bannedUserClass != null) {
				BannedClassRequirement reqBannedClass = ItemRequirements.getUserRequirement(BannedClassRequirement.class);
				if (reqBannedClass != null) {
					reqBannedClass.add(item, bannedUserClass, -1);
				}
			}
			LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_LEVEL, null);
			LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_CLASS, null);
			LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_BANNED_CLASS, null);
			
			// Replace %SOULBOUND% placeholder.
			SoulboundRequirement reqSoul = ItemRequirements.getUserRequirement(SoulboundRequirement.class);
			if (reqSoul != null && reqSoul.hasPlaceholder(item)) {
				reqSoul.add(item, -1);
			}
			
			// Replace %SET% placeholder.
			SetManager setManager = plugin.getModuleCache().getSetManager();
			if (setManager != null) {
				setManager.updateItemSet(item, null);
			}
			LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_ITEM_SET, null);
			
			// Replace %ENCHANTS% placeholder
			LoreUT.replaceEnchants(item);
			LoreUT.replacePlaceholder(item, "%TYPE%", itemGroupName);
			LoreUT.replacePlaceholder(item, "%MATERIAL%", plugin.lang().getEnum(item.getType()));
			
			// Delete left Attribute placeholders.
			meta = item.getItemMeta(); if (meta == null) return item;
			lore = meta.getLore(); if (lore == null) return item;
			
			for (ItemLoreStat<?> at : ItemStats.getStats()) {
				lore.remove(at.getPlaceholder());
			}
			for (ItemLoreStat<?> at : ItemStats.getDamages()) {
				lore.remove(at.getPlaceholder());
			}
			for (ItemLoreStat<?> at : ItemStats.getDefenses()) {
				lore.remove(at.getPlaceholder());
			}
			for (SocketAttribute.Type socketType : SocketAttribute.Type.values()) {
				for (ItemLoreStat<?> at : ItemStats.getSockets(socketType)) {
					lore.remove(at.getPlaceholder());
				}
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			return item;
		}
	}
}
