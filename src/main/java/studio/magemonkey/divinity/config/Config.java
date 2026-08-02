package studio.magemonkey.divinity.config;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
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
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.*;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.BleedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.DurabilityStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.DynamicBuffStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.PenetrationStat;
import studio.magemonkey.divinity.stats.tiers.Tier;
import studio.magemonkey.divinity.types.ItemGroup;
import studio.magemonkey.divinity.types.ItemSubType;

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
        this.setupDamageBuffs();
        this.setupDefenseBuffs();
        this.setupPenetrations();
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

        // Seed the new MC 1.20.5+/1.21+ vanilla-attribute stats with their shipped defaults for
        // servers carrying forward a pre-upgrade stats.yml that predates them, so upgrading
        // servers get the new attributes working out of the box rather than silently disabled.
        addMissingVanillaAttributeStatDefaults(cfg);
        addMissingStatFoundationDefaults(cfg);
        addMissingSkillCritStatDefaults(cfg);

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

    private void addMissingVanillaAttributeStatDefaults(@NotNull JYML cfg) {
        addMissingStatDefault(cfg, "SCALE", "Scale", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "WATER_MOVEMENT_EFFICIENCY", "Water Movement Efficiency",
                "&3▸ %name%: &f%value% %condition%", 100.0);
        addMissingStatDefault(cfg, "MOVEMENT_EFFICIENCY", "Movement Efficiency",
                "&3▸ %name%: &f%value% %condition%", 100.0);
        addMissingStatDefault(cfg, "SNEAKING_SPEED", "Sneaking Speed", "&3▸ %name%: &f%value% %condition%", 70.0);
        addMissingStatDefault(cfg, "BLOCK_BREAK_SPEED", "Block Break Speed",
                "&3▸ %name%: &f%value% %condition%", 200.0);
        addMissingStatDefault(cfg, "BLOCK_INTERACTION_RANGE", "Block Reach",
                "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "ENTITY_INTERACTION_RANGE", "Entity Reach",
                "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "EXPLOSION_KNOCKBACK_RESISTANCE", "Explosion KB Resistance",
                "&3▸ %name%: &f%value% %condition%", 100.0);
        addMissingStatDefault(cfg, "FALL_DAMAGE_MULTIPLIER", "Fall Damage Modifier",
                "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "FLYING_SPEED", "Flying Speed", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "GRAVITY", "Gravity", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "JUMP_STRENGTH", "Jump Strength", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "MAX_ABSORPTION", "Max Absorption", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "MINING_EFFICIENCY", "Mining Efficiency",
                "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "OXYGEN_BONUS", "Oxygen Bonus", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SAFE_FALL_DISTANCE", "Safe Fall Distance",
                "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "STEP_HEIGHT", "Step Height", "&3▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SUBMERGED_MINING_SPEED", "Submerged Mining Speed",
                "&3▸ %name%: &f%value% %condition%", 100.0);
        cfg.saveChanges();
    }

    private void addMissingStatDefault(
            @NotNull JYML cfg, @NotNull String path, @NotNull String name, @NotNull String format, double capacity) {
        cfg.addMissing(path + ".enabled", true);
        cfg.addMissing(path + ".name", name);
        cfg.addMissing(path + ".format", format);
        cfg.addMissing(path + ".capacity", capacity);
    }

    /**
     * Seeds the stat-foundation entries (mana pool, CC, healing, and the reserved
     * summon/projectile/bleed/stun placeholders) into general_stats.yml so servers upgrading
     * with a pre-existing stats file still get them, the same way addMissingVanillaAttributeStatDefaults
     * backfills the vanilla-attribute stats.
     */
    private void addMissingStatFoundationDefaults(@NotNull JYML cfg) {
        addMissingStatDefault(cfg, "MAX_MANA", "Max Mana", "&9▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "CC_RESISTANCE", "CC Resistance", "&e▸ %name%: &f%value% %condition%", 60.0);
        addMissingStatDefault(cfg, "CC_DURATION", "CC Duration", "&e▸ %name%: &f%value% %condition%", -1.0);
        addMissingStatDefault(cfg, "HEALING_CAST", "Healing Cast", "&a▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "HEALING_RECEIVED", "Healing Received", "&a▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SKILL_EFFECTIVNESS", "Skill Effectivness",
                "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SUMMON_POWER", "Summon power", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SUMMON_HP", "Summon HP", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "SUMMON_DURATION", "Summon duration", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "PROJECTILE_COUNT", "Projectile Count", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "PROJECTILE_SPEED", "Projectile Speed", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "BLEED_STACKS", "Bleed Stacks", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "BLEED_DURATION", "Bleed Duration", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "BLEED_DAMAGEBUFF", "Bleed base", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "STUN_STACKS", "Stun Stacks", "&b▸ %name%: &f%value% %condition%", -1);
        addMissingStatDefault(cfg, "STUN_DURATION", "Stun Duration", "&b▸ %name%: &f%value% %condition%", -1);
        cfg.saveChanges();
    }

    /**
     * Seeds SKILL_CRITICAL_RATE/DAMAGE (Fabled-skill-only crit, separated from the auto-attack-only
     * CRITICAL_RATE/DAMAGE) into general_stats.yml so servers upgrading with a pre-existing stats
     * file still get them, the same way addMissingVanillaAttributeStatDefaults backfills the
     * vanilla-attribute stats.
     */
    private void addMissingSkillCritStatDefaults(@NotNull JYML cfg) {
        addMissingStatDefault(cfg, "SKILL_CRITICAL_RATE", "Skill Crit. Rate",
                "&a▸ %name%: &f%value% %condition%", 100.0);
        addMissingStatDefault(cfg, "SKILL_CRITICAL_DAMAGE", "Skill Crit. Dmg",
                "&a▸ %name%: &f%value% %condition%", 3.5);
        cfg.saveChanges();
    }

    private void setupDamageBuffs() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/stats/damage_buffs_percent.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load damage_buffs_percent config: Configuration error");
            e.printStackTrace();
            return;
        }

        for (DamageAttribute dmg : ItemStats.getDamages()) {
            String id   = dmg.getId();
            String path = id + ".";
            cfg.addMissing(path + "enabled", true);
            cfg.addMissing(path + "name", dmg.getName() + " Buff %");
            cfg.addMissing(path + "format", "&3▸ %name%: &f%value%%condition%");
            cfg.addMissing(path + "capacity", -1.0);
            cfg.addMissing(path + "hook", Collections.singletonList(id));
        }
        cfg.saveChanges();

        for (String buffId : cfg.getSection("")) {
            if (!cfg.getBoolean(buffId + ".enabled")) continue;
            String      name   = StringUT.color(cfg.getString(buffId + ".name", buffId));
            String      format = StringUT.color(cfg.getString(buffId + ".format", "&3▸ %name%: &f%value%"));
            double      cap    = cfg.getDouble(buffId + ".capacity", -1D);
            Set<String> hooks  = new HashSet<>(cfg.getStringList(buffId + ".hook"));

            DynamicBuffStat buff = new DynamicBuffStat(
                    DynamicBuffStat.BuffTarget.DAMAGE, buffId, name, format, hooks, cap);
            ItemStats.registerDamageBuff(buff);
        }
    }

    private void setupDefenseBuffs() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/stats/defense_buffs_percent.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load defense_buffs_percent config: Configuration error");
            e.printStackTrace();
            return;
        }

        for (DefenseAttribute def : ItemStats.getDefenses()) {
            String id   = def.getId();
            String path = id + ".";
            cfg.addMissing(path + "enabled", true);
            cfg.addMissing(path + "name", def.getName() + " Buff %");
            cfg.addMissing(path + "format", "&9▸ %name%: &f%value%%condition%");
            cfg.addMissing(path + "capacity", -1.0);
            cfg.addMissing(path + "hook", Collections.singletonList(id));
        }
        cfg.saveChanges();

        for (String buffId : cfg.getSection("")) {
            if (!cfg.getBoolean(buffId + ".enabled")) continue;
            String      name   = StringUT.color(cfg.getString(buffId + ".name", buffId));
            String      format = StringUT.color(cfg.getString(buffId + ".format", "&9▸ %name%: &f%value%"));
            double      cap    = cfg.getDouble(buffId + ".capacity", -1D);
            Set<String> hooks  = new HashSet<>(cfg.getStringList(buffId + ".hook"));

            DynamicBuffStat buff = new DynamicBuffStat(
                    DynamicBuffStat.BuffTarget.DEFENSE, buffId, name, format, hooks, cap);
            ItemStats.registerDefenseBuff(buff);
        }
    }

    private void setupPenetrations() {
        JYML cfg;
        try {
            cfg = JYML.loadOrExtract(plugin, "/item_stats/stats/penetration.yml");
        } catch (InvalidConfigurationException e) {
            this.plugin.error("Failed to load penetration config: Configuration error");
            e.printStackTrace();
            return;
        }

        // Auto-generate a flat-pen entry for every registered damage type (if missing)
        for (DamageAttribute dmg : ItemStats.getDamages()) {
            String id   = dmg.getId() + "_pen";
            String path = id + ".";
            cfg.addMissing(path + "enabled", true);
            cfg.addMissing(path + "name", dmg.getName() + " Penetration");
            cfg.addMissing(path + "format", "&c▸ %name%: &f%value%%condition%");
            cfg.addMissing(path + "capacity", -1.0);
            cfg.addMissing(path + "percent-pen", false);
            cfg.addMissing(path + "hooks", Collections.singletonList(dmg.getId()));
        }
        cfg.saveChanges();

        for (String penId : cfg.getSection("")) {
            if (!cfg.getBoolean(penId + ".enabled")) continue;
            String      name       = StringUT.color(cfg.getString(penId + ".name", penId));
            String      format     = StringUT.color(cfg.getString(penId + ".format", "&c▸ %name%: &f%value%"));
            double      cap        = cfg.getDouble(penId + ".capacity", -1D);
            boolean     percentPen = cfg.getBoolean(penId + ".percent-pen", false);
            Set<String> hooks      = new HashSet<>(cfg.getStringList(penId + ".hooks"));

            new PenetrationStat(penId, name, format, hooks, percentPen, cap);
        }
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