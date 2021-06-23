package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookMobLevel;

public class MythicMobsHK extends NHook<QuantumRPG> implements HookMobLevel {

	private MythicMobs mm;
	
	public MythicMobsHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		this.mm = MythicMobs.inst();
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		
	}
	
	public boolean isMythicMob(@NotNull Entity entity) {
		return mm.getAPIHelper().isMythicMob(entity);
	}
	
	@NotNull
	public String getMythicNameByEntity(@NotNull Entity entity) {
		return mm.getAPIHelper().getMythicMobInstance(entity).getType().getInternalName();
	}
	
	public MythicMob getMythicInstance(@NotNull Entity entity) {
		return mm.getAPIHelper().getMythicMobInstance(entity).getType();
	}
	
	@Override
	public double getMobLevel(@NotNull Entity entity) {
		ActiveMob activeMob = mm.getMobManager().getMythicMobInstance(entity);
		if (activeMob == null) return 1D;
		
		return Math.max(1, activeMob.getLevel());
	}
	
	public void setSkillDamage(@NotNull Entity entity, double amount) {
		if (!isMythicMob(entity)) return;
		ActiveMob activeMob = mm.getMobManager().getMythicMobInstance(entity);
		activeMob.setLastDamageSkillAmount(amount);
	}
}
