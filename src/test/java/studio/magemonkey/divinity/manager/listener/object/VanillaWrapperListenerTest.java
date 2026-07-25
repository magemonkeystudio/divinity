package studio.magemonkey.divinity.manager.listener.object;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent.Start;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.ProjectileStats;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
public class VanillaWrapperListenerTest extends MockedTest {
    private PlayerMock damager;
    private PlayerMock target;

    @BeforeEach
    public void setup() {
        damager = genPlayer("Travja");
        target = genPlayer("Goflish");
    }

    @Test
    void swordDoesAppropriateDamage() {
        final double expectedDamage = 5;
        damager.getInventory().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        EntityStats.purge(damager);
        target.simulateDamage(5, damager);
        assertEventFired(DivinityDamageEvent.Start.class, event -> {
            assertEquals(target, event.getVictim(), "Event victim is not the target");
            assertEquals(damager, event.getDamager(), "Event damager is not the damager");
            assertEquals(expectedDamage, getTotalDamage(event), 0.001);

            return true;
        });

        assertEquals(20 - expectedDamage, target.getHealth(), 0.001);
    }

    @Test
    void tridentUsesSavedVanillaWeaponDamageAfterSwap() {
        ItemStack tridentItem = new ItemStack(Material.TRIDENT);
        double    expectedDamage = DamageAttribute.getVanillaDamage(tridentItem);
        Trident   trident = target.getWorld().spawn(target.getLocation(), Trident.class);

        damager.getInventory().setItemInMainHand(tridentItem);
        EntityStats.purge(damager);

        trident.setShooter(damager);
        trident.setItem(tridentItem);
        ProjectileStats.setPower(trident, 1D);

        damager.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        EntityStats.purge(damager);

        target.simulateDamage(1, trident);
        assertEventFired(Start.class, event -> {
            assertEquals(target, event.getVictim(), "Event victim is not the target");
            assertEquals(damager, event.getDamager(), "Event damager is not the damager");
            assertEquals(Material.TRIDENT, event.getWeapon().getType(), "Projectile weapon should be the trident");
            assertEquals(expectedDamage, getTotalDamage(event), 0.001);

            return true;
        });
    }

    @Test
    void tridentUsesSavedCustomWeaponDamageAfterSwap() {
        final double expectedDamage = 13D;
        ItemStack customTrident = new ItemStack(Material.TRIDENT);
        setLegacyDamage(customTrident, ItemStats.getDamageByDefault(), expectedDamage);

        damager.getInventory().setItemInMainHand(customTrident);
        EntityStats.purge(damager);

        Trident trident = target.getWorld().spawn(target.getLocation(), Trident.class);
        trident.setShooter(damager);
        trident.setItem(customTrident);
        ProjectileStats.setPower(trident, 1D);

        damager.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        EntityStats.purge(damager);

        target.simulateDamage(1, trident);
        assertEventFired(Start.class, event -> {
            assertEquals(target, event.getVictim(), "Event victim is not the target");
            assertEquals(damager, event.getDamager(), "Event damager is not the damager");
            assertEquals(Material.TRIDENT, event.getWeapon().getType(), "Projectile weapon should be the trident");
            assertEquals(expectedDamage, getTotalDamage(event), 0.001);
            assertTrue(getTotalDamage(event) > 1D, "Projectile damage should come from the saved trident, not raw event damage");

            return true;
        });
    }

    @Test
    void resistancePreservedInsteadOfBeingZeroedOut() {
        final double base       = 10D;
        final double resistance = -2D;

        EntityDamageByEntityEvent event = buildDamageEvent(base, resistance, null);

        assertEquals(resistance,
                event.getDamage(DamageModifier.RESISTANCE),
                0.001,
                "Resistance modifier should be preserved rather than zeroed out");
        assertEquals(base + resistance,
                event.getFinalDamage(),
                0.001,
                "Final damage should reflect the resistance reduction");
    }

    @Test
    void absorptionCappedToVictimsAvailableAmountWhenHitIsLarger() {
        target.setAbsorptionAmount(4D); // 2 golden hearts
        final double base = 10D;

        EntityDamageByEntityEvent event = buildDamageEvent(base, null, -10D);

        assertEquals(base,
                event.getDamage(DamageModifier.BASE),
                0.001,
                "Base damage should be left untouched; absorption should not also be subtracted from it");
        assertEquals(-4D,
                event.getDamage(DamageModifier.ABSORPTION),
                0.001,
                "Absorption modifier should be capped to the victim's available absorption amount");
        assertEquals(6D, event.getFinalDamage(), 0.001, "Absorption should only reduce final damage once");
    }

    @Test
    void absorptionNotConsumedBeyondTheIncomingDamage() {
        target.setAbsorptionAmount(20D); // 10 golden hearts
        final double base = 2D;

        EntityDamageByEntityEvent event = buildDamageEvent(base, null, -20D);

        assertEquals(-2D,
                event.getDamage(DamageModifier.ABSORPTION),
                0.001,
                "A small hit should only consume absorption equal to its own damage, not drain the whole pool");
        assertEquals(0D, event.getFinalDamage(), 0.001);
    }

    @Test
    void resistanceIsFactoredInBeforeAbsorptionIsCalculated() {
        target.setAbsorptionAmount(4D); // 2 golden hearts
        final double base       = 10D;
        final double resistance = -4D;

        EntityDamageByEntityEvent event = buildDamageEvent(base, resistance, -10D);

        assertEquals(resistance, event.getDamage(DamageModifier.RESISTANCE), 0.001);
        assertEquals(-4D,
                event.getDamage(DamageModifier.ABSORPTION),
                0.001,
                "Absorption should be computed off the damage remaining after resistance, then capped");
        assertEquals(base + resistance - 4D, event.getFinalDamage(), 0.001);
    }

    /**
     * Fires an {@link EntityDamageByEntityEvent} carrying only the given modifiers (mirroring how the
     * vanilla server would populate them before Divinity's listener runs), so the resistance/absorption
     * fix in {@code VanillaWrapperListener#onVanillaDamage} can be exercised in isolation.
     */
    private EntityDamageByEntityEvent buildDamageEvent(double base, Double resistance, Double absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, base);
        functions.put(DamageModifier.BASE, d -> base);

        if (resistance != null) {
            modifiers.put(DamageModifier.RESISTANCE, resistance);
            functions.put(DamageModifier.RESISTANCE, d -> resistance);
        }
        if (absorption != null) {
            modifiers.put(DamageModifier.ABSORPTION, absorption);
            functions.put(DamageModifier.ABSORPTION, d -> absorption);
        }

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager,
                target,
                DamageCause.ENTITY_ATTACK,
                modifiers,
                functions);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private double getTotalDamage(Start event) {
        return event.getDamageMap().values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private void setLegacyDamage(ItemStack item, DamageAttribute attribute, double value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(NamespacedKey.fromString("prorpgitems:item_damage_" + attribute.getId()),
                PersistentDataType.DOUBLE,
                value);
        item.setItemMeta(meta);
    }
}
