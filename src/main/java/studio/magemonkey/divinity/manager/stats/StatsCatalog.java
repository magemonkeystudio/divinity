package studio.magemonkey.divinity.manager.stats;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.core.Version;
import studio.magemonkey.codex.util.NumberUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.modules.list.classes.ClassManager;
import studio.magemonkey.divinity.modules.list.classes.api.UserClassData;
import studio.magemonkey.divinity.modules.list.classes.object.ClassAttributeType;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.stats.items.attributes.FabledAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.player.PlayerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER;

public final class StatsCatalog {

    private static final String DEFAULT_TITLE = "&8Combat Stats";
    private static final List<Integer> DEFAULT_DYNAMIC_SLOTS = defaultDynamicSlots();

    private final Divinity plugin;
    private final JYML config;
    private final List<Definition> definitions;

    public StatsCatalog(@NotNull Divinity plugin) throws Exception {
        this.plugin = plugin;
        this.config = JYML.loadOrExtract(plugin, "/stats/settings.yml");
        this.definitions = this.discoverDefinitions();
        this.ensureDefaults();
        this.config.saveChanges();
    }

    public boolean isEnabled() {
        return this.config.getBoolean("enabled", true);
    }

    public boolean reloadSettings() throws org.bukkit.configuration.InvalidConfigurationException {
        if (!this.config.reload()) return false;
        this.ensureDefaults();
        this.config.saveChanges();
        return true;
    }

    @NotNull
    public String getTitle() {
        return StringUT.color(this.config.getString("ui.title", DEFAULT_TITLE));
    }

    public int getInventorySize() {
        int size = this.config.getInt("ui.size", 54);
        if (size < 18 || size > 54 || size % 9 != 0) return 54;
        return size;
    }

    public int getPreviousSlot() {
        return this.getNavigationSlot("previous-slot", 0);
    }

    public int getCloseSlot() {
        return this.getNavigationSlot("close-slot", 4);
    }

    public int getNextSlot() {
        return this.getNavigationSlot("next-slot", 8);
    }

    @NotNull
    public List<Integer> getDynamicSlots() {
        int contentLimit = this.getInventorySize() - 9;
        List<Integer> configured = this.config.getIntegerList("ui.dynamic-slots");
        List<Integer> result = new ArrayList<>();
        for (Integer slot : configured) {
            if (slot != null && slot >= 0 && slot < contentLimit && !result.contains(slot)) {
                result.add(slot);
            }
        }
        return result;
    }

    @NotNull
    public String getLabel(@NotNull String key, @NotNull String fallback) {
        return StringUT.color(this.config.getString("ui.labels." + key, fallback));
    }

    @NotNull
    public List<String> getLoreTemplates(@NotNull String type) {
        return this.config.getStringList("ui.lore." + type);
    }

    public double getZeroThreshold() {
        return Math.max(0D, this.config.getDouble("dynamic-zero-threshold", 0.0001D));
    }

