package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.shampaggon.crackshot.CSDirector;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;

public class CrackShotHK extends NHook<QuantumRPG> {

	private CSDirector main;
	
	public CrackShotHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		try {
			this.main = ((CSDirector)plugin.getPluginManager().getPlugin(this.getPlugin()));
			return HookState.SUCCESS;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return HookState.ERROR;
		}
	}

	@Override
	protected void shutdown() {
		
	}

	public double getWeaponMeleeDamage(@NotNull Player player) {
		String wpnName = this.main.returnParentNode(player);
		double wpnDmg = this.main.getInt(wpnName + ".Shooting.Projectile_Damage");
		
		return wpnDmg;
	}
}
