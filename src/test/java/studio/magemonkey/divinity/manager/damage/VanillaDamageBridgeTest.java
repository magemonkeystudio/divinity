package studio.magemonkey.divinity.manager.damage;

import com.google.common.base.Function;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins what the bridge reads and writes on a real {@link EntityDamageEvent}. The events are built
 * directly and never fired, so only the bridge touches them.
 */
@SuppressWarnings("deprecation")
public class VanillaDamageBridgeTest extends MockedTest {
    private PlayerMock victim;

    @BeforeEach
    public void setup() {
        victim = genPlayer("Goflish");
        victim.setAbsorptionAmount(0D);
    }

    private EntityDamageEvent event(Map<DamageModifier, Double> values) {
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        values.forEach((modifier, value) -> functions.put(modifier, d -> value));
        return new EntityDamageEvent(victim, DamageCause.ENTITY_ATTACK, new EnumMap<>(values), functions);
    }

    private static Map<DamageModifier, Double> modifiers(Object... pairs) {
        Map<DamageModifier, Double> map = new EnumMap<>(DamageModifier.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((DamageModifier) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return map;
    }

    @Test
    void disableVanillaArmorZeroesArmorOnly() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.ARMOR, -3,
                DamageModifier.BLOCKING, -2));
        new VanillaDamageBridge(e).disableVanillaArmor();
        assertEquals(0D, e.getDamage(DamageModifier.ARMOR), 0D);
        assertEquals(10D, e.getDamage(DamageModifier.BASE), 0D);
        assertEquals(-2D, e.getDamage(DamageModifier.BLOCKING), 0D);
    }

    @Test
    void disableVanillaArmorDoesNothingWithoutArmorModifier() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10));
        new VanillaDamageBridge(e).disableVanillaArmor();
        assertFalse(e.isApplicable(DamageModifier.ARMOR));
        assertEquals(10D, e.getDamage(), 0D);
    }

    @Test
    void getBlockingDamageReadsTheModifier() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.BLOCKING, -10));
        assertEquals(-10D, new VanillaDamageBridge(e).getBlockingDamage(), 0D);
    }

    @Test
    void overrideBlockingWritesOnlyBlocking() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.BLOCKING, -10));
        new VanillaDamageBridge(e).overrideBlocking(0D);
        assertEquals(0D, e.getDamage(DamageModifier.BLOCKING), 0D);
        assertEquals(10D, e.getDamage(DamageModifier.BASE), 0D);

        new VanillaDamageBridge(e).overrideBlocking(1D);
        assertEquals(1D, e.getDamage(DamageModifier.BLOCKING), 0D);
    }

    @Test
    void overrideBlockingDoesNothingWithoutBlockingModifier() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10));
        new VanillaDamageBridge(e).overrideBlocking(0D);
        assertFalse(e.isApplicable(DamageModifier.BLOCKING));
        assertEquals(10D, e.getDamage(), 0D);
    }

    @Test
    void getInvulnerabilityReductionIsZeroWhenNotApplicable() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10));
        assertEquals(0D, new VanillaDamageBridge(e).getInvulnerabilityReduction(), 0D);
    }

    @Test
    void getInvulnerabilityReductionReadsTheModifierWhenPresent() {
        DamageModifier reduction;
        try {
            reduction = DamageModifier.valueOf("INVULNERABILITY_REDUCTION");
        } catch (IllegalArgumentException ex) {
            return; // Not part of this API version; the bridge returns 0 there (covered above).
        }
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, reduction, -4));
        assertEquals(-4D, new VanillaDamageBridge(e).getInvulnerabilityReduction(), 0D);
    }

    @Test
    void setBaseDamageWritesBaseOnly() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.BLOCKING, -2));
        new VanillaDamageBridge(e).setBaseDamage(7.5D);
        assertEquals(7.5D, e.getDamage(DamageModifier.BASE), 0D);
        assertEquals(-2D, e.getDamage(DamageModifier.BLOCKING), 0D);
    }

    @Test
    void settleFinalDamageLeavesAnEventWhoseFinalDamageIsBase() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.ARMOR, 0));
        victim.setAbsorptionAmount(5D);
        new VanillaDamageBridge(e).settleFinalDamage(victim);
        assertEquals(10D, e.getDamage(), 0D);
        assertEquals(0D, e.getDamage(DamageModifier.ARMOR), 0D);
        assertEquals(10D, e.getFinalDamage(), 0D);
    }

    @Test
    void settleFinalDamageZeroesOtherModifiersAndKeepsResistance() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.ARMOR, -3,
                DamageModifier.BLOCKING, -2, DamageModifier.MAGIC, -1, DamageModifier.RESISTANCE, -2));
        new VanillaDamageBridge(e).settleFinalDamage(victim);
        assertEquals(10D, e.getDamage(DamageModifier.BASE), 0D);
        assertEquals(0D, e.getDamage(DamageModifier.ARMOR), 0D);
        assertEquals(0D, e.getDamage(DamageModifier.BLOCKING), 0D);
        assertEquals(0D, e.getDamage(DamageModifier.MAGIC), 0D);
        assertEquals(-2D, e.getDamage(DamageModifier.RESISTANCE), 0D);
    }

    @Test
    void settleFinalDamageAbsorbsUpToTheVictimsAbsorptionAfterResistance() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.RESISTANCE, -2,
                DamageModifier.ABSORPTION, -1));
        victim.setAbsorptionAmount(5D);
        new VanillaDamageBridge(e).settleFinalDamage(victim);
        assertEquals(-5D, e.getDamage(DamageModifier.ABSORPTION), 0D);
        assertEquals(-2D, e.getDamage(DamageModifier.RESISTANCE), 0D);
        assertEquals(10D, e.getDamage(DamageModifier.BASE), 0D);
    }

    @Test
    void settleFinalDamageCapsAbsorptionAtDamageAfterResistance() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 10, DamageModifier.RESISTANCE, -2,
                DamageModifier.ABSORPTION, -1));
        victim.setAbsorptionAmount(20D);
        new VanillaDamageBridge(e).settleFinalDamage(victim);
        assertEquals(-8D, e.getDamage(DamageModifier.ABSORPTION), 0D);
    }

    @Test
    void settleFinalDamageAbsorbsNothingForZeroDamage() {
        EntityDamageEvent e = event(modifiers(DamageModifier.BASE, 0, DamageModifier.ARMOR, -3,
                DamageModifier.ABSORPTION, -1));
        victim.setAbsorptionAmount(20D);
        new VanillaDamageBridge(e).settleFinalDamage(victim);
        assertEquals(0D, e.getDamage(DamageModifier.ABSORPTION), 0D);
        assertEquals(0D, e.getDamage(DamageModifier.ARMOR), 0D);
    }
}
