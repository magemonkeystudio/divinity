package su.nightexpress.quantumrpg.types;

import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.utils.msg.MsgUT;

public enum OutputType {
    CHAT, ACTION_BAR, TITLES, NONE;

    public void msg(Entity p, String msg) {
        String[] spl;
        String up;
        String down;
        switch (this) {
            case CHAT:
                msg = msg.replace("/n", "");
                p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + msg);
                break;
            case TITLES:
                spl = msg.split("/n");
                up = spl[0];
                down = "";
                if (spl.length == 2)
                    down = spl[1];
                MsgUT.sendTitles(p, up, down, 10, 40, 10);
                break;
            case NONE:
                msg = msg.replace("/n", "");
                MsgUT.sendActionBar(p, msg);
                break;
        }
    }
}
