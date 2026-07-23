package studio.magemonkey.divinity.stats;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.meta.NBTAttribute;
import studio.magemonkey.codex.compat.Compat;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.hooks.Hooks;
import studio.magemonkey.codex.util.EntityUT;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.NamespaceResolver;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.EntityStatsBonusUpdateEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.data.api.DivinityUser;
import studio.magemonkey.divinity.data.api.UserProfile;
import studio.magemonkey.divinity.manager.damage.DamageMeta;
import studio.magemonkey.divinity.manager.effects.IEffect;
import studio.magemonkey.divinity.manager.effects.IEffectType;
import studio.magemonkey.divinity.manager.effects.IExpirableEffect;
import studio.magemonkey.divinity.manager.effects.IPeriodicEffect;
import studio.magemonkey.divinity.manager.effects.main.AdjustStatEffect;
import studio.magemonkey.divinity.manager.effects.main.ResistEffect;
import studio.magemonkey.divinity.modules.list.arrows.ArrowManager.QArrow;
import studio.magemonkey.divinity.modules.list.classes.ClassManager;
import studio.magemonkey.divinity.modules.list.essences.EssencesManager;
import studio.magemonkey.divinity.modules.list.essences.EssencesManager.Essence;
import studio.magemonkey.divinity.modules.list.runes.RuneManager;
import studio.magemonkey.divinity.modules.list.sets.SetManager;
import studio.magemonkey.divinity.stats.bonus.BonusCalculator;
import studio.magemonkey.divinity.stats.bonus.BonusMap;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.utils.ItemUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import static org.bukkit.attribute.AttributeModifier.Operation;

public class EntityStats {
    private static final Map<String, EntityStats> STATS;
    private static final SimpleStat.Type[]        ATTRIBUTE_BONUS_STATS;
    private static final NBTAttribute[]           ATTRIBUTE_BONUS_NBT;
    private static final double                   DEFAULT_ATTACK_POWER = 1D;

    static {
        STATS = Collections.synchronizedMap(new HashMap<>());

        ATTRIBUTE_BONUS_STATS = new SimpleStat.Type[]{
                TypedStat.Type.MAX_HEALTH,
                TypedStat.Type.ATTACK_SPEED,
                TypedStat.Type.MOVEMENT_SPEED
        };
        ATTRIBUTE_BONUS_NBT = new NBTAttribute[]{
                NBTAttribute.MAX_HEALTH,
                NBTAttribute.ATTACK_SPEED,
                NBTAttribute.MOVEMENT_SPEED
        };
    }

    private final Divinity                                                        plugin;
    @Getter
    private final Player                                                          player;
    private final boolean                                                         isNPC;
    private final EntityEquipment                                                 equipment;
    private final List<ItemStack>                                                 inventory;
    private final Map<PotionEffectType, PotionEffect>                             permaEffects;
    private final Map<ItemLoreStat<?>, List<BiFunction<Boolean, Double, Double>>> bonuses;
    private final Set<IEffect>                                                    effects;
    private       LivingEntity                                                    entity;
    private       DamageMeta                                                      damageMeta;

    private QArrow arrowBonus;
    private int    arrowLevel;

    private double  atkPower;
    private boolean aoeIgnore;

    EntityStats(Divinity plugin, @NotNull LivingEntity entity) {
        this.plugin = plugin;
        this.entity = entity;
        this.player = this.entity instanceof Player ? (Player) this.entity : null;
        this.isNPC = Hooks.isNPC(this.entity);
        this.permaEffects = Collections.synchronizedMap(new HashMap<>());

        this.equipment = entity.getEquipment();
        this.inventory = Collections.synchronizedList(new ArrayList<>());
        this.effects = Collections.synchronizedSet(new HashSet<>());
        this.bonuses = new HashMap<>();

        this.arrowBonus = null;
        this.arrowLevel = 0;

        this.atkPower = DEFAULT_ATTACK_POWER;
        this.aoeIgnore = false;

        if (this.isPlayer()) {
            if (EngineCfg.COMBAT_REDUCE_PLAYER_HEALTH_BAR) {
                this.player.setHealthScaled(true);
                this.player.setHealthScale(20D);
            }
//			else if (this.player.isHealthScaled()) {
//				this.player.setHealthScaled(false);
//			}
        }
    }

    public static void purge(@NotNull LivingEntity entity) {
        String uuid = entity.getUniqueId().toString();
        STATS.remove(uuid);
    }

    @NotNull
    public synchronized static Collection<EntityStats> getAll() {
        STATS.values().removeIf(stats -> !stats.entity.isValid() || stats.entity.isDead());
        return STATS.values();
    }

