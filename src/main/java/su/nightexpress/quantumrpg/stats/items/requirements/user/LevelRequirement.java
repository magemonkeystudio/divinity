package su.nightexpress.quantumrpg.stats.items.requirements.user;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.ILangMsg;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;

public class LevelRequirement extends DynamicUserRequirement<int[]> {

	public LevelRequirement(
			@NotNull String name, 
			@NotNull String format
			) {
		super(
			"level", 
			name, 
			format, 
			ItemTags.PLACEHOLDER_REQ_USER_LEVEL,
			ItemTags.TAG_REQ_USER_LEVEL, 
			PersistentDataType.INTEGER_ARRAY);
	}
	
	@Override
	@NotNull
	public String getBypassPermission() {
		return Perms.BYPASS_REQ_USER_LEVEL;
	}

	@Override
	public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
		int[] arr = this.getRaw(item);
		if (arr == null) return true;
		
		int min = arr[0];
		int max = arr.length == 2 ? arr[1] : min;
		int userLevel = EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN.getLevel(player);
		
		return min == max ? (userLevel >= min) : (userLevel >= min && userLevel <= max);
	}

	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, int[] levels) {
		if (levels.length == 0 || (levels.length == 1 && levels[0] <= 0)) return "";
		if (levels.length == 2 && levels[0] <= 0 && levels[1] <= 0) return "";
		
		if (levels.length == 1 || (levels.length == 2 && levels[0] == levels[1])) {
    		return EngineCfg.LORE_STYLE_REQ_USER_LVL_FORMAT_SINGLE
    				.replace("%min%", String.valueOf(levels[0]));
    	}
    	
    	int min = Math.min(levels[0], levels[1]);
    	int max = Math.max(levels[0], levels[1]);
    	return EngineCfg.LORE_STYLE_REQ_USER_LVL_FORMAT_RANGE
    			.replace("%max%", String.valueOf(max))
    			.replace("%min%", String.valueOf(min));
	}

	@Override
	@NotNull
	public ILangMsg getDenyMessage(@NotNull Player p, @NotNull ItemStack src) {
		return plugin.lang().Module_Item_Interact_Error_Level;
	}
}
