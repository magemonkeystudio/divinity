package su.nightexpress.quantumrpg.modules;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.QListener;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.CommandRegister;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.types.OutputType;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class QModule extends QListener<QuantumRPG> {
    private final boolean enabled;
    private final MExecutor exec;
    private final SimpleDateFormat sdf;
    protected QuantumRPG plugin;
    protected MyConfig cfg;
    private OutputType out;

    public QModule(QuantumRPG plugin, boolean enabled, MExecutor exec) {
        super(plugin);
        this.plugin = plugin;
        this.enabled = enabled;
        this.exec = exec;
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public QuantumRPG pl() {
        return this.plugin;
    }

    public abstract EModule type();

    public abstract String name();

    public abstract String version();

    public abstract boolean isResolvable();

    public abstract void setup();

    public abstract void shutdown();

    public abstract void updateCfg();

    public boolean isDropable() {
        return this instanceof QModuleDrop;
    }

    public boolean isLevelable() {
        return this instanceof QModuleLevel;
    }

    public boolean isRateable() {
        return this instanceof QModuleRate;
    }

    public boolean isSocketable() {
        return this instanceof QModuleSocket;
    }

    public boolean isActive() {
        return this.enabled;
    }

    public String getId() {
        return name().toLowerCase().replace(" ", "_");
    }

    public String getPath() {
        return "/modules/" + getId();
    }

    public String getFullPath() {
        return this.plugin.getDataFolder() + "/modules/" + getId();
    }

    private void setConfig() {
        this.cfg = new MyConfig(this.plugin, "/modules/" + getId(), "settings.yml");
        this.out = OutputType.CHAT;
        try {
            String s = this.cfg.getConfig().getString("messages-output");
            if (s != null)
                this.out = OutputType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log("Invalid messages output type!", LogType.WARN);
        }
    }

    public MyConfig getCfg() {
        return this.cfg;
    }

    private void registerCommands() {
        this.exec.init(this);
        String cmd = this.cfg.getConfig().getString("command-aliases");
        if (cmd == null || cmd.isEmpty()) {
            log("No command aliases found!", LogType.WARN);
            this.exec.shutdown();
            return;
        }
        String[] cmds = cmd.split(",");
        CommandRegister.reg(this.plugin, this.exec, this.exec, cmds, "", "");
    }

    private void unregisterCommands() {
        String cmd = this.cfg.getConfig().getString("command-aliases");
        if (cmd == null || cmd.isEmpty())
            return;
        String[] cmds = cmd.split(",");
        CommandRegister.unreg(this.plugin, cmds);
        this.exec.shutdown();
    }

    public String cmd() {
        String s = this.cfg.getConfig().getString("command-aliases");
        if (s != null)
            return s.split(",")[0];
        return "null";
    }

    public void out(Entity p, String msg) {
        if (p == null)
            return;
        this.out.msg(p, msg);
    }

    public void log(String msg, LogType type) {
        Date now = new Date();
        String date = this.sdf.format(now);
        String out_f = ChatColor.stripColor("[" + date + "/" + type.name() + "] " + msg);
        String out_c = type.color() + "[QRPG/" + type.name() + "] " + name() + ": " + ChatColor.GRAY + msg;
        this.plugin.getServer().getConsoleSender().sendMessage(out_c);
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(this.plugin.getDataFolder() + getPath() + "/log.txt", true));
            output.append(out_f);
            output.newLine();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enable() {
        if (!this.enabled)
            return;
        setConfig();
        updateCfg();
        setup();
        registerCommands();
        registerListeners();
    }

    public void unload() {
        if (!this.enabled)
            return;
        shutdown();
        unregisterCommands();
        unregisterListeners();
        this.cfg = null;
    }

    public void reload() {
        unload();
        enable();
    }
}