    @NotNull
    public static EntityStats get(@NotNull LivingEntity entity) {
        String      uuid   = entity.getUniqueId().toString();
        EntityStats eStats = STATS.computeIfAbsent(uuid, stats -> new EntityStats(Divinity.getInstance(), entity));
        eStats.updateHolder(entity);
        return eStats;
    }

    @NotNull
    public static String getEntityName(@NotNull Entity entity) {
        String name = Divinity.getInstance().lang().getEnum(entity.getType());

        if (entity instanceof Projectile) {
            Projectile       pp = (Projectile) entity;
            ProjectileSource ps = pp.getShooter();
            if (ps instanceof LivingEntity) {
                entity = (LivingEntity) ps;
            }
        }

        if (entity instanceof Player) {
            name = entity.getName();
        } else if (entity instanceof LivingEntity) {
            String cName = entity.getCustomName();
            if (cName != null) {
                name = cName;
            }
        }

        return name;
    }

    public static double getEntityMaxHealth(@NotNull LivingEntity entity) {
        AttributeInstance ai = entity.getAttribute(VersionManager.getNms().getAttribute("MAX_HEALTH"));
        if (ai == null) return 0;

        return ai.getValue();
    }

    public void handleDeath() {
        if (this.isPlayer()) {
            for (IEffect e : new HashSet<>(this.effects)) {
                if (e.resetOnDeath()) {
                    this.removeEffect(e);
                }
            }

            this.inventory.clear();
            this.permaEffects.clear();
            this.bonuses.clear();
            this.damageMeta = null;
            this.aoeIgnore = false;
            this.updateBonusAttributes();
        } else {
            this.purge();
        }
    }

    public void purge() {
        purge(entity);
    }

    @Nullable
    public DamageMeta getLastDamageMeta() {
        return this.damageMeta;
    }

    public void setLastDamageMeta(@Nullable DamageMeta meta) {
        this.damageMeta = meta;
    }

    private void updateHolder(@NotNull LivingEntity valid) {
        if (this.entity == null || !this.entity.equals(valid)) {
            this.entity = valid;
        }
    }

    public final boolean isPlayer() {
        return this.player != null;
    }

    public final boolean isNPC() {
        return this.isNPC;
    }

    public double getAttackPower() {
        return this.isNPC() ? DEFAULT_ATTACK_POWER : this.atkPower;
    }

    public void setAttackPower(double modifier) {
        this.atkPower = modifier;
    }

    public double getAttackPowerModifier() {
        double power = this.getAttackPower();
        if (power < 1D) { // Attack is on cooldown
            double mod = EngineCfg.COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN;
            return mod != 1D ? mod : power;
        }
        return power;
    }

    public void updateAttackPower() {
        if (this.isPlayer() && !this.isNPC()) {
            this.setAttackPower(this.player.getAttackCooldown());
        }
    }

    public boolean isIgnoreAOE() {
        return this.aoeIgnore;
    }

    public void setIgnoreAOE(boolean aoeIgnore) {
        this.aoeIgnore = aoeIgnore;
    }

    public void addEffect(@NotNull IEffect effect) {
        this.effects.add(effect);
        this.updateBonusAttributes();
    }

    public void removeEffect(@NotNull IEffect effect) {
        effect.clear();
        this.effects.remove(effect);
    }

    public boolean hasEffect(@NotNull IEffectType type) {
        return this.getActiveEffects(true).stream().anyMatch(effect -> effect.isType(type));
    }

    public double getEffectResist(@NotNull IEffectType type, boolean safe) {
        return this.getActiveEffects(true).stream().filter(effect -> effect instanceof ResistEffect)
                .mapToDouble(effect -> {
                    double resist = ((ResistEffect) effect).getResist(type);
                    if (resist != 0D) effect.trigger(safe); // TODO make the same as adjust effect count
                    return resist;
                }).sum();
    }

    /**
     * Gets all active effects for the entity. Optionally updates the bonus attributes
     *
     * @param update If true, the bonus attributes will be updated. Care should be taken to avoid infinite loops
     * @return A set of active effects
     */
    @NotNull
    public synchronized Set<IEffect> getActiveEffects(boolean update) {
        Set<IEffect> set      = new HashSet<>();
        Set<IEffect> toRemove = new HashSet<>();

        for (IEffect e : new HashSet<>(this.effects)) {
            if (e.isExpired()) {
                toRemove.add(e);
                continue;
            }

            if (e instanceof IExpirableEffect) {
                IExpirableEffect exp = (IExpirableEffect) e;
                if (exp instanceof IPeriodicEffect) {
                    IPeriodicEffect per = (IPeriodicEffect) exp;
                    if (!per.isReady()) {
                        continue;
                    }
                }
            }
            set.add(e);
        }

        // There's no need to update the bonus attributes
        toRemove.forEach(this::removeEffect);
        if (update) this.updateBonusAttributes();

        return set;
    }

