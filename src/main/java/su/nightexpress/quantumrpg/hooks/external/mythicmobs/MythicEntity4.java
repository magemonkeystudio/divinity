package su.nightexpress.quantumrpg.hooks.external.mythicmobs;

import io.lumine.mythic.bukkit.utils.lib.jooq.Require;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MythicEntity4 extends AbstractMythicEntity<MythicMob> {
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
