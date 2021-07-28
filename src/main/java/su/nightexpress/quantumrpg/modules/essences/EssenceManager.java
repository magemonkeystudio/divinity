package su.nightexpress.quantumrpg.modules.essences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModuleSocket;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.SocketSettings;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.types.DestroyType;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.utils.ParticleUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class EssenceManager extends QModuleSocket {
  private MyConfig essCfg;
  
  private int taskId;
  
  private final String NBT_KEY_ITEM_ESS = "ESSNECE_";
  
  public EssenceManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.ESSENCES;
  }
  
  public String name() {
    return "Essences";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.essCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "essences.yml");
    setupMain();
    startTask();
  }
  
  public void shutdown() {
    this.ss = null;
    stopTask();
  }
  
  private void setupMain() {
    setupSettings();
    setupEssences();
    setupSlot();
  }
  
  private void startTask() {
    this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            for (Player p : EssenceManager.this.plugin.getServer().getOnlinePlayers())
              EssenceManager.this.applyEffects(p); 
          }
        },  0L, 100L);
  }
  
  private void stopTask() {
    this.plugin.getServer().getScheduler().cancelTask(this.taskId);
  }
  
  private void applyEffects(Player p) {
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = EntityAPI.getEquipment((LivingEntity)p, false)).length, b = 0; b < i; ) {
      ItemStack itemStack = arrayOfItemStack[b];
      if (itemStack != null && itemStack.getType() != Material.AIR)
        for (EssenceEffect ee : getItemEssences(itemStack).values())
          ee.getType().play(p, ee.getLevel(), ee.getEffect());  
      b++;
    } 
  }
  
  protected void setupSettings() {
    JYML jYML = this.cfg.getConfig();
    String path = "socketing.";
    DestroyType destroy = DestroyType.CLEAR;
    try {
      destroy = DestroyType.valueOf(jYML.getString(String.valueOf(path) + "destroy-type").toUpperCase());
    } catch (IllegalArgumentException ex) {
      log("Invalid 'destroy-type' in '/essences/settings.yml'", LogType.WARN);
    } 
    path = "socketing.effects.";
    boolean eff_use = jYML.getBoolean(String.valueOf(path) + "enabled");
    String eff_de_value = jYML.getString(String.valueOf(path) + "failure");
    String eff_suc_value = jYML.getString(String.valueOf(path) + "success");
    path = "socketing.sounds.";
    boolean sound_use = jYML.getBoolean(String.valueOf(path) + "enabled");
    Sound sound_de_value = Sound.BLOCK_ANVIL_BREAK;
    try {
      sound_de_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "failure"));
    } catch (IllegalArgumentException ex) {
      log("Invalid sound for 'sounds.failure' in '/essences/settings.yml'", LogType.WARN);
    } 
    Sound sound_suc_value = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    try {
      sound_suc_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "success"));
    } catch (IllegalArgumentException ex) {
      log("Invalid sound for 'sounds.success' in '/essences/settings.yml'", LogType.WARN);
    } 
    path = "socketing.lore-format.";
    String header = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "header"));
    String empty_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "empty-socket"));
    String filled_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "filled-socket"));
    path = "item.";
    String display = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "name"));
    List<String> lore = jYML.getStringList(String.valueOf(path) + "lore");
    this.ss = new EssSettings(
        display, 
        lore, 
        
        destroy, 
        
        eff_use, 
        eff_de_value, 
        eff_suc_value, 
        
        sound_use, 
        sound_de_value, 
        sound_suc_value, 
        
        header, 
        empty_slot, 
        filled_slot);
    path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    int item_slot = jYML.getInt(String.valueOf(path) + "item-slot");
    int source_slot = jYML.getInt(String.valueOf(path) + "source-slot");
    int result_slot = jYML.getInt(String.valueOf(path) + "result-slot");
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    this.gui = new ESocketGUI(this, g_title, g_size, items, item_slot, source_slot, result_slot);
  }
  
  private void setupEssences() {
    JYML jYML = this.essCfg.getConfig();
    if (!jYML.isConfigurationSection("essences"))
      return; 
    for (String o : jYML.getConfigurationSection("essences").getKeys(false)) {
      EssenceType type;
      String path = "essences." + o + ".";
      String id = o.toLowerCase();
      boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
      if (!enabled)
        continue; 
      try {
        type = EssenceType.valueOf(jYML.getString(String.valueOf(path) + "effect.type"));
      } catch (IllegalArgumentException ex) {
        log("Invalid EffectType for essence '" + id + "'!", LogType.ERROR);
        continue;
      } 
      String effect = jYML.getString(String.valueOf(path) + "effect.name");
      Essence gem = new Essence(
          id, 
          path, 
          (FileConfiguration)jYML, 
          
          type, 
          effect);
      this.items.put(gem.getId(), gem);
    } 
  }
  
  private void setupSlot() {
    QSlotType st = QSlotType.ESSENCE;
    st.setModule(this);
    st.setHeader(getSettings().getHeader());
    st.setEmpty(getSettings().getEmptySlot());
    st.setFilled(getSettings().getFilledSlot());
  }
  
  public List<String> getFilledSocketKeys(ItemStack item) {
    List<String> list = new ArrayList<>();
    if (item == null || item.getType() == Material.AIR)
      return list; 
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("ESSNECE_")) {
        String v = nbt.getString(s);
        String id = v.split(":")[0];
        int lvl = Integer.parseInt(v.split(":")[1]);
        list.add(String.valueOf(id) + ":" + lvl);
      } 
    } 
    return list;
  }
  
  public ItemStack extractSocket(ItemStack item, int num) {
    List<String> has = getFilledSocketKeys(item);
    has.remove(num);
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    for (int i = 0; i < lore.size(); i++) {
      String s = lore.get(i);
      if (s.startsWith(this.ss.getFilledSlot()))
        lore.set(i, this.ss.getEmptySlot()); 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    NBTItem item2 = new NBTItem(item);
    for (String ss : item2.getKeys()) {
      if (ss.startsWith("ESSNECE_"))
        item2.removeKey(ss); 
    } 
    item = item2.getItem();
    for (String en : has) {
      String id = en.split(":")[0];
      int lvl = Integer.parseInt(en.split(":")[1]);
      ItemStack gem = ItemAPI.getItemByModule(type(), id, lvl, -1);
      item = insertSocket(item, gem);
    } 
    return item;
  }
  
  public ItemStack insertSocket(ItemStack target, ItemStack src) {
    String id = getItemId(src);
    int lvl = getLevel(src);
    String effect = getEssenceEffect(src);
    EssenceType type = getEssenceType(src);
    Essence e = (Essence)getItemById(id, Essence.class);
    String fill = String.valueOf(getSettings().getFilledSlot()) + e.getName().replace("%level%", String.valueOf(lvl)).replace("%rlevel%", Utils.IntegerToRomanNumeral(lvl));
    ItemMeta meta = target.getItemMeta();
    List<String> lore = meta.getLore();
    lore.set(getEmptySlotIndex(target), fill);
    meta.setLore(lore);
    target.setItemMeta(meta);
    NBTItem nnn = new NBTItem(target);
    nnn.setString("ESSNECE_" + getItemEssencesAmount(target), String.valueOf(id) + ":" + lvl + ":" + type + ":" + effect);
    return nnn.getItem();
  }
  
  public boolean hasEssences(ItemStack item) {
    return (getItemEssencesAmount(item) > 0);
  }
  
  public boolean hasEssence(ItemStack item, String id) {
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("ESSNECE_")) {
        String[] key = nbt.getString(s).split(":");
        if (key[0].equalsIgnoreCase(id))
          return true; 
      } 
    } 
    return false;
  }
  
  public int getItemEssencesAmount(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return 0; 
    NBTItem item2 = new NBTItem(item);
    int x = 0;
    for (String s : item2.getKeys()) {
      if (s.startsWith("ESSNECE_"))
        x++; 
    } 
    return x;
  }
  
  public int getItemEssenceLvl(ItemStack item, String id) {
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("ESSNECE_")) {
        String[] key = nbt.getString(s).split(":");
        if (key[0].equalsIgnoreCase(id))
          return Integer.parseInt(key[1]); 
      } 
    } 
    return 0;
  }
  
  public HashMap<EssenceType, EssenceEffect> getItemEssences(ItemStack item) {
    HashMap<EssenceType, EssenceEffect> map = new HashMap<>();
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("ESSNECE_")) {
        String[] val = nbt.getString(s).split(":");
        int lvl = Integer.parseInt(val[1]);
        EssenceType type = EssenceType.valueOf(val[2]);
        if (map.containsKey(type)) {
          int map_lvl = ((EssenceEffect)map.get(type)).getLevel();
          if (map_lvl >= lvl)
            continue; 
        } 
        String eff = val[3];
        if (val.length == 5)
          eff = String.valueOf(eff) + ":" + val[4]; 
        EssenceEffect ee = new EssenceEffect(type, eff, lvl);
        map.put(type, ee);
      } 
    } 
    return map;
  }
  
  private EssenceType getEssenceType(ItemStack item) {
    String id = getItemId(item);
    Essence es = (Essence)getItemById(id, Essence.class);
    return es.getEssenceType();
  }
  
  private String getEssenceEffect(ItemStack item) {
    String id = getItemId(item);
    Essence es = (Essence)getItemById(id, Essence.class);
    return es.getEffect();
  }
  
  @EventHandler
  public void onInvClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack gem = e.getCursor();
    if (!isItemOfThisModule(gem))
      return; 
    ItemStack target = e.getCurrentItem();
    if (target == null || !target.hasItemMeta() || !target.getItemMeta().hasLore())
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    String id = getItemId(gem);
    if (hasEssence(target, id)) {
      out((Entity)p, Lang.Essences_AlreadyHave.toMsg().replace("%item%", Utils.getItemName(target)));
      return;
    } 
    Essence gem3 = (Essence)getItemById(id, Essence.class);
    if (gem3 == null) {
      out((Entity)p, Lang.Other_Internal.toMsg());
      return;
    } 
    if (!gem3.isValidType(target)) {
      out((Entity)p, Lang.Essences_Enchanting_InvalidType.toMsg());
      return;
    } 
    if (!isInLevelRange(target, gem)) {
      out((Entity)p, Lang.Essences_Enchanting_BadLevel.toMsg());
      return;
    } 
    if (target.getItemMeta().getLore().contains(this.ss.getEmptySlot())) {
      e.setCursor(null);
      startSocketing(p, target, gem);
      e.setCancelled(true);
    } else {
      out((Entity)p, Lang.Essences_Enchanting_NoSlots.toMsg());
    } 
  }
  
  public EssSettings getSettings() {
    return (EssSettings)this.ss;
  }
  
  public enum EssenceType {
    HELIX, AURA, FOOT;
    
    public void play(Player p, int lvl, String eff) {
      if (this == HELIX) {
        ParticleUtils.helix(eff, (LivingEntity)p, lvl);
      } else if (this == AURA) {
        ParticleUtils.aura(eff, (LivingEntity)p, lvl);
      } else if (this == FOOT) {
        ParticleUtils.foot(eff, (LivingEntity)p, lvl);
      } 
    }
  }
  
  public class EssenceEffect {
    private EssenceManager.EssenceType type;
    
    private String effect;
    
    private int lvl;
    
    public EssenceEffect(EssenceManager.EssenceType type, String effect, int lvl) {
      this.type = type;
      this.effect = effect;
      this.lvl = lvl;
    }
    
    public EssenceManager.EssenceType getType() {
      return this.type;
    }
    
    public String getEffect() {
      return this.effect;
    }
    
    public int getLevel() {
      return this.lvl;
    }
  }
  
  public class Essence extends SocketItem {
    private EssenceManager.EssenceType type;
    
    private String effect;
    
    public Essence(String id, String path, FileConfiguration cfg, EssenceManager.EssenceType type, String effect) {
      super(id, path, cfg, EssenceManager.this.type());
      setType(type);
      setEffect(effect);
    }
    
    public EssenceManager.EssenceType getEssenceType() {
      return this.type;
    }
    
    public void setType(EssenceManager.EssenceType type) {
      this.type = type;
    }
    
    public String getEffect() {
      return this.effect;
    }
    
    public void setEffect(String effect) {
      this.effect = effect;
    }
    
    protected ItemStack build(int lvl, int suc) {
      ItemStack item = super.build(lvl, suc);
      if (item.getType() == Material.AIR)
        return item; 
      ItemMeta meta = item.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (meta.hasLore())
        lore = meta.getLore(); 
      String display = replacePlaceholders(EssenceManager.this.ss.getDisplay()
          .replace("%item_name%", meta.getDisplayName()), lvl);
      List<String> lore2 = new ArrayList<>();
      for (String s : EssenceManager.this.ss.getLore()) {
        if (s.equals("%item_lore%")) {
          for (String s2 : lore)
            lore2.add(s2); 
          continue;
        } 
        lore2.add(replacePlaceholders(s, lvl, suc));
      } 
      meta.setDisplayName(display);
      meta.setLore(lore2);
      item.setItemMeta(meta);
      return item;
    }
  }
  
  public class EssSettings extends SocketSettings {
    public EssSettings(String display, List<String> lore, DestroyType destroy, boolean eff_use, String eff_de_value, String eff_suc_value, boolean sound_use, Sound sound_de_value, Sound sound_suc_value, String header, String empty_slot, String filled_slot) {
      super(display, lore, destroy, eff_use, eff_de_value, eff_suc_value, sound_use, sound_de_value, sound_suc_value, header, empty_slot, filled_slot);
    }
  }
}