    public void triggerEffects() {
        this.getActiveEffects(true).forEach(effect -> effect.trigger(false));
    }

    public void triggerVisualEffects() {
        EssencesManager essencesManager = plugin.getModuleCache().getEssenceManager();
        if (essencesManager == null) return;

        for (ItemStack item : this.getEquipment()) {
            for (Entry<Essence, Integer> ee : essencesManager.getItemSockets(item)) {
                Essence essence = ee.getKey();
                essence.getEffect().play(this.entity, ee.getValue());
            }
        }
    }

    public void triggerPotionEffects() {
        this.permaEffects.clear();

        RuneManager runes = plugin.getModuleCache().getRuneManager();
        if (runes != null) {
            runes.addRuneEffects(entity);
        }

        SetManager sets = plugin.getModuleCache().getSetManager();
        if (sets != null) {
            sets.addSetPotionEffects(entity);
        }

        for (PotionEffect pe : this.getPermaPotionEffects()) {
            PotionEffectType type = pe.getType();
            PotionEffect     has  = this.entity.getPotionEffect(type);
            if (has != null) {
                if (has.getAmplifier() > pe.getAmplifier()) {
                    continue;
                }
                if (has.getAmplifier() == pe.getAmplifier() && has.getDuration() > pe.getDuration()) {
                    continue;
                }
            }
            this.entity.addPotionEffect(pe);
        }
    }

    @NotNull
    public synchronized Set<PotionEffect> getPermaPotionEffects() {
        return new HashSet<>(this.permaEffects.values());
    }

    public synchronized void addPermaPotionEffect(@NotNull PotionEffect effect) {
        PotionEffectType key = effect.getType();
        int              lvl = effect.getAmplifier();
        if (this.permaEffects.containsKey(key)) {
            PotionEffect pe2 = this.permaEffects.get(key);
            if (lvl <= pe2.getAmplifier()) {
                return;
            }
        }
        this.permaEffects.put(key, effect);
    }

    public void removePermaPotionEffect(@NotNull PotionEffectType key) {
        this.permaEffects.remove(key);
    }

    @NotNull
    public ItemStack getItemInMainHand() {
        if (this.equipment == null) {
            return new ItemStack(Material.AIR);
        }
        return this.equipment.getItemInMainHand();
    }

    @NotNull
    public ItemStack getItemInOffHand() {
        if (this.equipment == null) {
            return new ItemStack(Material.AIR);
        }
        return this.equipment.getItemInOffHand();
    }

    @NotNull
    public List<ItemStack> getArmor() {
        List<ItemStack> equip = this.getEquipment();

        ItemStack[] hands = new ItemStack[]{this.getItemInMainHand(), this.getItemInOffHand()};
        equip.removeIf(item -> item.isSimilar(hands[0]) || item.isSimilar(hands[1]));

        return equip;
    }

    @NotNull
    public synchronized List<ItemStack> getEquipment() {
        return new ArrayList<>(this.inventory);
    }

    private void updateInventory() {
        this.inventory.clear();

        ItemStack[] armor = new ItemStack[0];
        if (this.equipment != null) {
            armor = this.equipment.getArmorContents();
        }
        Arrays.stream(armor)
                .filter(item -> item != null && !ItemUT.isAir(item))
                .forEach(this.inventory::add);

        ItemStack main = this.getItemInMainHand();
        ItemStack off  = this.getItemInOffHand();

        if (!ItemUT.isAir(main) && (!ItemUtils.isArmor(main) || main.getType() == Material.SHIELD)) {
            this.inventory.add(main);
        }
        if (EngineCfg.ATTRIBUTES_EFFECTIVE_IN_OFFHAND || off.getType() == Material.SHIELD) {
            this.inventory.add(off);
        }

        if (this.isPlayer()) {
            this.inventory.removeIf(item -> item == null || !ItemUtils.canUse(item, this.player, false));
        }
    }

    public void updateAll() {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return;
        }

