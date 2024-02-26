package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.items.ItemType;
import mc.promcteam.engine.items.exception.MissingItemException;
import mc.promcteam.engine.items.exception.MissingProviderException;
import mc.promcteam.engine.items.providers.IProItemProvider;
import mc.promcteam.engine.items.providers.VanillaProvider;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.constants.JStrings;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LimitedItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ResourceManager.ResourceCategory;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.api.IAttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.command.CreateCommand;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.command.EditCommand;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AbilityGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.SingleAttributeGenerator;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.TypedStatGenerator;
import su.nightexpress.quantumrpg.modules.list.sets.SetManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.TypedStat;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.user.BannedClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.ClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.LevelRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.SoulboundRequirement;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.ProRpgItemsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;

public class ItemGeneratorManager extends QModuleDrop<GeneratorItem> {

    public static YamlConfiguration commonItemGenerator;

    private ResourceManager    resourceManager;
    private ItemAbilityHandler abilityHandler;

    public static final String PLACE_GEN_DAMAGE        = "%GENERATOR_DAMAGE%";
    public static final String PLACE_GEN_DEFENSE       = "%GENERATOR_DEFENSE%";
    public static final String PLACE_GEN_STATS         = "%GENERATOR_STATS%";
    public static final String PLACE_GEN_SOCKETS       = "%GENERATOR_SOCKETS_%TYPE%%";
    public static final String PLACE_GEN_ABILITY       = "%GENERATOR_SKILLS%";
    public static final String PLACE_GEN_SKILLAPI_ATTR = "%GENERATOR_SKILLAPI_ATTR%";

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
        if (ItemGeneratorManager.commonItemGenerator == null) {
            try (InputStreamReader in = new InputStreamReader(Objects.requireNonNull(plugin.getClass()
                    .getResourceAsStream(this.getPath() + "items/common.yml")))) {
                ItemGeneratorManager.commonItemGenerator = YamlConfiguration.loadConfiguration(in);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        try (InputStream in = plugin.getClass().getResourceAsStream(this.getPath() + "settings.yml")) {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.loadFromString(new String(in.readAllBytes()));
            cfg.addMissing("editor-gui", configuration.get("editor-gui"));
            cfg.saveChanges();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.resourceManager = new ResourceManager(this);

        this.abilityHandler = new ItemAbilityHandler(this);
        this.abilityHandler.setup();
    }

    @Override
    protected void onPostSetup() {
        super.onPostSetup();
        this.moduleCommand.addSubCommand(new CreateCommand(this));
        this.moduleCommand.addSubCommand(new EditCommand(this));
    }

    @Override
    public void shutdown() {
        AbstractEditorGUI editorGUI = AbstractEditorGUI.getInstance();
        if (editorGUI != null) {
            editorGUI.shutdown();
        }
        this.unregisterListeners();
        if (this.abilityHandler != null) {
            this.abilityHandler.shutdown();
            this.abilityHandler = null;
        }

        if (this.resourceManager != null) {
            this.resourceManager.shutdown();
            this.resourceManager = null;
        }
    }

    // loadItems is PostSetup method.
    @Override
    protected void loadItems() {
        super.loadItems();

        this.resourceManager.setup();
    }

    @NotNull
    public GeneratorItem load(String id, JYML cfg) {
        GeneratorItem itemGenerator = new GeneratorItem(plugin, cfg);
        items.put(id, itemGenerator);
        return itemGenerator;
    }

    public class GeneratorItem extends LimitedItem {

        private double prefixChance;
        private double suffixChance;

        private boolean       materialsWhitelist;
        private Set<ItemType> materialsList;

        private List<Integer>              modelDataList;
        private Map<String, List<Integer>> modelDataSpecial;

        private Map<String, BonusMap> materialsModifiers;

        private TreeMap<Integer, String[]> reqUserLvl;
        private TreeMap<Integer, String[]> reqUserClass;
        private TreeMap<Integer, String[]> reqBannedUserClass;

        private int                        enchantsMinAmount;
        private int                        enchantsMaxAmount;
        private boolean                    enchantsSafeOnly;
        private boolean                    enchantsSafeLevels;
        private       Map<Enchantment, String[]> enchantsList;
        private final TreeMap<Double, String> armorTrims = new TreeMap<>();

        private Set<IAttributeGenerator> attributeGenerators;
        private AbilityGenerator         abilityGenerator;

        public GeneratorItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
            super(plugin, cfg, ItemGeneratorManager.this);

            String path = "generator.";
            this.prefixChance = cfg.getDouble(path + "prefix-chance");
            this.suffixChance = cfg.getDouble(path + "suffix-chance");

            // Pre-cache available materials for Generator.
            this.materialsWhitelist = cfg.getBoolean(path + "materials.reverse");
            if (this.materialsWhitelist) {
                this.materialsList = new HashSet<>();
                Set<String> startWildcards = new HashSet<>();
                Set<String> endWildcards = new HashSet<>();

                for (String mat : cfg.getStringList(path + "materials.black-list")) {
                    String[] split = mat.split('\\'+JStrings.MASK_ANY, 2);
                    if (split.length == 2) { // We have a wildcard
                        if (split[0].isEmpty()) startWildcards.add(split[1].toUpperCase());
                        else if (split[1].isEmpty()) endWildcards.add(split[0].toUpperCase());
                        continue;
                    }
                    try {
                        ItemType itemType = NexEngine.get().getItemManager().getItemType(mat);
                        if (itemType.getNamespace().equals(ProRpgItemsProvider.NAMESPACE)) continue; // Avoid self-reference
                        this.materialsList.add(itemType);
                    } catch (MissingProviderException | MissingItemException ignored) {}
                }

                for (ItemType itemType : Config.getAllRegisteredMaterials()) {
                    String name = itemType.getNamespacedID().toUpperCase();
                    if (startWildcards.stream().anyMatch(name::endsWith) || endWildcards.stream().anyMatch(name::startsWith)) {
                        this.materialsList.add(itemType);
                    }
                }
            } else {
                this.materialsList = new HashSet<>(Config.getAllRegisteredMaterials());
                Set<String> materials = new HashSet<>(cfg.getStringList(path + "materials.black-list"));
                this.materialsList.removeIf(matAll -> {
                    String namespacedID = matAll.getNamespacedID();
                    String upperCaseNamespacedID = namespacedID.toUpperCase();
                    for (String mat : materials) {
                        String[] split = mat.split('\\'+JStrings.MASK_ANY, 2);
                        if (split.length == 2 ) { // We have a wildcard
                            if (split[0].isEmpty() && upperCaseNamespacedID.endsWith(split[1])) return true;
                            else if (split[1].isEmpty() && upperCaseNamespacedID.startsWith(split[0])) return true;
                        }
                        if (namespacedID.equals(mat)) return true;
                    }
                    return false;
                });
            }

            // Load Model Data values for specified item groups.
            path = "generator.materials.model-data.";
            this.modelDataList = cfg.getIntegerList(path + "default");
            this.modelDataSpecial = new HashMap<>();
            for (String specGroup : cfg.getSection(path + "special")) {
                List<Integer> specList = cfg.getIntegerList(path + "special." + specGroup);
                this.modelDataSpecial.put(specGroup.toLowerCase(), specList);
            }

            // Load Bonuses

            // Migrate old Stat Modifiers to Material Bonuses
            if (cfg.isConfigurationSection("generator.materials.stat-modifiers")) {
                ConfigurationSection section = cfg.getConfigurationSection("generator.materials.stat-modifiers");
                cfg.remove("generator.materials.stat-modifiers");
                cfg.set("generator.bonuses.material", section);
                cfg.save();
            }

            // Load Material bonuses
            path = "generator.bonuses.material.";
            this.materialsModifiers = new HashMap<>();
            for (String group : cfg.getSection("generator.bonuses.material")) {
                if (!ItemUtils.parseItemGroup(group)) {
                    error("Invalid item group provided: '" + group + "' in '" + path + "'. File: " + cfg.getFile()
                            .getName());
                    continue;
                }

                BonusMap bMap  = new BonusMap();
                String   path2 = path + group + ".";
                bMap.loadDamages(cfg, path2 + "damage-types");
                bMap.loadDefenses(cfg, path2 + "defense-types");
                bMap.loadStats(cfg, path2 + "item-stats");
                bMap.loadSkillAPIAttributes(cfg, path2 + "skillapi-attributes");
                bMap.loadAmmo(cfg, path2 + "ammo");
                bMap.loadHands(cfg, path2 + "hands");

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

            if (Version.CURRENT.isHigher(Version.V1_19_R3)) {
                path = "generator.armor-trimmings";
                double totalWeight = 0;
                for (String key : cfg.getSection(path)) {
                    double weight = cfg.getDouble(path+'.'+key);
                    if (weight == 0) {
                        continue;
                    }
                    if (key.equals("none")) {
                        totalWeight += weight;
                        armorTrims.put(totalWeight, null);
                        continue;
                    }
                    String[]     split        = key.toLowerCase().split(":");
                    if (split.length != 2) {
                        continue;
                    }
                    if (!split[0].equals("*") && Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(split[0])) == null) {
                        continue;
                    }
                    if (!split[1].equals("*") && Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(split[1])) == null) {
                        continue;
                    }
                    totalWeight += weight;
                    armorTrims.put(totalWeight, key);
                }
            }

            this.attributeGenerators = new HashSet<>();

            // Pre-cache Ammo Attributes
            this.addAttributeGenerator(new SingleAttributeGenerator<>(this.plugin,
                    this,
                    "generator.ammo-types.",
                    ItemStats.getAmmos(),
                    ItemTags.PLACEHOLDER_ITEM_AMMO));
            this.addAttributeGenerator(new SingleAttributeGenerator<>(this.plugin,
                    this,
                    "generator.hand-types.",
                    ItemStats.getHands(),
                    ItemTags.PLACEHOLDER_ITEM_HAND));

            this.addAttributeGenerator(new AttributeGenerator<>(this.plugin,
                    this,
                    "generator.damage-types.",
                    ItemStats.getDamages(),
                    ItemGeneratorManager.PLACE_GEN_DAMAGE));
            this.addAttributeGenerator(new AttributeGenerator<>(this.plugin,
                    this,
                    "generator.defense-types.",
                    ItemStats.getDefenses(),
                    ItemGeneratorManager.PLACE_GEN_DEFENSE));
            this.addAttributeGenerator(new TypedStatGenerator(this.plugin,
                    this,
                    "generator.item-stats.",
                    ItemStats.getStats(),
                    ItemGeneratorManager.PLACE_GEN_STATS));
            this.addAttributeGenerator(this.abilityGenerator = new AbilityGenerator(this.plugin,
                    this,
                    PLACE_GEN_ABILITY));
            SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
            this.addAttributeGenerator(new AttributeGenerator<>(this.plugin,
                    this,
                    "generator.skillapi-attributes.",
                    skillAPIHK == null ? List.of() : skillAPIHK.getAttributes(),
                    ItemGeneratorManager.PLACE_GEN_SKILLAPI_ATTR));
            if (skillAPIHK != null) {
                cfg.addMissing("generator.skillapi-attributes", commonItemGenerator.get("generator.skillapi-attributes"));
                cfg.addMissing("generator.skills", commonItemGenerator.get("generator.skills"));
            }

            // Pre-cache Socket Attributes
            for (SocketAttribute.Type socketType : SocketAttribute.Type.values()) {
                this.addAttributeGenerator(new AttributeGenerator<>(this.plugin,
                        this,
                        "generator.sockets." + socketType.name() + ".",
                        ItemStats.getSockets(socketType),
                        ItemGeneratorManager.PLACE_GEN_SOCKETS.replace("%TYPE%", socketType.name())));
            }


            // --------------- END OF CONFIG ---------------------- //

            cfg.saveChanges();
        }

        protected final int[] getUserLevelRequirement(int itemLvl) {
            if (this.reqUserLvl == null) return new int[]{0};

            Map.Entry<Integer, String[]> e = this.reqUserLvl.floorEntry(itemLvl);
            if (e == null) return new int[]{0};

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
        public Set<ItemType> getMaterialsList() {
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
        public BiFunction<Boolean, Double, Double> getMaterialModifier(@NotNull ItemStack item,
                                                                       @NotNull ItemLoreStat<?> stat) {
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
        public ItemStack create(int lvl, int uses, @Nullable ItemType mat) {
            lvl = this.validateLevel(lvl);
            if (uses < 1) uses = this.getCharges(lvl);

            return this.build(lvl, uses, mat);
        }

        @NotNull
        protected ItemStack build(int itemLvl, int uses, @Nullable ItemType mat) {
            ItemStack item;
            if (mat != null && materialsList.contains(mat)) {
                item = super.build(mat.create(), itemLvl, uses);
            } else {
                ItemType itemType = Rnd.get(new ArrayList<>(this.materialsList));
                item = super.build(itemType == null ? null : itemType.create(), itemLvl, uses);
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

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

            String itemGroupId   = ItemUtils.getItemGroupIdFor(item);
            String itemGroupName = ItemUtils.getItemGroupNameFor(item);

            String itemMaterial = NexEngine.get().getItemManager().getItemTypes(item).stream()
                    .filter(itemType -> itemType.getCategory() != IProItemProvider.Category.PRO)
                    .max(Comparator.comparing(ItemType::getCategory))
                    .orElseGet(() -> new VanillaProvider.VanillaItemType(item.getType())).getNamespacedID();

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
                BlockStateMeta bmeta  = (BlockStateMeta) meta;
                Banner         banner = (Banner) bmeta.getBlockState();

                DyeColor    bBaseColor    = Rnd.get(DyeColor.values());
                PatternType bPattern      = Rnd.get(PatternType.values());
                DyeColor    bPatternColor = Rnd.get(DyeColor.values());

                banner.setBaseColor(bBaseColor);
                banner.addPattern(new Pattern(bPatternColor, bPattern));
                banner.update();
                bmeta.setBlockState(banner);
            }

            if (!armorTrims.isEmpty() && meta instanceof ArmorMeta) {
                String trimString = armorTrims.ceilingEntry(Rnd.nextDouble()*armorTrims.lastKey()).getValue();
                ArmorTrim armorTrim;
                if (trimString == null) {
                    armorTrim = null;
                } else {
                    String[] split = trimString.split(":");
                    TrimMaterial trimMaterial = null;
                    if (split[0].equals("*")) {
                        int size = 0;
                        for (TrimMaterial ignored : Registry.TRIM_MATERIAL) {
                            size++;
                        }
                        int index = Rnd.get(size);
                        int i = 0;
                        for (Iterator<TrimMaterial> iterator = Registry.TRIM_MATERIAL.iterator(); iterator.hasNext();) {
                            TrimMaterial next = iterator.next();
                            if (index == i) {
                                trimMaterial = next;
                                break;
                            }
                            i++;
                        }
                    } else {
                        trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(split[0]));
                    }
                    TrimPattern trimPattern = null;
                    if (split[1].equals("*")) {
                        int size = 0;
                        for (TrimPattern ignored : Registry.TRIM_PATTERN) {
                            size++;
                        }
                        int index = Rnd.get(size);
                        int i = 0;
                        for (Iterator<TrimPattern> iterator = Registry.TRIM_PATTERN.iterator(); iterator.hasNext();) {
                            TrimPattern next = iterator.next();
                            if (index == i) {
                                trimPattern = next;
                                break;
                            }
                            i++;
                        }
                    } else {
                        trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(split[1]));
                    }
                    armorTrim = new ArmorTrim(Objects.requireNonNull(trimMaterial), Objects.requireNonNull(trimPattern));
                }
                ((ArmorMeta) meta).setTrim(armorTrim);
            }

            item.setItemMeta(meta);

            // Add enchants
            int                                    enchRoll  =
                    Rnd.get(this.getMinEnchantments(), this.getMaxEnchantments());
            int                                    enchCount = 0;
            List<Map.Entry<Enchantment, String[]>> enchants  = new ArrayList<>(this.enchantsList.entrySet());
            Collections.shuffle(enchants);

            for (Map.Entry<Enchantment, String[]> e : enchants) {
                if (enchCount >= enchRoll) {
                    break;
                }
                Enchantment enchant    = e.getKey();
                int[]       enchLevels = this.doMathExpression(itemLvl, e.getValue());
                int         enchLevel  = Rnd.get(enchLevels[0], enchLevels[1]);
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
                BannedClassRequirement reqBannedClass =
                        ItemRequirements.getUserRequirement(BannedClassRequirement.class);
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
            meta = item.getItemMeta();
            if (meta == null) return item;
            lore = meta.getLore();
            if (lore == null) return item;

            for (TypedStat at : ItemStats.getStats()) {
                lore.remove(at.getPlaceholder());
            }
            for (ItemLoreStat<?> at : ItemStats.getDamages()) {
                lore.remove(at.getPlaceholder());
            }
            for (ItemLoreStat<?> at : ItemStats.getDefenses()) {
                lore.remove(at.getPlaceholder());
            }
            SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
            if (skillAPIHK != null) {
                for (ItemLoreStat<?> at : skillAPIHK.getAttributes()) {
                    lore.remove(at.getPlaceholder());
                }
            }
            for (SocketAttribute.Type socketType : SocketAttribute.Type.values()) {
                for (ItemLoreStat<?> at : ItemStats.getSockets(socketType)) {
                    lore.remove(at.getPlaceholder());
                }
            }
            // TODO
            meta.setLore(lore);
            item.setItemMeta(meta);

            return item;
        }
    }
}
