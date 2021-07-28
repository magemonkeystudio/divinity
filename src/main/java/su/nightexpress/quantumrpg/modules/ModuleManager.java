package su.nightexpress.quantumrpg.modules;

import java.util.Collection;
import java.util.LinkedHashMap;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.active_items.ActiveItemManager;
import su.nightexpress.quantumrpg.modules.active_items.cmds.AICommand;
import su.nightexpress.quantumrpg.modules.arrows.ArrowManager;
import su.nightexpress.quantumrpg.modules.arrows.cmds.ArrowsCommand;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.modules.buffs.cmds.BuffsCommand;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.combatlog.CombatLogManager;
import su.nightexpress.quantumrpg.modules.combatlog.cmds.CombatLogCommand;
import su.nightexpress.quantumrpg.modules.consumes.ConsumeManager;
import su.nightexpress.quantumrpg.modules.consumes.cmds.ConsumesCommand;
import su.nightexpress.quantumrpg.modules.customitems.CustomItemsManager;
import su.nightexpress.quantumrpg.modules.customitems.cmds.CICommand;
import su.nightexpress.quantumrpg.modules.drops.DropManager;
import su.nightexpress.quantumrpg.modules.drops.cmds.DropsCommand;
import su.nightexpress.quantumrpg.modules.drops.drops2.DropManagerV2;
import su.nightexpress.quantumrpg.modules.essences.EssenceManager;
import su.nightexpress.quantumrpg.modules.essences.cmds.EssencesCommand;
import su.nightexpress.quantumrpg.modules.extractor.ExtractorManager;
import su.nightexpress.quantumrpg.modules.extractor.cmds.ExtractorCommand;
import su.nightexpress.quantumrpg.modules.gems.GemManager;
import su.nightexpress.quantumrpg.modules.gems.cmds.GemsCommand;
import su.nightexpress.quantumrpg.modules.identify.IdentifyManager;
import su.nightexpress.quantumrpg.modules.identify.cmds.IdentifyCommand;
import su.nightexpress.quantumrpg.modules.itemhints.ItemHintsManager;
import su.nightexpress.quantumrpg.modules.itemhints.cmds.ItemHintsCommand;
import su.nightexpress.quantumrpg.modules.magicdust.MagicDustManager;
import su.nightexpress.quantumrpg.modules.magicdust.cmds.MagicDustCommand;
import su.nightexpress.quantumrpg.modules.notifications.NotificationsManager;
import su.nightexpress.quantumrpg.modules.notifications.cmds.NotifyCommand;
import su.nightexpress.quantumrpg.modules.party.PartyManager;
import su.nightexpress.quantumrpg.modules.party.cmds.PartyCommand;
import su.nightexpress.quantumrpg.modules.refine.RefineManager;
import su.nightexpress.quantumrpg.modules.refine.cmds.RefineCommands;
import su.nightexpress.quantumrpg.modules.repair.RepairManager;
import su.nightexpress.quantumrpg.modules.repair.cmds.RepairCommand;
import su.nightexpress.quantumrpg.modules.resolve.ResolveManager;
import su.nightexpress.quantumrpg.modules.resolve.cmds.ResolveCommand;
import su.nightexpress.quantumrpg.modules.runes.RuneManager;
import su.nightexpress.quantumrpg.modules.runes.cmds.RunesCommand;
import su.nightexpress.quantumrpg.modules.sell.SellManager;
import su.nightexpress.quantumrpg.modules.sell.cmds.SellCommand;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.modules.sets.cmds.SetsCommand;
import su.nightexpress.quantumrpg.modules.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.modules.soulbound.cmds.SoulboundCommand;
import su.nightexpress.quantumrpg.modules.tiers.TierManager;
import su.nightexpress.quantumrpg.modules.tiers.cmds.TiersCommand;
import su.nightexpress.quantumrpg.utils.Utils;

public class ModuleManager {
  private QuantumRPG plugin;
  
  private LinkedHashMap<EModule, QModule> modules;
  
