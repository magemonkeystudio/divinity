package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;
import su.nightexpress.quantumrpg.modules.list.classes.object.ClassAttributeType;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

public class PlaceholderAPIHK extends NHook<QuantumRPG> {

	private QRPGExpansion expansion;
	
	public PlaceholderAPIHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}

	@Override @NotNull
	public HookState setup() {
		(this.expansion = new QRPGExpansion()).register();
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.expansion.unregister();
	}
    
    class QRPGExpansion extends PlaceholderExpansion {
        
    	private static final String NULL = "null";
    	
    	@Override
    	@NotNull
    	public String getAuthor() {
    		return plugin.getAuthor();
    	}
    	
    	@Override
    	@NotNull
    	public String getIdentifier() {
    		return "qrpg";
    	}

    	@Override
    	@NotNull
    	public String getVersion() {
    		return plugin.getDescription().getVersion();
    	}

    	@Override
    	public String onPlaceholderRequest(Player player, String tmp) {
    	   	if (player == null || tmp == null) return null;
    	   	
    	   	if (tmp.startsWith("itemstat_")) {
    	   		String tt = tmp.replace("itemstat_", "");
    	   		try {
    	   			AbstractStat.Type type = AbstractStat.Type.valueOf(tt.toUpperCase());
    	   			return String.valueOf(NumberUT.round(EntityStats.get(player).getItemStat(type, true)));
    	   		}	
    	   		catch (IllegalArgumentException ex) {
    	   			return NULL;
    	   		}
    	   	}
    	   	
    	   	if (tmp.startsWith("damage_")) {
    	   		String tt = tmp.replace("damage_", "");
    	   		DamageAttribute dt = ItemStats.getDamageById(tt);
    	   		if (dt == null) {
    	   			return NULL;
    	   		}
    	   		return String.valueOf(NumberUT.round(EntityStats.get(player).getDamageByType(dt)));
    	   	}
    	   	
    	   	if (tmp.startsWith("defense_")) {
    	   		String tt = tmp.replace("defense_", "");
    	   		DefenseAttribute dt = ItemStats.getDefenseById(tt);
    	   		if (dt == null) {
    	   			return NULL;
    	   		}
    	   		return String.valueOf(NumberUT.round(EntityStats.get(player).getDefenseByType(dt)));
    	   	}
    	   	
    	   	if (tmp.startsWith("class_")) {
    	   		ClassManager classManager = plugin.getModuleCache().getClassManager();
    	   		if (classManager == null) return NULL;
    	   		
    	   		UserClassData data = classManager.getUserData(player);
    	   		if (data == null) return "-";
    	   		
    	   		String tt = tmp.replace("class_", "");
    	   		if (tt.equalsIgnoreCase("name")) {
    	   			return data.getPlayerClass().getName();
    	   		}
    	   		if (tt.equalsIgnoreCase("id")) {
    	   			return data.getPlayerClass().getId();
    	   		}
    	   		if (tt.equalsIgnoreCase("skill_points")) {
    	   			return String.valueOf(data.getSkillPoints());
    	   		}
    	   		if (tt.equalsIgnoreCase("aspect_points")) {
    	   			return String.valueOf(data.getAspectPoints());
    	   		}
    	   		if (tt.equalsIgnoreCase("level")) {
    	   			return String.valueOf(data.getLevel());
    	   		}
    	   		if (tt.equalsIgnoreCase("level-max")) {
    	   			return String.valueOf(data.getPlayerClass().getMaxLevel());
    	   		}
    	   		if (tt.equalsIgnoreCase("exp")) {
    	   			return String.valueOf(data.getExp());
    	   		}
    	   		if (tt.equalsIgnoreCase("exp-to-up")) {
    	   			return String.valueOf(data.getExpToUp(false));
    	   		}
    	   		if (tt.equalsIgnoreCase("exp-max")) {
    	   			return String.valueOf(data.getExpToUp(true));
    	   		}
    	   		
    	   		if (tt.startsWith("attribute_")) {
    	   			String sName = tt.replace("attribute_", "");
    	   			try {
    	   				ClassAttributeType stat = ClassAttributeType.valueOf(sName.toUpperCase());
    	   				return NumberUT.format(data.getAttribute(stat));
    	   			}
    	   			catch (IllegalArgumentException ex) {
    	   				return NULL;
    	   			}
    	   		}
    	   		if (tt.startsWith("aspect_")) {
    	   			String sName = tt.replace("aspect_", "");
    	   			return String.valueOf(data.getAspect(sName));
    	   		}
    	   	}
    	   	
    	   	return null;
    	}
    }
}
