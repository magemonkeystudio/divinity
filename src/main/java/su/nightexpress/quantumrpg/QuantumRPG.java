package su.nightexpress.quantumrpg;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.cmds.MainCommand;
import su.nightexpress.quantumrpg.config.ConfigManager;
import su.nightexpress.quantumrpg.hooks.HookManager;
import su.nightexpress.quantumrpg.libs.glowapi.GlowPlugin;
import su.nightexpress.quantumrpg.libs.packetlistener.PacketListenerPlugin;
import su.nightexpress.quantumrpg.listeners.DamageListener;
import su.nightexpress.quantumrpg.listeners.GlobalListener;
import su.nightexpress.quantumrpg.modules.ModuleManager;
import su.nightexpress.quantumrpg.nms.NMS;
import su.nightexpress.quantumrpg.nms.VersionUtils;
import su.nightexpress.quantumrpg.tasks.TaskManager;
import su.nightexpress.quantumrpg.utils.Spells;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class QuantumRPG extends JavaPlugin {
  public static QuantumRPG instance;
  
  public DivineItemsAPI diapi;
  
  private MainCommand cmds;
  
  private ConfigManager cm;
  
  private ModuleManager mm;
  
  private HookManager hm;
  
  private PluginManager pm;
  
  private TaskManager tasks;
  
  private VersionUtils vu;
  
  private PacketListenerPlugin plp;
  
  private GlowPlugin gp;
  
  public void onEnable() {
    instance = this;
    this.pm = getServer().getPluginManager();
    sendStatus();
    this.vu = new VersionUtils();
    if (!this.vu.setup()) {
      this.pm.disablePlugin((Plugin)this);
      return;
    } 
    this.plp = new PacketListenerPlugin(this);
    this.plp.setup();
    this.gp = new GlowPlugin(this);
    this.gp.setup();
    this.cmds = new MainCommand(this);
    getCommand("quantumrpg").setExecutor((CommandExecutor)this.cmds);
    this.tasks = new TaskManager(this);
    this.cm = new ConfigManager(this);
    this.mm = new ModuleManager(this);
    this.hm = new HookManager(this);
    loadManagers();
  }
  
  private void loadManagers() {
    this.hm.setup();
    this.cm.setup();
    this.mm.initialize();
    this.pm.registerEvents((Listener)new DamageListener(this), (Plugin)this);
    this.pm.registerEvents((Listener)new GlobalListener(), (Plugin)this);
    this.tasks.start();
    Spells.startPjEfTask();
  }
  
  private void unloadManagers() {
    this.tasks.stop();
    getServer().getScheduler().cancelTasks((Plugin)this);
    HandlerList.unregisterAll((Plugin)this);
    this.hm.disable();
    this.mm.shutdown();
  }
  
  public void onDisable() {
    unloadManagers();
    this.gp.disable();
    instance = null;
  }
  
  public void reload() {
    unloadManagers();
    loadManagers();
  }
  
  public NMS getNMS() {
    return this.vu.getNMS();
  }
  
  private void sendStatus() {
    QuantumRPG quantumRPG = this;
    getServer().getConsoleSender().sendMessage("§2---------[ §aPlugin Initializing §2]---------");
    getServer().getConsoleSender().sendMessage("§7> §fPlugin name: §a" + quantumRPG.getName());
    getServer().getConsoleSender().sendMessage("§7> §fAuthor: §a" + (String)quantumRPG.getDescription().getAuthors().get(0));
    getServer().getConsoleSender().sendMessage("§7> §fVersion: §a" + quantumRPG.getDescription().getVersion());
  }
  
  public void debug(String s) {
    LogUtil.send(s, LogType.DEBUG);
  }
  
  public static QuantumRPG getInstance() {
    return instance;
  }
  
  public DivineItemsAPI getAPI() {
    return this.diapi;
  }
  
  public MainCommand getCommander() {
    return this.cmds;
  }
  
  public ConfigManager getCM() {
    return this.cm;
  }
  
  public ModuleManager getMM() {
    return this.mm;
  }
  
  public HookManager getHM() {
    return this.hm;
  }
  
  public PluginManager getPluginManager() {
    return this.pm;
  }
  
  public GlowPlugin getGlowLib() {
    return this.gp;
  }
  
  public TaskManager getTM() {
    return this.tasks;
  }
  
  public VersionUtils getVU() {
    return this.vu;
  }
  
  public <T extends su.nightexpress.quantumrpg.modules.QModule> T getModule(Class<T> clazz) {
    return (T)this.mm.getModule(clazz);
  }
}
