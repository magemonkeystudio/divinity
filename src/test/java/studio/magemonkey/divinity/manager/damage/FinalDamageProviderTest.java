package studio.magemonkey.divinity.manager.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import studio.magemonkey.codex.api.damage.DamageContexts;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.DamageScope;
import studio.magemonkey.codex.api.damage.FinalDamageEvent;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.DivinityHitEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Divinity as a provider of Codex's {@link FinalDamageEvent} (Combat 4.3b): {@link FinalDamageEvent#willFire}
 * answers false for every hit Divinity's handler leaves before firing anything, and true for a hit that then fires
 * the event. Each early-exit test also fires the hit, so the predicate and the handler are checked against each
 * other.
 */
class FinalDamageProviderTest extends CombatTestBase {
    private final List<FinalDamageEvent> fired = new ArrayList<>();
    private       Listener               recorder;

    @BeforeEach
    void listen() {
        recorder = new Listener() {
            @EventHandler
            public void on(FinalDamageEvent e) {fired.add(e);}
        };
        server.getPluginManager().registerEvents(recorder, plugin);
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);
    }

    @AfterEach
    void stopListening() {
        HandlerList.unregisterAll(recorder);
        fired.clear();
    }

    /** Asks the predicate, then fires the event, and returns what the predicate said. */
    private boolean askThenFire(EntityDamageEvent event) {
        boolean willFire = FinalDamageEvent.willFire(event);
        fired.clear();
        server.getPluginManager().callEvent(event);
        return willFire;
    }

    @Test
    void divinityIsRegistered() {
        assertTrue(FinalDamageEvent.hasProvider());
    }

    @Test
    void swing_willFire_andFiresOnce() {
        EntityDamageByEntityEvent event = meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE);

        assertTrue(askThenFire(event));
        assertEquals(1, fired.size());
        FinalDamageEvent hit = fired.get(0);
        assertInstanceOf(DivinityHitEvent.Result.class, hit);
        assertSame(event, hit.getDamageEvent());
        assertEquals(Map.of("physical", 20D), hit.getDamages());
    }

    @Test
    void environmentalDamage_willFire_andFires() {
        EntityDamageEvent event = new EntityDamageEvent(victim, DamageCause.FALL, modifiers(4), functions(modifiers(4)));

        assertTrue(askThenFire(event));
        assertEquals(1, fired.size());
    }

    @Test
    void askingClaimsNoRequest() {
        DamageRequest request = DamageRequest.builder()
                .origin(DamageOrigin.SKILL)
                .source(attacker)
                .weaponStats(WeaponStatsMode.REPLACE)
                .amount("DIVINITY_fire", 4)
                .build();
        try (DamageScope ignored = DamageContexts.open(victim, request)) {
            EntityDamageByEntityEvent event = meleeEvent(attacker, victim, 4);
            assertTrue(FinalDamageEvent.willFire(event));
            assertTrue(DamageContexts.isAnnounced(victim), "The predicate must not claim the request");

            server.getPluginManager().callEvent(event);
        }

        assertEquals(1, fired.size());
        assertSame(request, fired.get(0).getRequest());
        assertEquals(Map.of("fire", 4D), fired.get(0).getDamages());
    }

    // ------------------------------------------------------------------ //
    // Early exits
    // ------------------------------------------------------------------ //

    @Test
    void vanillaOnlyDamageHandling_willNotFire() {
        EngineCfg.VANILLA_ONLY_DAMAGE_HANDLING = true;
        try {
            assertFalse(askThenFire(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE)));
        } finally {
            EngineCfg.VANILLA_ONLY_DAMAGE_HANDLING = false;
        }
        assertEquals(List.of(), fired);
    }

    @Test
    void targetThatIsNotLiving_willNotFire() {
        Entity snowball = victim.getWorld().spawn(victim.getLocation(), Snowball.class);
        try {
            assertFalse(askThenFire(meleeEvent(attacker, snowball, VANILLA_SWORD_DAMAGE)));
        } finally {
            snowball.remove();
        }
        assertEquals(List.of(), fired);
    }

    @Test
    void rawRequest_willNotFire() {
        try (DamageScope ignored = DamageContexts.open(victim,
                DamageRequest.builder().source(attacker).weaponStats(WeaponStatsMode.RAW).build())) {
            EntityDamageByEntityEvent event = meleeEvent(attacker, victim, 7);
            assertFalse(FinalDamageEvent.willFire(event));
            assertTrue(DamageContexts.isAnnounced(victim), "The predicate must not claim the request");
            server.getPluginManager().callEvent(event);
            assertFalse(event.isCancelled());
            assertEquals(7D, event.getDamage(), DELTA, "Divinity leaves a RAW hit alone");
        }
        assertEquals(List.of(), fired);
    }

    @Test
    void fabledFakeDamage_willNotFire() throws Exception {
        EntityDamageByEntityEvent fake = meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE);
        FabledHook hook = new FakeAwareFabledHook(plugin, fake);
        installFabledHook(hook);
        Plugin fabled = MockBukkit.createMockPlugin("Fabled");
        try {
            assertFalse(askThenFire(fake));
            assertEquals(List.of(), fired);
            assertTrue(askThenFire(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE)),
                    "A real hit while Fabled is enabled still fires");
            assertEquals(1, fired.size());
        } finally {
            server.getPluginManager().disablePlugin(fabled);
            removeFabledHook(hook);
        }
    }

    @Test
    void disabledVanillaSweep_willNotFire() {
        boolean before = EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP;
        EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP = true;
        EntityDamageByEntityEvent sweep = new EntityDamageByEntityEvent(attacker, victim,
                DamageCause.ENTITY_SWEEP_ATTACK, modifiers(1), functions(modifiers(1)));
        try {
            assertFalse(askThenFire(sweep));
        } finally {
            EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP = before;
        }
        assertEquals(List.of(), fired);
        assertTrue(sweep.isCancelled());
    }

    // ------------------------------------------------------------------ //
    // True, but no event: each of these ends with the hit cancelled
    // ------------------------------------------------------------------ //

    @Test
    void dodgedHit_willFire_butFiresNothing_andIsCancelled() {
        liftCap(TypedStat.Type.DODGE_RATE);
        ItemStack chest = armor(Map.of());
        stat(chest, TypedStat.Type.DODGE_RATE, 100);
        victim.getInventory().setChestplate(chest);
        refresh(victim);
        EntityDamageByEntityEvent event = meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE);

        assertTrue(askThenFire(event));
        assertEquals(List.of(), fired);
        assertTrue(event.isCancelled());
    }

    @Test
    void attackOnSelf_willFire_butIsCancelled() {
        // Codex's canFights refuses an attack on oneself; the predicate does not repeat that check
        EntityDamageByEntityEvent event = meleeEvent(victim, victim, VANILLA_SWORD_DAMAGE);

        assertTrue(askThenFire(event));
        assertEquals(List.of(), fired);
        assertTrue(event.isCancelled());
    }

    private void installFabledHook(FabledHook hook) throws Exception {
        Field field = NHook.class.getDeclaredField("pluginName");
        field.setAccessible(true);
        field.set(hook, EHook.SKILL_API);
        plugin.getHooks().getHooks(plugin).add(hook);
    }

    /** A Fabled hook that treats one event as one of Fabled's throwaway events, as Fabled would. */
    private static class FakeAwareFabledHook extends FabledHook {
        private final EntityDamageByEntityEvent fake;

        FakeAwareFabledHook(Divinity plugin, EntityDamageByEntityEvent fake) {
            super(plugin);
            this.fake = fake;
        }

        @Override
        public boolean isFakeDamage(EntityDamageByEntityEvent event) {
            return event == fake;
        }

        /** Would call into Fabled, which is not loaded. */
        @Override
        public void updateSkills(Player player) {
        }
    }
}
