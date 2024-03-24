package com.promcteam.divinity.hooks.external;

import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import com.promcteam.codex.utils.NumberUT;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.list.classes.ClassManager;
import com.promcteam.divinity.modules.list.classes.api.UserClassData;
import com.promcteam.divinity.modules.list.classes.object.ClassAttributeType;
import com.promcteam.divinity.stats.EntityStats;
import com.promcteam.divinity.stats.items.ItemStats;
import com.promcteam.divinity.stats.items.attributes.DamageAttribute;
import com.promcteam.divinity.stats.items.attributes.DefenseAttribute;
import com.promcteam.divinity.stats.items.attributes.api.SimpleStat;
import com.promcteam.divinity.stats.items.attributes.api.TypedStat;

public class PlaceholderAPIHK extends NHook<QuantumRPG> {

    private QRPGExpansion expansion;

    public PlaceholderAPIHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        (this.expansion = new QRPGExpansion()).register();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {
        PlaceholderAPI.unregisterExpansion(this.expansion);
//		this.expansion.unregister();
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
                    SimpleStat.Type type = TypedStat.Type.valueOf(tt.toUpperCase());
                    return String.valueOf(NumberUT.round(EntityStats.get(player).getItemStat(type, true)));
                } catch (IllegalArgumentException ex) {
                    return NULL;
                }
            }

            if (tmp.startsWith("damage_")) {
                String          tt = tmp.replace("damage_", "");
                DamageAttribute dt = ItemStats.getDamageById(tt);
                if (dt == null) {
                    return NULL;
                }
                return String.valueOf(NumberUT.round(EntityStats.get(player).getDamageByType(dt)));
            }

            if (tmp.startsWith("defense_")) {
                String           tt = tmp.replace("defense_", "");
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
                    } catch (IllegalArgumentException ex) {
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
