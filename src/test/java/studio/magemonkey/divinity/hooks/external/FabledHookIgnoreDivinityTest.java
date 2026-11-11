package studio.magemonkey.divinity.hooks.external;

import com.google.common.base.Function;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.testutil.MockedTest;
import studio.magemonkey.fabled.api.DefaultCombatProtection;
import studio.magemonkey.fabled.api.event.SkillDamageEvent;
import studio.magemonkey.fabled.api.skills.Skill;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * A Fabled skill with "ignore-divinity" marks its caster so that Divinity leaves the next hit alone. The
 * mark is for that one hit only. These tests cover the ways it used to outlive the hit and make the
 * caster's next ordinary attack skip Divinity as well (issue #367).
 */
@SuppressWarnings("deprecation")
class FabledHookIgnoreDivinityTest extends MockedTest {
    private PlayerMock caster;
    private PlayerMock target;
    private FabledHook hook;

    @BeforeEach
    void setup() {
        caster = genPlayer("Caster");
        target = genPlayer("Target");
        hook = new FabledHook(plugin);
        hook.registerListeners();
    }

    @AfterEach
    void tearDown() {
        hook.unregisterListeners();
        DefaultCombatProtection.fakeDamageEvents.clear();
    }

    @Test
    void skillHit_marksTheCasterUntilTheHitLands() {
        server.getPluginManager().callEvent(skillDamage(true));
        assertTrue(hook.ignoreDivinity(caster), "The caster should be marked once the skill damage is announced");

        server.getPluginManager().callEvent(hit());
        assertFalse(hook.ignoreDivinity(caster), "The mark should be gone once the hit has been handled");
    }

    @Test
    void cancelledHit_stillClearsTheMark() {
        server.getPluginManager().callEvent(skillDamage(true));
        EntityDamageByEntityEvent hit = hit();
        hit.setCancelled(true);

        server.getPluginManager().callEvent(hit);

        assertFalse(hook.ignoreDivinity(caster));
    }

    @Test
    void cancelledSkillDamage_doesNotMarkTheCaster() {
        SkillDamageEvent event = skillDamage(true);
        event.setCancelled(true);

        server.getPluginManager().callEvent(event);

        assertFalse(hook.ignoreDivinity(caster), "No hit follows cancelled skill damage, so nothing would clear it");
    }

    @Test
    void repeatedSkillDamage_isClearedByOneHit() {
        server.getPluginManager().callEvent(skillDamage(true));
        server.getPluginManager().callEvent(skillDamage(true));

        server.getPluginManager().callEvent(hit());

        assertFalse(hook.ignoreDivinity(caster));
    }

    @Test
    void fabledsAttackPermissionCheck_doesNotClearTheMark() {
        server.getPluginManager().callEvent(skillDamage(true));
        EntityDamageByEntityEvent check = hit();
        DefaultCombatProtection.fakeDamageEvents.add(check);
        // Fabled always cancels these itself so that they deal no damage
        check.setCancelled(true);

        server.getPluginManager().callEvent(check);

        assertTrue(hook.ignoreDivinity(caster), "The real hit has not happened yet");
    }

    @Test
    void skillWithoutIgnoreDivinity_doesNotMarkTheCaster() {
        server.getPluginManager().callEvent(skillDamage(false));

        assertFalse(hook.ignoreDivinity(caster));
    }

    private SkillDamageEvent skillDamage(boolean ignoreDivinity) {
        return new SkillDamageEvent(mock(Skill.class), caster, target, 5, "default", true, ignoreDivinity, false);
    }

    private EntityDamageByEntityEvent hit() {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, 5D);
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        functions.put(DamageModifier.BASE, d -> 5D);
        return new EntityDamageByEntityEvent(caster, target, DamageCause.ENTITY_ATTACK, modifiers, functions);
    }
}
