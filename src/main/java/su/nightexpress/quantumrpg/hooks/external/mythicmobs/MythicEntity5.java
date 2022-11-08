package su.nightexpress.quantumrpg.hooks.external.mythicmobs;

import io.lumine.mythic.api.mobs.MythicMob;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MythicEntity5 extends AbstractMythicEntity<MythicMob> {
    private final MythicMob mob;

    @Override
    public String getInternalName() {
        return mob.getInternalName();
    }

    @Override
    public String getFaction() {
        return mob.getFaction();
    }
}
