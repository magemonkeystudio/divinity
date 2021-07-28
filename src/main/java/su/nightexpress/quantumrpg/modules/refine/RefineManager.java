package su.nightexpress.quantumrpg.modules.refine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
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
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModuleRate;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.NBTUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class RefineManager extends QModuleRate {
  private MyConfig itemsCfg;
  
  private int max_lvl;
  
  private Map<Integer, Integer> lvl_down;
  
  private Map<ItemStat, Double> inc_stat;
  
  private Map<String, Double> inc_dmg;
  
  private Map<String, Double> inc_def;
  
  private double inc_lvl;
  
  private boolean eff_use;
  
  private String eff_de_value;
  
  private String eff_suc_value;
  
  private boolean sound_use;
  
  private Sound sound_de_value;
  
  private Sound sound_suc_value;
  
  private boolean f_name_prefix;
  
  private String f_name_format;
  
  private String f_lore_format;
  
  private String i_name;
  
  private List<String> i_lore;
  
  private static final String FSP = "§r§r§8§8§r§r §r§r§8§8§r§r";
  
  private final String NBT_FINE_LVL = "REFINE_LVL";
  
  public RefineManager(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public EModule type() {
    return EModule.REFINE;
  }
  
  public String name() {
    return "Refine";
  }
  
  public String version() {
    return "1.0.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void setup() {
    this.lvl_down = new HashMap<>();
    this.inc_stat = new HashMap<>();
    this.inc_dmg = new HashMap<>();
    this.inc_def = new HashMap<>();
    setupCfg();
    setupItems();
  }
  
  public void updateCfg() {
    JYML jYML = this.cfg.getConfig();
    byte b;
    int i;
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat e = arrayOfItemStat[b];
      if (e != ItemStat.DURABILITY)
        if (!jYML.contains("refine.stats-per-lvl." + e.name()))
          jYML.set("refine.stats-per-lvl." + e.name(), Double.valueOf(2.0D));  
      b++;
    } 
    for (DamageType dt : Config.getDamageTypes().values()) {
      if (!jYML.contains("refine.damage-per-lvl." + dt.getId()))
        jYML.set("refine.damage-per-lvl." + dt.getId(), Double.valueOf(3.0D)); 
    } 
    for (ArmorType dt : Config.getArmorTypes().values()) {
      if (!jYML.contains("refine.defense-per-lvl." + dt.getId()))
        jYML.set("refine.defense-per-lvl." + dt.getId(), Double.valueOf(6.0D)); 
    } 
    if (!jYML.contains("refine.inc-per-level"))
      jYML.set("refine.inc-per-level", Double.valueOf(0.05D)); 
    this.cfg.save();
  }
  
  public void shutdown() {
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  private void setupCfg() {
    JYML jYML = this.cfg.getConfig();
    String path = "refine.";
    this.max_lvl = jYML.getInt(String.valueOf(path) + "max-level");
    if (jYML.isConfigurationSection(String.valueOf(path) + "fail-level-downgrade"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "fail-level-downgrade").getKeys(false)) {
        int lvl = Integer.parseInt(m);
        int down = jYML.getInt(String.valueOf(path) + "fail-level-downgrade." + m);
        this.lvl_down.put(Integer.valueOf(lvl), Integer.valueOf(down));
      }  
    if (jYML.isConfigurationSection(String.valueOf(path) + "defense-per-lvl"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "defense-per-lvl").getKeys(false)) {
        ArmorType at = Config.getArmorTypeById(m);
        if (at == null) {
          log("Invalid armor type '" + m + "' in 'refine.defense-per-lvl'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "defense-per-lvl." + m);
        if (c > 0.0D)
          this.inc_def.put(at.getId(), Double.valueOf(c)); 
      }  
    if (jYML.isConfigurationSection(String.valueOf(path) + "damage-per-lvl"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "damage-per-lvl").getKeys(false)) {
        DamageType at = Config.getDamageTypeById(m);
        if (at == null) {
          log("Invalid damage type '" + m + "' in 'refine.damage-per-lvl'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "damage-per-lvl." + m);
        if (c > 0.0D)
          this.inc_dmg.put(at.getId(), Double.valueOf(c)); 
      }  
    byte b;
    int i;
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat e = arrayOfItemStat[b];
      if (e != ItemStat.DURABILITY) {
        double c = jYML.getDouble(String.valueOf(path) + "stats-per-lvl." + e.name());
        if (c > 0.0D)
          this.inc_stat.put(e, Double.valueOf(c)); 
      } 
      b++;
    } 
    this.inc_lvl = jYML.getDouble(String.valueOf(path) + "inc-per-level");
    path = "refine.effects.";
    this.eff_use = jYML.getBoolean(String.valueOf(path) + "enabled");
    this.eff_de_value = jYML.getString(String.valueOf(path) + "failure");
    this.eff_suc_value = jYML.getString(String.valueOf(path) + "success");
    path = "refine.sounds.";
    this.sound_use = jYML.getBoolean(String.valueOf(path) + "enabled");
    this.sound_de_value = Sound.BLOCK_ANVIL_BREAK;
    try {
      this.sound_de_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "failure"));
    } catch (IllegalArgumentException ex) {
      log("Invalid sound in &f'refine.sounds.failure'", LogType.WARN);
    } 
    this.sound_suc_value = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    try {
      this.sound_suc_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "success"));
    } catch (IllegalArgumentException ex) {
      log("Invalid sound in &f'refine.sounds.failure'", LogType.WARN);
    } 
    path = "format.item-name.";
    this.f_name_prefix = jYML.getBoolean(String.valueOf(path) + "as-prefix");
    this.f_name_format = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "format"));
    path = "format.item-lore.";
    this.f_lore_format = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "format"));
    path = "item.";
    this.i_name = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "name"));
    this.i_lore = jYML.getStringList(String.valueOf(path) + "lore");
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
    this.gui = new RefineGUI(this, g_title, g_size, items, item_slot, source_slot, result_slot);
  }
  
  private void setupItems() {
    this.itemsCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "items.yml");
    JYML jYML = this.itemsCfg.getConfig();
    if (!jYML.isConfigurationSection("items"))
      return; 
    for (String o : jYML.getConfigurationSection("items").getKeys(false)) {
      String path = "items." + o + ".";
      boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
      if (!enabled)
        continue; 
      String id = o;
      EnchStone es = new EnchStone(id, path, (FileConfiguration)jYML);
      this.items.put(es.getId(), es);
    } 
  }
  
  public boolean canRefine(ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return false; 
    if (!ItemUtils.isArmor(item) && !ItemUtils.isTool(item) && !ItemUtils.isWeapon(item))
      return false; 
    if (isRefined(item) && getRefineLevel(item) >= this.max_lvl)
      return false; 
    return true;
  }
  
  public boolean isRefined(ItemStack item) {
    NBTItem nbt = new NBTItem(item);
    return nbt.hasKey("REFINE_LVL").booleanValue();
  }
  
  public int getRefineLevel(ItemStack item) {
    if (!isRefined(item))
      return 0; 
    NBTItem nbt = new NBTItem(item);
    return nbt.getInteger("REFINE_LVL").intValue();
  }
  
  private ItemStack setRefineLevel(ItemStack item, int lvl) {
    NBTItem nbt = new NBTItem(item);
    if (lvl > 0) {
      nbt.setInteger("REFINE_LVL", Integer.valueOf(lvl));
    } else {
      nbt.removeKey("REFINE_LVL");
    } 
    return nbt.getItem();
  }
  
  public Map<ItemStat, Double> getRefinedStats(ItemStack item) {
    Map<ItemStat, Double> map = new HashMap<>();
    if (!isRefined(item))
      return map; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    for (ItemStat is : this.inc_stat.keySet()) {
      if (!ItemAPI.hasAttribute(item, is))
        continue; 
      int pos = ItemAPI.getStatLoreIndex(item, is);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double val = getFromLore(lore, line, pos);
      map.put(is, Double.valueOf(val));
    } 
    return map;
  }
  
  public double getRefinedStat(ItemStack item, ItemStat is) {
    if (!isRefined(item))
      return 0.0D; 
    if (!ItemAPI.hasAttribute(item, is))
      return 0.0D; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    int pos = ItemAPI.getStatLoreIndex(item, is);
    if (pos < 0)
      return 0.0D; 
    String line = lore.get(pos);
    return getFromLore(lore, line, pos);
  }
  
  public Map<String, Double> getRefinedDefense(ItemStack item) {
    Map<String, Double> map = new HashMap<>();
    if (!isRefined(item))
      return map; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    for (String id : this.inc_def.keySet()) {
      if (!ItemAPI.hasDefenseType(id, item))
        continue; 
      int pos = ItemAPI.getDefenseLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double val = getFromLore(lore, line, pos);
      map.put(id, Double.valueOf(val));
    } 
    return map;
  }
  
  public double getRefinedDefense(ItemStack item, String id) {
    if (!isRefined(item))
      return 0.0D; 
    if (!ItemAPI.hasDefenseType(id, item))
      return 0.0D; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    int pos = ItemAPI.getDefenseLoreIndex(id, item);
    if (pos < 0)
      return 0.0D; 
    String line = lore.get(pos);
    return getFromLore(lore, line, pos);
  }
  
  public Map<String, Double> getRefinedDamage(ItemStack item) {
    Map<String, Double> map = new HashMap<>();
    if (!isRefined(item))
      return map; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    for (String id : this.inc_dmg.keySet()) {
      if (!ItemAPI.hasDamageType(id, item))
        continue; 
      int pos = ItemAPI.getDamageLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double val = getFromLore(lore, line, pos);
      map.put(id, Double.valueOf(val));
    } 
    return map;
  }
  
  public double getRefinedDamage(ItemStack item, String id) {
    if (!isRefined(item))
      return 0.0D; 
    if (!ItemAPI.hasDamageType(id, item))
      return 0.0D; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    int pos = ItemAPI.getDamageLoreIndex(id, item);
    if (pos < 0)
      return 0.0D; 
    String line = lore.get(pos);
    return getFromLore(lore, line, pos);
  }
  
  private double getFromLore(List<String> lore, String line, int pos) {
    if (line.contains("§r§r§8§8§r§r §r§r§8§8§r§r")) {
      String[] split = line.split("§r§r§8§8§r§r §r§r§8§8§r§r");
      String delete = split[1];
      String[] ff = this.f_lore_format.split("%amount%");
      if (ff.length >= 1)
        delete = delete.replace(ff[0], ""); 
      if (ff.length >= 2)
        delete = delete.replace(ff[1], ""); 
      return Double.parseDouble(delete);
    } 
    return 0.0D;
  }
  
  public ItemStack refineItem(ItemStack item) {
    if (!canRefine(item))
      return item; 
    int lvl = getRefineLevel(item);
    item = resetFines(item);
    item = addFines(item, lvl + 1);
    return item;
  }
  
  public ItemStack downgradeItem(ItemStack item) {
    if (!isRefined(item))
      return item; 
    int lvl = getRefineLevel(item);
    int down = 1;
    if (this.lvl_down.containsKey(Integer.valueOf(lvl)))
      down = ((Integer)this.lvl_down.get(Integer.valueOf(lvl))).intValue(); 
    if (down > lvl)
      down = lvl; 
    int lvl2 = lvl - down;
    item = resetFines(item);
    if (lvl2 > 0)
      item = addFines(item, lvl2); 
    return item;
  }
  
  public String getNameWithoutLevel(String name) {
    if (!name.contains("§r§r§8§8§r§r §r§r§8§8§r§r"))
      return name; 
    if (this.f_name_prefix)
      return name.split("§r§r§8§8§r§r §r§r§8§8§r§r")[1]; 
    return name.split("§r§r§8§8§r§r §r§r§8§8§r§r")[0];
  }
  
  private ItemStack addFines(ItemStack item, int lvl) {
    if (item == null || !item.hasItemMeta())
      return item; 
    ItemMeta meta = item.getItemMeta();
    String name = Utils.getItemName(item);
    String format = this.f_name_format.replace("%lvl%", String.valueOf(lvl));
    if (this.f_name_prefix) {
      name = String.valueOf(ItemAPI.getItemFirstColor(item)) + format + "§r§r§8§8§r§r §r§r§8§8§r§r" + name;
    } else {
      name = String.valueOf(ItemAPI.getItemFirstColor(item)) + name + "§r§r§8§8§r§r §r§r§8§8§r§r" + format;
    } 
    meta.setDisplayName(name);
    int item_lvl = Math.max(0, NBTUtils.getItemLevel(item));
    List<String> lore = new ArrayList<>(meta.getLore());
    for (Map.Entry<ItemStat, Double> e : this.inc_stat.entrySet()) {
      ItemStat is = e.getKey();
      if (!ItemAPI.hasAttribute(item, is))
        continue; 
      int pos = ItemAPI.getStatLoreIndex(item, is);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double amount = (((Double)e.getValue()).doubleValue() + item_lvl * this.inc_lvl) * lvl;
      addInLore(lore, line, pos, amount);
    } 
    for (Map.Entry<String, Double> e : this.inc_def.entrySet()) {
      String id = e.getKey();
      if (!ItemAPI.hasDefenseType(id, item))
        continue; 
      int pos = ItemAPI.getDefenseLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double amount = (((Double)e.getValue()).doubleValue() + item_lvl * this.inc_lvl) * lvl;
      addInLore(lore, line, pos, amount);
    } 
    for (Map.Entry<String, Double> e : this.inc_dmg.entrySet()) {
      String id = e.getKey();
      if (!ItemAPI.hasDamageType(id, item))
        continue; 
      int pos = ItemAPI.getDamageLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      double amount = (((Double)e.getValue()).doubleValue() + item_lvl * this.inc_lvl) * lvl;
      addInLore(lore, line, pos, amount);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    item = setRefineLevel(item, lvl);
    item = this.plugin.getNMS().fixNBT(item);
    return item;
  }
  
  private void addInLore(List<String> lore, String line, int pos, double amount) {
    amount = Utils.round3(amount);
    String format = this.f_lore_format.replace("%amount%", String.valueOf(amount));
    line = String.valueOf(line) + "§r§r§8§8§r§r §r§r§8§8§r§r" + format;
    lore.set(pos, line);
  }
  
  private ItemStack resetFines(ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return item; 
    if (!isRefined(item))
      return item; 
    ItemMeta meta = item.getItemMeta();
    String name = meta.getDisplayName();
    if (name.contains("§r§r§8§8§r§r §r§r§8§8§r§r")) {
      if (this.f_name_prefix) {
        String lvl_name = name.split("§r§r§8§8§r§r §r§r§8§8§r§r")[0];
        name = name.substring(lvl_name.length() + "§r§r§8§8§r§r §r§r§8§8§r§r".length());
      } else {
        String[] arr = name.split("§r§r§8§8§r§r §r§r§8§8§r§r");
        String lvl_name = arr[1];
        name = name.substring(0, name.length() - lvl_name.length() + "§r§r§8§8§r§r §r§r§8§8§r§r".length());
      } 
      meta.setDisplayName(name);
    } 
    List<String> lore = new ArrayList<>(meta.getLore());
    for (ItemStat is : this.inc_stat.keySet()) {
      if (!ItemAPI.hasAttribute(item, is))
        continue; 
      int pos = ItemAPI.getStatLoreIndex(item, is);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      delInLore(lore, line, pos);
    } 
    for (String id : this.inc_def.keySet()) {
      if (!ItemAPI.hasDefenseType(id, item))
        continue; 
      int pos = ItemAPI.getDefenseLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      delInLore(lore, line, pos);
    } 
    for (String id : this.inc_dmg.keySet()) {
      if (!ItemAPI.hasDamageType(id, item))
        continue; 
      int pos = ItemAPI.getDamageLoreIndex(id, item);
      if (pos < 0)
        continue; 
      String line = lore.get(pos);
      delInLore(lore, line, pos);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    item = this.plugin.getNMS().fixNBT(item);
    item = setRefineLevel(item, 0);
    return item;
  }
  
  private void delInLore(List<String> lore, String line, int pos) {
    if (line.contains("§r§r§8§8§r§r §r§r§8§8§r§r")) {
      String[] split = line.split("§r§r§8§8§r§r §r§r§8§8§r§r");
      String delete = line.substring(0, line.length() - split[1].length() + "§r§r§8§8§r§r §r§r§8§8§r§r".length());
      lore.set(pos, delete);
    } 
  }
  
  private void openEnchantGUI(Player p, ItemStack item, ItemStack gem) {
    p.getInventory().removeItem(new ItemStack[] { item });
    if (gem.getAmount() > 1) {
      ItemStack gem2 = new ItemStack(gem);
      gem2.setAmount(gem.getAmount() - 1);
      gem.setAmount(1);
      Utils.addItem(p, gem2);
    } 
    ItemStack result = new ItemStack(refineItem(new ItemStack(item)));
    this.gui.openSocketing(p, item, gem, result);
  }
  
  void playEffects(Player p, boolean success) {
    if (this.sound_use) {
      Sound s;
      if (success) {
        s = this.sound_suc_value;
      } else {
        s = this.sound_de_value;
      } 
      p.playSound(p.getLocation(), s, 0.8F, 0.8F);
    } 
    if (this.eff_use) {
      String eff;
      if (success) {
        eff = this.eff_suc_value;
      } else {
        eff = this.eff_de_value;
      } 
      Utils.playEffect(eff, p.getEyeLocation(), 0.3F, 0.0F, 0.3F, 0.3F, 45);
    } 
  }
  
  @EventHandler
  public void onInvClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack gem = e.getCursor();
    if (!isItemOfThisModule(gem))
      return; 
    ItemStack target = e.getCurrentItem();
    if (!canRefine(target))
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    String id = getItemId(gem);
    EnchStone gem3 = (EnchStone)getItemById(id, EnchStone.class);
    if (gem3 == null) {
      out((Entity)p, Lang.Other_Internal.toMsg());
      return;
    } 
    if (!gem3.isValidType(target)) {
      out((Entity)p, Lang.Refine_Enchanting_InvalidType.toMsg());
      return;
    } 
    if (!isInLevelRange(target, gem)) {
      out((Entity)p, Lang.Refine_Enchanting_BadLevel.toMsg());
      return;
    } 
    e.setCursor(null);
    openEnchantGUI(p, target, gem);
    e.setCancelled(true);
  }
  
  public class EnchStone extends SocketItem {
    public EnchStone(String id, String path, FileConfiguration cfg) {
      super(id, path, cfg, RefineManager.this.type());
    }
    
    protected ItemStack build(int lvl, int suc) {
      ItemStack item = super.build(lvl, suc);
      if (item.getType() == Material.AIR)
        return item; 
      ItemMeta meta = item.getItemMeta();
      List<String> lore = new ArrayList<>();
      if (meta.hasLore())
        lore = meta.getLore(); 
      String display = replacePlaceholders(RefineManager.this.i_name
          .replace("%item_name%", meta.getDisplayName()), lvl, suc);
      List<String> lore2 = new ArrayList<>();
      for (String s : RefineManager.this.i_lore) {
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
  
  public ItemStack extractSocket(ItemStack item, int i) {
    return downgradeItem(item);
  }
  
  public ItemStack insertSocket(ItemStack target, ItemStack src) {
    return refineItem(target);
  }
}
