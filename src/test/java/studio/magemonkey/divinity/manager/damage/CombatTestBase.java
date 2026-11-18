package studio.magemonkey.divinity.manager.damage;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;

/**
 * The setup the combat tests written after the characterization tests share: an attacker, a victim that
 * fires real damage events, and helpers to build Divinity items. {@code CombatCharacterizationTest} keeps
 * its own copy of these on purpose, so that it is not touched by changes here.
 */
abstract class CombatTestBase extends MockedTest {
    protected static final double DELTA                = 1e-6;
    protected static final double VANILLA_SWORD_DAMAGE = 6D;

    private final Map<TypedStat.Type, Double> originalCaps = new EnumMap<>(TypedStat.Type.class);

    protected PlayerMock   attacker;
    protected FiringPlayer victim;

    @BeforeEach
    void setupCombat() {
        attacker = new TickSafePlayer(server, "Attacker");
        server.addPlayer(attacker);
        players.add(attacker);
        victim = new FiringPlayer(server, "Victim");
        server.addPlayer(victim);
        players.add(victim);

        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;
        doAnswer(ans -> isIronSword(ans.getArgument(0)) ? VANILLA_SWORD_DAMAGE : 0D)
                .when(VersionManager.getNms()).getDefaultDamage(any());
        doAnswer(ans -> isIronSword(ans.getArgument(0)))
                .when(VersionManager.getNms()).isWeapon(any());
        doAnswer(ans -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(((String) ans.getArgument(0)).toLowerCase(
                Locale.US)))).when(VersionManager.getNms()).getAttribute(anyString());
    }

    @AfterEach
    void tearDownCombat() {
        int offset = 0;
        for (PlayerMock player : players) {
            player.teleport(player.getLocation().add(10_000 + 100 * offset++, 0, 0));
        }
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }
        players.forEach(EntityStats::purge);
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        originalCaps.forEach((type, cap) -> ItemStats.getStat(type).setCapability(cap));
        originalCaps.clear();
    }

    private static boolean isIronSword(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.IRON_SWORD;
    }

    protected void refresh(LivingEntity entity) {
        EntityStats.purge(entity);
        EntityStats.get(entity).updateAll();
    }

    protected Map<String, Double> damagesOf(LivingEntity target) {
        return ids(Objects.requireNonNull(EntityStats.get(target).getLastDamageMeta()));
    }

    protected static Map<String, Double> ids(DamageMeta meta) {
        Map<String, Double> damages = new HashMap<>();
        meta.getDamages().forEach((attribute, amount) -> damages.put(attribute.getId(), amount));
        return damages;
    }

    protected static Map<String, Double> rounded(Map<String, Double> values) {
        Map<String, Double> rounded = new HashMap<>();
        values.forEach((key, value) -> rounded.put(key, Math.round(value * 1e6) / 1e6));
        return rounded;
    }

    protected static Map<DamageModifier, Double> modifiers(double base) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, base);
        return modifiers;
    }

    protected static Map<DamageModifier, Function<? super Double, Double>> functions(Map<DamageModifier, Double> modifiers) {
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        modifiers.forEach((modifier, value) -> functions.put(modifier, d -> value));
        return functions;
    }

    protected EntityDamageByEntityEvent meleeEvent(Entity damager, Entity target, double eventDamage) {
        return new EntityDamageByEntityEvent(damager,
                target,
                DamageCause.ENTITY_ATTACK,
                modifiers(eventDamage),
                functions(modifiers(eventDamage)));
    }

    protected ItemStack weapon(Map<String, Double> damages) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        damages.forEach((id, value) -> Objects.requireNonNull(ItemStats.getDamageById(id))
                .add(item, new StatBonus(new double[]{value}, false, null), -1));
        return item;
    }

    protected ItemStack armor(Map<String, Double> defenses) {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        defenses.forEach((id, value) -> Objects.requireNonNull(ItemStats.getDefenseById(id))
                .add(item, new StatBonus(new double[]{value}, false, null), -1));
        return item;
    }

    protected void stat(ItemStack item, TypedStat.Type type, double value) {
        ((SimpleStat) Objects.requireNonNull(ItemStats.getStat(type), type.name() + " is not enabled"))
                .add(item, new StatBonus(new double[]{value}, false, null), -1);
    }

    protected void liftCap(TypedStat.Type type) {
        TypedStat stat = Objects.requireNonNull(ItemStats.getStat(type), type.name() + " is not enabled");
        originalCaps.putIfAbsent(type, stat.getCapability());
        stat.setCapability(-1);
    }

    /**
     * Registers a Fabled hook by hand, since Fabled is not loaded in the tests.
     */
    protected FabledHook installFabledHook() throws Exception {
        FabledHook hook  = new FabledHook(plugin);
        Field      field = NHook.class.getDeclaredField("pluginName");
        field.setAccessible(true);
        field.set(hook, EHook.SKILL_API);
        plugin.getHooks().getHooks(plugin).add(hook);
        return hook;
    }

    protected void removeFabledHook(FabledHook hook) {
        plugin.getHooks().getHooks(plugin).remove(hook);
    }

    static class TickSafePlayer extends PlayerMock {
        TickSafePlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name, UUID.randomUUID());
        }

        @Override
        public float getAttackCooldown() {
            return 1F;
        }
    }

    /**
     * Fires the damage event a real server would for {@code damage(amount, source)}, and remembers it.
     */
    static class FiringPlayer extends TickSafePlayer {
        final   List<EntityDamageEvent>  events    = new ArrayList<>();
        private final Map<Material, Integer> cooldowns = new EnumMap<>(Material.class);
        EntityDamageEvent lastEvent;

        FiringPlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name);
        }

        @Override
        public void damage(double amount, @Nullable Entity source) {
            if (source == null) {
                lastEvent = new EntityDamageEvent(this,
                        DamageCause.CUSTOM,
                        modifiers(amount),
                        functions(modifiers(amount)));
            } else {
                lastEvent = new EntityDamageByEntityEvent(source,
                        this,
                        DamageCause.ENTITY_ATTACK,
                        modifiers(amount),
                        functions(modifiers(amount)));
            }
            events.add(lastEvent);
            server.getPluginManager().callEvent(lastEvent);
        }

        @Override
        public void damage(double amount) {
            damage(amount, (Entity) null);
        }

        @Override
        public int getCooldown(@NotNull Material material) {
            return cooldowns.getOrDefault(material, 0);
        }

        @Override
        public void setCooldown(@NotNull Material material, int ticks) {
            cooldowns.put(material, ticks);
        }
    }
}