    private void hideAttributeTooltip(@NotNull ItemMeta meta) {
        if (Version.CURRENT.isAtLeast(Version.V1_20_R4)) {
            Attribute movementSpeed = VersionManager.getNms().getAttribute("MOVEMENT_SPEED");
            if (movementSpeed != null
                    && (!meta.hasAttributeModifiers() || meta.getAttributeModifiers(movementSpeed) == null)) {
                meta.addAttributeModifier(movementSpeed, new AttributeModifier(
                        "divinity_stats_icon_hide_attributes", 0D, ADD_NUMBER
                ));
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }

    @NotNull
    public List<Row> getRows(@NotNull Player player, @NotNull EntityStats stats) {
        List<Row> rows = new ArrayList<>();
        ClassManager classManager = this.plugin.getModuleCache().getClassManager();
        UserClassData classData = classManager == null ? null : classManager.getUserData(player);
        FabledHook fabledHook = (FabledHook) this.plugin.getHook(EHook.SKILL_API);
        PlayerData fabledData = fabledHook != null && fabledHook.isFabledLoaded() ? Fabled.getData(player) : null;

        for (Definition definition : this.definitions) {
            if (!this.config.getBoolean(definition.path + ".enabled", true)) continue;
            String mode = this.config.getString(definition.path + ".mode", "dynamic").toLowerCase(Locale.ROOT);
            if (!mode.equals("static") && !mode.equals("dynamic")) continue;

            Value value = this.readValue(definition, player, stats, classData, fabledData);
            if (value == null) value = new Value(0D, 0D, false, false, false, "");
            if (mode.equals("dynamic")
                    && Math.abs(value.total - value.base) <= this.getZeroThreshold()) continue;

            String name = this.config.getString(definition.path + ".name", definition.defaultName);
            String description = this.config.getString(
                    definition.path + ".description", this.defaultDescription(definition));
            String iconName = this.config.getString(definition.path + ".icon", definition.defaultIcon.name());
            Material icon = Material.matchMaterial(iconName);
            if (icon == null || icon.isAir()) icon = definition.defaultIcon;
            int slot = this.config.getInt(definition.path + ".slot", -1);
            String format = this.config.getString(definition.path + ".format", definition.defaultFormat);
            int priority = this.config.getInt(definition.path + ".priority", this.defaultPriority(definition));

            rows.add(new Row(
                    definition.path,
                    priority,
                    name,
                    definition.category,
                    icon,
                    mode.equals("static"),
                    slot,
                    value.base,
                    value.total - value.base,
                    value.total,
                    format,
                    description,
                    value.totalOnly,
                    value.note
            ));
        }
        return rows;
    }

    private int getNavigationSlot(@NotNull String key, int fallback) {
        fallback += this.getInventorySize() - 9;
        int slot = this.config.getInt("ui." + key, fallback);
        return slot >= this.getInventorySize() - 9 && slot < this.getInventorySize() ? slot : fallback;
    }

    @NotNull
    private List<Definition> discoverDefinitions() throws Exception {
        List<Definition> result = new ArrayList<>();

        Registry.ATTRIBUTE.stream()
                .sorted(Comparator.comparing(attribute -> attribute.getKey().toString()))
                .forEach(attribute -> {
                    String id = attribute.getKey().toString();
                    String format = id.endsWith("knockback_resistance") ? "percent" : "number";
                    result.add(new Definition(
                            "minecraft", safeKey(id), "Minecraft attribute", prettyName(id),
                            this.defaultMinecraftIcon(id), format, Kind.MINECRAFT, attribute
                    ));
                });

        java.util.Arrays.stream(TypedStat.Type.values())
            .sorted(Comparator.comparing(TypedStat.Type::name))
            .forEach(type -> {
                String id = type.name().toLowerCase(Locale.ROOT);
                String format = type.isPercent() ? "percent" : "number";
                if (type == TypedStat.Type.CRITICAL_DAMAGE) format = "multiplier";
                result.add(new Definition(
                        "divinity.item-stats", id, "Divinity item stat", prettyName(id),
                        this.defaultStatIcon(id), format, Kind.DIVINITY_STAT, type,
                        type.isGlobal() ? "dynamic" : "hidden"
                ));
                });

        ItemStats.getDamages().stream()
                .sorted(Comparator.comparing(DamageAttribute::getId))
                .forEach(attribute -> result.add(new Definition(
                        "divinity.damage-types", safeKey(attribute.getId()), "Divinity damage type",
                        prettyName(attribute.getId()) + " Damage", Material.IRON_SWORD,
                        "number", Kind.DAMAGE, attribute
                )));

        ItemStats.getDefenses().stream()
                .sorted(Comparator.comparing(DefenseAttribute::getId))
                .forEach(attribute -> result.add(new Definition(
                        "divinity.defense-types", safeKey(attribute.getId()), "Divinity defense type",
                        prettyName(attribute.getId()) + " Defense", Material.SHIELD,
                        "number", Kind.DEFENSE, attribute
                )));

                this.addConfiguredTypes(result, "/item_stats/damage.yml", "divinity.damage-types", Kind.DAMAGE);
                this.addConfiguredTypes(result, "/item_stats/defense.yml", "divinity.defense-types", Kind.DEFENSE);

        for (ClassAttributeType type : ClassAttributeType.values()) {
            result.add(new Definition(
                    "divinity.class-attributes", type.name().toLowerCase(Locale.ROOT),
                    "Divinity class attribute", prettyName(type.name()),
                    this.defaultMinecraftIcon(type.name()), "number", Kind.CLASS_ATTRIBUTE, type
            ));
        }

        FabledHook fabledHook = (FabledHook) this.plugin.getHook(EHook.SKILL_API);
        if (fabledHook != null && fabledHook.isFabledLoaded()) {
            fabledHook.getAttributes().stream()
                    .sorted(Comparator.comparing(FabledAttribute::getId))
                    .forEach(attribute -> result.add(new Definition(
                            "fabled", safeKey(attribute.getId()), "Fabled attribute", attribute.getName(),
                            this.defaultFabledIcon(attribute.getId()), "number", Kind.FABLED, attribute
                    )));
        }

        return result;
    }

    private void addConfiguredTypes(@NotNull List<Definition> definitions, @NotNull String resource,
                                    @NotNull String categoryPath, @NotNull Kind kind) throws Exception {
        JYML typeConfig = JYML.loadOrExtract(this.plugin, resource);
        for (String id : typeConfig.getSection("")) {
            String safeId = safeKey(id);
            String path = "attributes." + categoryPath + "." + safeId;
            if (definitions.stream().anyMatch(definition -> definition.path.equals(path))) continue;

            boolean enabled = typeConfig.getBoolean(id + ".enabled", true);
            String name = typeConfig.getString(id + ".name", prettyName(id));
            Material icon = kind == Kind.DAMAGE ? Material.IRON_SWORD : Material.SHIELD;
            Object source = kind == Kind.DAMAGE ? ItemStats.getDamageById(id) : ItemStats.getDefenseById(id);
            definitions.add(new Definition(categoryPath, safeId,
                    kind == Kind.DAMAGE ? "Divinity damage type" : "Divinity defense type",
                    name, icon, "number", kind, source == null ? id : source,
                    enabled ? "dynamic" : "hidden"));
        }
    }

    private void ensureDefaults() {
        this.config.addMissing("enabled", true);
        this.config.addMissing("dynamic-zero-threshold", 0.0001D);
        this.config.addMissing("ui.title", DEFAULT_TITLE);
        this.config.addMissing("ui.size", 54);
        this.config.addMissing("ui.dynamic-slots", DEFAULT_DYNAMIC_SLOTS);
        this.config.addMissing("ui.previous-slot", 45);
        this.config.addMissing("ui.close-slot", 49);
        this.config.addMissing("ui.next-slot", 53);
        this.config.addMissing("ui.labels.base", "Base");
        this.config.addMissing("ui.labels.bonus", "Bonus");
        this.config.addMissing("ui.labels.total", "Total");
        this.config.addMissing("ui.labels.current", "Current value");
        this.config.addMissing("ui.labels.previous", "Previous page");
        this.config.addMissing("ui.labels.close", "Close");
        this.config.addMissing("ui.labels.next", "Next page");
        this.config.addMissing("ui.lore.standard", java.util.Arrays.asList(
                "&8{category}",
                "&7{label_base}: &f{base}",
                "&7{label_bonus}: {bonus_color}{bonus_sign}{bonus}",
                "&7{label_total}: &e{total}",
                "",
                "&7{description}"
        ));
        this.config.addMissing("ui.lore.total-only", java.util.Arrays.asList(
                "&8{category}",
                "&7{label_current}: &f{total}",
                "{note}",
                "",
                "&7{description}"
        ));

        for (Definition definition : this.definitions) {
            this.config.addMissing(definition.path + ".enabled", true);
            this.config.addMissing(definition.path + ".priority", this.defaultPriority(definition));
            this.config.addMissing(definition.path + ".mode", definition.defaultMode);
            this.config.addMissing(definition.path + ".slot", -1);
            this.config.addMissing(definition.path + ".name", redName(definition.defaultName));
            this.config.addMissing(definition.path + ".icon", definition.defaultIcon.name());
            this.config.addMissing(definition.path + ".format", definition.defaultFormat);
            this.config.addMissing(definition.path + ".description", this.defaultDescription(definition));
        }

        for (String id : this.config.getSection("attributes.fabled")) {
            String description = "Fabled " + prettyName(id)
                    + " attribute. Its gameplay effect depends on your Fabled configuration.";
            this.config.addMissing("attributes.fabled." + id + ".description", description);
        }
    }

    @NotNull
    private String defaultDescription(@NotNull Definition definition) {
        String id = definition.path.substring(definition.path.lastIndexOf('.') + 1);
        switch (definition.kind) {
            case MINECRAFT:
                if (id.startsWith("minecraft_")) id = id.substring("minecraft_".length());
                switch (id) {
                    case "air_drag_modifier": return "Changes air resistance, affecting how quickly the entity loses momentum while moving through air.";
                    case "armor": return "Reduces damage from many attacks. It is one part of Minecraft's armor-based damage calculation.";
                    case "armor_toughness": return "Helps armor remain effective against attacks that deal high damage.";
                    case "attack_damage": return "Changes the damage dealt by melee attacks before other combat modifiers are applied.";
                    case "attack_knockback": return "Changes how strongly the entity's attacks push their target backward.";
                    case "attack_speed": return "Changes the time between attacks. Higher attack speed allows attacks to recover sooner.";
                    case "below_name_distance": return "Sets how far away displays positioned below the entity's name can be seen.";
                    case "block_break_speed": return "Changes the rate at which the entity breaks blocks, subject to tool and block rules.";
                    case "block_interaction_range": return "Sets the farthest distance at which the player can target or interact with a block.";
                    case "bounciness": return "Changes the entity's bounce response when landing on surfaces that cause bouncing.";
                    case "burning_time": return "Multiplies how long the entity burns after being ignited.";
                    case "camera_distance": return "Changes how far the camera sits from the player in third-person view.";
                    case "entity_interaction_range": return "Sets the farthest distance at which the player can reach or interact with an entity.";
                    case "explosion_knockback_resistance": return "Reduces the push force the entity receives from explosions.";
                    case "fall_damage_multiplier": return "Multiplies fall damage received. Values below 1 reduce it; 0 prevents it.";
                    case "flying_speed": return "Changes the movement speed used while the entity is flying.";
                    case "follow_range": return "Sets how far away the entity can detect targets and begin following them.";
                    case "friction_modifier": return "Changes the effect of surface friction on the entity's movement.";
                    case "gravity": return "Changes the gravity applied to the entity. Lower values make it fall more slowly.";
                    case "jump_strength": return "Changes the upward force applied when the entity jumps.";
                    case "knockback_resistance": return "Reduces the push force the entity receives from attacks.";
                    case "luck": return "Modifies results in game mechanics that explicitly use the entity's luck attribute.";
                    case "max_absorption": return "Sets the maximum absorption hearts the entity can hold; it does not grant absorption by itself.";
                    case "max_health": return "Sets the entity's health capacity. Increasing it does not automatically heal the entity.";
                    case "mining_efficiency": return "Improves block-breaking speed when the held tool is suitable for the block.";
                    case "movement_efficiency": return "Reduces movement slowdowns from terrain such as mud, powder snow, or similar surfaces.";
                    case "movement_speed": return "Changes the entity's normal ground movement speed.";
                    case "name_tag_distance": return "Sets how far away the entity's name tag remains visible to other players.";
                    case "oxygen_bonus": return "Adds extra air time before the entity begins to take drowning damage.";
                    case "safe_fall_distance": return "Sets the fall distance the entity can safely reach before fall damage is applied.";
                    case "scale": return "Changes the entity's scale, affecting its size and related dimensions.";
                    case "sneaking_speed": return "Changes how quickly the player moves while sneaking.";
                    case "spawn_reinforcements": return "Affects a zombie's chance to call reinforcements when attacked.";
                    case "step_height": return "Sets how high an obstacle the entity can walk over without jumping.";
                    case "submerged_mining_speed": return "Improves block-breaking speed while the entity is submerged in water.";
                    case "sweeping_damage_ratio": return "Sets the fraction of attack damage applied to extra targets hit by a vanilla sweeping attack.";
                    case "tempt_range": return "Sets how far away eligible animals can detect food used to tempt them.";
                    case "water_movement_efficiency": return "Reduces movement slowdown in water, helping the entity move more effectively.";
                    case "waypoint_receive_range": return "Sets the maximum distance at which the player can receive waypoint transmissions.";
                    case "waypoint_transmit_range": return "Sets the maximum distance over which the player's waypoints can be transmitted.";
                    default: return "Changes the entity's " + prettyName(id) + " attribute.";
                }
            case DIVINITY_STAT:
                switch ((TypedStat.Type) definition.source) {
                    case ACCURACY_RATE: return "Improves hit accuracy and helps attacks overcome an enemy's dodge chance.";
                    case AOE_DAMAGE: return "Deals a percentage of the main hit's damage to nearby enemies within the configured AoE range.";
                    case ARMOR: return "Adds armor points, reducing many types of incoming attack damage.";
                    case ARMOR_TOUGHNESS: return "Helps armor reduce damage from especially strong attacks.";
                    case ATTACK_SPEED: return "Applies a percentage change to the player's attack speed.";
                    case BASE_ATTACK_SPEED: return "Sets the item's starting attack speed before other attack-speed bonuses are applied.";
                    case BLEED_RATE: return "Gives attacks a chance to make the target bleed and take damage over time.";
                    case BLOCK_DAMAGE: return "Reduces the incoming damage amount when a block is successful.";
                    case BLOCK_RATE: return "Gives you a chance to block an incoming attack. A successful shield block can trigger shield effects.";
                    case BURN_RATE: return "Gives attacks a chance to ignite the target, setting it on fire.";
                    case CRITICAL_DAMAGE: return "Multiplies damage on a critical hit. For example, 1.5x deals 50% extra damage.";
                    case CRITICAL_RATE: return "Gives each eligible attack a chance to deal critical damage.";
                    case DISARM_RATE: return "Gives attacks a chance to make the target drop or lose its held weapon.";
                    case DODGE_RATE: return "Gives incoming attacks a chance to miss; the attacker's accuracy can counter it.";
                    case DURABILITY: return "The item's current custom durability. It decreases when the item is used and can reach zero.";
                    case HEALTH_REGEN: return "Increases health restored by health-regeneration effects.";
                    case KNOCKBACK_RESISTANCE: return "Reduces how far attacks push you backward.";
                    case LOOT_RATE: return "Scales eligible Divinity custom mob-drop chances, not drop quantity. +50% turns a 20% chance into 30%. Drops configured to ignore modifiers and vanilla drops are unaffected.";
                    case MANA_REGEN: return "Increases mana restored by mana-regeneration effects.";
                    case MAX_HEALTH: return "Adds to your maximum health; it does not restore health immediately.";
                    case MOVEMENT_SPEED: return "Applies a percentage change to the player's movement speed.";
                    case PENETRATION: return "Reduces the effectiveness of the target's PvE or PvP defense when you hit.";
                    case PVE_DAMAGE: return "Increases your damage against non-player entities.";
                    case PVE_DEFENSE: return "Reduces damage you receive from non-player entities.";
                    case PVP_DAMAGE: return "Increases your damage against other players.";
                    case PVP_DEFENSE: return "Reduces damage you receive from other players.";
                    case SALE_PRICE: return "Changes the item's sell value when processed by a compatible Divinity selling feature.";
                    case THORNMAIL: return "Returns a percentage of damage you receive to the attacker.";
                    case VAMPIRISM: return "Restores health based on a percentage of damage you deal, up to your maximum health.";
                    default: return "Changes the item's " + prettyName(id) + " stat.";
                }
            case DAMAGE:
                if (definition.source instanceof DamageAttribute) {
                    return "Adds " + prettyName(((DamageAttribute) definition.source).getId())
                            + " damage to attacks.";
                }
                return "Adds this damage type to attacks.";
            case DEFENSE:
                if (definition.source instanceof DefenseAttribute) {
                    return "Reduces incoming " + prettyName(((DefenseAttribute) definition.source).getId())
                            + " damage.";
                }
                return "Reduces incoming damage of this type.";
            case CLASS_ATTRIBUTE:
                switch ((ClassAttributeType) definition.source) {
                    case ARMOR: return "Adds armor from the player's class.";
                    case ARMOR_TOUGHNESS: return "Adds armor toughness from the player's class.";
                    case ATTACK_DAMAGE: return "Adds attack damage from the player's class.";
                    case ATTACK_SPEED: return "Adds attack speed from the player's class.";
                    case FLYING_SPEED: return "Adds flying speed from the player's class.";
                    case KNOCKBACK_RESISTANCE: return "Adds knockback resistance from the player's class.";
                    case LUCK: return "Adds luck from the player's class.";
                    case MAX_HEALTH: return "Adds maximum health from the player's class.";
                    case MOVEMENT_SPEED: return "Adds movement speed from the player's class.";
                    default: return "Adds this attribute from the player's class.";
                }
            case FABLED:
                return "Fabled " + prettyName(id)
                        + " attribute. Its gameplay effect depends on your Fabled configuration.";
            default:
                return "";
        }
    }

    private int defaultPriority(@NotNull Definition definition) {
        return (this.definitions.indexOf(definition) + 1) * 10;
    }

    @Nullable
    private Value readValue(
            @NotNull Definition definition,
            @NotNull Player player,
            @NotNull EntityStats stats,
            @Nullable UserClassData classData,
            @Nullable PlayerData fabledData
    ) {
        switch (definition.kind) {
            case MINECRAFT: {
                Attribute attribute = (Attribute) definition.source;
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) return new Value(0D, 0D, false, false, false, "");
                double scale = attribute == VersionManager.getNms().getAttribute("KNOCKBACK_RESISTANCE") ? 100D : 1D;
                return new Value(instance.getBaseValue() * scale, instance.getValue() * scale,
                        scale != 1D, false, false, "");
            }
            case DIVINITY_STAT: {
                TypedStat.Type type = (TypedStat.Type) definition.source;
                TypedStat typedStat = ItemStats.getStat(type);
                if (typedStat == null) return new Value(0D, 0D, type.isPercent(), false, false, "");
                double base = type == TypedStat.Type.CRITICAL_DAMAGE ? 1D : 0D;
                return new Value(base, stats.getItemStat(type, true),
                    type.isPercent(), type == TypedStat.Type.CRITICAL_DAMAGE, false, "");
            }
            case DAMAGE: {
                if (!(definition.source instanceof DamageAttribute)) {
                    return new Value(0D, 0D, false, false, true, "Type is not enabled");
                }
                DamageAttribute attribute = (DamageAttribute) definition.source;
                double value = stats.getDamageByType(attribute);
                return new Value(0D, value, false, false, true, "Current damage roll");
            }
            case DEFENSE: {
                if (!(definition.source instanceof DefenseAttribute)) {
                    return new Value(0D, 0D, false, false, true, "Type is not enabled");
                }
                DefenseAttribute attribute = (DefenseAttribute) definition.source;
                double base = 0D;
                if (attribute.isDefault()) {
                    Attribute armor = VersionManager.getNms().getAttribute("ARMOR");
                    AttributeInstance instance = armor == null ? null : player.getAttribute(armor);
                    if (instance != null) base = instance.getBaseValue();
                }
                return new Value(base, stats.getDefenseByType(attribute), false, false, false, "");
            }
            case CLASS_ATTRIBUTE: {
                if (classData == null) return new Value(0D, 0D, false, false, false, "");
                ClassAttributeType type = (ClassAttributeType) definition.source;
                double base = classData.getPlayerClass().getAttributeValue(type, classData.getLevel());
                return new Value(base, classData.getAttribute(type), false, false, false, "");
            }
            case FABLED: {
                if (fabledData == null) return new Value(0D, 0D, false, false, false, "");
                FabledAttribute attribute = (FabledAttribute) definition.source;
                return new Value(fabledData.getInvestedAttribute(attribute.getId()),
                        fabledData.getAttribute(attribute.getId()), false, false, false, "");
            }
            default:
                return null;
        }
    }

    private Material defaultMinecraftIcon(@NotNull String id) {
        String key = id.toLowerCase(Locale.ROOT);
        if (key.endsWith("max_health")) return Material.GOLDEN_APPLE;
        if (key.endsWith("armor_toughness")) return Material.IRON_BLOCK;
        if (key.endsWith("armor")) return Material.IRON_CHESTPLATE;
        if (key.endsWith("attack_damage")) return Material.DIAMOND_SWORD;
        if (key.endsWith("attack_speed")) return Material.IRON_SWORD;
        if (key.endsWith("attack_knockback")) return Material.SLIME_BALL;
        if (key.endsWith("below_name_distance") || key.endsWith("name_tag_distance")) return Material.NAME_TAG;
        if (key.endsWith("block_break_speed") || key.endsWith("mining_efficiency")
                || key.endsWith("submerged_mining_speed")) return Material.DIAMOND_PICKAXE;
        if (key.endsWith("block_interaction_range") || key.endsWith("entity_interaction_range")
                || key.endsWith("camera_distance")) return Material.SPYGLASS;
        if (key.endsWith("bounciness") || key.endsWith("scale")) return Material.SLIME_BLOCK;
        if (key.endsWith("burning_time")) return Material.FLINT_AND_STEEL;
        if (key.endsWith("fall_damage_multiplier") || key.endsWith("safe_fall_distance")) {
            return Material.LEATHER_BOOTS;
        }
        if (key.endsWith("movement_speed") || key.endsWith("flying_speed")) return Material.FEATHER;
        if (key.endsWith("follow_range")) return Material.COMPASS;
        if (key.endsWith("friction_modifier")) return Material.ICE;
        if (key.endsWith("gravity")) return Material.ANVIL;
        if (key.endsWith("jump_strength")) return Material.RABBIT_FOOT;
        if (key.endsWith("luck")) return Material.EMERALD;
        if (key.endsWith("max_absorption")) return Material.GOLDEN_APPLE;
        if (key.endsWith("knockback_resistance") || key.endsWith("explosion_knockback_resistance")) {
            return Material.SHIELD;
        }
        if (key.endsWith("oxygen_bonus")) return Material.WATER_BUCKET;
        if (key.endsWith("sneaking_speed") || key.endsWith("movement_efficiency")) {
            return Material.LEATHER_BOOTS;
        }
        if (key.endsWith("spawn_reinforcements")) return Material.ZOMBIE_SPAWN_EGG;
        if (key.endsWith("step_height")) return Material.LADDER;
        if (key.endsWith("sweeping_damage_ratio")) return Material.IRON_SWORD;
        if (key.endsWith("tempt_range")) return Material.WHEAT;
        if (key.endsWith("water_movement_efficiency")) return Material.TURTLE_HELMET;
        if (key.endsWith("waypoint_receive_range") || key.endsWith("waypoint_transmit_range")) {
            return Material.COMPASS;
        }
        if (key.endsWith("air_drag_modifier")) return Material.ELYTRA;
        return Material.COMPASS;
    }

    private Material defaultStatIcon(@NotNull String id) {
        String key = id.toLowerCase(Locale.ROOT);
        if (key.contains("armor_toughness")) return Material.IRON_BLOCK;
        if (key.contains("armor")) return Material.IRON_CHESTPLATE;
        if (key.contains("attack_speed")) return Material.IRON_SWORD;
        if (key.contains("attack") || key.contains("damage") || key.contains("bleed")
                || key.contains("disarm") || key.contains("critical") || key.contains("penetration")) {
            return Material.DIAMOND_SWORD;
        }
        if (key.contains("block") || key.contains("dodge") || key.contains("defense")
                || key.contains("knockback") || key.contains("thorn")) return Material.SHIELD;
        if (key.contains("burn")) return Material.BLAZE_POWDER;
        if (key.contains("accuracy")) return Material.BOW;
        if (key.contains("durability")) return Material.ANVIL;
        if (key.contains("health")) return Material.GOLDEN_APPLE;
        if (key.contains("loot") || key.contains("sale")) return Material.EMERALD;
        if (key.contains("mana")) return Material.LAPIS_LAZULI;
        if (key.contains("movement") || key.contains("speed")) return Material.FEATHER;
        if (key.contains("vampirism")) return Material.REDSTONE;
        return Material.COMPASS;
    }

    private Material defaultFabledIcon(@NotNull String id) {
        String key = id.toLowerCase(Locale.ROOT);
        if (key.contains("strength")) return Material.DIAMOND_SWORD;
        if (key.contains("dexterity")) return Material.FEATHER;
        if (key.contains("intelligence")) return Material.ENCHANTED_BOOK;
        if (key.contains("spirit")) return Material.AMETHYST_SHARD;
        if (key.contains("vitality")) return Material.GOLDEN_APPLE;
        return Material.EMERALD;
    }

    @NotNull
    private static String redName(@NotNull String name) {
        return name.startsWith("&c") ? name : "&c" + name;
    }

    private static String safeKey(@NotNull String id) {
        return id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
    }

    private static String prettyName(@NotNull String id) {
        String key = id.substring(id.lastIndexOf(':') + 1).replace('_', ' ').replace('-', ' ');
        StringBuilder result = new StringBuilder();
        for (String word : key.split("\\s+")) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            if (word.length() > 1) result.append(word.substring(1));
        }
        return result.toString();
    }

