package studio.magemonkey.divinity.manager.damage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.DamageScope;
import studio.magemonkey.codex.api.damage.FinalDamageEvent;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.api.event.DivinityHitEvent;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The new damage event surface (Combat 4.3a): when {@link DivinityHitEvent.Prepare} and
 * {@link DivinityHitEvent.Result} fire relative to the deprecated events, what they report, and what changing
 * or cancelling them does.
 */
@SuppressWarnings("deprecation")
class DivinityHitEventTest extends CombatTestBase {
    private final List<String>                        fired    = new ArrayList<>();
    private final List<DivinityHitEvent>              hits     = new ArrayList<>();
    private       Consumer<DivinityHitEvent.Prepare>  onPrepare = e -> {};
    private       Consumer<DivinityHitEvent.Result>   onResult  = e -> {};
    private       Listener                            recorder;

    @BeforeEach
    void listen() {
        recorder = new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.BeforeScale e) {fired.add("BeforeScale");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Start e) {fired.add("Start");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Dodge e) {fired.add("Dodge");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Pre e) {fired.add("Pre");}

            @EventHandler(priority = EventPriority.LOWEST)
            public void on(DivinityDamageEvent.Exit e) {fired.add("Exit");}

            @EventHandler
            public void on(DivinityHitEvent.Prepare e) {
                fired.add("Prepare");
                hits.add(e);
                onPrepare.accept(e);
            }

