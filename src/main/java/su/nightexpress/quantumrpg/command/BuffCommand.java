package su.nightexpress.quantumrpg.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.manager.effects.buffs.SavedBuff;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

public class BuffCommand extends ISubCommand<QuantumRPG> {

	public BuffCommand(@NotNull QuantumRPG plugin) {
		super(plugin, new String[] {"buff"}, Perms.ADMIN);
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Buff_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Buff_Usage.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}
	
	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return PlayerUT.getPlayerNames();
		}
		if (i == 2) {
			return Arrays.asList("damage","defense","stat");
		}
		if (i == 3) {
			if (args[2].equalsIgnoreCase("damage")) {
				List<String> list = new ArrayList<>();
				for (DamageAttribute d : ItemStats.getDamages()) {
					list.add(d.getId());
				}
				return list;
			}
			if (args[2].equalsIgnoreCase("defense")) {
				List<String> list = new ArrayList<>();
				for (DefenseAttribute d : ItemStats.getDefenses()) {
					list.add(d.getId());
				}
				return list;
			}
			if (args[2].equalsIgnoreCase("stat")) {
				List<String> list = new ArrayList<>();
				for (AbstractStat<?> d : ItemStats.getStats()) {
					list.add(d.getId());
				}
				return list;
			}
		}
		if (i == 4) {
			return Arrays.asList("<amount>", "10", "25%");
		}
		if (i == 5) {
			return Arrays.asList("<duration>", "60", "300");
		}
		return super.getTab(player, i, args);
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 6) {
			this.printUsage(sender);
			return;
		}
		
		Player player = plugin.getServer().getPlayer(args[1]);
		if (player == null) {
			this.errPlayer(sender);
			return;
		}
		
		RPGUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) {
			this.errPlayer(sender);
			return;
		}
		
		Set<SavedBuff> userBuffs = null;
		
		String type = args[2];
		String statId = args[3];
		
		boolean isModifier = args[4].endsWith("%");
		double amount = this.getNumD(sender, args[4].replace("%", ""), 0);
		int seconds = this.getNumI(sender, args[5], 0);
		if (seconds == 0) return;
		
		ItemLoreStat<?> stat = null;
		if (type.equalsIgnoreCase("damage")) {
			stat = ItemStats.getDamageById(statId);
			userBuffs = user.getActiveProfile().getDamageBuffs();
		}
		else if (type.equalsIgnoreCase("defense")) {
			stat = ItemStats.getDefenseById(statId);
			userBuffs = user.getActiveProfile().getDefenseBuffs();
		}
		else if (type.equalsIgnoreCase("stat")) {
			AbstractStat.Type statType = AbstractStat.Type.getByName(statId);
			if (statType != null) {
				stat = ItemStats.getStat(statType);
				userBuffs = user.getActiveProfile().getItemStatBuffs();
			}
		}
		
		if (stat == null || userBuffs == null) {
			sender.sendMessage("Invalid stat id!");
			return;
		}
		
		SavedBuff buff = new SavedBuff(stat, amount, isModifier, seconds);
		userBuffs.add(buff);
		
		plugin.lang().Command_Buff_Done
		.replace("%time%", TimeUT.formatTime(seconds * 1000L))
		.replace("%stat%", stat.getName())
		.replace("%amount%", args[4])
		.replace("%player%", player.getName())
		.send(sender);
		
		plugin.lang().Command_Buff_Get
		.replace("%time%", TimeUT.formatTime(seconds * 1000L))
		.replace("%stat%", stat.getName())
		.replace("%amount%", args[4])
		.send(player);
	}
}
