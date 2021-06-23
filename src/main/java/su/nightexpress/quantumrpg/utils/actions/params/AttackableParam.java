package su.nightexpress.quantumrpg.utils.actions.params;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.utils.actions.params.IParamValue;

public class AttackableParam extends mc.promcteam.engine.utils.actions.params.list.AttackableParam {

	public AttackableParam() {
		super();
	}

	@Override
	public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
		boolean b = val.getBoolean();
		for (Entity e : new HashSet<>(targets)) {
			boolean attackable = Hooks.canFights(exe, e);
			if (attackable != b) {
				targets.remove(e);
			}
		}
	}
}
