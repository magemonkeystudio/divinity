/*
package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.zthana.racesofthana.RacesOfThana;
import com.zthana.racesofthana.objects.Race;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookClass;

public class RacesOfThanaHK extends NHook<QuantumRPG> implements HookClass {

	private RacesOfThana races;
	
	public RacesOfThanaHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override @NotNull
	public HookState setup() {
		this.races = (RacesOfThana) plugin.getPluginManager().getPlugin(this.getPlugin());
		return this.races != null ? HookState.SUCCESS : HookState.ERROR;
	}
	
	@Override
	public void shutdown() {
		
	}
	
	@Override @NotNull
	public String getClass(@NotNull Player player) {
		Race race = races.getRaceHandler().getRace(player);
		return race == null ? "N/A" : StringUT.colorOff(race.getName());
	}
	
	@Override
	public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
		throw new UnsupportedOperationException("Your class plugin does not provides mana function.");
	}
}
*/
