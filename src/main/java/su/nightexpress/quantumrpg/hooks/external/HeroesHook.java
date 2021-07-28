package su.nightexpress.quantumrpg.hooks.external;

import com.herocraftonline.heroes.Heroes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class HeroesHook extends Hook implements HookLevel, HookClass {
    private Heroes h;

    public HeroesHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        this.h = Heroes.getInstance();
    }

    public void shutdown() {
    }

    public int getLevel(Player p) {
        return this.h.getCharacterManager().getHero(p).getLevel();
    }

    public String getClass(Player p) {
        return ChatColor.stripColor(this.h.getCharacterManager().getHero(p).getHeroClass().getName());
    }
}
