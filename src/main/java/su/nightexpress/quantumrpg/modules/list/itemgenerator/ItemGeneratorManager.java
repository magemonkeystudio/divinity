package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.gamingmesh.jobs.Jobs;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.AureliumSkillsSkillRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.AureliumSkillsStatRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.JobsRebornRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.McMMORequirement;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;

public class ItemGeneratorManager extends QModuleDrop<GeneratorItem> {

    public static YamlConfiguration commonItemGenerator;

    private ResourceManager resourceManager;
    private ItemAbilityHandler abilityHandler;

    public static final String PLACE_GEN_DAMAGE = "%GENERATOR_DAMAGE%";
    public static final String PLACE_GEN_DEFENSE = "%GENERATOR_DEFENSE%";
    public static final String PLACE_GEN_STATS = "%GENERATOR_STATS%";
    public static final String PLACE_GEN_SOCKETS = "%GENERATOR_SOCKETS_%TYPE%%";
    public static final String PLACE_GEN_ABILITY = "%GENERATOR_SKILLS%";
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

        this.registerListeners();
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

        private boolean materialsWhitelist;
        private Set<Material> materialsList;

        private List<Integer> modelDataList;
        private Map<String, List<Integer>> modelDataSpecial;

        private Map<String, BonusMap> materialsModifiers;

        private TreeMap<Integer, String[]> reqUserLvl;
        private TreeMap<Integer, String[]> reqUserClass;
        private TreeMap<Integer, String[]> reqBannedUserClass;
        private TreeMap<Integer, String[]> reqMcMMOSkills;
        private TreeMap<Integer, String[]> reqJobs;
        private TreeMap<Integer, String[]> reqAureliumSkillsSkill;
        private TreeMap<Integer, String[]> reqAureliumSkillsStat;

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
                    boolean matches = isWildCard ? (mAll.startsWith(mCfgRaw) || mAll.endsWith(mCfgRaw))
                            : mAll.equalsIgnoreCase(mCfgRaw);

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
                    error("Invalid item group provided: '" + group + "' in '" + path + "'. File: " + cfg.getFile()
                            .getName());
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

            // API Requirements
            if (ItemRequirements.isRegisteredUser(McMMORequirement.class)) {
                this.reqMcMMOSkills = new TreeMap<>();
                for (String skill : cfg.getSection(path + "mcmmo-skill")) {
                    //Deprecated and might need a change in future
                    if (PrimarySkillType.getSkill(skill) == null) {
                        QuantumRPG.getInstance().warn("The required mcmmo skill cannot be found: " + skill);
                        continue;
                    }

                    for (String sLvl : cfg.getSection(path + "mcmmo-skill." + skill)) {
                        int itemLvl = StringUT.getInteger(sLvl, -1);
                        if (itemLvl <= 0) continue;

                        String reqRaw = cfg.getString(path + "mcmmo-skill." + skill + "." + sLvl);
                        if (reqRaw == null || reqRaw.isEmpty()) continue;

                        String[] reqEdit = new String[]{skill, reqRaw.split(":")[0], reqRaw.split(":")[1]};
                        this.reqMcMMOSkills.put(itemLvl, reqEdit);
                    }
                }
            }

            if (ItemRequirements.isRegisteredUser(JobsRebornRequirement.class)) {
                this.reqJobs = new TreeMap<>();
                for (String job : cfg.getSection(path + "jobs-job")) {
                    if (Jobs.getJob(job) == null) {
                        QuantumRPG.getInstance().warn("The required job cannot be found: " + job);
                        continue;
                    }

                    for (String sLvl : cfg.getSection(path + "jobs-job." + job)) {
                        int itemLvl = StringUT.getInteger(sLvl, -1);
                        if (itemLvl <= 0) continue;

                        String reqRaw = cfg.getString(path + "jobs-job." + job + "." + sLvl);
                        if (reqRaw == null || reqRaw.isEmpty()) continue;

                        String[] reqEdit = new String[]{job, reqRaw.split(":")[0], reqRaw.split(":")[1]};
                        this.reqJobs.put(itemLvl, reqEdit);
                    }
                }
            }

            if (ItemRequirements.isRegisteredUser(AureliumSkillsSkillRequirement.class)) {
                this.reqAureliumSkillsSkill = new TreeMap<>();
                for (String skill : cfg.getSection(path + "aurelium-skill")) {
                    if (AureliumAPI.getPlugin().getSkillRegistry().getSkill(skill) == null) {
                        QuantumRPG.getInstance().warn("The required aurelium skill cannot be found: " + skill);
                        continue;
                    }

                    for (String sLvl : cfg.getSection(path + "aurelium-skill." + skill)) {
                        int itemLvl = StringUT.getInteger(sLvl, -1);
                        if (itemLvl <= 0) continue;

                        String reqRaw = cfg.getString(path + "aurelium-skill." + skill + "." + sLvl);
                        if (reqRaw == null || reqRaw.isEmpty()) continue;

                        String[] reqEdit = new String[]{skill, reqRaw.split(":")[0], reqRaw.split(":")[1]};
                        this.reqAureliumSkillsSkill.put(itemLvl, reqEdit);
                    }
                }
            }

