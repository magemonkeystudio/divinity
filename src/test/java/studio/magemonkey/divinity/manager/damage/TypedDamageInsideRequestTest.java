package studio.magemonkey.divinity.manager.damage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.DamageScope;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.codex.registry.DamageRegistry;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.api.event.DivinityHitEvent;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A skill whose damage classifier is {@code DIVINITY_<type>} goes through {@code DamageRegistry.dealDamage},
 * and so through {@link DamageManager#dealDamage}, while the skill's own request is open on the target. These
 * pin what Divinity does with the two together (Combat 4.3a).
 */
@SuppressWarnings("deprecation")
class TypedDamageInsideRequestTest extends CombatTestBase {
    private static final List<String> FULL_PIPELINE = List.of("BeforeScale", "Start", "Pre", "Exit");
    private static final List<String> EXEMPT        = List.of("BeforeScale", "Start", "Pre");

    private final List<String>          fired   = new ArrayList<>();
    private final List<DivinityHitEvent> results = new ArrayList<>();
    private       Listener              recorder;

    @BeforeEach
    void listen() {
        recorder = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.BeforeScale e) {fired.add("BeforeScale");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Start e) {fired.add("Start");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Pre e) {fired.add("Pre");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Exit e) {fired.add("Exit");}

            @EventHandler(priority = EventPriority.MONITOR)
            public void on(DivinityHitEvent.Result e) {results.add(e);}
        };
        server.getPluginManager().registerEvents(recorder, plugin);
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("fire", 10D)));
        refresh(attacker);
        refresh(victim);
    }

    @AfterEach
    void stopListening() {
        HandlerList.unregisterAll(recorder);
        fired.clear();
        results.clear();
    }

    private DamageRequest outer(WeaponStatsMode mode) {
        return DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .source(attacker)
                .amount("DIVINITY_fire", 4)
                .weaponStats(mode)
                .build();
    }

    @Test
    void outerIgnore_usesOnlyTheTypedAmount_andTheHitIsExempt() {
        armAttackerWith(TypedStat.Type.VAMPIRISM);
        DamageRequest request = outer(WeaponStatsMode.IGNORE);

        try (DamageScope scope = DamageContexts.open(victim, request)) {
            assertTrue(DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker));
            assertTrue(scope.isClaimed(), "The skill's own request is the one the hit used");
        }

        // Fire 4 against fire defense 10, as in damageRegistry_divinityType_isReducedByMatchingDefense
        assertEquals(Map.of("fire", 2.72D), rounded(damagesOf(victim)));
        assertEquals(EXEMPT, fired, "No Exit, as for ignore-divinity: no durability cost");
        assertEquals(10D, attacker.getHealth(), DELTA, "No on-hit effects: vampirism did not heal");
        assertSame(request, EntityStats.get(victim).getLastDamageMeta().getRequest());
        assertEquals(1, results.size());
        assertEquals(WeaponStatsMode.IGNORE, results.get(0).getWeaponStatsMode());
        assertEquals(DamageOrigin.SKILL, results.get(0).getOrigin());
        assertEquals(Map.of("DIVINITY_fire", 4D), results.get(0).getRequestedAmounts());
    }

    @Test
    void outerRaw_leavesTheEventAlone() {
        try (DamageScope scope = DamageContexts.open(victim, outer(WeaponStatsMode.RAW))) {
            assertTrue(DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker));
            assertTrue(scope.isClaimed());
        }

        assertEquals(List.of(), fired, "None of Divinity's damage events fire");
        assertEquals(List.of(), results);
        assertEquals(4D, victim.lastEvent.getFinalDamage(), DELTA, "The fire defense is not applied");
    }

    @Test
    void outerAdd_replacesWithTheTypedAmount_asWithoutARequest() {
        DamageRequest request = outer(WeaponStatsMode.ADD);

        try (DamageScope scope = DamageContexts.open(victim, request)) {
            assertTrue(DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker));
            assertTrue(scope.isClaimed());
        }

        assertEquals(Map.of("fire", 2.72D), rounded(damagesOf(victim)));
        assertEquals(2.72D, victim.lastEvent.getFinalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, fired);
        assertSame(request, EntityStats.get(victim).getLastDamageMeta().getRequest());
        assertEquals(WeaponStatsMode.REPLACE, results.get(0).getWeaponStatsMode());
        assertEquals(DamageOrigin.SKILL, results.get(0).getOrigin(), "The dealer's origin, not EXTERNAL");
    }

    @Test
    void outerReplace_replacesWithTheTypedAmount() {
        try (DamageScope ignored = DamageContexts.open(victim, outer(WeaponStatsMode.REPLACE))) {
            DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker);
        }

        assertEquals(Map.of("fire", 2.72D), rounded(damagesOf(victim)));
        assertEquals(FULL_PIPELINE, fired);
    }

    @Test
    void typedAmountIsTheOneDealt_notTheOneInTheRequest() {
        try (DamageScope ignored = DamageContexts.open(victim, DamageRequest.builder()
                .source(attacker)
                .amount("DIVINITY_fire", 100)
                .weaponStats(WeaponStatsMode.IGNORE)
                .build())) {
            DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker);
        }

        assertEquals(Map.of("fire", 2.72D), rounded(damagesOf(victim)));
    }

    @Test
    void aSecondHitInsideTheSameRequest_isAnOrdinarySwing() {
        try (DamageScope ignored = DamageContexts.open(victim, outer(WeaponStatsMode.IGNORE))) {
            DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker);
            fired.clear();

            victim.damage(VANILLA_SWORD_DAMAGE, attacker);
        }

        assertEquals(Map.of("physical", 20D), damagesOf(victim));
        assertEquals(FULL_PIPELINE, fired);
        assertNull(EntityStats.get(victim).getLastDamageMeta().getRequest());
    }

    @Test
    void unknownType_dealsNothing_andLeavesTheRequestOpen() {
        try (DamageScope scope = DamageContexts.open(victim, outer(WeaponStatsMode.IGNORE))) {
            assertFalse(DamageRegistry.dealDamage(victim, 4, "DIVINITY_not_a_type", attacker));
            assertFalse(scope.isClaimed());
        }

        assertNull(victim.lastEvent);
    }

    @Test
    void withoutAnOuterRequest_nothingChanges() {
        DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker);

        assertEquals(Map.of("fire", 2.72D), rounded(damagesOf(victim)));
        assertEquals(FULL_PIPELINE, fired);
        DamageRequest inner = EntityStats.get(victim).getLastDamageMeta().getRequest();
        assertNotNull(inner);
        assertEquals(DamageOrigin.EXTERNAL, inner.getOrigin());
        assertEquals(WeaponStatsMode.REPLACE, inner.getWeaponStats());
    }

    /** Gives the attacker's sword a certain stat, so an on-hit effect would be visible. */
    private void armAttackerWith(TypedStat.Type type) {
        org.bukkit.inventory.ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, type, 100);
        attacker.getInventory().setItemInMainHand(sword);
        attacker.setHealth(10D);
        refresh(attacker);
    }
}
