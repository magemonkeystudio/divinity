package su.nightexpress.quantumrpg.hooks.external;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.ActiveMob;
import mc.promcteam.engine.hooks.HookState;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.Optional;

public class MythicMobsHKv5 extends AbstractMythicMobsHK {

    private MythicPlugin mm;

    public MythicMobsHKv5(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        this.mm = MythicProvider.get();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isMythicMob(@NotNull Entity entity) {
        return mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(entity.getUniqueId())).findFirst().isPresent();
    }

    @Override
    @NotNull
    public String getMythicNameByEntity(@NotNull Entity entity) {
        MythicMob mob = getMythicInstance(entity);
        return mob == null ? null : mob.getInternalName();
    }

    public MythicMob getMythicInstance(@NotNull Entity entity) {
        Optional<ActiveMob> mob = mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(entity.getUniqueId())).findFirst();

        return mob.isPresent() ? mob.get().getType() : null;
    }

    @Override
    public double getMobLevel(@NotNull Entity entity) {
        ActiveMob mob = getActiveMythicInstance(entity);

        return mob != null ? Math.max(1, mob.getLevel()) : 1;
    }

    @Override
    public void setSkillDamage(@NotNull Entity entity, double amount) {
        if (!isMythicMob(entity)) return;
        ActiveMob am1 = getActiveMythicInstance(entity);
        am1.setLastDamageSkillAmount(amount);
    }

    public ActiveMob getActiveMythicInstance(@NotNull Entity e) {
        Optional<ActiveMob> mob = mm.getMobManager().getActiveMobs().stream()
                .filter(a -> a.getUniqueId().equals(e.getUniqueId())).findFirst();

        return mob.isPresent() ? mob.get() : null;
    }
}