  public ModuleManager(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void initialize() {
    this.modules = new LinkedHashMap<>();
    register((QModule)new GemManager(this.plugin, EModule.GEMS.isEnabled(), (MExecutor)new GemsCommand(this.plugin)));
    register((QModule)new EssenceManager(this.plugin, EModule.ESSENCES.isEnabled(), (MExecutor)new EssencesCommand(this.plugin)));
    register((QModule)new RuneManager(this.plugin, EModule.RUNES.isEnabled(), (MExecutor)new RunesCommand(this.plugin)));
    register((QModule)new ConsumeManager(this.plugin, EModule.CONSUMABLES.isEnabled(), (MExecutor)new ConsumesCommand(this.plugin)));
    register((QModule)new MagicDustManager(this.plugin, EModule.MAGIC_DUST.isEnabled(), (MExecutor)new MagicDustCommand(this.plugin)));
    register((QModule)new ArrowManager(this.plugin, EModule.ARROWS.isEnabled(), (MExecutor)new ArrowsCommand(this.plugin)));
    register((QModule)new SetManager(this.plugin, EModule.SETS.isEnabled(), (MExecutor)new SetsCommand(this.plugin)));
    register((QModule)new ActiveItemManager(this.plugin, EModule.ACTIVE_ITEMS.isEnabled(), (MExecutor)new AICommand(this.plugin)));
    register((QModule)new CustomItemsManager(this.plugin, EModule.CUSTOM_ITEMS.isEnabled(), (MExecutor)new CICommand(this.plugin)));
    register((QModule)new TierManager(this.plugin, EModule.TIERS.isEnabled(), (MExecutor)new TiersCommand(this.plugin)));
    register((QModule)new RefineManager(this.plugin, EModule.REFINE.isEnabled(), (MExecutor)new RefineCommands(this.plugin)));
    register((QModule)new IdentifyManager(this.plugin, EModule.IDENTIFY.isEnabled(), (MExecutor)new IdentifyCommand(this.plugin)));
    register((QModule)new SoulboundManager(this.plugin, EModule.SOULBOUND.isEnabled(), (MExecutor)new SoulboundCommand(this.plugin)));
    register((QModule)new RepairManager(this.plugin, EModule.REPAIR.isEnabled(), (MExecutor)new RepairCommand(this.plugin)));
    register((QModule)new ResolveManager(this.plugin, EModule.RESOLVE.isEnabled(), (MExecutor)new ResolveCommand(this.plugin)));
    register((QModule)new ExtractorManager(this.plugin, EModule.EXTRACTOR.isEnabled(), (MExecutor)new ExtractorCommand(this.plugin)));
    register((QModule)new PartyManager(this.plugin, EModule.PARTY.isEnabled(), (MExecutor)new PartyCommand(this.plugin)));
    register((QModule)new ItemHintsManager(this.plugin, EModule.ITEM_HINTS.isEnabled(), (MExecutor)new ItemHintsCommand(this.plugin)));
    if (Config.g_useDropsV2) {
      register((QModule)new DropManagerV2(this.plugin, EModule.DROPS.isEnabled(), (MExecutor)new DropsCommand(this.plugin)));
    } else {
      register((QModule)new DropManager(this.plugin, EModule.DROPS.isEnabled(), (MExecutor)new DropsCommand(this.plugin)));
    } 
    register((QModule)new SellManager(this.plugin, EModule.SELL.isEnabled(), (MExecutor)new SellCommand(this.plugin)));
    register((QModule)new BuffManager(this.plugin, EModule.BUFFS.isEnabled(), (MExecutor)new BuffsCommand(this.plugin)));
    register((QModule)new CombatLogManager(this.plugin, EModule.COMBAT_LOG.isEnabled(), (MExecutor)new CombatLogCommand(this.plugin)));
    register((QModule)new NotificationsManager(this.plugin, EModule.NOTIFICATIONS.isEnabled(), (MExecutor)new NotifyCommand(this.plugin)));
    sendStatus();
  }
  
  private void sendStatus() {
    this.plugin.getServer().getConsoleSender().sendMessage("§6---------[ §eModules Initializing §6]---------");
    for (QModule m : this.modules.values())
      this.plugin.getServer().getConsoleSender().sendMessage("§7> §f" + m.name() + "§7 v" + m.version() + ": " + Utils.getModuleStatus(m)); 
  }
  
  public void shutdown() {
    for (QModule m : this.modules.values()) {
      m.unload();
      m = null;
    } 
    this.modules.clear();
  }
  
  private void register(QModule m) {
    m.enable();
    this.modules.put(m.type(), m);
  }
  
  public <T extends QModule> T getModule(Class<T> clazz) {
    for (QModule module : this.modules.values()) {
      if (clazz.isAssignableFrom(module.getClass()))
        return (T)module; 
    } 
    return null;
  }
  
  public QModule getModule(EModule e) {
    return this.modules.get(e);
  }
  
  public Collection<QModule> getModules() {
    return this.modules.values();
  }
}
