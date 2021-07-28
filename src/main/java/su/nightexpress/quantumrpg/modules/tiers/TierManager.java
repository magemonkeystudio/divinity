package su.nightexpress.quantumrpg.modules.tiers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LeveledItem;
import su.nightexpress.quantumrpg.modules.QModuleLevel;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.modules.tiers.resources.ResourceSubType;
import su.nightexpress.quantumrpg.modules.tiers.resources.ResourceType;
import su.nightexpress.quantumrpg.modules.tiers.resources.Resources;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.AmmoType;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.BonusType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.types.WpnHand;
import su.nightexpress.quantumrpg.utils.Files;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.NBTUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class TierManager extends QModuleLevel {
  private Random r;
  
  public TierManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.TIERS;
  }
  
  public String name() {
    return "Tiers";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.r = Utils.r;
    this.plugin.getCM().extract("modules/" + getId() + "/tiers");
    Resources.clear();
    Resources.setup();
    setupTiers();
  }
  
  public void shutdown() {}
  
  private void updateTier(String file) {
    File f = new File(this.plugin.getDataFolder() + getPath() + "/tiers/", file);
    YamlConfiguration cfg = new YamlConfiguration();
    cfg = YamlConfiguration.loadConfiguration(f);
    if (!cfg.contains("item.hand-types")) {
      cfg.set("item.hand-types.ONE", Double.valueOf(80.0D));
      cfg.set("item.hand-types.TWO", Double.valueOf(20.0D));
    } 
    if (!cfg.contains("item.restrictions.min-item-stats"))
      cfg.set("item.restrictions.min-item-stats", Integer.valueOf(1)); 
    if (!cfg.contains("item.restrictions.min-damage-types"))
      cfg.set("item.restrictions.min-damage-types", Integer.valueOf(1)); 
    if (!cfg.contains("item.restrictions.max-damage-types"))
      cfg.set("item.restrictions.max-damage-types", Integer.valueOf(2)); 
    if (!cfg.contains("item.restrictions.min-defense-types"))
      cfg.set("item.restrictions.min-defense-types", Integer.valueOf(1)); 
    if (!cfg.contains("item.restrictions.max-defense-types"))
      cfg.set("item.restrictions.max-defense-types", Integer.valueOf(2)); 
    if (!cfg.isConfigurationSection("item.restrictions.material-modifiers")) {
      String path = "item.restrictions.material-modifiers.";
      cfg.set(String.valueOf(path) + "iron_sword.damage-types.physical", Double.valueOf(1.15D));
      cfg.set(String.valueOf(path) + "iron_helmet.defense-types.physical", Double.valueOf(1.25D));
      cfg.set(String.valueOf(path) + "axe.item-stats.CRITICAL_DAMAGE", Double.valueOf(1.5D));
    } 
    try {
      cfg.save(f);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  private void setupTiers() {
    for (String s : Files.getFilesFolder("tiers/tiers")) {
      updateTier(s);
      Tier t = loadFromConfig(s);
      this.items.put(t.getId(), t);
    } 
  }
  
  private Tier loadFromConfig(String file) {
    File f = new File(this.plugin.getDataFolder() + getPath() + "/tiers/", file);
    JYML cfg = new JYML(f);
    String t_id = file.replace(".yml", "");
    String t_name = cfg.getString("name");
    String t_color = ChatColor.translateAlternateColorCodes('&', cfg.getString("color"));
    boolean bc = cfg.getBoolean("broadcast-on-find");
    boolean equip = cfg.getBoolean("equip-on-mob-spawn");
    double pref_rate = cfg.getDouble("prefix-chance");
    double suf_rate = cfg.getDouble("suffix-chance");
    String path = "item.";
    String i_metaname = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
    List<String> i_lore = cfg.getStringList(String.valueOf(path) + "lore");
    for (int i = 0; i < i_lore.size(); i++) {
      String s = i_lore.get(i);
      i_lore.set(i, ChatColor.translateAlternateColorCodes('&', s.replace("%c%", t_color)));
    } 
    boolean c_rand = cfg.getBoolean(String.valueOf(path) + "color.random");
    String[] c_val = cfg.getString(String.valueOf(path) + "color.rgb").split(",");
    Color c_c = Color.fromRGB(Integer.parseInt(c_val[0]), Integer.parseInt(c_val[1]), Integer.parseInt(c_val[2]));
    boolean mat_reverse = cfg.getBoolean(String.valueOf(path) + "materials.reverse");
    List<String> materials = cfg.getStringList(String.valueOf(path) + "materials.black-list");
    Set<Material> materials2 = new HashSet<>(Resources.getAllMaterials());
    List<Material> mat_list = new ArrayList<>();
    if (!mat_reverse)
      mat_list = new ArrayList<>(materials2); 
    for (String s : materials) {
      for (Material material : materials2) {
        if ((s.contains("*") && (material.name().startsWith(s.replace("*", "")) || material.name().endsWith(s.replace("*", "")))) || (
          !s.contains("*") && material.name().equalsIgnoreCase(s))) {
          if (mat_reverse) {
            mat_list.add(material);
            continue;
          } 
          mat_list.remove(material);
        } 
      } 
    } 
    boolean data_reverse = cfg.getBoolean(String.valueOf(path) + "material-data.reverse");
    boolean data_unbreak = cfg.getBoolean(String.valueOf(path) + "material-data.unbreakable");
    List<Integer> data_list = cfg.getIntegerList(String.valueOf(path) + "material-data.black-list");
    HashMap<String, List<Integer>> data_spec = new HashMap<>();
    if (cfg.isConfigurationSection(String.valueOf(path) + "material-data.special"))
      for (String o3 : cfg.getConfigurationSection(String.valueOf(path) + "material-data.special").getKeys(false)) {
        String dtype = o3.toString();
        List<Integer> lis = cfg.getIntegerList(String.valueOf(path) + "material-data.special." + dtype);
        data_spec.put(dtype, lis);
      }  
    int ench_min = cfg.getInt(String.valueOf(path) + "enchants.min");
    int ench_max = cfg.getInt(String.valueOf(path) + "enchants.max");
    boolean ench_safe = cfg.getBoolean(String.valueOf(path) + "enchants.safe-only");
    HashMap<Enchantment, String> ench_list = new HashMap<>();
    for (String l : cfg.getStringList(String.valueOf(path) + "enchants.list")) {
      Enchantment en = Enchantment.getByName(l.split(":")[0]);
      if (en == null)
        continue; 
      String level = String.valueOf(l.split(":")[1]) + ":" + l.split(":")[2];
      ench_list.put(en, level);
    } 
    HashMap<DamageType, String> dtypes = new HashMap<>();
    for (String ss : cfg.getConfigurationSection(String.valueOf(path) + "damage-types").getKeys(false)) {
      DamageType dt = (DamageType)Config.getDamageTypes().get(ss.toLowerCase());
      if (dt != null) {
        double c = cfg.getDouble(String.valueOf(path) + "damage-types." + ss + ".chance");
        if (c <= 0.0D)
          continue; 
        double m1 = cfg.getDouble(String.valueOf(path) + "damage-types." + ss + ".min");
        double m2 = cfg.getDouble(String.valueOf(path) + "damage-types." + ss + ".max");
        String s = String.valueOf(c) + ":" + m1 + ":" + m2;
        dtypes.put(dt, s);
      } 
    } 
    HashMap<ArmorType, String> armtypes = new HashMap<>();
    for (String ss : cfg.getConfigurationSection(String.valueOf(path) + "armor-types").getKeys(false)) {
      ArmorType dt = Config.getArmorTypeById(ss);
      if (dt != null) {
        double c = cfg.getDouble(String.valueOf(path) + "armor-types." + ss + ".chance");
        if (c <= 0.0D)
          continue; 
        double m1 = cfg.getDouble(String.valueOf(path) + "armor-types." + ss + ".min");
        double m2 = cfg.getDouble(String.valueOf(path) + "armor-types." + ss + ".max");
        String s = String.valueOf(c) + ":" + m1 + ":" + m2;
        armtypes.put(dt, s);
      } 
    } 
    HashMap<AmmoType, Double> atypes = new HashMap<>();
    byte b1;
    int j;
    AmmoType[] arrayOfAmmoType;
    for (j = (arrayOfAmmoType = AmmoType.values()).length, b1 = 0; b1 < j; ) {
      AmmoType at = arrayOfAmmoType[b1];
      if (cfg.contains(String.valueOf(path) + "ammo-types." + at.name())) {
        double c = cfg.getDouble(String.valueOf(path) + "ammo-types." + at.name());
        if (c > 0.0D)
          atypes.put(at, Double.valueOf(c)); 
      } 
      b1++;
    } 
    atypes = (HashMap<AmmoType, Double>)Utils.sortByValue(atypes);
    HashMap<WpnHand, Double> htypes = new HashMap<>();
    WpnHand[] arrayOfWpnHand;
    for (int k = (arrayOfWpnHand = WpnHand.values()).length; j < k; ) {
      WpnHand at = arrayOfWpnHand[j];
      if (cfg.contains(String.valueOf(path) + "hand-types." + at.name())) {
        double c = cfg.getDouble(String.valueOf(path) + "hand-types." + at.name());
        if (c > 0.0D)
          htypes.put(at, Double.valueOf(c)); 
      } 
      j++;
    } 
    htypes = (HashMap<WpnHand, Double>)Utils.sortByValue(htypes);
    path = "item.restrictions.";
    boolean rest_soul = cfg.getBoolean(String.valueOf(path) + "soulbound");
    boolean rest_untrade = cfg.getBoolean(String.valueOf(path) + "untradeable");
    String rest_level = cfg.getString(String.valueOf(path) + "levels");
    double rest_level_scale = cfg.getDouble(String.valueOf(path) + "level-scale-values");
    List<String> rest_level_scale_black = cfg.getStringList(String.valueOf(path) + "level-scale-black-list");
    List<String> rest_class = cfg.getStringList(String.valueOf(path) + "classes");
    MaterialMod rest_matmod = new MaterialMod(cfg);
    int min_att = cfg.getInt(String.valueOf(path) + "min-item-stats");
    int max_att = cfg.getInt(String.valueOf(path) + "max-item-stats");
    int min_dmg = cfg.getInt(String.valueOf(path) + "min-damage-types");
    int max_dmg = cfg.getInt(String.valueOf(path) + "max-damage-types");
    int min_def = cfg.getInt(String.valueOf(path) + "min-defense-types");
    int max_def = cfg.getInt(String.valueOf(path) + "max-defense-types");
    HashMap<ItemStat, String> att = new HashMap<>();
    byte b2;
    int m;
    ItemStat[] arrayOfItemStat;
    for (m = (arrayOfItemStat = ItemStat.values()).length, b2 = 0; b2 < m; ) {
      ItemStat at = arrayOfItemStat[b2];
      path = "item.item-stats." + at.name();
      if (cfg.contains(path)) {
        double d_c = cfg.getDouble(String.valueOf(path) + ".default.chance");
        if (d_c > 0.0D) {
          double d_m1 = cfg.getDouble(String.valueOf(path) + ".default.min");
          double d_m2 = cfg.getDouble(String.valueOf(path) + ".default.max");
          String s = String.valueOf(d_c) + ":" + d_m1 + ":" + d_m2;
          att.put(at, s);
        } 
      } 
      b2++;
    } 
    HashMap<QSlotType, String> slots = new HashMap<>();
    QSlotType[] arrayOfQSlotType;
    for (int n = (arrayOfQSlotType = QSlotType.values()).length; m < n; ) {
      QSlotType dt = arrayOfQSlotType[m];
      path = "item.sockets." + dt.name();
      if (cfg.contains(path)) {
        double c = cfg.getDouble(String.valueOf(path) + ".chance");
        if (c > 0.0D) {
          int m1 = cfg.getInt(String.valueOf(path) + ".min");
          int m2 = cfg.getInt(String.valueOf(path) + ".max");
          String s = String.valueOf(c) + ":" + m1 + ":" + m2;
          slots.put(dt, s);
        } 
      } 
      m++;
    } 
    HashMap<ResourceType, List<String>> source = new HashMap<>();
    source.put(ResourceType.PREFIX, Resources.getSource(ResourceType.PREFIX, ResourceSubType.TIER, t_id));
    source.put(ResourceType.SUFFIX, Resources.getSource(ResourceType.SUFFIX, ResourceSubType.TIER, t_id));
    Tier tier = new Tier(
        t_id, 
        t_name, 
        t_color, 
        bc, 
        equip, 
        pref_rate, 
        suf_rate, 
        
        i_metaname, 
        i_lore, 
        
        c_rand, 
        c_c, 
        
        mat_reverse, 
        mat_list, 
        
        data_reverse, 
        data_unbreak, 
        data_list, 
        data_spec, 
        
        ench_min, 
        ench_max, 
        ench_safe, 
        ench_list, 
        
        dtypes, 
        armtypes, 
        atypes, 
        htypes, 
        
        rest_soul, 
        rest_untrade, 
        rest_level, 
        rest_level_scale, 
        rest_level_scale_black, 
        rest_class, 
        rest_matmod, 
        
        min_att, 
        max_att, 
        
        min_dmg, 
        max_dmg, 
        min_def, 
        max_def, 
        
        att, 
        
        slots, 
        
        source);
    return tier;
  }
  
  public ItemStack replaceDamageTypes(ItemStack item, Tier t1, double scale2) {
    int pos = item.getItemMeta().getLore().indexOf("%DAMAGE_TYPES%");
    if (pos < 0)
      return item; 
    int min = t1.getMinDamageTypes();
    int max = t1.getMaxDamageTypes();
    if (!ItemUtils.isWeapon(item) || t1.getDamageTypes().isEmpty() || max == 0)
      return replaceLore(item, "DAMAGE_TYPES", "delz"); 
    int roll = t1.getDamageTypes().size();
    if (min >= 0) {
      if (max < 0)
        max = roll; 
      roll = Utils.randInt(min, max);
    } 
    roll = Math.min(roll, t1.getDamageTypes().size());
    if (roll <= 0)
      return replaceLore(item, "DAMAGE_TYPES", "delz"); 
    Map<DamageType, Double> map = new HashMap<>();
    for (DamageType dt : t1.getDamageTypes().keySet()) {
      String s = t1.getDamageTypes().get(dt);
      double c = Double.parseDouble(s.split(":")[0]);
      map.put(dt, Double.valueOf(c));
    } 
    for (int i = 0; i < roll; i++) {
      DamageType dt = (DamageType)Utils.getRandomItem(map, true);
      double scale = scale2;
      double mod = t1.getMaterialModifiers().getModifier(item, BonusType.DAMAGE, dt.getId());
      String s = t1.getDamageTypes().get(dt);
      if (t1.getLevelScaleBlack().contains(dt.getId()))
        scale = 1.0D; 
      double m1 = Double.parseDouble(s.split(":")[1]) * scale * mod;
      double m2 = Double.parseDouble(s.split(":")[2]) * scale * mod;
      double val1 = Utils.round3(Utils.getRandDouble(m1, m2));
      double val2 = Utils.round3(Utils.getRandDouble(m1, m2));
      ItemAPI.addDamageType(item, dt, val1, val2, pos);
      map.remove(dt);
    } 
    item = replaceLore(item, "DAMAGE_TYPES", "delz");
    return item;
  }
  
  public ItemStack replaceArmorTypes(ItemStack item, Tier t1, double scale2) {
    int pos = item.getItemMeta().getLore().indexOf("%ARMOR_TYPES%");
    if (pos < 0)
      return item; 
    int min = t1.getMinDefenseTypes();
    int max = t1.getMaxDefenseTypes();
    if (!ItemUtils.isArmor(item) || t1.getArmorTypes().isEmpty() || max == 0)
      return replaceLore(item, "ARMOR_TYPES", "delz"); 
    int roll = t1.getArmorTypes().size();
    if (min >= 0) {
      if (max < 0)
        max = roll; 
      roll = Utils.randInt(min, max);
    } 
    roll = Math.min(roll, t1.getArmorTypes().size());
    if (roll <= 0)
      return replaceLore(item, "ARMOR_TYPES", "delz"); 
    Map<ArmorType, Double> map = new HashMap<>();
    for (ArmorType dt : t1.getArmorTypes().keySet()) {
      String s = t1.getArmorTypes().get(dt);
      double c = Double.parseDouble(s.split(":")[0]);
      map.put(dt, Double.valueOf(c));
    } 
    for (int i = 0; i < roll; i++) {
      ArmorType dt = (ArmorType)Utils.getRandomItem(map, true);
      double scale = scale2;
      double mod = t1.getMaterialModifiers().getModifier(item, BonusType.DEFENSE, dt.getId());
      String s = t1.getArmorTypes().get(dt);
      if (t1.getLevelScaleBlack().contains(dt.getId()))
        scale = 1.0D; 
      double m1 = Double.parseDouble(s.split(":")[1]) * scale * mod;
      double m2 = Double.parseDouble(s.split(":")[2]) * scale * mod;
      double val = Utils.round3(Utils.getRandDouble(m1, m2));
      ItemAPI.addDefenseType(item, dt, val, pos);
      map.remove(dt);
    } 
    item = replaceLore(item, "ARMOR_TYPES", "delz");
    return item;
  }
  
  public ItemStack replaceAmmoTypes(ItemStack item, Tier t1) {
    int pos = item.getItemMeta().getLore().indexOf("%AMMO_TYPE%");
    if (pos < 0)
      return item; 
    if (item.getType() != Material.BOW || t1.getDamageTypes().isEmpty())
      return replaceLore(item, "AMMO_TYPE", "delz"); 
    AmmoType at = (AmmoType)Utils.getRandomItem(t1.getAmmoTypes(), true);
    item = ItemAPI.setAmmoType(item, at, pos);
    item = replaceLore(item, "AMMO_TYPE", "delz");
    return item;
  }
  
  public ItemStack replaceHandTypes(ItemStack item, Tier t1) {
    int pos = item.getItemMeta().getLore().indexOf("%HAND_TYPE%");
    if (pos < 0)
      return item; 
    if (!ItemUtils.isWeapon(item) || t1.getHandTypes().isEmpty())
      return replaceLore(item, "HAND_TYPE", "delz"); 
    WpnHand at = (WpnHand)Utils.getRandomItem(t1.getHandTypes(), true);
    item = ItemAPI.setHandType(item, at, pos);
    item = replaceLore(item, "HAND_TYPE", "delz");
    return item;
  }
  
  public ItemStack replaceClass(ItemStack item, Tier t1) {
    if (Config.getClassPlugin() != null && !t1.getClasses().isEmpty()) {
      String[] item_c = t1.getClasses().<String>toArray(new String[t1.getClasses().size()]);
      String val = LoreUT.getStrSeparated(item_c);
      String lvl_str = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(val)).replace("%state%", Lang.Lore_State_false.toMsg());
      item = NBTUtils.setItemClass(item, item_c);
      return replaceLore(item, "CLASS", lvl_str);
    } 
    return replaceLore(item, "CLASS", "delz");
  }
  
  public ItemStack replaceUntrade(ItemStack item) {
    SoulboundManager s = (SoulboundManager)this.plugin.getMM().getModule(EModule.SOULBOUND);
    return replaceLore(item, "SOULBOUND", s.getUntradeString());
  }
  
  public ItemStack replaceSlots(ItemStack item, Tier t1) {
    for (QSlotType dt : t1.getSockets().keySet()) {
      String s = t1.getSockets().get(dt);
      double c = Double.parseDouble(s.split(":")[0]);
      int slots = 0;
      if (Utils.getRandDouble(0.0D, 100.0D) <= c) {
        int m1 = Integer.parseInt(s.split(":")[1]);
        int m2 = Integer.parseInt(s.split(":")[2]);
        slots = Utils.randInt(m1, m2);
      } 
      String head = "delz";
      if (slots > 0 && dt.getModule() != null && dt.getModule().isActive())
        head = dt.getHeader(); 
      int pos = item.getItemMeta().getLore().indexOf("%" + dt.name() + "%") + 1;
      if (pos != -1) {
        item = replaceLore(item, dt.name(), head);
        for (int j = 0; j < slots; j++)
          ItemAPI.addDivineSlot(item, dt, pos); 
      } 
    } 
    byte b;
    int i;
    QSlotType[] arrayOfQSlotType;
    for (i = (arrayOfQSlotType = QSlotType.values()).length, b = 0; b < i; ) {
      QSlotType dt = arrayOfQSlotType[b];
      item = replaceLore(item, dt.name(), "delz");
      b++;
    } 
    return item;
  }
  
  public ItemStack replaceStats(ItemStack item, Tier t1, double scale2) {
    Set<ItemStat> list = new HashSet<>(t1.getStats().keySet());
    int z1 = 0;
    for (ItemStat at : list) {
      double scale = scale2;
      if (t1.getLevelScaleBlack().contains(at.name()))
        scale = 1.0D; 
      String s = t1.getStats().get(at);
      if (((t1.getMaxAttributes() != 0 && z1 < t1.getMaxAttributes()) || t1.getMaxAttributes() == -1) && 
        !ItemAPI.hasAttribute(item, at)) {
        int pos = item.getItemMeta().getLore().indexOf("%" + at.name() + "%");
        if (pos < 0)
          continue; 
        double def_c = Double.parseDouble(s.split(":")[0]);
        if (Utils.getRandDouble(0.0D, 100.0D) <= def_c) {
          double def_m1 = Double.parseDouble(s.split(":")[1]) * scale;
          double def_m2 = Double.parseDouble(s.split(":")[2]) * scale;
          double val = Utils.round3(Utils.getRandDouble(def_m1, def_m2));
          item = ItemAPI.addItemStat(item, at, val, pos);
          if (t1.getMaxAttributes() > 0)
            z1++; 
        } 
      } 
    } 
    for (ItemStat at : list)
      item = replaceLore(item, at.name(), "delz"); 
    item = replaceLore(item, "BONUS_STATS", "delz");
    return item;
  }
  
  public ItemStack replaceStats2(ItemStack item, Tier t1, double scale2) {
    int min = t1.getMinAttributes();
    int max = t1.getMaxAttributes();
    if (max == 0) {
      byte b1;
      int j;
      ItemStat[] arrayOfItemStat1;
      for (j = (arrayOfItemStat1 = ItemStat.values()).length, b1 = 0; b1 < j; ) {
        ItemStat at = arrayOfItemStat1[b1];
        item = replaceLore(item, at.name(), "delz");
        b1++;
      } 
      return item;
    } 
    int roll = t1.getStats().size();
    if (min >= 0) {
      if (max < 0)
        max = roll; 
      roll = Utils.randInt(min, max);
    } 
    roll = Math.min(roll, t1.getStats().size());
    if (roll <= 0) {
      byte b1;
      int j;
      ItemStat[] arrayOfItemStat1;
      for (j = (arrayOfItemStat1 = ItemStat.values()).length, b1 = 0; b1 < j; ) {
        ItemStat at = arrayOfItemStat1[b1];
        item = replaceLore(item, at.name(), "delz");
        b1++;
      } 
      return item;
    } 
    Map<ItemStat, Double> map = new HashMap<>();
    for (ItemStat dt : t1.getStats().keySet()) {
      String s = t1.getStats().get(dt);
      double c = Double.parseDouble(s.split(":")[0]);
      map.put(dt, Double.valueOf(c));
    } 
    if (!map.isEmpty())
      for (int j = 0; j < roll && 
        !map.isEmpty(); j++) {
        ItemStat dt = (ItemStat)Utils.getRandomItem(map, true);
        int pos = item.getItemMeta().getLore().indexOf("%" + dt.name() + "%");
        if (pos < 0) {
          map.remove(dt);
        } else {
          double scale = scale2;
          double mod = t1.getMaterialModifiers().getModifier(item, BonusType.ITEM_STAT, dt.name());
          String s = t1.getStats().get(dt);
          if (t1.getLevelScaleBlack().contains(dt.name()))
            scale = 1.0D; 
          double m1 = Double.parseDouble(s.split(":")[1]) * scale * mod;
          double m2 = Double.parseDouble(s.split(":")[2]) * scale * mod;
          double val = Utils.round3(Utils.getRandDouble(m1, m2));
          item = ItemAPI.addItemStat(item, dt, val, pos);
          map.remove(dt);
          ItemStat fix = null;
          if (ItemUtils.isWeapon(item)) {
            if (ItemAPI.hasAttribute(item, ItemStat.CRITICAL_DAMAGE) && !ItemAPI.hasAttribute(item, ItemStat.CRITICAL_RATE)) {
              fix = ItemStat.CRITICAL_RATE;
            } else if (ItemAPI.hasAttribute(item, ItemStat.CRITICAL_RATE) && !ItemAPI.hasAttribute(item, ItemStat.CRITICAL_DAMAGE)) {
              fix = ItemStat.CRITICAL_DAMAGE;
            } 
          } else if (ItemUtils.isArmor(item)) {
            if (ItemAPI.hasAttribute(item, ItemStat.BLOCK_DAMAGE) && !ItemAPI.hasAttribute(item, ItemStat.BLOCK_RATE)) {
              fix = ItemStat.BLOCK_RATE;
            } else if (ItemAPI.hasAttribute(item, ItemStat.BLOCK_RATE) && !ItemAPI.hasAttribute(item, ItemStat.BLOCK_DAMAGE)) {
              fix = ItemStat.BLOCK_DAMAGE;
            } 
          } 
          if (fix != null && t1.getStats().containsKey(fix)) {
            String s2 = t1.getStats().get(fix);
            int pos2 = item.getItemMeta().getLore().indexOf("%" + fix.name() + "%");
            double def_m1 = Double.parseDouble(s2.split("!")[0].split(":")[1]) * scale;
            double def_m2 = Double.parseDouble(s2.split("!")[0].split(":")[2]) * scale;
            double val2 = Utils.round3(Utils.getRandDouble(def_m1, def_m2));
            item = ItemAPI.addItemStat(item, fix, val2, pos2);
          } 
        } 
      }  
    byte b;
    int i;
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat at = arrayOfItemStat[b];
      item = replaceLore(item, at.name(), "delz");
      b++;
    } 
    item = replaceLore(item, "BONUS_STATS", "delz");
    return item;
  }
  
  public ItemStack replaceLore(ItemStack item, String type, String replacer) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      return item; 
    String newt = "%" + type.toUpperCase() + "%";
    int pos = 0;
    for (String s : lore) {
      if (s.contains(newt)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        if (!replacer.equals("delz") && !replacer.equals(""))
          lore.add(pos, s.replace(newt, replacer)); 
        break;
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }
  
  class MaterialMod {
    private Map<String, Map<BonusType, Map<String, Double>>> mods = new HashMap<>();
    
    public MaterialMod(JYML cfg) {
      for (String type : cfg.getSection("item.restrictions.material-modifiers")) {
        Map<BonusType, Map<String, Double>> map_bonus = new HashMap<>();
        String path = "item.restrictions.material-modifiers." + type + ".";
        if (!ItemUtils.isValidItemGroup(type)) {
          LogUtil.send("Invalid item group &f" + type + " &7for &f'material-modifiers' &7in &f" + cfg.getFileName(), LogType.ERROR);
          continue;
        } 
        Map<String, Double> map_dmg = new HashMap<>();
        for (String dmg : cfg.getSection(String.valueOf(path) + "damage-types")) {
          if (Config.getDamageTypeById(dmg) == null) {
            LogUtil.send("Invalid damage type &f" + dmg + " &7for &f'material-modifiers' &7in &f" + cfg.getFileName(), LogType.ERROR);
            continue;
          } 
          String path2 = String.valueOf(path) + "damage-types." + dmg;
          double mod = cfg.getDouble(path2);
          map_dmg.put(dmg.toLowerCase(), Double.valueOf(mod));
        } 
        Map<String, Double> map_def = new HashMap<>();
        for (String def : cfg.getSection(String.valueOf(path) + "defense-types")) {
          if (Config.getArmorTypeById(def) == null) {
            LogUtil.send("Invalid defense type &f" + def + " &7for &f'material-modifiers' &7in &f" + cfg.getFileName(), LogType.ERROR);
            continue;
          } 
          String path2 = String.valueOf(path) + "defense-types." + def;
          double mod = cfg.getDouble(path2);
          map_def.put(def.toLowerCase(), Double.valueOf(mod));
        } 
        Map<String, Double> map_stat = new HashMap<>();
        for (String stat : cfg.getSection(String.valueOf(path) + "item-stats")) {
          String path2 = String.valueOf(path) + "item-stats." + stat;
          double mod = cfg.getDouble(path2);
          map_stat.put(stat.toLowerCase(), Double.valueOf(mod));
        } 
        map_bonus.put(BonusType.DAMAGE, map_dmg);
        map_bonus.put(BonusType.DEFENSE, map_def);
        map_bonus.put(BonusType.ITEM_STAT, map_stat);
        this.mods.put(type, map_bonus);
      } 
    }
    
    public double getModifier(ItemStack item, BonusType type, String value) {
      if (this.mods.isEmpty())
        return 1.0D; 
      String group = "";
      String mat = item.getType().name().toLowerCase();
      ItemSubType ist = Config.getItemSubType(item);
      ItemGroup ig = ItemGroup.getItemGroup(item);
      if (this.mods.containsKey(mat)) {
        group = mat;
      } else if (ist != null && this.mods.containsKey(ist.getId())) {
        group = ist.getId();
      } else if (ig != null && this.mods.containsKey(ig.name().toLowerCase())) {
        group = ig.name().toLowerCase();
      } else {
        return 1.0D;
      } 
      Map<BonusType, Map<String, Double>> map2 = this.mods.get(group);
      if (map2.containsKey(type)) {
        Map<String, Double> map3 = map2.get(type);
        value = value.toLowerCase();
        if (map3.containsKey(value))
          return ((Double)map3.get(value)).doubleValue(); 
      } 
      return 1.0D;
    }
  }
  
  public class Tier extends LeveledItem {
    private String t_name;
    
    private String t_color;
    
    private boolean bc;
    
    private boolean equip;
    
    private double pref_rate;
    
    private double suf_rate;
    
    private boolean i_c_rand;
    
    private Color i_c_value;
    
    private boolean mat_reverse;
    
    private List<Material> mat_list;
    
    private boolean data_reverse;
    
    private boolean data_unbreak;
    
    private List<Integer> data_list;
    
    private HashMap<String, List<Integer>> data_spec;
    
    private int ench_min;
    
    private int ench_max;
    
    private boolean ench_safe;
    
    private HashMap<Enchantment, String> ench_list;
    
    private HashMap<DamageType, String> dtypes;
    
    private HashMap<ArmorType, String> armtypes;
    
    private HashMap<AmmoType, Double> atypes;
    
    private HashMap<WpnHand, Double> htypes;
    
    private boolean rest_soul;
    
    private boolean rest_untrade;
    
    private String rest_level;
    
    private double rest_level_scale;
    
    private List<String> rest_level_scale_black;
    
    private List<String> rest_class;
    
    private TierManager.MaterialMod rest_matmod;
    
    private int min_att;
    
    private int max_att;
    
    private int min_dmg;
    
    private int max_dmg;
    
    private int min_def;
    
    private int max_def;
    
    private HashMap<ItemStat, String> att;
    
    private HashMap<QSlotType, String> slots;
    
    private HashMap<ResourceType, List<String>> source;
    
    public Tier(String t_id, String t_name, String t_color, boolean bc, boolean equip, double pref_rate, double suf_rate, String i_metaname, List<String> i_lore, boolean i_c_rand, Color i_c_value, boolean mat_reverse, List<Material> mat_list, boolean data_reverse, boolean data_unbreak, List<Integer> data_list, HashMap<String, List<Integer>> data_spec, int ench_min, int ench_max, boolean ench_safe, HashMap<Enchantment, String> ench_list, HashMap<DamageType, String> dtypes, HashMap<ArmorType, String> armtypes, HashMap<AmmoType, Double> atypes, HashMap<WpnHand, Double> htypes, boolean rest_soul, boolean rest_untrade, String rest_level, double rest_level_scale, List<String> rest_level_scale_black, List<String> rest_class, TierManager.MaterialMod rest_matmod, int min_att, int max_att, int min_dmg, int max_dmg, int min_def, int max_def, HashMap<ItemStat, String> att, HashMap<QSlotType, String> slots, HashMap<ResourceType, List<String>> source) {
      super(t_id, i_metaname, i_lore, TierManager.this.type());
      setTierName(t_name);
      setColor(t_color);
      setBroadcast(bc);
      setEquipOnEntity(equip);
      this.pref_rate = pref_rate;
      this.suf_rate = suf_rate;
      setRandomLeatherColor(i_c_rand);
      setLeatherColor(i_c_value);
      setMaterialReversed(mat_reverse);
      setMaterials(mat_list);
      setDataReversed(data_reverse);
      setDataUnbreak(data_unbreak);
      setDatas(data_list);
      setDataSpecial(data_spec);
      setMinEnchantments(ench_min);
      setMaxEnchantments(ench_max);
      setSafeEnchant(ench_safe);
      setEnchantments(ench_list);
      setDamageTypes(dtypes);
      setArmorTypes(armtypes);
      setAmmoTypes(atypes);
      setHandTypes(htypes);
      setNeedSoul(rest_soul);
      setNonTrade(rest_untrade);
      setLevels(rest_level);
      setLevelScale(rest_level_scale);
      setLevelScaleBlack(rest_level_scale_black);
      setClasses(rest_class);
      this.rest_matmod = rest_matmod;
      this.min_att = min_att;
      this.max_att = max_att;
      this.min_dmg = min_dmg;
      this.max_dmg = max_dmg;
      this.min_def = min_def;
      this.max_def = max_def;
      setStats(att);
      setSockets(slots);
      setSource(source);
    }
    
    public String getTierName() {
      return this.t_name;
    }
    
    public void setTierName(String t_name) {
      this.t_name = t_name;
    }
    
    public String getColor() {
      return this.t_color;
    }
    
    public void setColor(String t_color) {
      this.t_color = t_color;
    }
    
    public boolean isBroadcast() {
      return this.bc;
    }
    
    public void setBroadcast(boolean bc) {
      this.bc = bc;
    }
    
    public boolean isEquipOnEntity() {
      return this.equip;
    }
    
    public void setEquipOnEntity(boolean equip) {
      this.equip = equip;
    }
    
    public double getPrefixChance() {
      return this.pref_rate;
    }
    
    public double getSuffixChance() {
      return this.suf_rate;
    }
    
    public boolean isRandomLeatherColor() {
      return this.i_c_rand;
    }
    
    public void setRandomLeatherColor(boolean i_c_rand) {
      this.i_c_rand = i_c_rand;
    }
    
    public Color getLeatherColor() {
      return this.i_c_value;
    }
    
    public void setLeatherColor(Color i_c_value) {
      this.i_c_value = i_c_value;
    }
    
    public boolean isMaterialReversed() {
      return this.mat_reverse;
    }
    
    public void setMaterialReversed(boolean mat_reverse) {
      this.mat_reverse = mat_reverse;
    }
    
    public List<Material> getMaterials() {
      return this.mat_list;
    }
    
    public void setMaterials(List<Material> mat_list) {
      this.mat_list = mat_list;
    }
    
    public boolean isDataReversed() {
      return this.data_reverse;
    }
    
    public void setDataReversed(boolean data_reverse) {
      this.data_reverse = data_reverse;
    }
    
    public boolean isDataUnbreak() {
      return this.data_unbreak;
    }
    
    public void setDataUnbreak(boolean data_unbreak) {
      this.data_unbreak = data_unbreak;
    }
    
    public List<Integer> getDatas() {
      return this.data_list;
    }
    
    public void setDatas(List<Integer> data_list) {
      this.data_list = data_list;
    }
    
    public HashMap<String, List<Integer>> getDataSpecial() {
      return this.data_spec;
    }
    
    public void setDataSpecial(HashMap<String, List<Integer>> data_spec) {
      this.data_spec = data_spec;
    }
    
    public int getMinEnchantments() {
      return this.ench_min;
    }
    
    public void setMinEnchantments(int ench_min) {
      this.ench_min = ench_min;
    }
    
    public int getMaxEnchantments() {
      return this.ench_max;
    }
    
    public void setMaxEnchantments(int ench_max) {
      this.ench_max = ench_max;
    }
    
    public boolean isSafeEnchant() {
      return this.ench_safe;
    }
    
    public void setSafeEnchant(boolean ench_safe) {
      this.ench_safe = ench_safe;
    }
    
    public HashMap<Enchantment, String> getEnchantments() {
      return this.ench_list;
    }
    
    public void setEnchantments(HashMap<Enchantment, String> ench_list) {
      this.ench_list = ench_list;
    }
    
    public HashMap<DamageType, String> getDamageTypes() {
      return this.dtypes;
    }
    
    public void setDamageTypes(HashMap<DamageType, String> dtypes) {
      this.dtypes = dtypes;
    }
    
    public HashMap<ArmorType, String> getArmorTypes() {
      return this.armtypes;
    }
    
    public void setArmorTypes(HashMap<ArmorType, String> armtypes) {
      this.armtypes = armtypes;
    }
    
    public HashMap<AmmoType, Double> getAmmoTypes() {
      return this.atypes;
    }
    
    public void setAmmoTypes(HashMap<AmmoType, Double> atypes) {
      this.atypes = atypes;
    }
    
    public HashMap<WpnHand, Double> getHandTypes() {
      return this.htypes;
    }
    
    public void setHandTypes(HashMap<WpnHand, Double> htypes) {
      this.htypes = htypes;
    }
    
    public boolean isNeedSoul() {
      return this.rest_soul;
    }
    
    public void setNeedSoul(boolean rest_soul) {
      this.rest_soul = rest_soul;
    }
    
    public boolean isNonTrade() {
      return this.rest_untrade;
    }
    
    public void setNonTrade(boolean rest_untrade) {
      this.rest_untrade = rest_untrade;
    }
    
    public String getLevels() {
      return this.rest_level;
    }
    
    public void setLevels(String rest_level) {
      this.rest_level = rest_level;
    }
    
    public double getLevelScale() {
      return this.rest_level_scale;
    }
    
    public void setLevelScale(double rest_level_scale) {
      this.rest_level_scale = rest_level_scale;
    }
    
    public List<String> getLevelScaleBlack() {
      return this.rest_level_scale_black;
    }
    
    public void setLevelScaleBlack(List<String> rest_level_scale_black) {
      this.rest_level_scale_black = rest_level_scale_black;
    }
    
    public List<String> getClasses() {
      return this.rest_class;
    }
    
    public void setClasses(List<String> rest_class) {
      this.rest_class = rest_class;
    }
    
    public TierManager.MaterialMod getMaterialModifiers() {
      return this.rest_matmod;
    }
    
    public int getMinAttributes() {
      return this.min_att;
    }
    
    public int getMaxAttributes() {
      return this.max_att;
    }
    
    public int getMinDamageTypes() {
      return this.min_dmg;
    }
    
    public int getMaxDamageTypes() {
      return this.max_dmg;
    }
    
    public int getMinDefenseTypes() {
      return this.min_def;
    }
    
    public int getMaxDefenseTypes() {
      return this.max_def;
    }
    
    public HashMap<ItemStat, String> getStats() {
      return this.att;
    }
    
    public void setStats(HashMap<ItemStat, String> att) {
      this.att = att;
    }
    
    public HashMap<QSlotType, String> getSockets() {
      return this.slots;
    }
    
    public void setSockets(HashMap<QSlotType, String> slots) {
      this.slots = slots;
    }
    
    public HashMap<ResourceType, List<String>> getSource() {
      return this.source;
    }
    
    public void setSource(HashMap<ResourceType, List<String>> source) {
      this.source = source;
    }
    
    public ItemStack create() {
      return create(-1, (Material)null);
    }
    
    public ItemStack create(int lvl) {
      return create(lvl, (Material)null);
    }
    
    public ItemStack create(int lvl, Material mat) {
      ItemStack item = new ItemStack(Material.STONE_SWORD);
      if (getMaterials().size() <= 0)
        return item; 
      if (mat != null && getMaterials().contains(mat)) {
        item.setType(mat);
      } else {
        item.setType(getMaterials().get(TierManager.this.r.nextInt(getMaterials().size())));
      } 
      ItemMeta meta = item.getItemMeta();
      List<Integer> dlist = getDatas();
      if (getDataSpecial().containsKey(item.getType().name()))
        dlist = getDataSpecial().get(item.getType().name()); 
      if (!dlist.isEmpty()) {
        if (isDataUnbreak())
          meta.spigot().setUnbreakable(true); 
        if (isDataReversed()) {
          item.setDurability((short)((Integer)dlist.get(TierManager.this.r.nextInt(dlist.size()))).intValue());
        } else {
          int data = Utils.randInt(1, item.getType().getMaxDurability());
          while (dlist.contains(Integer.valueOf(data)))
            data = Utils.randInt(1, item.getType().getMaxDurability()); 
          item.setDurability((short)data);
        } 
      } 
      meta.setLore(getLore());
      String item_type = item.getType().name();
      String rand_pref = "";
      String rand_suf = "";
      String rand_type = "";
      String rand_pre_mat = "";
      String rand_suf_mat = "";
      String rand_pre_type = "";
      String rand_suf_type = "";
      if (this.pref_rate >= Utils.getRandDouble(0.0D, 100.0D)) {
        List<String> tier_pref = getSource().get(ResourceType.PREFIX);
        if (!tier_pref.isEmpty())
          rand_pref = tier_pref.get(TierManager.this.r.nextInt(tier_pref.size())); 
        List<String> mat1 = Resources.getSourceByMaterial(ResourceType.PREFIX, item_type);
        if (!mat1.isEmpty())
          rand_pre_mat = mat1.get(TierManager.this.r.nextInt(mat1.size())); 
        List<String> types1 = Resources.getSourceBySubType(ResourceType.PREFIX, item_type);
        if (types1 != null && !types1.isEmpty())
          rand_pre_type = types1.get(TierManager.this.r.nextInt(types1.size())); 
      } 
      if (this.suf_rate >= Utils.getRandDouble(0.0D, 100.0D)) {
        List<String> tier_suf = getSource().get(ResourceType.SUFFIX);
        if (!tier_suf.isEmpty())
          rand_suf = tier_suf.get(TierManager.this.r.nextInt(tier_suf.size())); 
        List<String> mat2 = Resources.getSourceByMaterial(ResourceType.SUFFIX, item_type);
        if (mat2.size() > 0)
          rand_suf_mat = mat2.get(TierManager.this.r.nextInt(mat2.size())); 
        List<String> types2 = Resources.getSourceBySubType(ResourceType.SUFFIX, item_type);
        if (types2 != null && types2.size() > 0)
          rand_suf_type = types2.get(TierManager.this.r.nextInt(types2.size())); 
      } 
      rand_type = ItemUtils.getItemGroupName(item.getType().name());
      String names = getName()
        .replace("%itemtype%", rand_type)
        .replace("%suffix_tier%", rand_suf)
        .replace("%prefix_tier%", rand_pref)
        
        .replace("%prefix_type%", rand_pre_type)
        .replace("%suffix_type%", rand_suf_type)
        
        .replace("%prefix_material%", rand_pre_mat)
        .replace("%suffix_material%", rand_suf_mat)
        
        .replace("%c%", "");
      names = names.trim().replaceAll("\\s+", " ");
      names = String.valueOf(getColor()) + names;
      meta.setDisplayName(names);
      meta.addItemFlags(ItemFlag.values());
      item.setItemMeta(meta);
      if (item.getType().name().startsWith("LEATHER_")) {
        LeatherArmorMeta lam = (LeatherArmorMeta)item.getItemMeta();
        if (isRandomLeatherColor()) {
          lam.setColor(Color.fromRGB(TierManager.this.r.nextInt(255), TierManager.this.r.nextInt(255), TierManager.this.r.nextInt(255)));
        } else {
          lam.setColor(getLeatherColor());
        } 
        item.setItemMeta((ItemMeta)lam);
      } else if (item.getType() == Material.SHIELD) {
        meta = item.getItemMeta();
        BlockStateMeta bmeta = (BlockStateMeta)meta;
        if (bmeta != null && bmeta.hasBlockState() && bmeta.getBlockState() != null) {
          Banner banner = (Banner)bmeta.getBlockState();
          DyeColor[] c1 = DyeColor.values();
          DyeColor cc1 = c1[TierManager.this.r.nextInt(c1.length - 1)];
          banner.setBaseColor(cc1);
          PatternType[] type = PatternType.values();
          PatternType pp = type[TierManager.this.r.nextInt(type.length - 1)];
          DyeColor[] c2 = DyeColor.values();
          DyeColor cc2 = c2[TierManager.this.r.nextInt(c2.length - 1)];
          banner.addPattern(new Pattern(cc2, pp));
          banner.update();
          bmeta.setBlockState((BlockState)banner);
          item.setItemMeta((ItemMeta)bmeta);
        } 
      } 
      String[] s_lvl = getLevels().split("-");
      int ml = 1;
      int mx = 1;
      try {
        ml = Integer.parseInt(s_lvl[0]);
        mx = Integer.parseInt(s_lvl[1]);
      } catch (NumberFormatException|ArrayIndexOutOfBoundsException numberFormatException) {}
      if (lvl > 0 && lvl > mx)
        lvl = mx; 
      if (lvl > 0 && lvl < ml)
        lvl = ml; 
      if (lvl <= 0)
        lvl = TierManager.this.r.nextInt(mx - ml + 1) + ml; 
      double scale = (getLevelScale() * 100.0D - 100.0D) * lvl / 100.0D + 1.0D;
      int min_e = getMinEnchantments();
      int max_e = getMaxEnchantments();
      if (min_e >= 0 && max_e >= 0) {
        int total = TierManager.this.r.nextInt(max_e - min_e + 1) + min_e;
        List<Enchantment> list_e = new ArrayList<>();
        for (Enchantment e : getEnchantments().keySet())
          list_e.add(e); 
        for (int i = 0; i < total && 
          !list_e.isEmpty(); i++) {
          Enchantment ee = list_e.get(TierManager.this.r.nextInt(list_e.size()));
          double min_lvl = Math.max(1, Integer.parseInt(((String)getEnchantments().get(ee)).split(":")[0]));
          double max_lvl = Math.max(1, Integer.parseInt(((String)getEnchantments().get(ee)).split(":")[1]));
          int total_lvl = Utils.randInt((int)min_lvl, (int)max_lvl);
          if (isSafeEnchant()) {
            if (ee.canEnchantItem(item) && Utils.checkEnchantConflict(item, ee)) {
              item.addUnsafeEnchantment(ee, total_lvl);
            } else {
              i--;
            } 
          } else {
            item.addUnsafeEnchantment(ee, total_lvl);
          } 
          list_e.remove(ee);
        } 
      } 
      item = TierManager.this.replaceDamageTypes(item, this, scale);
      item = TierManager.this.replaceArmorTypes(item, this, scale);
      item = TierManager.this.replaceAmmoTypes(item, this);
      item = TierManager.this.replaceHandTypes(item, this);
      item = ItemUtils.replaceLevel(item, lvl);
      item = TierManager.this.replaceClass(item, this);
      item = ItemUtils.replaceEnchants(item);
      if (EModule.SOULBOUND.isEnabled()) {
        boolean s1 = isNeedSoul();
        boolean s2 = isNonTrade();
        if (s1 && s2) {
          s1 = false;
          s2 = false;
        } 
        if (s1) {
          item = new ItemStack(ItemUtils.replaceSoul(item));
        } else if (s2) {
          item = new ItemStack(TierManager.this.replaceUntrade(item));
        } else {
          item = new ItemStack(TierManager.this.replaceLore(item, "SOULBOUND", "delz"));
        } 
      } else {
        item = new ItemStack(TierManager.this.replaceLore(item, "SOULBOUND", "delz"));
      } 
      item = new ItemStack(TierManager.this.replaceStats2(item, this, scale));
      item = new ItemStack(TierManager.this.replaceSlots(item, this));
      item = ItemUtils.replaceSet(item);
      item = TierManager.this.replaceLore(item, "TYPE", rand_type);
      item = TierManager.this.replaceLore(item, "TIER", getTierName());
      item = TierManager.this.replaceLore(item, "MATERIAL", TierManager.this.plugin.getCM().getDefaultItemName(item));
      item = setItemId(item, this.id);
      item = setModule(item);
      item = TierManager.this.plugin.getNMS().fixNBT(item);
      return item;
    }
  }
}
