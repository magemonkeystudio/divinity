package su.nightexpress.quantumrpg.utils.msg;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MsgUT {
    private static QuantumRPG plugin = QuantumRPG.instance;

    private static Map<Player, List<DelayedMsg>> msg = new WeakHashMap<>();

    public static void sendActionBar(Entity player, String msg) {
        if (player.getType() == EntityType.PLAYER)
            plugin.getNMS().sendActionBar((Player) player, msg);
    }

    public static void sendTitles(Entity player, String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        if (player.getType() == EntityType.PLAYER)
            plugin.getNMS().sendTitles((Player) player, titleText, subtitleText, fadeIn, stay, fadeOut);
    }

    public static void sendDelayed(Player p, String m, int cd) {
        List<DelayedMsg> list;
        if (msg.containsKey(p)) {
            list = msg.get(p);
            for (DelayedMsg delayedMsg : list) {
                if (delayedMsg.isExpired()) {
                    list.remove(delayedMsg);
                    continue;
                }
                if (delayedMsg.getMsg().equalsIgnoreCase(m))
                    return;
            }
        } else {
            list = new ArrayList<>();
        }
        DelayedMsg dm = new DelayedMsg(m, cd);
        list.add(dm);
        msg.put(p, list);
        p.sendMessage(m);
    }
}