            @EventHandler
            public void on(DivinityHitEvent.Result e) {
                fired.add("Result");
                hits.add(e);
                onResult.accept(e);
            }
        };
        server.getPluginManager().registerEvents(recorder, plugin);
    }

    @AfterEach
    void stopListening() {
        HandlerList.unregisterAll(recorder);
        fired.clear();
        hits.clear();
        onPrepare = e -> {};
        onResult = e -> {};
    }

    private EntityDamageByEntityEvent swing(ItemStack weapon, double eventDamage) {
        attacker.getInventory().setItemInMainHand(weapon);
        refresh(attacker);
        refresh(victim);
        fired.clear();
        hits.clear();
        EntityDamageByEntityEvent event = meleeEvent(attacker, victim, eventDamage);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private DivinityHitEvent.Result result() {
        return (DivinityHitEvent.Result) hits.stream()
                .filter(hit -> hit instanceof DivinityHitEvent.Result)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No Result fired: " + fired));
    }

    // ------------------------------------------------------------------ //
    // Order
    // ------------------------------------------------------------------ //

    @Test
    void firesBetweenTheDeprecatedEvents() {
        swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(List.of("BeforeScale", "Prepare", "Start", "Pre", "Result", "Exit"), fired);
    }

    @Test
    void eachEventHasItsOwnHandlerList() {
        assertNotSame(DivinityHitEvent.Prepare.getHandlerList(), DivinityHitEvent.Result.getHandlerList());
    }

    @Test
    void resultSharesCodexsHandlerList() {
        swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertSame(FinalDamageEvent.getHandlerList(), DivinityHitEvent.Result.getHandlerList());
        assertSame(FinalDamageEvent.getHandlerList(), result().getHandlers());
    }

    @Test
    void dodgedHit_firesPrepareButNoResult() {
        liftCap(TypedStat.Type.DODGE_RATE);
        ItemStack chest = armor(Map.of());
        stat(chest, TypedStat.Type.DODGE_RATE, 100);
        victim.getInventory().setChestplate(chest);

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(List.of("BeforeScale", "Prepare", "Start", "Dodge"), fired);
        assertTrue(event.isCancelled());
    }

    @Test
    void rawRequest_firesNothing() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);

        try (DamageScope ignored = DamageContexts.open(victim,
                DamageRequest.builder().source(attacker).weaponStats(WeaponStatsMode.RAW).build())) {
            victim.damage(7, attacker);
        }

        assertEquals(List.of(), fired);
    }

    // ------------------------------------------------------------------ //
    // What the hit is
    // ------------------------------------------------------------------ //

    @Test
    void undescribedSwing_isAWeaponMeleeHitManagedAsAdd() {
        swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        for (DivinityHitEvent hit : hits) {
            assertFalse(hit.isDescribed());
            assertNull(hit.getRequest());
            assertEquals(DamageOrigin.WEAPON_MELEE, hit.getOrigin());
            assertEquals(WeaponStatsMode.ADD, hit.getWeaponStatsMode());
            assertEquals(Map.of(), hit.getRequestedAmounts());
            assertSame(attacker, hit.getAttacker());
            assertSame(victim, hit.getVictim());
            assertNotNull(hit.getWeapon());
        }
    }

    @Test
    void environmentalDamage_isAnEnvironmentHit() {
        refresh(victim);
        victim.damage(4);

        assertEquals(DamageOrigin.ENVIRONMENT, result().getOrigin());
        assertNull(result().getAttacker());
        assertEquals(Map.of("physical", 4D), result().getDamages());
    }

    @Test
    void describedHit_reportsTheRequest() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);
        DamageRequest request = DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .source(attacker)
                .weaponStats(WeaponStatsMode.REPLACE)
                .amount("DIVINITY_fire", 4)
                .build();

        try (DamageScope ignored = DamageContexts.open(victim, request)) {
            victim.damage(4, attacker);
        }

        DivinityHitEvent.Result result = result();
        assertTrue(result.isDescribed());
        assertSame(request, result.getRequest());
        assertEquals(DamageOrigin.SKILL, result.getOrigin());
        assertEquals(WeaponStatsMode.REPLACE, result.getWeaponStatsMode());
        assertEquals(Map.of("DIVINITY_fire", 4D), result.getRequestedAmounts());
        assertEquals(Map.of("fire", 4D), result.getDamages());
    }

    @Test
    void legacyIgnoreDivinity_isReportedAsIgnore_andStillGetsAResult() throws Exception {
        FabledHook hook = installFabledHook();
        try {
            hook.ignoreDivinity(attacker, true);
            swing(weapon(Map.of("physical", 20D)), 7);
        } finally {
            hook.ignoreDivinity(attacker, false);
            removeFabledHook(hook);
        }

        assertEquals(WeaponStatsMode.IGNORE, result().getWeaponStatsMode());
        assertEquals(List.of("BeforeScale", "Prepare", "Start", "Pre", "Result"), fired,
                "Result fires for an IGNORE hit; Exit still does not");
    }

    // ------------------------------------------------------------------ //
    // Prepare
    // ------------------------------------------------------------------ //

    @Test
    void prepare_changingTheDamageChangesTheHit() {
        onPrepare = e -> {
            assertEquals(20D, e.getDamage("physical"), DELTA);
            e.setDamage("physical", 10);
            assertTrue(e.setDamage("fire", 5));
            assertFalse(e.setDamage("not_a_type", 5));
        };

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(Map.of("physical", 10D, "fire", 5D), damagesOf(victim));
        assertEquals(15D, event.getFinalDamage(), DELTA);
    }

    @Test
    void prepare_damageSetToZeroLeavesTheHit() {
        onPrepare = e -> e.setDamage("fire", 0);

        swing(weapon(Map.of("physical", 20D, "fire", 10D)), VANILLA_SWORD_DAMAGE);

        assertEquals(Map.of("physical", 20D), damagesOf(victim));
    }

    @Test
    void prepare_changingTheDefenseChangesTheHit() {
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));
        onPrepare = e -> {
            assertEquals(10D, e.getDefense("physical"), DELTA);
            e.setDefense("physical", 0);
        };

        swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(Map.of("physical", 20D), damagesOf(victim));
    }

    @Test
    void prepare_seenByStartListeners() {
        List<Double> seenAtStart = new ArrayList<>();
        Listener start = new Listener() {
            @EventHandler(priority = EventPriority.NORMAL)
            public void on(DivinityDamageEvent.Start e) {
                seenAtStart.add(e.getDamageMap().get(ItemStats.getDamageById("physical")));
            }
        };
        server.getPluginManager().registerEvents(start, plugin);
        onPrepare = e -> e.setDamage("physical", 3);
        try {
            swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);
        } finally {
            HandlerList.unregisterAll(start);
        }

        assertEquals(List.of(3D), seenAtStart);
    }

    @Test
    void prepare_cancelled_stopsTheHit() {
        onPrepare = e -> e.setCancelled(true);

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertTrue(event.isCancelled());
        assertEquals(List.of("BeforeScale", "Prepare"), fired);
    }

    // ------------------------------------------------------------------ //
    // Result
    // ------------------------------------------------------------------ //

    @Test
    void result_reportsTheNumbersAfterDefense() {
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        DivinityHitEvent.Result result = result();
        // As in CombatCharacterizationTest#defense_currentFormula
        double physical = damagesOf(victim).get("physical");
        assertTrue(physical < 20D);
        assertEquals(physical, result.getDamage("physical"), DELTA);
        assertEquals(physical, result.getTotalDamage(), DELTA);
        assertEquals(20D - physical, result.getDefended(ItemStats.getDamageById("physical")), DELTA);
        assertEquals(Map.of(ItemStats.getDamageById("physical"), 20D), result.getIncomingDamages());
        assertEquals(physical, event.getFinalDamage(), 0.01);
        assertFalse(result.isCritical());
        assertFalse(result.isBlocked());
    }

    @Test
    void result_changingTheDamageIsWhatTheVictimTakes_andWhatPostEffectsSee() {
        liftCap(TypedStat.Type.VAMPIRISM);
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.VAMPIRISM, 50);
        attacker.setHealth(10D);
        List<Double> seenAtExit = new ArrayList<>();
        Listener exit = new Listener() {
            @EventHandler
            public void on(DivinityDamageEvent.Exit e) {seenAtExit.add(e.getDamageMeta().getTotalDamage());}
        };
        server.getPluginManager().registerEvents(exit, plugin);
        onResult = e -> e.setDamage("physical", 4);
        EntityDamageByEntityEvent event;
        try {
            event = swing(sword, VANILLA_SWORD_DAMAGE);
        } finally {
            HandlerList.unregisterAll(exit);
        }

        assertEquals(4D, event.getFinalDamage(), DELTA);
        assertEquals(Map.of("physical", 4D), damagesOf(victim));
        assertEquals(List.of(4D), seenAtExit);
        assertEquals(12D, attacker.getHealth(), DELTA, "Vampirism heals half of the changed damage");
    }

    @Test
    void result_setTotalDamage_keepsEachTypesShare() {
        onResult = e -> {
            assertEquals(30D, e.getTotalDamage(), DELTA);
            e.setTotalDamage(15);
        };

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D, "fire", 10D)), 0);

        assertEquals(Map.of("physical", 10D, "fire", 5D), rounded(damagesOf(victim)));
        assertEquals(15D, event.getFinalDamage(), DELTA);
    }

    @Test
    void result_setTotalDamage_withNothingLeft_goesToTheDefaultType() {
        onResult = e -> {
            e.setDamage("physical", 0);
            e.setTotalDamage(5);
        };

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(5D, event.getFinalDamage(), DELTA);
        assertEquals(5D, damagesOf(victim).get(ItemStats.getDamageByDefault().getId()), DELTA);
    }

    @Test
    void result_damageTakenToNothing_cancelsTheHitAsAFullyMitigatedOne() {
        onResult = e -> e.setTotalDamage(0);

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertTrue(event.isCancelled());
        assertFalse(fired.contains("Exit"));
    }

    @Test
    void result_cancelled_writesNothingAndSkipsPostEffects() {
        liftCap(TypedStat.Type.VAMPIRISM);
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.VAMPIRISM, 50);
        attacker.setHealth(10D);
        onResult = e -> e.setCancelled(true);

        EntityDamageByEntityEvent event = swing(sword, VANILLA_SWORD_DAMAGE);

        assertTrue(event.isCancelled());
        assertEquals(VANILLA_SWORD_DAMAGE, event.getDamage(), DELTA, "BASE is left as the event had it");
        assertEquals(List.of("BeforeScale", "Prepare", "Start", "Pre", "Result"), fired);
        assertEquals(10D, attacker.getHealth(), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Result as Codex's FinalDamageEvent
    // ------------------------------------------------------------------ //

    /** Registers a listener of Codex's event, as a plugin that cannot see Divinity would. */
    private Listener onFinalDamage(EventPriority priority, Consumer<FinalDamageEvent> action) {
        Listener listener = new Listener() {};
        server.getPluginManager().registerEvent(FinalDamageEvent.class, listener, priority,
                (l, e) -> action.accept((FinalDamageEvent) e), plugin);
        return listener;
    }

    @Test
    void codexListener_receivesOneEventPerHit_theResult() {
        List<FinalDamageEvent> seen = new ArrayList<>();
        Listener codex = onFinalDamage(EventPriority.NORMAL, seen::add);
        try {
            swing(weapon(Map.of("physical", 20D, "fire", 10D)), 0);
        } finally {
            HandlerList.unregisterAll(codex);
        }

        assertEquals(1, seen.size(), "Prepare is not a FinalDamageEvent");
        assertSame(result(), seen.get(0));
        FinalDamageEvent hit = seen.get(0);
        assertEquals(Map.of("physical", 20D, "fire", 10D), hit.getDamages());
        assertEquals(30D, hit.getTotalDamage(), DELTA);
        assertSame(victim, hit.getVictim());
        assertSame(attacker, hit.getAttacker());
        assertEquals(DamageOrigin.WEAPON_MELEE, hit.getOrigin());
        assertEquals(WeaponStatsMode.ADD, hit.getWeaponStatsMode());
        assertNull(hit.getRequest());
    }

    @Test
    void codexAndDivinityListeners_seeAndChangeTheSameNumbers() {
        List<Double> seenByCodexAfter = new ArrayList<>();
        // Codex's listener runs first and changes the hit; Divinity's Result listener sees that and changes it
        // again; a later Codex listener sees Divinity's change
        Listener first = onFinalDamage(EventPriority.LOW, e -> {
            assertEquals(20D, e.getDamage("physical"), DELTA);
            e.setDamage("physical", 8);
        });
        Listener last = onFinalDamage(EventPriority.HIGH, e -> seenByCodexAfter.add(e.getDamage("physical")));
        onResult = e -> {
            assertEquals(8D, e.getDamage("physical"), DELTA);
            assertEquals(8D, e.getDamage(ItemStats.getDamageById("physical")), DELTA);
            assertEquals(Map.of(ItemStats.getDamageById("physical"), 8D), e.getDamagesByAttribute());
            e.setDamage(ItemStats.getDamageById("physical"), 5);
        };
        EntityDamageByEntityEvent event;
        try {
            event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);
        } finally {
            HandlerList.unregisterAll(first);
            HandlerList.unregisterAll(last);
        }

        assertEquals(List.of(5D), seenByCodexAfter);
        assertEquals(5D, event.getFinalDamage(), DELTA);
        assertEquals(Map.of("physical", 5D), damagesOf(victim));
    }

    @Test
    void codexListener_setTotalDamage_keepsEachTypesShare() {
        Listener codex = onFinalDamage(EventPriority.NORMAL, e -> e.setTotalDamage(15));
        EntityDamageByEntityEvent event;
        try {
            event = swing(weapon(Map.of("physical", 20D, "fire", 10D)), 0);
        } finally {
            HandlerList.unregisterAll(codex);
        }

        assertEquals(Map.of("physical", 10D, "fire", 5D), rounded(damagesOf(victim)));
        assertEquals(15D, event.getFinalDamage(), DELTA);
    }

    @Test
    void result_setDamage_unknownTypeId_isIgnored() {
        onResult = e -> {
            assertFalse(e.isDamageType("not_a_type"));
            assertTrue(e.isDamageType("physical"));
            e.setDamage("not_a_type", 5);
            assertEquals(0D, e.getDamage("not_a_type"), DELTA);
            assertEquals(Map.of("physical", 20D), e.getDamages());
            assertEquals(20D, e.getTotalDamage(), DELTA);
        };

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(20D, event.getFinalDamage(), DELTA);
    }

    @Test
    void result_negativeOrNaNDamage_countsAsZero_ratherThanThrowing() {
        onResult = e -> {
            e.setDamage("physical", -3);
            assertEquals(0D, e.getDamage("physical"), DELTA);
            e.setDamage("physical", Double.NaN);
            assertEquals(0D, e.getDamage("physical"), DELTA);
            e.setDamage("physical", 2);
        };

        EntityDamageByEntityEvent event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(2D, event.getFinalDamage(), DELTA);
    }

    @Test
    void result_damagesById_cannotBeChangedDirectly() {
        onResult = e -> assertThrows(UnsupportedOperationException.class, () -> e.getDamages().put("physical", 1D));

        swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);

        assertEquals(Map.of("physical", 20D), damagesOf(victim));
    }

    @Test
    void codexListener_cancelling_cancelsTheBukkitEvent() {
        Listener codex = onFinalDamage(EventPriority.NORMAL, e -> e.setCancelled(true));
        EntityDamageByEntityEvent event;
        try {
            event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);
        } finally {
            HandlerList.unregisterAll(codex);
        }

        assertTrue(event.isCancelled());
        assertTrue(result().isCancelled());
        assertEquals(VANILLA_SWORD_DAMAGE, event.getDamage(), DELTA, "BASE is left as the event had it");
        assertFalse(fired.contains("Exit"));
    }

    @Test
    void codexListener_unCancelling_unCancelsTheBukkitEvent() {
        onResult = e -> e.setCancelled(true);
        Listener codex = onFinalDamage(EventPriority.HIGH, e -> e.setCancelled(false));
        EntityDamageByEntityEvent event;
        try {
            event = swing(weapon(Map.of("physical", 20D)), VANILLA_SWORD_DAMAGE);
        } finally {
            HandlerList.unregisterAll(codex);
        }

        assertFalse(event.isCancelled());
        assertEquals(20D, event.getFinalDamage(), DELTA);
    }
}
