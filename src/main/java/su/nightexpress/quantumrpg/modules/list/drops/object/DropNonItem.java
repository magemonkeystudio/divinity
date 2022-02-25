package su.nightexpress.quantumrpg.modules.list.drops.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class DropNonItem {

    private ConfigurationSection config;

    public DropNonItem(ConfigurationSection config){
        this.config = config;
    }

    public abstract void execute(Player target);
}
