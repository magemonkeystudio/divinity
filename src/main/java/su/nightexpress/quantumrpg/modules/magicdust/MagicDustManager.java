package su.nightexpress.quantumrpg.modules.magicdust;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LeveledItem;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleLevel;
import su.nightexpress.quantumrpg.modules.QModuleRate;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class MagicDustManager extends QModuleLevel {
  private MagicDust item;
  
  private MagicSettings ss;
  
  private final String NBT_KEY_MDUST = "QRPG_DUST_RATE";
  
  public MagicDustManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.MAGIC_DUST;
  }
  
  public String name() {
    return "Magic Dust";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    setupMain();
  }
  
  public void shutdown() {
    this.item = null;
    this.ss = null;
  }
  
  public void setupMain() {
    JYML jYML = this.cfg.getConfig();
    int min_lvl = jYML.getInt("general.min-level");
    int max_lvl = jYML.getInt("general.max-level");
    int max_suc = Math.min(100, jYML.getInt("general.max-item-success-rate", 80));
    double rate_inc = jYML.getDouble("general.rate-inc");
    double rate_inc_lvl = jYML.getDouble("general.rate-inc-per-lvl");
    double rate_max = jYML.getDouble("general.rate-inc-max");
    String effect = jYML.getString("general.effect");
    Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    try {
      sound = Sound.valueOf(jYML.getString("general.sound").toUpperCase());
    } catch (IllegalArgumentException ex) {
      log("Invalid sound for magic dust!", LogType.WARN);
    } 
    this.ss = new MagicSettings(
        min_lvl, 
        max_lvl, 
        max_suc, 
        rate_inc, 
        rate_inc_lvl, 
        rate_max, 
        effect, 
        sound);
    this.item = new MagicDust("magic-dust", "item.", (FileConfiguration)jYML);
    this.items.put(this.item.getId(), this.item);
  }
  
  public int getDustRate(ItemStack item) {
    NBTItem nbt = new NBTItem(item);
    return nbt.getInteger("QRPG_DUST_RATE").intValue();
  }
  
  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack dust = e.getCursor();
    if (!isItemOfThisModule(dust))
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    ItemStack target = e.getCurrentItem();
    EModule em = ItemAPI.getItemModule(target);
    if (em == null || !em.isEnabled())
      return; 
    QModule q = this.plugin.getMM().getModule(em);
    if (!q.isRateable())
      return; 
    QModuleRate qs = (QModuleRate)q;
    int rate = qs.getSocketRate(target);
    if (rate < 0)
      return; 
    Player p = (Player)e.getWhoClicked();
    if (rate >= this.ss.getMaxRate()) {
      out((Entity)p, Lang.MagicDust_Maximum.toMsg().replace("%rate%", String.valueOf(rate)));
      return;
    } 
    if (target.getAmount() > 1) {
      out((Entity)p, Lang.MagicDust_NoStack.toMsg());
      return;
    } 
    e.setCancelled(true);
    if (dust.getAmount() == 1) {
      e.setCursor(null);
    } else {
      dust.setAmount(dust.getAmount() - 1);
      e.setCursor(dust);
    } 
    String id = qs.getItemId(target);
    int lvl = qs.getLevel(target);
    SocketItem si = (SocketItem)qs.getItemById(id);
    int rate2 = Math.min(this.ss.getMaxRate(), getDustRate(dust) + rate);
    e.setCurrentItem(si.create(lvl, rate2));
    this.ss.playEffects(p);
    out((Entity)p, Lang.MagicDust_Done.toMsg());
  }
  
  class MagicSettings {
    private int min_lvl;
    
    private int max_lvl;
    
    private int max_suc;
    
    private double rate_inc;
    
    private double rate_inc_lvl;
    
    private double rate_max;
    
    private String effect;
    
    private Sound sound;
    
    public MagicSettings(int min_lvl, int max_lvl, int max_suc, double rate_inc, double rate_inc_lvl, double rate_max, String effect, Sound sound) {
      this.min_lvl = min_lvl;
      this.max_lvl = max_lvl;
      this.max_suc = max_suc;
      this.rate_inc = rate_inc;
      this.rate_inc_lvl = rate_inc_lvl;
      this.rate_max = rate_max;
      this.effect = effect;
      this.sound = sound;
    }
    
    public int getMinLvl() {
      return this.min_lvl;
    }
    
    public int getMaxLvl() {
      return this.max_lvl;
    }
    
    public int getMaxRate() {
      return this.max_suc;
    }
    
    public double getRateInc() {
      return this.rate_inc;
    }
    
    public double getRateIncLvl() {
      return this.rate_inc_lvl;
    }
    
    public double getRateIncMax() {
      return this.rate_max;
    }
    
    public void playEffects(Player p) {
      p.playSound(p.getLocation(), this.sound, 0.5F, 0.5F);
      Utils.playEffect(this.effect, p.getLocation().add(0.0D, 0.85D, 0.0D), 0.3F, 0.3F, 0.3F, 0.1F, 50);
    }
  }
  
  public class MagicDust extends LeveledItem {
    public MagicDust(String id, String path, FileConfiguration cfg) {
      super(id, path, cfg, MagicDustManager.this.type());
    }
    
    protected ItemStack build(int lvl) {
      ItemStack item = super.build(lvl);
      if (item.getType() == Material.AIR)
        return item; 
      ItemMeta meta = item.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (meta.hasLore())
        lore = meta.getLore(); 
      double d = MagicDustManager.this.ss.getRateInc() + MagicDustManager.this.ss.getRateIncLvl() * (lvl - 1.0D);
      int rate = (int)Math.min(MagicDustManager.this.ss.getMaxRate(), d);
      for (int i = 0; i < lore.size(); i++) {
        String s = lore.get(i);
        lore.set(i, s.replace("%rate%", String.valueOf(rate)));
      } 
      meta.setLore(lore);
      item.setItemMeta(meta);
      NBTItem nbt = new NBTItem(item);
      nbt.setInteger("QRPG_DUST_RATE", Integer.valueOf(rate));
      ItemStack item2 = nbt.getItem();
      return item2;
    }
  }
}
