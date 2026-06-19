package studio.magemonkey.divinity.manager.listener.object;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Trident;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
