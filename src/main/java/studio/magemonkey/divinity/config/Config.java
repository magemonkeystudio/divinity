package studio.magemonkey.divinity.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.items.ItemType;
import studio.magemonkey.codex.config.api.IConfigTemplate;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.actions.ActionManipulator;
import studio.magemonkey.codex.util.constants.JStrings;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.stats.VanillaAttributeStat;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.*;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.BleedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.DurabilityStat;
import studio.magemonkey.divinity.stats.tiers.Tier;
import studio.magemonkey.divinity.types.ItemGroup;
import studio.magemonkey.divinity.types.ItemSubType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Config extends IConfigTemplate {

    public Config(@NotNull Divinity plugin) {
        super(plugin);
    }

    private static Map<String, Tier>        TIERS_MAP;
    private static Map<String, ItemSubType> ITEM_SUB_TYPES;

    @Override
    public void load() {
        String path = "tiers.";
        Config.TIERS_MAP = new LinkedHashMap<>();
        for (String tierId : cfg.getSection("tiers")) {
            String path2     = "tiers." + tierId + ".";
            String tierColor = cfg.getString(path2 + "color", "&f");
            String tierName  = cfg.getString(path2 + "name", tierId);

            Tier tier = new Tier(tierId, tierName, tierColor);
            TIERS_MAP.put(tier.getId(), tier);
        }


        for (ItemGroup itemGroup : ItemGroup.values()) {
            path = "item-groups." + itemGroup.name() + ".";

            itemGroup.setName(cfg.getString(path + "name", itemGroup.name()));
            itemGroup.setMaterials(cfg.getStringSet(path + "materials"));
        }


        ITEM_SUB_TYPES = new HashMap<>();
        for (String typeId : cfg.getSection("item-sub-types")) {
            path = "item-sub-types." + typeId + ".";
            String name = cfg.getString(path + "name", typeId);

            ItemSubType ist = new ItemSubType(typeId, name, cfg.getStringSet(path + "materials"));
            ITEM_SUB_TYPES.put(ist.getId(), ist);
        }
    }

    public void setupAttributes() {
        this.setupDamages();
        this.setupDefense();
        this.setupStats();
        this.setupHand();
        this.setupAmmo();
        this.setupSockets();
    }

    private void setupDamages() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/damage.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load damage config (" + this.plugin.getName()
                    + "/item_stats/damage.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        for (String dmgId : cfg.getSection("")) {
            String path = dmgId + ".";

            int               dmgPriority = cfg.getInt(path + "priority");
            String            dmgName     = StringUT.color(cfg.getString(path + "name", dmgId));
            String            dmgFormat   = StringUT.color(cfg.getString(path + "format", "%name%: &f%value%"));
            ActionManipulator dmgActions  = new ActionManipulator(plugin, cfg, path + "on-hit-actions");

            Set<String> dmgCauses = new HashSet<>();
            for (String sName : cfg.getStringList(path + "attached-damage-causes")) {
                dmgCauses.add(sName.toUpperCase());
            }

            Map<String, Double> dmgBiomeMod = new HashMap<>();
            for (String sName : cfg.getSection(path + "biome-damage-modifier")) {
                double bMod = cfg.getDouble(path + "biome-damage-modifier." + sName);
                dmgBiomeMod.put(sName.toUpperCase(), bMod);
            }

            Map<String, Double> dmgEntityMod = new HashMap<>();
            for (String eType : cfg.getSection(path + "entity-type-modifier")) {
                double dMod = cfg.getDouble(path + "entity-type-modifier." + eType);
                dmgEntityMod.put(eType.toUpperCase(), dMod);
            }

            Map<String, Double> mythicFactionMod = new HashMap<>();
            for (String eType : cfg.getSection(path + "mythic-mob-faction-modifier")) {
                double dMod = cfg.getDouble(path + "mythic-mob-faction-modifier." + eType);
                mythicFactionMod.put(eType.toLowerCase(), dMod);
            }

            DamageAttribute damageAttribute = new DamageAttribute(
                    dmgId,
                    dmgName,
                    dmgFormat,
                    dmgPriority,
                    dmgActions,
                    dmgCauses,
                    dmgBiomeMod,
                    dmgEntityMod,
                    mythicFactionMod
            );

            ItemStats.registerDamage(damageAttribute);
        }
    }

    private void setupDefense() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/defense.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load defense config (" + this.plugin.getName()
                    + "/item_stats/defense.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        for (String defId : cfg.getSection("")) {
            String path = defId + ".";

            cfg.addMissing(path + "protection-factor", 0.25D);
            cfg.saveChanges();

            int    defPriority = cfg.getInt(path + "priority");
            String defFormat   = StringUT.color(cfg.getString(path + "format", "&6▸ %name%: &f%value%"));
            String defName     = StringUT.color(cfg.getString(path + "name", defId));

            Set<String> defDamages = new HashSet<>();
            for (String s : cfg.getStringList(path + "block-damage-types")) {
                defDamages.add(s.toLowerCase());
            }
            double defProtFactor = cfg.getDouble(path + "protection-factor", 0.25D);

            DefenseAttribute defenseAttribute = new DefenseAttribute(
                    defId,
                    defName,
                    defFormat,
                    defPriority,
                    defDamages,
                    defProtFactor);

            ItemStats.registerDefense(defenseAttribute);
        }
    }

    /**
     * item_stats/stats.yml was relocated to item_stats/stats/general_stats.yml. Servers upgrading
     * with an already-customized stats.yml would otherwise have it orphaned and silently replaced
     * by a fresh, default-only general_stats.yml. If the old file is still present and the new one
     * hasn't been created yet, carry the old file's content forward so existing customizations
     * (names, formats, capacities, enabled/disabled toggles) survive the upgrade.
     */
    private void migrateLegacyStatsFile() {
        java.io.File oldFile = new java.io.File(plugin.getDataFolder(), "item_stats/stats.yml");
        java.io.File newFile = new java.io.File(plugin.getDataFolder(), "item_stats/stats/general_stats.yml");
        if (!oldFile.exists() || newFile.exists()) return;

        try {
            newFile.getParentFile().mkdirs();
            java.nio.file.Files.copy(oldFile.toPath(), newFile.toPath());
            this.plugin.info("Migrated item_stats/stats.yml to item_stats/stats/general_stats.yml");
        } catch (java.io.IOException e) {
            this.plugin.error("Failed to migrate item_stats/stats.yml to the new "
                    + "item_stats/stats/general_stats.yml location: " + e.getMessage());
        }
    }

    private void setupStats() {
        this.migrateLegacyStatsFile();

        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/stats/general_stats.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load stats config (" + this.plugin.getName()
                    + "/item_stats/stats/general_stats.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        this.addMissingStatDefaults(cfg);

        for (SimpleStat.Type statType : TypedStat.Type.values()) {
            String path2 = statType.name() + ".";
            if (!cfg.getBoolean(path2 + "enabled")) {
                continue;
            }

            String statName   = StringUT.color(cfg.getString(path2 + "name", statType.name()));
            String statFormat = StringUT.color(cfg.getString(path2 + "format", "&a▸ %name%: &f%value%"));
            double statCap    = cfg.getDouble(path2 + "capacity", -1D);

            TypedStat stat;
            if (statType == TypedStat.Type.DURABILITY) {
                stat = new DurabilityStat(statName, statFormat, statCap);
            } else if (statType == TypedStat.Type.BLEED_RATE) {
                String  formula  = cfg.getString(path2 + "settings.damage", "%damage% * 0.5");
                boolean ofMax    = cfg.getBoolean(path2 + "settings.of-max-health");
                double  duration = cfg.getDouble(path2 + "settings.duration", 10);
                stat = new BleedStat(statName, statFormat, statCap, formula, ofMax, duration);
            } else {
                stat = new SimpleStat(statType, statName, statFormat, statCap);
            }

            ItemStats.registerStat(stat);
        }
    }

    /**
     * Copies any missing keys for stats added after servers already had a stats file from the bundled
     * general_stats.yml, so a carried-forward file gets them enabled with their shipped defaults instead of
     * silently disabled by an absent section. The bundled file stays the single source of those defaults.
     * <p>
     * Limited to these stats on purpose: for older stats a missing section may be an admin's deliberate removal.
     */
    private void addMissingStatDefaults(@NotNull JYML cfg) {
        YamlConfiguration defaults;
        try (InputStream in = plugin.getResource("item_stats/stats/general_stats.yml")) {
            if (in == null) {
                this.plugin.error("Bundled item_stats/stats/general_stats.yml is missing; cannot add new stat defaults");
                return;
            }
            defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.plugin.error("Failed to read bundled item_stats/stats/general_stats.yml: " + e.getMessage());
            return;
        }

        List<String> stats = new ArrayList<>();
        stats.add(TypedStat.Type.ARMOR_TOUGHNESS.name());
        for (VanillaAttributeStat stat : VanillaAttributeStat.values()) {
            stats.add(stat.getStatType().name());
        }

        for (String stat : stats) {
            ConfigurationSection section = defaults.getConfigurationSection(stat);
            if (section == null) continue;
            for (String key : section.getKeys(true)) {
                if (!section.isConfigurationSection(key)) {
                    cfg.addMissing(stat + "." + key, section.get(key));
                }
            }
        }
        cfg.saveChanges();
    }

    private void setupHand() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/hand.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load hand config (" + this.plugin.getName()
                    + "/item_stats/hand.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        for (HandAttribute.Type handType : HandAttribute.Type.values()) {
            String path2 = handType.name() + ".";
            if (!cfg.getBoolean(path2 + "enabled")) {
                continue;
            }

            String handName   = StringUT.color(cfg.getString(path2 + "name", handType.name()));
            String handFormat = StringUT.color(cfg.getString(path2 + "format", "&7Hand: &f%name%"));

            HandAttribute hand = new HandAttribute(handType, handName, handFormat);
            ItemStats.registerHand(hand);
        }
    }

    private void setupAmmo() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/ammo.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load ammo config (" + this.plugin.getName()
                    + "/item_stats/ammo.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        for (AmmoAttribute.Type ammoType : AmmoAttribute.Type.values()) {
            String path2 = ammoType.name() + ".";
            if (!cfg.getBoolean(path2 + "enabled")) {
                continue;
            }

            String ammoName   = StringUT.color(cfg.getString(path2 + "name", ammoType.name()));
            String ammoFormat = StringUT.color(cfg.getString(path2 + "format", "&7Ammo Type: &f%name%"));

            AmmoAttribute ammo = new AmmoAttribute(ammoType, ammoName, ammoFormat);
            ItemStats.registerAmmo(ammo);
        }
    }

    private void setupSockets() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/sockets.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load sockets config (" + this.plugin.getName()
                    + "/item_stats/sockets.yml): Configuration error");
            e.printStackTrace();
            return;
        }

        for (SocketAttribute.Type type : SocketAttribute.Type.values()) {
            String path = type.name() + ".";

            for (String catId : cfg.getSection(path + "categories")) {
                String path2             = path + "categories." + catId + ".";
                String catTierId         = cfg.getString(path2 + "tier", JStrings.DEFAULT);
                String catName           = cfg.getString(path2 + "name", catId);
                String catFormatMain     = cfg.getString(path2 + "format.main", "%value%");
                String catFormatValEmpty = cfg.getString(path2 + "format.value.empty", "%TIER_COLOR%□ <%name%>");
                String catFormatValFill  = cfg.getString(path2 + "format.value.filled", "%TIER_COLOR%▣ &7%value%");

                Tier catTier = Config.getTier(catTierId);
                if (catTier == null) {
                    plugin.error("Invalid tier '" + catTierId + "' for Socket Attribute '" + catId + "' !");
                    continue;
                }

                SocketAttribute socket = new SocketAttribute(
                        type,

                        catId,
                        catName,
                        catFormatMain,

                        catTier,

                        catFormatValEmpty,
                        catFormatValFill);

                ItemStats.registerSocket(socket);
            }
        }
    }

    @Nullable
    public static Tier getTier(@NotNull String id) {
        return TIERS_MAP.get(id.toLowerCase());
    }

    @NotNull
    public static Collection<Tier> getTiers() {
        return TIERS_MAP.values();
    }

    @NotNull
    public static Set<String> getSubTypeIds() {
        return new HashSet<>(ITEM_SUB_TYPES.keySet());
    }

    @Nullable
    public static ItemSubType getSubTypeById(@NotNull String id) {
        return ITEM_SUB_TYPES.get(id.toLowerCase());
    }

    @Nullable
    public static ItemSubType getItemSubType(@NotNull ItemStack item) {
        return ITEM_SUB_TYPES.values().stream().filter(itemSubType -> itemSubType.isItemOfThis(item))
                .findFirst().orElse(null);
    }

    @Deprecated
    @Nullable
    public static ItemSubType getItemSubType(@NotNull Material material) {
        return getItemSubType(material.name());
    }

    @Nullable
    public static ItemSubType getItemSubType(@NotNull String mat) {
        return ITEM_SUB_TYPES.values().stream().filter(type -> type.isItemOfThis(mat)).findFirst().orElse(null);
    }

    @NotNull
    public static Set<ItemType> getAllRegisteredMaterials() {
        Set<ItemType> set = new HashSet<>();

        for (ItemGroup group : ItemGroup.values()) {
            set.addAll(group.getMaterials());
        }

        return set;
    }
}