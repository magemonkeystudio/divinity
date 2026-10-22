package studio.magemonkey.divinity.hooks.external;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import studio.magemonkey.divinity.modules.list.itemgenerator.generators.AbilityGenerator;
import studio.magemonkey.divinity.testutil.MockedTest;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.player.PlayerData;
import studio.magemonkey.fabled.api.skills.Skill;
import studio.magemonkey.fabled.cast.PlayerTextCastingData;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regression tests for issue #298: entering Fabled's text casting mode moves the held slot to the cast
 * slot, which must not make Divinity strip the skills granted by the item the player was holding.
 */
class FabledHookTest extends MockedTest {
    private static final String SKILL_ID = "Fireball";

    private MockedStatic<Fabled>  fabled;
    private PlayerData            playerData;
    private PlayerTextCastingData castingData;
    private Skill                 skill;
    private PlayerMock            player;
    private FabledHook            hook;

    @BeforeEach
    void setup() {
        if (AbilityGenerator.ABILITY_KEY == null) {
            AbilityGenerator.ABILITY_KEY = NamespacedKey.fromString("skills", plugin);
        }

        // Not added to the server, so Divinity's periodic entity tasks leave it alone while ticking
        player = new PlayerMock(server, "Caster", UUID.randomUUID());
        playerData = mock(PlayerData.class);
        castingData = mock(PlayerTextCastingData.class);
        skill = mock(Skill.class);
        when(playerData.getTextCastingData()).thenReturn(castingData);
        // The player was previously granted the skill by the item they were holding
        when(playerData.getExternallyAddedSkills()).thenReturn(Set.of(
                new PlayerData.ExternallyAddedSkill(SKILL_ID, AbilityGenerator.ABILITY_KEY, 1)));

        fabled = mockStatic(Fabled.class);
        fabled.when(() -> Fabled.getData(player)).thenReturn(playerData);
        fabled.when(() -> Fabled.getSkill(SKILL_ID)).thenReturn(skill);

        hook = new FabledHook(plugin);
    }

    @AfterEach
    void tearDown() {
        fabled.close();
    }

    @Test
    void updateSkills_whileTextCasting_keepsItemSkills() {
        // Held slot now points at the (empty) cast slot, as it does in casting mode
        when(castingData.isCasting()).thenReturn(true);

        hook.updateSkills(player);
        server.getScheduler().performTicks(2);

        verify(playerData, never()).removeSkillExternally(any(), any());
    }

    @Test
    void updateSkills_notCasting_removesSkillsOfItemsNoLongerHeld() {
        when(castingData.isCasting()).thenReturn(false);

        hook.updateSkills(player);
        server.getScheduler().performTicks(2);

        verify(playerData).removeSkillExternally(skill, AbilityGenerator.ABILITY_KEY);
    }
}
