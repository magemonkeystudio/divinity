package su.nightexpress.quantumrpg.config;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.hooks.NHook;
import mc.promcteam.engine.utils.StringUT;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.hooks.HookMobLevel;
import su.nightexpress.quantumrpg.hooks.internal.DefaultHook;
import su.nightexpress.quantumrpg.hooks.internal.QuantumRPGHook;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.ChargesAttribute;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.item.*;
import su.nightexpress.quantumrpg.stats.items.requirements.user.BannedClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.ClassRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.LevelRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.JobsRebornRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.McMMORequirement;

import java.util.*;

public class EngineCfg {

    private QuantumRPG plugin;
    private JYML cfg;

    public EngineCfg(@NotNull QuantumRPG plugin) {
        this.plugin = plugin;
        this.cfg = JYML.loadOrExtract(plugin, "engine.yml");
    }

    public static boolean PACKETS_ENABLED;
    public static boolean PACKETS_MOD_GLOW_COLOR;
    public static boolean PACKETS_REDUCE_COMBAT_PARTICLES;


    public static HookLevel HOOK_PLAYER_LEVEL_PLUGIN;
    public static HookClass HOOK_PLAYER_CLASS_PLUGIN;
    public static HookMobLevel HOOK_MOB_LEVEL_PLUGIN;

    public static boolean ATTRIBUTES_EFFECTIVE_FOR_MOBS;
    public static boolean ATTRIBUTES_EFFECTIVE_IN_OFFHAND;
    public static boolean ATTRIBUTES_ALLOW_HOLD_REQUIREMENTS;

    public static boolean ATTRIBUTES_DURABILITY_BREAK_ITEMS;
    public static boolean ATTRIBUTES_DURABILITY_REDUCE_FOR_MOBS;
    public static boolean ATTRIBUTES_DURABILITY_REDUCE_FOR_SKILL_API;


    public static double COMBAT_SHIELD_BLOCK_BONUS_RATE;
    public static double COMBAT_SHIELD_BLOCK_BONUS_DAMAGE_MOD;
    public static int COMBAT_SHIELD_BLOCK_COOLDOWN;
    public static boolean COMBAT_DISABLE_VANILLA_SWEEP;
    public static boolean COMBAT_REDUCE_PLAYER_HEALTH_BAR;
    public static boolean COMBAT_FISHING_HOOK_DO_DAMAGE;
    public static boolean COMBAT_BOWS_DO_FULL_MELEE_DAMAGE;
    public static double COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN;
    public static double COMBAT_MAX_GET_TARGET_DISTANCE;

    public static String LORE_CHAR_PERCENT;
    public static String LORE_CHAR_NEGATIVE;
    public static String LORE_CHAR_POSITIVE;
    public static String LORE_CHAR_MULTIPLIER;

    public static String LORE_STYLE_SEPAR_VALUE;
    public static String LORE_STYLE_SEPAR_COLOR;

    public static String LORE_STYLE_DAMAGE_FORMAT_SINGLE;
    public static String LORE_STYLE_DAMAGE_FORMAT_RANGE;

    private static String LORE_STYLE_DURA_FORMAT_NAME;
    private static String LORE_STYLE_DURA_FORMAT_UNBREAKABLE;
    private static TreeMap<Integer, String> LORE_STYLE_DURA_FORMAT_MAP;

    public static String LORE_STYLE_ATT_CHARGES_FORMAT_DEFAULT;
    public static String LORE_STYLE_ATT_CHARGES_FORMAT_UNLIMITED;
    public static boolean CHARGES_BREAK_ITEMS_ENABLED;
    public static Set<String> CHARGES_BREAK_ITEMS_STOP_MODULES;

    public static boolean LORE_STYLE_REQ_USER_DYN_UPDATE = true;
    private static Map<Boolean, String> LORE_STYLE_REQ_USER_DYN_STATE;

    public static String LORE_STYLE_REQ_USER_LVL_FORMAT_SINGLE;
    public static String LORE_STYLE_REQ_USER_LVL_FORMAT_RANGE;

