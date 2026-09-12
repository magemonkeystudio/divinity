package studio.magemonkey.divinity;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.testutil.MockedTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for plugin.yml's permission alias graph. Perms.PREFIX resolves to
 * divinity.* nodes, which the Java code checks directly with plain hasPermission() calls
 * (no runtime shim). Backward compatibility for servers still granting the legacy
 * quantumrpg.* nodes relies entirely on plugin.yml's `children` links making a granted
 * quantumrpg.* node imply the corresponding divinity.* node. These tests exercise that
 * alias graph through Bukkit's real permission resolution (via MockBukkit), rather than
 * just asserting against the YAML structure.
 */
class PermsAliasTest extends MockedTest {

    private PlayerMock grantedOnly(String permission) {
        PlayerMock player = genPlayer("perm-test-" + permission.replace('.', '_'), false);
        player.addAttachment(plugin).setPermission(permission, true);
        return player;
    }

    @Test
    void legacyDismantleGuiGrant_impliesDivinityDismantleGui() {
        PlayerMock player = grantedOnly("quantumrpg.dismantle.gui");
        assertTrue(player.hasPermission("divinity.dismantle.gui"));
    }

    @Test
    void legacySellGuiGrant_impliesDivinitySellGui() {
        PlayerMock player = grantedOnly("quantumrpg.sell.gui");
        assertTrue(player.hasPermission("divinity.sell.gui"));
    }

    @Test
    void legacyEssencesGuiUserGrant_impliesDivinityEssencesGuiUser() {
        PlayerMock player = grantedOnly("quantumrpg.essences.gui.user");
        assertTrue(player.hasPermission("divinity.essences.gui.user"));
    }

    @Test
    void legacyEssencesCmdMerchantOthersGrant_impliesDivinityEquivalent() {
        PlayerMock player = grantedOnly("quantumrpg.essences.cmd.merchant.others");
        assertTrue(player.hasPermission("divinity.essences.cmd.merchant.others"));
    }

    @Test
    void legacyBypassUntradeableGrant_impliesDivinityEquivalent() {
        PlayerMock player = grantedOnly("quantumrpg.bypass.requirement.untradeable");
        assertTrue(player.hasPermission("divinity.bypass.requirement.untradeable"));
    }

    @Test
    void legacyAdminGrant_cascadesToDivinityAdminAndDescendants() {
        PlayerMock player = grantedOnly("quantumrpg.admin");
        assertTrue(player.hasPermission("divinity.admin"));
        // divinity.admin's children should still cascade from a legacy grant.
        assertTrue(player.hasPermission("divinity.dismantle.gui"));
        assertTrue(player.hasPermission("divinity.bypass.requirement.untradeable"));
    }

    @Test
    void legacyUserGrant_impliesDivinityUser() {
        PlayerMock player = grantedOnly("quantumrpg.user");
        assertTrue(player.hasPermission("divinity.user"));
    }

    @Test
    void noGrant_doesNotImplyEitherNamespace() {
        PlayerMock player = genPlayer("perm-test-none", false);
        assertFalse(player.hasPermission("divinity.dismantle.gui"));
        assertFalse(player.hasPermission("quantumrpg.dismantle.gui"));
    }

    @Test
    void opPlayer_getsDivinityPermissionsByDefault() {
        PlayerMock op = genPlayer("perm-test-op", true);
        assertTrue(op.hasPermission("divinity.dismantle.gui"));
        assertTrue(op.hasPermission("divinity.admin"));
    }
}