        this.updateInventory();
        this.updateBonus();
    }

    private void addBonus(@NotNull BonusMap bMap) {
        bMap.getBonuses()
                .forEach((stat, func) -> this.bonuses.computeIfAbsent(stat, list -> new ArrayList<>()).add(func));
    }

    @NotNull
    public List<BiFunction<Boolean, Double, Double>> getBonuses(@NotNull ItemLoreStat<?> stat) {
        List<BiFunction<Boolean, Double, Double>> bonuses =
                new ArrayList<>(this.bonuses.computeIfAbsent(stat, list -> new ArrayList<>()));

        BonusMap arrowBonus = this.arrowBonus != null ? this.arrowBonus.getBonusMap(this.arrowLevel) : null;
        if (arrowBonus != null) {
            BiFunction<Boolean, Double, Double> arrowBif = arrowBonus.getBonus(stat);
            if (arrowBif != null) bonuses.add(arrowBif);
        }

        if (this.isPlayer() && !this.isNPC()) {
            DivinityUser user = plugin.getUserManager().getOrLoadUser(this.player);
            if (user != null) {
                UserProfile prof = user.getActiveProfile();
                bonuses.add(prof.getBuff(stat));
            }
        }

        return bonuses;
    }

    private void updateBonus() {
        this.bonuses.clear();

        // Update sets bonuses
        SetManager set = plugin.getModuleCache().getSetManager();
        if (set != null) {
            set.getActiveSetBonuses(this.entity).forEach(this::addBonus);
        }

        // Update class Aspect bonuses (which are item-stats and damage and defense)
        ClassManager m = plugin.getModuleCache().getClassManager();
        if (m != null && this.isPlayer()) {
            m.getClassEntityStatsBonuses(this.player).forEach(this::addBonus);
        }

        // Apply AttributeModifiers for bonus stats
        this.updateBonusAttributes();

        double maxHealth = EntityStats.getEntityMaxHealth(this.entity);
        if (this.entity.getHealth() > maxHealth) {
            this.entity.setHealth(maxHealth);
        }

        // Call custom event
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            EntityStatsBonusUpdateEvent statsEvent = new EntityStatsBonusUpdateEvent(this.entity, this);
            plugin.getPluginManager().callEvent(statsEvent);
        });
    }

    /**
     * If the player somehow gets into a glitched state, this method can help clean up their attribute bonuses
     * without needing to delete their player data.
     */
    private void purgeAttributeBonuses() {
        for (NBTAttribute nbt : ATTRIBUTE_BONUS_NBT) {
            AttributeInstance attr = entity.getAttribute(nbt.getAttribute());
            if (attr == null) continue;


            attr.getModifiers()
                    .stream()
                    .filter(mod -> {
                        UUID uuid;
                        try {
                            uuid = mod.getUniqueId();
                        } catch (Exception e) {
                            String attKey = VersionManager.getCompat().getAttributeKey(mod);
                            try {
                                uuid = UUID.fromString(attKey.replace("minecraft:", ""));
                            } catch (Exception ignored) {
                                return false;
                            }
                        }

                        return uuid.equals(Compat.ATTRIBUTE_BONUS_UUID);
                    })
                    .collect(Collectors.toList())
                    .forEach(attr::removeModifier);
        }
    }

    private void updateBonusAttributes() {
        // These stats were being copied from Gems onto the player, but they automatically get applied
        // since they're set on the item as attributes. Thus... they were doubling their effects.
        // I'm disabling these, so they don't double up, but not removing entirely because I'm not
        // entirely sure at this point if these are used elsewhere that might cause issues.
        //   GemManager     gems    = plugin.getModuleCache().getGemManager();
        //   List<BonusMap> gemsMap = gems != null ? new ArrayList<>() : null;
        //   if (gemsMap != null && gems != null) {
        //       for (ItemStack item : this.getEquipment()) {
        //           for (Entry<Gem, Integer> e : gems.getItemSockets(item)) {
        //               Gem      g    = e.getKey();
        //               BonusMap bMap = g.getBonusMap(e.getValue());
        //               if (bMap == null) continue;
        //
        //               gemsMap.add(bMap);
        //           }
        //       }
        //   }

        for (int i = 0; i < ATTRIBUTE_BONUS_STATS.length; i++) {
            SimpleStat.Type statType = ATTRIBUTE_BONUS_STATS[i];
            NBTAttribute    nbt      = ATTRIBUTE_BONUS_NBT[i];

            // Get Gems bonuses
            TypedStat typedStat = ItemStats.getStat(statType);
            if (!(typedStat instanceof SimpleStat)) continue;
            SimpleStat stat = (SimpleStat) typedStat;

            List<BiFunction<Boolean, Double, Double>> bonuses = this.getBonuses(stat);

//            if (gemsMap != null) {
//                gemsMap.forEach(gemBonus -> bonuses.add(gemBonus.getBonus(stat)));
//            }

            double attBase = EntityUT.getAttributeBase(entity, nbt.getAttribute());
            double value   = BonusCalculator.SIMPLE_BONUS.apply(attBase, bonuses);
            value = this.getEffectBonus(stat, false).applyAsDouble(value);

            this.applyBonusAttribute(nbt, value);
        }

        // MC 1.21+ vanilla attributes — handled separately, not in NBTAttribute enum
        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("SCALE");
            if (attr != null) this.applyScaleAttribute(attr, calcVanillaStatValue(TypedStat.Type.SCALE));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("WATER_MOVEMENT_EFFICIENCY");
            if (attr != null) applyVanillaAttributeModifier(attr, WATER_MOV_EFF_MODIFIER_UUID,
                    "divinity.water_movement_efficiency", calcVanillaStatValue(TypedStat.Type.WATER_MOVEMENT_EFFICIENCY));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("MOVEMENT_EFFICIENCY");
            if (attr != null) applyVanillaAttributeModifier(attr, MOV_EFF_MODIFIER_UUID,
                    "divinity.movement_efficiency", calcVanillaStatValue(TypedStat.Type.MOVEMENT_EFFICIENCY));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("SNEAKING_SPEED");
            if (attr != null) applyVanillaAttributeModifier(attr, SNEAK_SPEED_MODIFIER_UUID,
                    "divinity.sneaking_speed", calcVanillaStatValue(TypedStat.Type.SNEAKING_SPEED));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("BLOCK_BREAK_SPEED");
            if (attr != null) applyVanillaAttributeModifier(attr, BLOCK_BREAK_SPEED_UUID,
                    "divinity.block_break_speed", calcVanillaStatValue(TypedStat.Type.BLOCK_BREAK_SPEED));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("BLOCK_INTERACTION_RANGE");
            if (attr != null) applyVanillaAttributeModifier(attr, BLOCK_INTERACT_RANGE_UUID,
                    "divinity.block_interaction_range", calcVanillaStatValue(TypedStat.Type.BLOCK_INTERACTION_RANGE));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("ENTITY_INTERACTION_RANGE");
            if (attr != null) applyVanillaAttributeModifier(attr, ENTITY_INTERACT_RANGE_UUID,
                    "divinity.entity_interaction_range", calcVanillaStatValue(TypedStat.Type.ENTITY_INTERACTION_RANGE));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("EXPLOSION_KNOCKBACK_RESISTANCE");
            if (attr != null) applyVanillaAttributeModifier(attr, EXPLOSION_KB_RES_UUID,
                    "divinity.explosion_knockback_resistance", calcVanillaStatValue(TypedStat.Type.EXPLOSION_KNOCKBACK_RESISTANCE));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("FALL_DAMAGE_MULTIPLIER");
            if (attr != null) applyVanillaAttributeModifier(attr, FALL_DAMAGE_MULT_UUID,
                    "divinity.fall_damage_multiplier", calcVanillaStatValue(TypedStat.Type.FALL_DAMAGE_MULTIPLIER));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("FLYING_SPEED");
            if (attr != null) applyVanillaAttributeModifier(attr, FLYING_SPEED_UUID,
                    "divinity.flying_speed", calcVanillaStatValue(TypedStat.Type.FLYING_SPEED));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("GRAVITY");
            if (attr != null) applyVanillaAttributeModifier(attr, GRAVITY_UUID,
                    "divinity.gravity", calcVanillaStatValue(TypedStat.Type.GRAVITY));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("JUMP_STRENGTH");
            if (attr != null) applyVanillaAttributeModifier(attr, JUMP_STRENGTH_UUID,
                    "divinity.jump_strength", calcVanillaStatValue(TypedStat.Type.JUMP_STRENGTH));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("MAX_ABSORPTION");
            if (attr != null) applyVanillaAttributeModifier(attr, MAX_ABSORPTION_UUID,
                    "divinity.max_absorption", calcVanillaStatValue(TypedStat.Type.MAX_ABSORPTION));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("MINING_EFFICIENCY");
            if (attr != null) applyVanillaAttributeModifier(attr, MINING_EFFICIENCY_UUID,
                    "divinity.mining_efficiency", calcVanillaStatValue(TypedStat.Type.MINING_EFFICIENCY));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("OXYGEN_BONUS");
            if (attr != null) applyVanillaAttributeModifier(attr, OXYGEN_BONUS_UUID,
                    "divinity.oxygen_bonus", calcVanillaStatValue(TypedStat.Type.OXYGEN_BONUS));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("SAFE_FALL_DISTANCE");
            if (attr != null) applyVanillaAttributeModifier(attr, SAFE_FALL_DISTANCE_UUID,
                    "divinity.safe_fall_distance", calcVanillaStatValue(TypedStat.Type.SAFE_FALL_DISTANCE));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("STEP_HEIGHT");
            if (attr != null) applyVanillaAttributeModifier(attr, STEP_HEIGHT_UUID,
                    "divinity.step_height", calcVanillaStatValue(TypedStat.Type.STEP_HEIGHT));
        } catch (Exception ignored) {}

        try {
            Attribute attr = (Attribute) VersionManager.getNms().getAttribute("SUBMERGED_MINING_SPEED");
            if (attr != null) applyVanillaAttributeModifier(attr, SUBMERGED_MINING_SPEED_UUID,
                    "divinity.submerged_mining_speed", calcVanillaStatValue(TypedStat.Type.SUBMERGED_MINING_SPEED));
        } catch (Exception ignored) {}
    }

    private static final UUID SCALE_MODIFIER_UUID              = UUID.fromString("d141e000-5ca1-4000-0000-000000000001");
    private static final UUID WATER_MOV_EFF_MODIFIER_UUID      = UUID.fromString("d141e000-5ca1-4000-0000-000000000002");
    private static final UUID MOV_EFF_MODIFIER_UUID            = UUID.fromString("d141e000-5ca1-4000-0000-000000000003");
    private static final UUID SNEAK_SPEED_MODIFIER_UUID        = UUID.fromString("d141e000-5ca1-4000-0000-000000000004");
    private static final UUID BLOCK_BREAK_SPEED_UUID           = UUID.fromString("d141e000-5ca1-4000-0000-000000000005");
    private static final UUID BLOCK_INTERACT_RANGE_UUID        = UUID.fromString("d141e000-5ca1-4000-0000-000000000006");
    private static final UUID ENTITY_INTERACT_RANGE_UUID       = UUID.fromString("d141e000-5ca1-4000-0000-000000000007");
    private static final UUID EXPLOSION_KB_RES_UUID            = UUID.fromString("d141e000-5ca1-4000-0000-000000000008");
    private static final UUID FALL_DAMAGE_MULT_UUID            = UUID.fromString("d141e000-5ca1-4000-0000-000000000009");
    private static final UUID FLYING_SPEED_UUID                = UUID.fromString("d141e000-5ca1-4000-0000-00000000000a");
    private static final UUID GRAVITY_UUID                     = UUID.fromString("d141e000-5ca1-4000-0000-00000000000b");
    private static final UUID JUMP_STRENGTH_UUID               = UUID.fromString("d141e000-5ca1-4000-0000-00000000000c");
    private static final UUID MAX_ABSORPTION_UUID              = UUID.fromString("d141e000-5ca1-4000-0000-00000000000d");
    private static final UUID MINING_EFFICIENCY_UUID           = UUID.fromString("d141e000-5ca1-4000-0000-00000000000e");
    private static final UUID OXYGEN_BONUS_UUID                = UUID.fromString("d141e000-5ca1-4000-0000-00000000000f");
    private static final UUID SAFE_FALL_DISTANCE_UUID          = UUID.fromString("d141e000-5ca1-4000-0000-000000000010");
    private static final UUID STEP_HEIGHT_UUID                 = UUID.fromString("d141e000-5ca1-4000-0000-000000000011");
    private static final UUID SUBMERGED_MINING_SPEED_UUID      = UUID.fromString("d141e000-5ca1-4000-0000-000000000012");

    @SuppressWarnings("deprecation")
    private void applyVanillaAttributeModifier(@NotNull Attribute attr, @NotNull UUID modUuid, @NotNull String modName, double value) {
        AttributeInstance attInst = this.entity.getAttribute(attr);
        if (attInst == null) return;
        for (AttributeModifier mod : new HashSet<>(attInst.getModifiers())) {
            try {
                if (modUuid.equals(mod.getUniqueId())) {
                    if (mod.getAmount() == value) return;
                    attInst.removeModifier(mod);
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (value == 0D) return;
        attInst.addModifier(new AttributeModifier(modUuid, modName, value, Operation.ADD_NUMBER));
    }

    @SuppressWarnings("deprecation")
    private void applyScaleAttribute(@NotNull Attribute scaleAttr, double value) {
        applyVanillaAttributeModifier(scaleAttr, SCALE_MODIFIER_UUID, "divinity.scale", value);
    }

    private double calcVanillaStatValue(@NotNull TypedStat.Type type) {
        TypedStat typedStat = ItemStats.getStat(type);
        if (!(typedStat instanceof SimpleStat)) return 0D;
        SimpleStat ss = (SimpleStat) typedStat;
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
        for (ItemStack item : this.getEquipment()) {
            if (item == null || item.getType().isAir()) continue;
            bonuses.addAll(ss.get(item, player));
        }
        bonuses.addAll(this.getBonuses(ss));
        double value = BonusCalculator.SIMPLE_FULL.apply(0D, bonuses);
        value = this.getEffectBonus(ss, false).applyAsDouble(value);
        if (ss.getCapability() >= 0 && value > ss.getCapability()) value = ss.getCapability();
        return value / 100D;
    }

    private void applyBonusAttribute(@NotNull NBTAttribute att, double value) {
        AttributeInstance attInst = this.entity.getAttribute(att.getAttribute());
        if (attInst == null) return;

        if (att == NBTAttribute.MOVEMENT_SPEED) {
            value = 0.1 * (1D + value / 100D) - 0.1;
        } else if (att == NBTAttribute.ATTACK_SPEED) {
            value = value / 1000D * 4D; // 4 is Default hand attack speed
        }

        // Remove this bonus only if same UUID and different value
        // Do not modify if value is the same
        for (AttributeModifier attMod : new HashSet<>(attInst.getModifiers())) {
            UUID uuid = null;
            try {
                uuid = attMod.getUniqueId();
            } catch (Exception ignored) {
            }

            String attKey    = VersionManager.getCompat().getAttributeKey(attMod);
            String targetKey = VersionManager.getCompat().getAttributeKey(att);
            if (Compat.ATTRIBUTE_BONUS_UUID.equals(uuid) || targetKey.equals(attKey)) {
                if (attMod.getAmount() == value) {
                    return;
                }
                attInst.removeModifier(attMod);
                break;
            }
        }

        if (value == 0D) return;

        AttributeModifier am = VersionManager.getCompat().createAttributeModifier(att, value, Operation.ADD_NUMBER);
        attInst.addModifier(am);
    }

    @NotNull
    private synchronized DoubleUnaryOperator getEffectBonus(@NotNull ItemLoreStat<?> stat, boolean safe) {
        DoubleUnaryOperator operator = (value -> value);

        for (IEffect effect : this.getActiveEffects(false)) {
            if (effect instanceof AdjustStatEffect) {
                AdjustStatEffect    adjust         = (AdjustStatEffect) effect;
                DoubleUnaryOperator operatorEffect = adjust.getAdjust(stat, safe);
                if (operatorEffect != null) {
                    operator = operator.andThen(operatorEffect);
                }
            }
        }
        return operator;
    }

    public void setArrowBonus(@Nullable QArrow arrow, int level) {
        this.arrowBonus = arrow;
        this.arrowLevel = level;
    }

    // Used for skills description
    public double getDamage() {
        return this.getDamageTypes(true).values().stream().mapToDouble(d -> d).sum();
    }

    public double getDamageByType(@NotNull DamageAttribute type) {
        return this.getDamageTypes(true).getOrDefault(type, 0D);
    }

    public double getDefenseByType(@NotNull DefenseAttribute type) {
        return this.getDefenseTypes(true).getOrDefault(type, 0D);
    }

    // Damage not cached due to broke of range values
    @NotNull
    public Map<DamageAttribute, Double> getDamageTypes(boolean safe) {
        return this.getDamageTypes(safe, null);
    }

    @NotNull
    public Map<DamageAttribute, Double> getDamageTypes(boolean safe, @Nullable ItemStack mainHandOverride) {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return Collections.emptyMap();
        }

        Map<DamageAttribute, Double> map   = new HashMap<>();
        Biome                        bio   = this.entity.getLocation().getBlock().getBiome();
        List<ItemStack>              equip = this.getEquipmentForStats(mainHandOverride);

        for (DamageAttribute dmgAtt : ItemStats.getDamages()) {
            List<BiFunction<Boolean, double[], double[]>> bonuses = new ArrayList<>();
            for (ItemStack item : equip) {
                bonuses.addAll(dmgAtt.get(item, player));
            }

            // Check for empty map before add default damage.
            // The default damage is always the latest in list
            // so map will be filled if there was any damage types.
            //if (value == 0D && dmgAtt.isDefault() && map.isEmpty()) {
            //    value = 1D; // Default hand damage for default damage type.
            //}

            for (BiFunction<Boolean, Double, Double> bonus : this.getBonuses(dmgAtt)) {
                bonuses.add((isPercent, input) -> input.length == 2
                        ? new double[]{
                        bonus.apply(isPercent, input[0]),
                        bonus.apply(isPercent, input[1])}
                        : new double[]{
                                bonus.apply(isPercent, input[0])});
            }
            double[] range = BonusCalculator.RANGE_FULL.apply(new double[]{0, 0}, bonuses);
            double   value = Rnd.getDouble(range[0], range[1]);
            value *= dmgAtt.getDamageModifierByBiome(bio); // Multiply by Biome
            value = this.getEffectBonus(dmgAtt, safe).applyAsDouble(value);

            if (value > 0D) {
                map.put(dmgAtt, value);
            }
        }

        return map;
    }

    @NotNull
    public Map<DefenseAttribute, Double> getDefenseTypes(boolean safe) {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return Collections.emptyMap();
        }

        List<ItemStack>               equip = this.getEquipment();
        Map<DefenseAttribute, Double> map   = new HashMap<>();

        for (DefenseAttribute dt : ItemStats.getDefenses()) {
            List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
            for (ItemStack item : equip) {
                bonuses.addAll(dt.get(item, player));
            }
            bonuses.addAll(this.getBonuses(dt));
            if (dt.isDefault()) {
                AttributeInstance attribute = entity.getAttribute(VersionManager.getNms().getAttribute("ARMOR"));
                if (attribute != null) {
                    bonuses.add((isPercent, input) -> isPercent ? input : input + attribute.getBaseValue());
                }
            }

            double value = BonusCalculator.SIMPLE_FULL.apply(0D, bonuses);
            value = this.getEffectBonus(dt, safe).applyAsDouble(value);
            if (value > 0D) {
                map.put(dt, value);
            }
        }

        return map;
    }

    public Map<SimpleStat.Type, Double> getItemStats(boolean safe) {
        return this.getItemStats(safe, null);
    }

    public Map<SimpleStat.Type, Double> getItemStats(boolean safe, @Nullable ItemStack mainHandOverride) {
        if ((!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer())) {
            return Collections.emptyMap();
        }

        Map<SimpleStat.Type, Double> map = new HashMap<>();

        for (SimpleStat.Type type : TypedStat.Type.values()) {
            double value = this.getItemStat(type, safe, mainHandOverride);

            if (value > 0D) {
                map.put(type, value);
            }
        }

        return map;
    }

    public double getItemStat(@NotNull SimpleStat.Type type, boolean safe) {
        return this.getItemStat(type, safe, null);
    }

    public double getItemStat(@NotNull SimpleStat.Type type, boolean safe, @Nullable ItemStack mainHandOverride) {
        if ((!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) || !type.isGlobal()) {
            return 0D;
        }

        SimpleStat stat = (SimpleStat) ItemStats.getStat(type);
        if (stat == null) return 0D;

        List<ItemStack>                           equip   = this.getEquipmentForStats(mainHandOverride);
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();

        for (ItemStack item : equip) {
            if (item == null || item.getType().isAir()) continue;
            bonuses.addAll(stat.get(item, player));
        }

        bonuses.addAll(this.getBonuses(stat));

        if (type == TypedStat.Type.ARMOR_TOUGHNESS) {
            AttributeInstance attribute =
                    entity.getAttribute(VersionManager.getNms().getAttribute("ARMOR_TOUGHNESS"));
            if (attribute != null) {
                bonuses.add((isPercent, input) -> isPercent ? input : input + attribute.getValue());
            }
        }

        // Get Sets bonuses
        double value = BonusCalculator.SIMPLE_FULL.apply(type == TypedStat.Type.CRITICAL_DAMAGE ? 1D : 0D, bonuses);
        value = this.getEffectBonus(stat, safe).applyAsDouble(value);

        if (stat.getCapability() >= 0) {
            if (value > stat.getCapability()) {
                value = stat.getCapability();
            }
        }

        return value;
    }

    @NotNull
    private List<ItemStack> getEquipmentForStats(@Nullable ItemStack mainHandOverride) {
        List<ItemStack> equip = this.getEquipment();
        if (mainHandOverride == null) {
            return equip;
        }

        ItemStack currentMainHand = this.getItemInMainHand();
        equip.removeIf(item -> item != null && item.isSimilar(currentMainHand));

        if (!ItemUT.isAir(mainHandOverride) && (!ItemUtils.isArmor(mainHandOverride)
                || mainHandOverride.getType() == Material.SHIELD)) {
            equip.add(mainHandOverride);
        }

        if (this.isPlayer()) {
            equip.removeIf(item -> item == null || !ItemUtils.canUse(item, this.player, false));
        }

        return equip;
    }

    public double getEnchantProtectFactor(@NotNull Enchantment en) {
        int epfPer = 1; // EPF Per each enchantment level
        if (en == NamespaceResolver.getEnchantment("FIRE_PROTECTION", "PROTECTION_FIRE")
                || en == NamespaceResolver.getEnchantment("BLAST_PROTECTION", "PROTECTION_EXPLOSION")
                || en == NamespaceResolver.getEnchantment("PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE")) {
            epfPer = 2;
        } else if (en == NamespaceResolver.getEnchantment("FEATHER_FALLING", "PROTECTION_FALL")) {
            epfPer = 3;
        }

        double epf = 0;

        for (ItemStack i : this.getEquipment()) {
            int lvl = i.getEnchantmentLevel(en);
            epf += (lvl * epfPer);
        }

        return Math.min(20D, epf);
    }
}