    public static String LORE_STYLE_REQ_USER_CLASS_FORMAT_SEPAR;
    public static String LORE_STYLE_REQ_USER_CLASS_FORMAT_COLOR;
    public static int LORE_STYLE_REQ_USER_CLASS_FORMAT_MAX;
    public static String LORE_STYLE_REQ_USER_CLASS_FORMAT_NEWLINE;

    public static String LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_SEPAR;
    public static String LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_COLOR;
    public static int LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_MAX;
    public static String LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_NEWLINE;

    public static String LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_SINGLE;
    public static String LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_RANGE;
    public static String LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_SINGLE;
    public static String LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_RANGE;

    public static String LORE_STYLE_REQ_ITEM_LVL_FORMAT_SINGLE;
    public static String LORE_STYLE_REQ_ITEM_LVL_FORMAT_RANGE;

    public static String LORE_STYLE_REQ_ITEM_TYPE_FORMAT_SEPAR;
    public static String LORE_STYLE_REQ_ITEM_TYPE_FORMAT_COLOR;

    public static String LORE_STYLE_REQ_ITEM_MODULE_FORMAT_SEPAR;
    public static String LORE_STYLE_REQ_ITEM_MODULE_FORMAT_COLOR;

    public static String LORE_STYLE_ENCHANTMENTS_FORMAT_MAIN;
    public static int LORE_STYLE_ENCHANTMENTS_FORMAT_MAX_ROMAN;

    public static String LORE_STYLE_SKILLAPI_ATTRIBUTE_FORMAT;