    private static List<Integer> defaultDynamicSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < 45; slot++) slots.add(slot);
        return slots;
    }

    public static final class Row {
        public final String path;
        public final int priority;
        public final String name;
        public final String category;
        public final Material icon;
        public final boolean isStatic;
        public final int slot;
        public final double base;
        public final double bonus;
        public final double total;
        public final String format;
        public final String description;
        public final boolean totalOnly;
        public final String note;

        private Row(String path, int priority, String name, String category, Material icon, boolean isStatic, int slot,
                    double base, double bonus, double total, String format, String description,
                    boolean totalOnly, String note) {
            this.path = path;
            this.priority = priority;
            this.name = name;
            this.category = category;
            this.icon = icon;
            this.isStatic = isStatic;
            this.slot = slot;
            this.base = base;
            this.bonus = bonus;
            this.total = total;
            this.format = format;
            this.description = description;
            this.totalOnly = totalOnly;
            this.note = note;
        }

        public ItemStack toItem(@NotNull StatsCatalog catalog) {
            ItemStack item = new ItemStack(this.icon);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            catalog.hideAttributeTooltip(meta);
            meta.setDisplayName(StringUT.color(this.name));
            List<String> lore = new ArrayList<>();
            String loreType = this.totalOnly ? "total-only" : "standard";
            for (String template : catalog.getLoreTemplates(loreType)) {
                if (template.isEmpty()) {
                    if (!lore.isEmpty()) lore.add(" ");
                    continue;
                }
                if (this.note.isEmpty() && template.contains("{note}")) continue;
                if (this.description.isEmpty() && template.contains("{description}")) continue;
                List<String> descriptionLines = template.contains("{description}")
                        ? wrapDescription(this.description, 30)
                        : java.util.Collections.singletonList("");
                for (String descriptionLine : descriptionLines) {
                    String line = template
                            .replace("{name}", this.name)
                            .replace("{category}", this.category)
                            .replace("{base}", this.format(this.base))
                            .replace("{bonus}", this.format(this.bonus))
                            .replace("{total}", this.format(this.total))
                            .replace("{value}", this.format(this.total))
                            .replace("{description}", descriptionLine)
                            .replace("{bonus_sign}", this.bonus >= 0 ? "+" : "")
                            .replace("{bonus_color}", this.bonus >= 0 ? "&a" : "&c")
                            .replace("{note}", this.note.isEmpty() ? "" : "&8" + this.note)
                            .replace("{label_base}", catalog.getLabel("base", "Base"))
                            .replace("{label_bonus}", catalog.getLabel("bonus", "Bonus"))
                            .replace("{label_total}", catalog.getLabel("total", "Total"))
                            .replace("{label_current}", catalog.getLabel("current", "Current value"));
                    if (!line.isEmpty()) lore.add(StringUT.color(line));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        @NotNull
        static List<String> wrapDescription(@NotNull String description, int maxLength) {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : description.trim().split("\\s+")) {
                if (word.isEmpty()) continue;
                if (line.length() > 0 && line.length() + 1 + word.length() > maxLength) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
            if (line.length() > 0) lines.add(line.toString());
            return lines;
        }

        private String format(double value) {
            String formatted = NumberUT.format(value);
            if (this.format.equalsIgnoreCase("percent")) return formatted + "%";
            if (this.format.equalsIgnoreCase("multiplier")) return formatted + "x";
            return formatted;
        }
    }

    private enum Kind {
        MINECRAFT,
        DIVINITY_STAT,
        DAMAGE,
        DEFENSE,
        CLASS_ATTRIBUTE,
        FABLED
    }

    private static final class Definition {
        private final String path;
        private final String category;
        private final String defaultName;
        private final Material defaultIcon;
        private final String defaultFormat;
        private final Kind kind;
        private final Object source;
        private final String defaultMode;

        private Definition(String categoryPath, String id, String category, String defaultName,
                           Material defaultIcon, String defaultFormat, Kind kind, Object source) {
            this(categoryPath, id, category, defaultName, defaultIcon, defaultFormat, kind, source, "dynamic");
        }

        private Definition(String categoryPath, String id, String category, String defaultName,
                           Material defaultIcon, String defaultFormat, Kind kind, Object source, String defaultMode) {
            this.path = "attributes." + categoryPath + "." + id;
            this.category = category;
            this.defaultName = defaultName;
            this.defaultIcon = defaultIcon;
            this.defaultFormat = defaultFormat;
            this.kind = kind;
            this.source = source;
            this.defaultMode = defaultMode;
        }
    }

    private static final class Value {
        private final double base;
        private final double total;
        private final boolean percent;
        private final boolean multiplier;
        private final boolean totalOnly;
        private final String note;

        private Value(double base, double total, boolean percent, boolean multiplier, boolean totalOnly, String note) {
            this.base = base;
            this.total = total;
            this.percent = percent;
            this.multiplier = multiplier;
            this.totalOnly = totalOnly;
            this.note = note;
        }
    }
}