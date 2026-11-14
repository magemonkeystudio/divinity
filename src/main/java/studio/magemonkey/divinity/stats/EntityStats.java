package studio.magemonkey.divinity.stats;

import lombok.Getter;
import org.bukkit.Material;
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
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import static org.bukkit.attribute.AttributeModifier.Operation;

public class EntityStats {
    private static final Map<String, EntityStats> STATS;
    private static final SimpleStat.Type[]        ATTRIBUTE_BONUS_STATS;
    private static final NBTAttribute[]           ATTRIBUTE_BONUS_NBT;
    private static final double                   DEFAULT_ATTACK_POWER = 1D;
    private static final long                     MILLIS_PER_TICK      = 50L;

    /** Where snapshots read the time from. Tests replace it. */
    private static volatile LongSupplier clock = System::currentTimeMillis;

    static void setClock(@NotNull LongSupplier supplier) {
        clock = supplier;
    }

    static void resetClock() {
        clock = System::currentTimeMillis;
    }

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

    /**
     * What was worked out from this entity's gear, or null when nothing is cached. Everything that can change
     * it ends up in {@link #invalidateSnapshot()}, or is checked each time it is used; see {@link StatSnapshot}.
     */
    private volatile StatSnapshot snapshot;
    /** The bonus maps (sets, classes) behind {@link #bonuses}, so a refresh can tell whether they changed. */
    private final    List<BonusMap> bonusMaps = new ArrayList<>();

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

    /**
     * Whether stats are being kept for the entity. Stats, and the snapshot on them, are released when a
     * player quits, when an entity dies and when an entity is no longer valid (see {@link #getAll()}).
     */
    public static boolean isTracked(@NotNull LivingEntity entity) {
        return STATS.containsKey(entity.getUniqueId().toString());
    }

    /**
     * Drops every entity's snapshot. For changes that affect everyone, such as a reload of the stat config.
     */
    public static void invalidateAllSnapshots() {
        List<EntityStats> all;
        synchronized (STATS) {
            all = new ArrayList<>(STATS.values());
        }
        all.forEach(EntityStats::invalidateSnapshot);
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
            this.bonusMaps.clear();
            this.damageMeta = null;
            this.aoeIgnore = false;
            this.invalidateSnapshot();
            this.updateBonusAttributes();
        } else {
            this.invalidateSnapshot();
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

    /**
     * @deprecated Divinity no longer uses this flag. Area damage hits are recognised by their damage request.
     */
    @Deprecated
    public boolean isIgnoreAOE() {
        return this.aoeIgnore;
    }

    /**
     * @deprecated Divinity no longer uses this flag. Area damage hits are recognised by their damage request.
     */
    @Deprecated
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

    /**
     * Reads the entity's equipment and bonuses again, and drops the snapshot of its stats whether or not anything
     * looks different. This is what to call when something that is not equipment may have changed: a class,
     * a profile, a set.
     */
    public void updateAll() {
        this.invalidateSnapshot();
        if(EngineCfg.VANILLA_ONLY_ENTITY_STATS) return;
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return;
        }

        this.updateInventory();
        this.updateBonus();
    }

    /**
     * Reads the entity's equipment and bonuses again after a hit, and keeps the snapshot if they are what it
     * was built from. A hit wears gear down and changes nothing else, and it asks for this on the next tick for
     * both fighters, so dropping the snapshot here every time would leave it never used between two hits.
     * <p>
     * A change that has no event of its own, such as a plugin setting a piece of armor, is still seen here:
     * the equipment is compared with the copies the snapshot was built from.
     */
    public void refreshAfterHit() {
        if(EngineCfg.VANILLA_ONLY_ENTITY_STATS) return;
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return;
        }

        this.updateInventory();
        this.updateBonus();

