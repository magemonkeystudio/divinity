package su.nightexpress.quantumrpg.manager.listener.object;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import su.nightexpress.quantumrpg.api.event.RPGDamageEvent;
import su.nightexpress.quantumrpg.testutil.MockedTest;

import static org.junit.Assert.assertEquals;

@Disabled("Waiting for MockBukkit PRs")
public class VanillaWrapperListenerTest extends MockedTest {
    private Player damager;
    private Player target;

    @BeforeEach
    public void setup() {
        damager = genPlayer("Travja");
        target = genPlayer("Goflish");
    }

    @Test
    void swordDoesAppropriateDamage() {
        final double expectedDamage = 5;
        damager.getInventory().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        ((PlayerMock) target).simulateDamage(5, damager);
        server.getPluginManager().assertEventFired(RPGDamageEvent.Start.class, event -> {
            double damage = 0;
            for (Double value : event.getDamageMap().values()) {
                damage += value;
            }

            assertEquals("Event victim is not the target", target, event.getVictim());
            assertEquals("Event damager is not the damager", damager, event.getDamager());
            assertEquals(expectedDamage, damage, 0.001);

            return true;
        });

        assertEquals(20 - expectedDamage, target.getHealth(), 0.001);
    }

    @Test
    void tridentDoesAppropriateDamage() {
        final double expectedDamage = 9;
        Trident      trident        = target.getWorld().spawn(target.getLocation(), Trident.class);
        trident.setShooter(damager);
        ((PlayerMock) target).simulateDamage(9, trident);
        server.getPluginManager().assertEventFired(RPGDamageEvent.Start.class, event -> {
            double damage = 0;
            for (Double value : event.getDamageMap().values()) {
                damage += value;
            }

            assertEquals("Event victim is not the target", target, event.getVictim());
            assertEquals("Event damager is not the damager", damager, event.getDamager());
            assertEquals(expectedDamage, damage, 0.001);

            return true;
        });

        assertEquals(20 - expectedDamage, target.getHealth(), 0.001);
    }
}