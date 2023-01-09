package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.pwing.races.api.PwingRacesAPI;
import net.pwing.races.api.race.Race;
import net.pwing.races.api.race.RaceManager;
import net.pwing.races.api.race.RacePlayer;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class PwingRacesHK extends NHook<QuantumRPG> implements HookClass, HookLevel {
	
	public PwingRacesHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public HookState setup() {
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		
	}

	@Override
	@NotNull
	public String getClass(@NotNull Player player) {
        RaceManager raceManager = PwingRacesAPI.getRaceManager();
        RacePlayer racePlayer = raceManager.getRacePlayer(player);
        return racePlayer.getActiveRace().getName();
	}
	
	@Override
	public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
		throw new UnsupportedOperationException("Your class plugin does not provides mana function.");
	}

	@Override
	public int getLevel(@NotNull Player player) {
		 RaceManager raceManager = PwingRacesAPI.getRaceManager();
	     RacePlayer racePlayer = raceManager.getRacePlayer(player);
	     Race race = racePlayer.getActiveRace();
	     
		return racePlayer.getRaceData(race).getLevel();
	}
}