        StatSnapshot current = this.snapshot;
        if (current != null && !current.matches(this.inventory, this.bonusMaps)) {
            this.snapshot = null;
        }
    }

    /**
     * Forgets what was worked out from this entity's gear, so the next read does it again.
     */
    public void invalidateSnapshot() {
        this.snapshot = null;
    }

    boolean hasSnapshot() {
        return this.snapshot != null;
    }

    private void addBonus(@NotNull BonusMap bMap) {
        this.bonusMaps.add(bMap);
        bMap.getBonuses()
                .forEach((stat, func) -> this.bonuses.computeIfAbsent(stat, list -> new ArrayList<>()).add(func));
    }

    @NotNull
    public List<BiFunction<Boolean, Double, Double>> getBonuses(@NotNull ItemLoreStat<?> stat) {
        return this.getBonuses(stat, this.arrowBonus, this.arrowLevel);
    }

    @NotNull
    private List<BiFunction<Boolean, Double, Double>> getBonuses(@NotNull ItemLoreStat<?> stat,
                                                                 @Nullable QArrow arrow,
                                                                 int arrowLevel) {
        List<BiFunction<Boolean, Double, Double>> bonuses =
                new ArrayList<>(this.bonuses.computeIfAbsent(stat, list -> new ArrayList<>()));

        BonusMap arrowBonus = arrow != null ? arrow.getBonusMap(arrowLevel) : null;
        if (arrowBonus != null) {
            BiFunction<Boolean, Double, Double> arrowBif = arrowBonus.getBonus(stat);
            if (arrowBif != null) bonuses.add(arrowBif);
        }

        UserProfile prof = this.getActiveProfile();
        if (prof != null) {
            bonuses.add(prof.getBuff(stat));
        }

        return bonuses;
    }

    private void updateBonus() {
        this.bonuses.clear();
        this.bonusMaps.clear();

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

    // What is cached is each damage type's minimum and maximum. The roll is made on every call, so ranges
    // keep their distribution.
    @NotNull
    public Map<DamageAttribute, Double> getDamageTypes(boolean safe) {
        return this.getDamageTypes(safe, null);
    }

    @NotNull
    public Map<DamageAttribute, Double> getDamageTypes(boolean safe, @Nullable ItemStack mainHandOverride) {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return Collections.emptyMap();
        }

        Biome bio = this.entity.getLocation().getBlock().getBiome();
        return StatSnapshot.roll(this.getDamageRanges(mainHandOverride), bio, stat -> this.getEffectBonus(stat, safe));
    }

    /**
     * Each damage type's minimum and maximum, summed over the equipment. Those are what the gear, sets,
     * classes and profile buffs decide; the roll, the biome and the effects come after.
     * <p>
     * Read from the snapshot, except when the answer depends on something that belongs to one hit: a different
     * main hand (a projectile's weapon) or a custom arrow's bonus.
     */
    @NotNull
    private Map<DamageAttribute, double[]> getDamageRanges(@Nullable ItemStack mainHandOverride) {
        if (mainHandOverride != null || this.arrowBonus != null) {
            return this.buildDamageRanges(mainHandOverride, this.arrowBonus, this.arrowLevel);
        }

        StatSnapshot current = this.snapshot();
        synchronized (current) {
            if (current.damageRanges == null) {
                current.damageRanges = this.buildDamageRanges(null, null, 0);
            }
            return current.damageRanges;
        }
    }

    @NotNull
    private Map<DamageAttribute, double[]> buildDamageRanges(@Nullable ItemStack mainHandOverride,
                                                             @Nullable QArrow arrow,
                                                             int arrowLevel) {
        Map<DamageAttribute, double[]> ranges = StatSnapshot.newRanges();
        List<ItemStack>                equip  = this.getEquipmentForStats(mainHandOverride);

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

            for (BiFunction<Boolean, Double, Double> bonus : this.getBonuses(dmgAtt, arrow, arrowLevel)) {
                bonuses.add((isPercent, input) -> input.length == 2
                        ? new double[]{
                        bonus.apply(isPercent, input[0]),
                        bonus.apply(isPercent, input[1])}
                        : new double[]{
                                bonus.apply(isPercent, input[0])});
            }
            ranges.put(dmgAtt, BonusCalculator.RANGE_FULL.apply(new double[]{0, 0}, bonuses));
        }
        return ranges;
    }

    @NotNull
    public Map<DefenseAttribute, Double> getDefenseTypes(boolean safe) {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) {
            return Collections.emptyMap();
        }

        Map<DefenseAttribute, Double> map = new HashMap<>();
        for (Map.Entry<DefenseAttribute, Double> entry : this.getDefenseValues().entrySet()) {
            DefenseAttribute dt    = entry.getKey();
            double           value = this.getEffectBonus(dt, safe).applyAsDouble(entry.getValue());
            if (value > 0D) {
                map.put(dt, value);
            }
        }

        return map;
    }

    /**
     * Each defense type's value from gear, sets, classes and profile buffs, before the active effects.
     */
    @NotNull
    private Map<DefenseAttribute, Double> getDefenseValues() {
        if (this.arrowBonus != null) {
            return this.buildDefenseValues(this.arrowBonus, this.arrowLevel);
        }

        StatSnapshot current = this.snapshot();
        synchronized (current) {
            // The vanilla armor attribute is added to the default defense and has no event of its own
            double armor = this.attributeValue("ARMOR", true);
            if (current.defenses == null || Double.compare(current.armorKey, armor) != 0) {
                current.armorKey = armor;
                current.defenses = this.buildDefenseValues(null, 0);
            }
            return current.defenses;
        }
    }

    @NotNull
    private Map<DefenseAttribute, Double> buildDefenseValues(@Nullable QArrow arrow, int arrowLevel) {
        List<ItemStack>               equip = this.getEquipment();
        Map<DefenseAttribute, Double> map   = new LinkedHashMap<>();

        for (DefenseAttribute dt : ItemStats.getDefenses()) {
            List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
            for (ItemStack item : equip) {
                bonuses.addAll(dt.get(item, player));
            }
            bonuses.addAll(this.getBonuses(dt, arrow, arrowLevel));
            if (dt.isDefault()) {
                AttributeInstance attribute = entity.getAttribute(VersionManager.getNms().getAttribute("ARMOR"));
                if (attribute != null) {
                    bonuses.add((isPercent, input) -> isPercent ? input : input + attribute.getBaseValue());
                }
            }

            map.put(dt, BonusCalculator.SIMPLE_FULL.apply(0D, bonuses));
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

        double value = mainHandOverride != null || this.arrowBonus != null
                ? this.buildItemStat(type, stat, mainHandOverride, this.arrowBonus, this.arrowLevel)
                : this.getItemStatValue(type, stat);

        return StatSnapshot.finish(stat, value, this.getEffectBonus(stat, safe));
    }

    /**
     * An item stat's value from gear, sets, classes and profile buffs, before the active effects and the cap.
     */
    private double getItemStatValue(@NotNull SimpleStat.Type type, @NotNull SimpleStat stat) {
        StatSnapshot current = this.snapshot();
        synchronized (current) {
            // The vanilla toughness attribute is added to this stat and has no event of its own
            double toughness = type == TypedStat.Type.ARMOR_TOUGHNESS
                    ? this.attributeValue("ARMOR_TOUGHNESS", false)
                    : Double.NaN;
            Double cached = current.itemStats.get(type);
            if (cached != null && type == TypedStat.Type.ARMOR_TOUGHNESS
                    && Double.compare(current.toughnessKey, toughness) != 0) {
                cached = null;
            }
            if (cached == null) {
                current.toughnessKey = type == TypedStat.Type.ARMOR_TOUGHNESS ? toughness : current.toughnessKey;
                cached = this.buildItemStat(type, stat, null, null, 0);
                current.itemStats.put(type, cached);
            }
            return cached;
        }
    }

    private double buildItemStat(@NotNull SimpleStat.Type type,
                                 @NotNull SimpleStat stat,
                                 @Nullable ItemStack mainHandOverride,
                                 @Nullable QArrow arrow,
                                 int arrowLevel) {
        List<ItemStack>                           equip   = this.getEquipmentForStats(mainHandOverride);
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();

        for (ItemStack item : equip) {
            if (item == null || item.getType().isAir()) continue;
            bonuses.addAll(stat.get(item, player));
        }

        bonuses.addAll(this.getBonuses(stat, arrow, arrowLevel));

        if (type == TypedStat.Type.ARMOR_TOUGHNESS) {
            AttributeInstance attribute =
                    entity.getAttribute(VersionManager.getNms().getAttribute("ARMOR_TOUGHNESS"));
            if (attribute != null) {
                bonuses.add((isPercent, input) -> isPercent ? input : input + attribute.getValue());
            }
        }

        // Get Sets bonuses
        return BonusCalculator.SIMPLE_FULL.apply(type == TypedStat.Type.CRITICAL_DAMAGE ? 1D : 0D, bonuses);
    }

    /**
     * The value of a vanilla attribute that a cached stat adds in, or NaN if the entity has none. It is
     * compared with the value a snapshot was built with, because plugins and potions change attributes
     * without telling anyone.
     */
    private double attributeValue(@NotNull String name, boolean base) {
        AttributeInstance attribute = entity.getAttribute(VersionManager.getNms().getAttribute(name));
        if (attribute == null) return Double.NaN;
        return base ? attribute.getBaseValue() : attribute.getValue();
    }

    /**
     * Takes what a projectile fired now would carry, so that the stats it hits with are the stats it was fired
     * with. Damage ranges and item stats are taken with the weapon in place of the main hand, as they are for
     * any projectile hit, and with the custom arrow's bonus. The active effects are taken as well, and the
     * charges they have are used up now rather than at impact.
     *
     * @return null if the entity has no stats from items to take
     */
    @Nullable
    public StatSnapshot snapshotForLaunch(@Nullable ItemStack weapon, @Nullable QArrow arrow, int arrowLevel) {
        if (!EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS && !this.isPlayer()) return null;

        Map<ItemLoreStat<?>, DoubleUnaryOperator> effects = new HashMap<>();
        StatSnapshot frozen = new StatSnapshot(this.entity.getLocation().getBlock().getBiome(), effects);

        Map<DamageAttribute, double[]> ranges = this.buildDamageRanges(weapon, arrow, arrowLevel);
        frozen.setDamageRanges(ranges);
        for (DamageAttribute dmgAtt : ranges.keySet()) {
            effects.put(dmgAtt, this.getEffectBonus(dmgAtt, false));
        }

        for (SimpleStat.Type type : TypedStat.Type.values()) {
            if (!type.isGlobal()) continue;
            SimpleStat stat = (SimpleStat) ItemStats.getStat(type);
            if (stat == null) continue;

            frozen.itemStats.put(type, this.buildItemStat(type, stat, weapon, arrow, arrowLevel));
            effects.put(stat, this.getEffectBonus(stat, false));
        }
        return frozen;
    }

    /**
     * The current snapshot, or a new one if there is none or it is out of date. A snapshot is out of date when
     * it is older than {@code performance.entity-stats.snapshot-max-age}, when a profile buff it includes has run
     * out, or when the player's profile or its buffs are not what it was built with. A maximum age of 0 turns
     * the cache off: every call then works from a snapshot that is thrown away.
     */
    @NotNull
    private StatSnapshot snapshot() {
        long        now     = clock.getAsLong();
        UserProfile profile = this.getActiveProfile();

        StatSnapshot current = this.snapshot;
        if (current != null && current.isCurrent(now, profile)) {
            return current;
        }

        long maxAge     = Math.max(0, EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE) * MILLIS_PER_TICK;
        long validUntil = now + maxAge;
        int  revision   = 0;
        if (profile != null) {
            // Asking for the next expiry also drops buffs that have already run out
            validUntil = Math.min(validUntil, profile.getNextBuffExpiry());
            revision = profile.getBuffRevision();
        }

        StatSnapshot created = new StatSnapshot(validUntil,
                profile,
                revision,
                StatSnapshot.copyOf(this.inventory),
                new ArrayList<>(this.bonusMaps));
        if (maxAge > 0) {
            this.snapshot = created;
        }
        return created;
    }

    @Nullable
    private UserProfile getActiveProfile() {
        if (!this.isPlayer() || this.isNPC()) return null;
        DivinityUser user = plugin.getUserManager().getOrLoadUser(this.player);
        return user == null ? null : user.getActiveProfile();
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
        StatSnapshot current = this.snapshot();
        synchronized (current) {
            return current.enchantFactors.computeIfAbsent(en, this::computeEnchantProtectFactor);
        }
    }

    private double computeEnchantProtectFactor(@NotNull Enchantment en) {
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
