package su.nightexpress.quantumrpg.modules.runes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.ChatColor;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class RuneManager extends QModuleSocket {
  private MyConfig runesCfg;
  
  private int taskId;
  
  private final String NBT_KEY_ITEM_RUNE = "RUNE_";
  
  public RuneManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.RUNES;
  }
  
  public String name() {
    return "Runes";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.runesCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "runes.yml");
    setupSettings();
    setupSlot();
    setupRunes();
    startTask();
  }
  
  public void shutdown() {
    stopTask();
    this.ss = null;
  }
  
  private void setupSlot() {
    QSlotType st = QSlotType.RUNE;
    st.setModule(this);
    st.setHeader(getSettings().getHeader());
    st.setEmpty(getSettings().getEmptySlot());
    st.setFilled(getSettings().getFilledSlot());
  }
  
  protected void setupSettings() {
    JYML jYML = this.cfg.getConfig();
    String path = "item.";
    String display = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "name"));
    List<String> lore = jYML.getStringList(String.valueOf(path) + "lore");
    path = "general.";
    boolean stack = jYML.getBoolean(String.valueOf(path) + "stack-levels");
    path = "socketing.";
    DestroyType destroy = DestroyType.SOURCE;
    try {
      destroy = DestroyType.valueOf(jYML.getString(String.valueOf(path) + "destroy-type").toUpperCase());
    } catch (IllegalArgumentException ex) {
      log("Invalid 'destroy-type' in '/runes/settings.yml'", LogType.WARN);
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
      log("Invalid sound for 'sounds.failure' in '/runes/settings.yml'", LogType.WARN);
    } 
    Sound sound_suc_value = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    try {
      sound_suc_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "success"));
    } catch (IllegalArgumentException ex) {
      log("Invalid sound for 'sounds.success' in '/runes/settings.yml'", LogType.WARN);
    } 
    path = "socketing.lore-format.";
    String header = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "header"));
    String empty_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "empty-socket"));
    String filled_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "filled-socket"));
    this.ss = new RuneSettings(
        display, 
        lore, 
        
        destroy, 
        stack, 
        
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
    this.gui = new RSocketGUI(this, g_title, g_size, items, item_slot, source_slot, result_slot);
  }
  
  private void setupRunes() {
    JYML jYML = this.runesCfg.getConfig();
    if (!jYML.isConfigurationSection("runes"))
      return; 
    for (String o : jYML.getConfigurationSection("runes").getKeys(false)) {
      String path = "runes." + o + ".";
      boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
      if (!enabled)
        continue; 
      String effect = jYML.getString(String.valueOf(path) + "effect");
      Rune rune = new Rune(o, path, (FileConfiguration)jYML, effect);
      this.items.put(rune.getId(), rune);
    } 
  }
  
  private void startTask() {
    this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            for (Player p : RuneManager.this.plugin.getServer().getOnlinePlayers())
              RuneManager.this.setRuneEffects(p); 
          }
        },  10L, 60L);
  }
  
  private void stopTask() {
    this.plugin.getServer().getScheduler().cancelTask(this.taskId);
  }
  
  public List<String> getFilledSocketKeys(ItemStack item) {
    List<String> list = new ArrayList<>();
    if (item == null || item.getType() == Material.AIR)
      return list; 
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("RUNE_")) {
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
      if (ss.startsWith("RUNE_"))
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
    Rune r = (Rune)getItemById(id, Rune.class);
    if (r == null)
      return target; 
    int lvl = getLevel(src);
    String effect = getRuneEffect(src);
    String fill = String.valueOf(getSettings().getFilledSlot()) + r.getName().replace("%level%", String.valueOf(lvl)).replace("%rlevel%", Utils.IntegerToRomanNumeral(lvl));
    ItemMeta meta = target.getItemMeta();
    List<String> lore = meta.getLore();
    lore.set(getEmptySlotIndex(target), fill);
    meta.setLore(lore);
    target.setItemMeta(meta);
    NBTItem nnn = new NBTItem(target);
    nnn.setString("RUNE_" + getItemRunesAmount(target), String.valueOf(id) + ":" + lvl + ":" + effect);
    return nnn.getItem();
  }
  
  public void setRuneEffects(Player p) {
    HashMap<PotionEffectType, Integer> eff = new HashMap<>();
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = EntityAPI.getEquipment((LivingEntity)p, false)).length, b = 0; b < i; ) {
      ItemStack itemStack = arrayOfItemStack[b];
      if (itemStack != null && itemStack.hasItemMeta()) {
        NBTItem i2 = new NBTItem(itemStack);
        for (String key : i2.getKeys()) {
          if (key.startsWith("RUNE_")) {
            String[] s1 = i2.getString(key).split(":");
            PotionEffectType pet = PotionEffectType.getByName(s1[2]);
            if (pet == null)
              continue; 
            int lvl = Integer.parseInt(s1[1]);
            if (getSettings().isStackLevels() && 
              eff.containsKey(pet))
              lvl += ((Integer)eff.get(pet)).intValue(); 
            eff.put(pet, Integer.valueOf(lvl));
          } 
        } 
      } 
      b++;
    } 
    if (eff.isEmpty())
      return; 
    for (PotionEffectType pt : eff.keySet()) {
      int lvl = ((Integer)eff.get(pt)).intValue();
      int dur = 100;
      if (pt.getName().equalsIgnoreCase("NIGHT_VISION"))
        dur = 1200; 
      PotionEffect pp = new PotionEffect(pt, dur, lvl - 1);
      p.removePotionEffect(pt);
      p.addPotionEffect(pp);
    } 
  }
  
  public boolean hasRune(String id, ItemStack item) {
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("RUNE_")) {
        String[] key = nbt.getString(s).split(":");
        if (key[0].equalsIgnoreCase(id))
          return true; 
      } 
    } 
    return false;
  }
  
  public int getItemRuneLvl(ItemStack item, String rune) {
    NBTItem nbt = new NBTItem(item);
    for (String s : nbt.getKeys()) {
      if (s.startsWith("RUNE_")) {
        String[] key = nbt.getString(s).split(":");
        if (key[0].equalsIgnoreCase(rune))
          return Integer.parseInt(key[1]); 
      } 
    } 
    return 0;
  }
  
  private String getRuneEffect(ItemStack rune) {
    String id = getItemId(rune);
    Rune r = (Rune)getItemById(id, Rune.class);
    return r.getEffect();
  }
  
  public int getItemRunesAmount(ItemStack item) {
    NBTItem item2 = new NBTItem(item);
    int x = 0;
    for (String s : item2.getKeys()) {
      if (s.startsWith("RUNE_"))
        x++; 
    } 
    return x;
  }
  
  public RuneSettings getSettings() {
    return (RuneSettings)this.ss;
  }
  
  @EventHandler
  public void onClickInventory(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    Player p = (Player)e.getWhoClicked();
    ItemStack rune = e.getCursor();
    if (!isItemOfThisModule(rune))
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
    String id = getItemId(rune);
    Rune r = (Rune)getItemById(id, Rune.class);
    if (r == null) {
      out((Entity)p, Lang.Other_Internal.toMsg());
      return;
    } 
    if (!r.isValidType(target)) {
      out((Entity)p, Lang.Runes_Enchanting_InvalidType.toMsg());
      return;
    } 
    if (hasRune(id, target)) {
      out((Entity)p, Lang.Runes_Enchanting_AlreadyHave.toMsg());
      return;
    } 
    if (!isInLevelRange(target, rune)) {
      out((Entity)p, Lang.Runes_Enchanting_BadLevel.toMsg());
      return;
    } 
    if (target.getItemMeta().getLore().contains(this.ss.getEmptySlot())) {
      e.setCursor(null);
      startSocketing(p, target, rune);
      e.setCancelled(true);
    } else {
      out((Entity)p, Lang.Runes_Enchanting_NoSlots.toMsg());
    } 
  }
  
  public class Rune extends SocketItem {
    private String effect;
    
    public Rune(String id, String path, FileConfiguration cfg, String effect) {
      super(id, path, cfg, RuneManager.this.type());
      setEffect(effect);
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
      String display = replacePlaceholders(RuneManager.this.ss.getDisplay()
          .replace("%item_name%", meta.getDisplayName()), lvl);
      List<String> lore2 = new ArrayList<>();
      for (String s : RuneManager.this.ss.getLore()) {
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
  
  public class RuneSettings extends SocketSettings {
    private boolean stack;
    
    public RuneSettings(String display, List<String> lore, DestroyType destroy, boolean stack, boolean eff_use, String eff_de_value, String eff_suc_value, boolean sound_use, Sound sound_de_value, Sound sound_suc_value, String header, String empty_slot, String filled_slot) {
      super(display, lore, destroy, eff_use, eff_de_value, eff_suc_value, sound_use, sound_de_value, sound_suc_value, header, empty_slot, filled_slot);
      setStackLevels(stack);
    }
    
    public boolean isStackLevels() {
      return this.stack;
    }
    
    public void setStackLevels(boolean stack) {
      this.stack = stack;
    }
  }
}