    public void setup() {
        this.plugin.info("Loading engine configuration...");

        // P A C K E T S //

        String path = "packets.";
        EngineCfg.PACKETS_ENABLED = cfg.getBoolean(path + "enabled");
        EngineCfg.PACKETS_MOD_GLOW_COLOR = cfg.getBoolean(path + "modules.glow-color") && plugin.cfg().isModuleEnabled(EModule.ITEM_HINTS);
        EngineCfg.PACKETS_REDUCE_COMBAT_PARTICLES = cfg.getBoolean(path + "modules.reduce-damage-particles");

        // C O M P A T I B I L I T Y //

        path = "compatibility.";
        QuantumRPGHook internalHook = new QuantumRPGHook(this.plugin);
        DefaultHook defHook = new DefaultHook(this.plugin);

        String pUserLevel = cfg.getString(path + "player-level-plugin", plugin.getName());
        String pUserClass = cfg.getString(path + "player-class-plugin", plugin.getName());

        if (pUserLevel.equalsIgnoreCase(plugin.getName()) && plugin.cfg().isModuleEnabled(EModule.CLASSES)) {
            EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN = internalHook;
        } else {
            NHook<?> hookLevel = plugin.getHook(pUserLevel);
            if (hookLevel instanceof HookLevel) {
                EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN = (HookLevel) hookLevel;
                this.plugin.info("Using " + hookLevel.getPlugin() + " as a player level plugin.");
            } else {
                EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN = defHook;
                this.plugin.info("Using Vanilla Exp as a player level plugin.");
            }
        }

        if (pUserClass.equalsIgnoreCase(plugin.getName()) && plugin.cfg().isModuleEnabled(EModule.CLASSES)) {
            EngineCfg.HOOK_PLAYER_CLASS_PLUGIN = internalHook;
        } else {
            NHook<?> hookClass = plugin.getHook(pUserClass);
            if (hookClass instanceof HookClass) {
                EngineCfg.HOOK_PLAYER_CLASS_PLUGIN = (HookClass) hookClass;
                this.plugin.info("Using " + hookClass.getPlugin() + " as a player class plugin.");
            } else {
                EngineCfg.HOOK_PLAYER_CLASS_PLUGIN = defHook;
                this.plugin.info("Using Vault Permissions as a player class plugin.");
            }
        }

        NHook<?> hookMobLevel = plugin.getHook(cfg.getString(path + "mob-level-plugin", "null"));
        if (hookMobLevel != null && hookMobLevel instanceof HookMobLevel) {
            EngineCfg.HOOK_MOB_LEVEL_PLUGIN = (HookMobLevel) hookMobLevel;
        } else {
            EngineCfg.HOOK_MOB_LEVEL_PLUGIN = defHook;
        }

        // P E R F O R M A N C E //

        path = "performance.";

        path = "performance.entity-stats.";

        // A T T R I B U T E S //
        path = "attributes.";
        EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS = cfg.getBoolean(path + "effective-for-mobs");
        EngineCfg.ATTRIBUTES_EFFECTIVE_IN_OFFHAND = cfg.getBoolean(path + "effective-in-offhand");
        EngineCfg.ATTRIBUTES_ALLOW_HOLD_REQUIREMENTS = cfg.getBoolean(path + "allow-hold-items-you-cant-use");

        path = "attributes.durability.";
        EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS = cfg.getBoolean(path + "break-items-on-zero");
        EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_MOBS = cfg.getBoolean(path + "effective-for.mobs");
        EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_SKILL_API = cfg.getBoolean(path + "effective-for.skill-api-skills");

        // C O M B A T //

        path = "combat.";
        EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP = cfg.getBoolean(path + "disable-vanilla-sweep-attack");
        EngineCfg.COMBAT_REDUCE_PLAYER_HEALTH_BAR = cfg.getBoolean(path + "compress-player-health-bar");
        EngineCfg.COMBAT_FISHING_HOOK_DO_DAMAGE = cfg.getBoolean(path + "fishing-hook-do-damage");
        EngineCfg.COMBAT_BOWS_DO_FULL_MELEE_DAMAGE = cfg.getBoolean(path + "bows-do-full-melee-damage");
        EngineCfg.COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN = cfg.getDouble(path + "damage-modifier-for-cooldown");
        EngineCfg.COMBAT_MAX_GET_TARGET_DISTANCE = cfg.getDouble(path + "max-get-target-distance", 30D);

        path = "combat.shield.block.";
        COMBAT_SHIELD_BLOCK_BONUS_RATE = cfg.getDouble(path + "block-rate-bonus", 100D);
        COMBAT_SHIELD_BLOCK_BONUS_DAMAGE_MOD = cfg.getDouble(path + "block-damage-bonus", 35D);
        COMBAT_SHIELD_BLOCK_COOLDOWN = cfg.getInt(path + "cooldown", 2);

        // L O R E //

        path = "lore.chars.";
        EngineCfg.LORE_CHAR_PERCENT = StringUT.color(cfg.getString(path + "percent", "%"));
        EngineCfg.LORE_CHAR_POSITIVE = StringUT.color(cfg.getString(path + "positive", "&a+"));
        EngineCfg.LORE_CHAR_NEGATIVE = StringUT.color(cfg.getString(path + "negative", "&c"));
        EngineCfg.LORE_CHAR_MULTIPLIER = StringUT.color(cfg.getString(path + "multiplier", "x"));

        path = "lore.stats.style.separator.";
        EngineCfg.LORE_STYLE_SEPAR_VALUE = StringUT.color(cfg.getString(path + "value", "&7/"));
        EngineCfg.LORE_STYLE_SEPAR_COLOR = StringUT.color(cfg.getString(path + "color", "&f"));

        path = "lore.stats.style.damage.";
        EngineCfg.LORE_STYLE_DAMAGE_FORMAT_SINGLE = StringUT.color(cfg.getString(path + "format.single", "&f%value%"));
        EngineCfg.LORE_STYLE_DAMAGE_FORMAT_RANGE = StringUT.color(cfg.getString(path + "format.double", "&f%min% &7- &f%max%"));

        path = "lore.stats.style.durability.";
        EngineCfg.LORE_STYLE_DURA_FORMAT_MAP = new TreeMap<>();
        EngineCfg.LORE_STYLE_DURA_FORMAT_NAME = cfg.getString(path + "format", "default");
        EngineCfg.LORE_STYLE_DURA_FORMAT_UNBREAKABLE = StringUT.color(cfg.getString(path + "unbreakable", "&fUnbreakable"));

        for (String pId : cfg.getSection(path + "format-list." + EngineCfg.LORE_STYLE_DURA_FORMAT_NAME)) {
            String format = cfg.getString(path + "format-list." + EngineCfg.LORE_STYLE_DURA_FORMAT_NAME + "." + pId);
            if (format == null) continue;
            int perc = StringUT.getInteger(pId, -1);
            if (perc < 0) continue;

            EngineCfg.LORE_STYLE_DURA_FORMAT_MAP.put(perc, StringUT.color(format));
        }
        if (EngineCfg.LORE_STYLE_DURA_FORMAT_MAP.isEmpty()) {
            EngineCfg.LORE_STYLE_DURA_FORMAT_MAP.put(0, StringUT.color("&f%current%&7/&f%max%"));
            plugin.error("Invalid durability format provided. Using default value.");
        }

        path = "lore.stats.style.charges.";
        cfg.addMissing(path + "break-items-on-zero.enabled", true);
        cfg.addMissing(path + "break-items-on-zero.excluded-modules", Arrays.asList("item_generator"));

        if (cfg.getBoolean(path + "enabled")) {
            String aName = StringUT.color(cfg.getString(path + "name", "Charges"));
            String aFormat = StringUT.color(cfg.getString(path + "format.main", "&7%name%: &f%value%"));

            EngineCfg.LORE_STYLE_ATT_CHARGES_FORMAT_DEFAULT = StringUT.color(cfg.getString(path + "format.value.default", "&f%min%&7/&f%max%"));
            EngineCfg.LORE_STYLE_ATT_CHARGES_FORMAT_UNLIMITED = StringUT.color(cfg.getString(path + "format.value.unlimited", "Unlimited"));

            if (CHARGES_BREAK_ITEMS_ENABLED = cfg.getBoolean(path + "break-items-on-zero.enabled", true)) {
                CHARGES_BREAK_ITEMS_STOP_MODULES = cfg.getStringSet(path + "break-items-on-zero.excluded-modules");
            }

            ChargesAttribute charges = new ChargesAttribute(aName, aFormat);
            ItemStats.registerAttribute(charges);
        }

        path = "lore.stats.style.requirements.user.dynamic.";
        if (EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE = cfg.getBoolean(path + "enabled")) {
            EngineCfg.LORE_STYLE_REQ_USER_DYN_STATE = new HashMap<>();
            for (boolean b : new boolean[]{true, false}) {
                String reqState = cfg.getString(path + "format." + String.valueOf(b));
                if (reqState == null) {
                    if (b) reqState = "&a&l✓ &r&a";
                    else reqState = "&c&l✗ &r&c";
                }
                EngineCfg.LORE_STYLE_REQ_USER_DYN_STATE.put(b, StringUT.color(reqState));
            }
        }

        path = "lore.stats.style.requirements.user.level.";
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Player Level"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "%state%%name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_USER_LVL_FORMAT_SINGLE = StringUT.color(cfg.getString(path + "format.value.single", "%value%+"));
            EngineCfg.LORE_STYLE_REQ_USER_LVL_FORMAT_RANGE = StringUT.color(cfg.getString(path + "format.value.range", "%min%-%max%"));

            LevelRequirement reqLvl = new LevelRequirement(rName, rFormat);
            ItemRequirements.registerUserRequirement(reqLvl);
        }