            if (ItemRequirements.isRegisteredUser(AureliumSkillsStatRequirement.class)) {
                this.reqAureliumSkillsStat = new TreeMap<>();
                for (String stat : cfg.getSection(path + "aurelium-stat")) {
                    if (AureliumAPI.getPlugin().getStatRegistry().getStat(stat) == null) {
                        QuantumRPG.getInstance().warn("The required aurelium stat cannot be found: " + stat);
                        continue;
                    }

                    for (String sLvl : cfg.getSection(path + "aurelium-stat." + stat)) {
                        int itemLvl = StringUT.getInteger(sLvl, -1);
                        if (itemLvl <= 0) continue;

                        String reqRaw = cfg.getString(path + "aurelium-stat." + stat + "." + sLvl);
                        if (reqRaw == null || reqRaw.isEmpty()) continue;

                        String[] reqEdit = new String[]{stat, reqRaw.split(":")[0], reqRaw.split(":")[1]};
                        this.reqAureliumSkillsStat.put(itemLvl, reqEdit);
                    }
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
            this.addAttributeGenerator(new AttributeGenerator<>(this.plugin,
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

        @Nullable
        protected final String[] getMcMMOSkillsRequirement(int itemLvl) {
            if (this.reqMcMMOSkills == null)
                return null;

            Map.Entry<Integer, String[]> e = this.reqMcMMOSkills.floorEntry(itemLvl);
            if (e == null) return null;
            return e.getValue();
        }

        @Nullable
        protected final String[] getJobsRequirement(int itemLvl) {
            if (this.reqJobs == null)
                return null;

            Map.Entry<Integer, String[]> e = this.reqJobs.floorEntry(itemLvl);
            if (e == null) return null;
            return e.getValue();
        }

        @Nullable
        protected final String[] getAureliumSkillsSkillRequirement(int itemLvl) {
            if (this.reqAureliumSkillsSkill == null)
                return null;

            Map.Entry<Integer, String[]> e = this.reqAureliumSkillsSkill.floorEntry(itemLvl);
            if (e == null) return null;
            return e.getValue();
        }

        @Nullable
        protected final String[] getAureliumSkillsStatRequirement(int itemLvl) {
            if (this.reqAureliumSkillsStat == null)
                return null;

            Map.Entry<Integer, String[]> e = this.reqAureliumSkillsStat.floorEntry(itemLvl);
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
            } else {
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
            int enchRoll =
                    Rnd.get(this.getMinEnchantments(), this.getMaxEnchantments());
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
                BannedClassRequirement reqBannedClass =
                        ItemRequirements.getUserRequirement(BannedClassRequirement.class);
                if (reqBannedClass != null) {
                    reqBannedClass.add(item, bannedUserClass, -1);
                }
            }

            String[] mcmmo = getMcMMOSkillsRequirement(itemLvl);
            if (mcmmo != null) {
                McMMORequirement reqMcMMO = ItemRequirements.getUserRequirement(McMMORequirement.class);
                if (reqMcMMO != null) {
                    reqMcMMO.add(item, mcmmo, -1);
                }
            }

            String[] jobs = getJobsRequirement(itemLvl);
            if (jobs != null) {
                JobsRebornRequirement reqJobs = ItemRequirements.getUserRequirement(JobsRebornRequirement.class);
                if (reqJobs != null) {
                    reqJobs.add(item, jobs, -1);
                }
            }

            String[] aureliumSkillsSkills = getAureliumSkillsSkillRequirement(itemLvl);
            if (aureliumSkillsSkills != null) {
                AureliumSkillsSkillRequirement reqAureliumSkills = ItemRequirements.getUserRequirement(AureliumSkillsSkillRequirement.class);
                if (reqAureliumSkills != null) {
                    reqAureliumSkills.add(item, aureliumSkillsSkills, -1);
                }
            }

            String[] aureliumSkillsStats = getAureliumSkillsStatRequirement(itemLvl);
            if (aureliumSkillsStats != null) {
                AureliumSkillsStatRequirement reqAureliumStats = ItemRequirements.getUserRequirement(AureliumSkillsStatRequirement.class);
                if (reqAureliumStats != null) {
                    reqAureliumStats.add(item, aureliumSkillsStats, -1);
                }
            }

            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_LEVEL, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_CLASS, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_BANNED_CLASS, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_MCMMO_SKILL, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_JOBS_JOB, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_AURELIUM_SKILLS_SKILL, null);
            LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_REQ_USER_AURELIUM_SKILLS_STAT, null);

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

            for (ItemLoreStat<?> at : ItemStats.getStats()) {
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

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        AbstractEditorGUI editorInstance = AbstractEditorGUI.getInstance();
        if (editorInstance == null || !editorInstance.getPlayer().equals(event.getPlayer())) {
            return;
        }
        editorInstance.onChat(event);
    }
}