        path = "lore.stats.style.requirements.user.class.";
        cfg.addMissing(path + "format.max-classes-per-line", -1);
        cfg.addMissing(path + "format.value.newline", "&7  ");
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Player Class"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "%state%%name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_SEPAR = StringUT.color(cfg.getString(path + "format.value.separator", "&7/"));
            EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_COLOR = StringUT.color(cfg.getString(path + "format.value.color", "&f"));
            EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_MAX = cfg.getInt(path + "format.max-classes-per-line", -1);
            EngineCfg.LORE_STYLE_REQ_USER_CLASS_FORMAT_NEWLINE = StringUT.color(cfg.getString(path + "format.value.newline", "&7  "));

            ClassRequirement reqClass = new ClassRequirement(rName, rFormat);
            ItemRequirements.registerUserRequirement(reqClass);
        }

        path = "lore.stats.style.requirements.user.banned-class.";
        cfg.addMissing(path + "enabled", true);
        cfg.addMissing(path + "name", "Banned Player Class");
        cfg.addMissing(path + "format.main", "%state%%name%: %value%");
        cfg.addMissing(path + "format.value.separator", "&7/");
        cfg.addMissing(path + "format.value.color", "&f");
        cfg.addMissing(path + "format.max-classes-per-line", -1);
        cfg.addMissing(path + "format.value.newline", "&7  ");
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "McMMO Skill"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "%state%%name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_SEPAR = StringUT.color(cfg.getString(path + "format.value.separator", "&7/"));
            EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_COLOR = StringUT.color(cfg.getString(path + "format.value.color", "&f"));
            EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_MAX = cfg.getInt(path + "format.max-classes-per-line", 4);
            EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_NEWLINE = StringUT.color(cfg.getString(path + "format.value.newline", "&7  "));

            BannedClassRequirement bannedClass = new BannedClassRequirement(rName, rFormat);
            ItemRequirements.registerUserRequirement(bannedClass);
        }

        path = "lore.stats.style.requirements.user.extensions.mcmmo-skill.";
        cfg.addMissing(path + "enabled", true);
        cfg.addMissing(path + "name", "McMMO");
        cfg.addMissing(path + "format.main", "%state%%name%: %value%");
        cfg.addMissing(path + "format.value.single", "%skill% | %min%+");
        cfg.addMissing(path + "format.value.range", "%skill% | %min%-%max%&f");
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "McMMO"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "%state%%name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_SINGLE = StringUT.color(cfg.getString(path + "format.value.single", "%skill% | %min%+"));
            EngineCfg.LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_RANGE = StringUT.color(cfg.getString(path + "format.value.range", "%skill% | %min%-%max%&f"));

            McMMORequirement mcMMO = new McMMORequirement(rName, rFormat);
            if (QuantumRPG.getInstance().isHooked(EHook.MCMMO))
                ItemRequirements.registerUserRequirement(mcMMO);
        }

        path = "lore.stats.style.requirements.user.extensions.jobs-job.";
        cfg.addMissing(path + "enabled", true);
        cfg.addMissing(path + "name", "Jobs");
        cfg.addMissing(path + "format.main", "%state%%name%: %value%");
        cfg.addMissing(path + "format.value.single", "%job% | %min%+");
        cfg.addMissing(path + "format.value.range", "%job% | %min%-%max%&f");
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Jobs"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "%state%%name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_SINGLE = StringUT.color(cfg.getString(path + "format.value.single", "%job% | %min%+"));
            EngineCfg.LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_RANGE = StringUT.color(cfg.getString(path + "format.value.range", "%job% | %min%-%max%&f"));

            JobsRebornRequirement jobs = new JobsRebornRequirement(rName, rFormat);
            if (QuantumRPG.getInstance().isHooked(EHook.JOBS))
                ItemRequirements.registerUserRequirement(jobs);
        }

        path = "lore.stats.style.requirements.item.level.";
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Item Level"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_ITEM_LVL_FORMAT_SINGLE = StringUT.color(cfg.getString(path + "format.value.single", "%value%+"));
            EngineCfg.LORE_STYLE_REQ_ITEM_LVL_FORMAT_RANGE = StringUT.color(cfg.getString(path + "format.value.range", "%min%-%max%"));

            ItemLevelRequirement reqLevel = new ItemLevelRequirement(rName, rFormat);
            ItemRequirements.registerItemRequirement(reqLevel);
        }

        path = "lore.stats.style.requirements.item.type.";
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Item Type"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_ITEM_TYPE_FORMAT_SEPAR = StringUT.color(cfg.getString(path + "format.value.separator", "&7/"));
            EngineCfg.LORE_STYLE_REQ_ITEM_TYPE_FORMAT_COLOR = StringUT.color(cfg.getString(path + "format.value.color", "&f"));

            ItemTypeRequirement reqClass = new ItemTypeRequirement(rName, rFormat);
            ItemRequirements.registerItemRequirement(reqClass);
        }

        path = "lore.stats.style.requirements.item.module.";
        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Item Module"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name%: %value%"));

            EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_SEPAR = StringUT.color(cfg.getString(path + "format.value.separator", "&7/"));
            EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_COLOR = StringUT.color(cfg.getString(path + "format.value.color", "&f"));

            ItemModuleRequirement reqModule = new ItemModuleRequirement(rName, rFormat);
            ItemRequirements.registerItemRequirement(reqModule);
        }

        path = "lore.stats.style.requirements.item.socket.";
        if (plugin.cfg().isModuleEnabled(EModule.GEMS) || plugin.cfg().isModuleEnabled(EModule.ESSENCES) || plugin.cfg().isModuleEnabled(EModule.RUNES)) {
            String rName = StringUT.color(cfg.getString(path + "name", "Socket"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name%: %value%"));

            ItemSocketRequirement reqSOcket = new ItemSocketRequirement(rName, rFormat);
            ItemRequirements.registerItemRequirement(reqSOcket);
        }

        path = "lore.stats.style.requirements.item.tier.";
        cfg.addMissing(path + "enabled", true);
        cfg.addMissing(path + "name", "Tier");
        cfg.addMissing(path + "format.main", "&c▸ %name%: %value%");

        if (cfg.getBoolean(path + "enabled")) {
            String rName = StringUT.color(cfg.getString(path + "name", "Tier"));
            String rFormat = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name%: %value%"));

            ItemTierRequirement tierRequirement = new ItemTierRequirement(rName, rFormat);
            ItemRequirements.registerItemRequirement(tierRequirement);
        }

        path = "lore.stats.style.enchantments.";
        cfg.addMissing(path + "format.main", "&c▸ %name% %value%");
        cfg.addMissing(path + "format.max-roman", 10);
        EngineCfg.LORE_STYLE_ENCHANTMENTS_FORMAT_MAIN = StringUT.color(cfg.getString(path + "format.main", "&c▸ %name% %value%"));
        EngineCfg.LORE_STYLE_ENCHANTMENTS_FORMAT_MAX_ROMAN = cfg.getInt(path + "format.max-roman", 10);

        path = "lore.stats.style.skillapi-attribute-format";
        cfg.addMissing(path, "&7%attrPre%&3%name%&7%attrPost%");
        EngineCfg.LORE_STYLE_SKILLAPI_ATTRIBUTE_FORMAT = StringUT.color(cfg.getString(path, "&7%attrPre%&3%name%&7%attrPost%"));

        cfg.saveChanges();
    }

    @NotNull
    public static String getDurabilityFormat(int current, int max) {
        if (current < 0) {
            return EngineCfg.LORE_STYLE_DURA_FORMAT_UNBREAKABLE;
        }

        int percent = (int) (((double) current / (double) max) * 100D);
        String best = EngineCfg.LORE_STYLE_DURA_FORMAT_MAP.floorEntry(percent).getValue();

        return best
                .replace("%max%", String.valueOf(max))
                .replace("%current%", String.valueOf(current))
                .replace("%percent%", String.valueOf(percent));
    }

    @NotNull
    public static String getDynamicRequirementState(boolean b) {
        if (!EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE) {
            throw new IllegalStateException("Dynamic Lore Requirements update is disabled! (#030)");
        }
        return EngineCfg.LORE_STYLE_REQ_USER_DYN_STATE.get(b);
    }
}
