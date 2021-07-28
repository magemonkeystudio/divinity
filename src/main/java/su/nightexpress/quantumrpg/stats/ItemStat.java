package su.nightexpress.quantumrpg.stats;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.utils.ItemUtils;

public enum ItemStat {
  DIRECT_DAMAGE("Direct Damage", "&f▸", "&f", 100.0D, ItemType.WEAPON, true, true),
  AOE_DAMAGE("AoE Damage", "&3▸", "&f", -1.0D, ItemType.WEAPON, true, true),
  PVP_DAMAGE("PvP Damage", "&b▸", "&f", -1.0D, ItemType.WEAPON, true, true),
  PVE_DAMAGE("PvE Damage", "&b▸", "&f", -1.0D, ItemType.WEAPON, true, true),
  DODGE_RATE("Dodge Rate", "&6▸", "&f", -1.0D, ItemType.ARMOR, true, true),
  ACCURACY_RATE("Accuracy Rate", "&6▸", "&f", -1.0D, ItemType.WEAPON, true, true),
  BLOCK_RATE("Block Rate", "&6▸", "&f", -1.0D, ItemType.ARMOR, true, true),
  BLOCK_DAMAGE("Block Damage", "&6▸", "&f", -1.0D, ItemType.ARMOR, true, true),
  LOOT_RATE("Loot Rate", "&e▸", "&f", 250.0D, ItemType.BOTH, true, true),
  BURN_RATE("Chance to Burn", "&c▸", "&f", 100.0D, ItemType.WEAPON, true, true),
  PVP_DEFENSE("PvP Defense", "&b▸", "&f", -1.0D, ItemType.ARMOR, true, true),
  PVE_DEFENSE("PvE Defense", "&b▸", "&f", -1.0D, ItemType.ARMOR, true, true),
  CRITICAL_RATE("Critical Rate", "&e▸", "&f", 75.0D, ItemType.WEAPON, true, true),
  CRITICAL_DAMAGE("Critical Damage", "&e▸", "&f", 3.0D, ItemType.WEAPON, false, false),
  DURABILITY("Durability", "&f▸", "&f", -1.0D, ItemType.BOTH, false, false),
  MOVEMENT_SPEED("Movement speed", "&3▸", "&f", -1.0D, ItemType.BOTH, true, true),
  PENETRATION("Armor Penetration", "&c▸", "&f", 45.0D, ItemType.WEAPON, true, true),
  ATTACK_SPEED("Attack Speed", "&3▸", "&f", -1.0D, ItemType.WEAPON, true, true),
  VAMPIRISM("Vampirism", "&c▸", "&f", 45.0D, ItemType.WEAPON, true, true),
  MAX_HEALTH("Max Health", "&3▸", "&f", 30.0D, ItemType.BOTH, false, true),
  BLEED_RATE("Chance to Open Wounds", "&c▸", "&f", 75.0D, ItemType.WEAPON, true, true),
  DISARM_RATE("Chance to Disarm", "&c▸", "&f", 25.0D, ItemType.WEAPON, true, true),
  RANGE("Range", "&6▸", "&f", 7.0D, ItemType.WEAPON, false, false),
  SALE_PRICE("Sale Price", "&6▸", "&f", -1.0D, ItemType.BOTH, true, true),
  THORNMAIL("Thornmail", "&c▸", "&f", 35.0D, ItemType.ARMOR, true, true),
  HEALTH_REGEN("Health Regen", "&c▸", "&f", -1.0D, ItemType.BOTH, true, true),
  MANA_REGEN("Mana Regen", "&9▸", "&f", -1.0D, ItemType.BOTH, true, true);
  
  private String name;
  
  private String prefix;
  
  private String value;
  
  private double cap;
  
  private ItemType type;
  
  private boolean perc;
  
  private boolean plus;
  
  private StatSettings settings;
  
  private double cost;
  
  private String format;
  
  ItemStat(String name, String prefix, String value, double cap, ItemType type, boolean perc, boolean plus) {
    this.name = name;
    this.prefix = prefix;
    this.value = value;
    this.cap = cap;
    this.type = type;
    this.perc = perc;
    this.plus = plus;
    this.cost = 0.0D;
    setFormat("%att_prefix% %att_name%: %att_value%");
  }
  
  public String getName() {
    return ChatColor.translateAlternateColorCodes('&', this.name);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getPrefix() {
    return ChatColor.translateAlternateColorCodes('&', this.prefix);
  }
  
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
  
  public String getValue() {
    return ChatColor.translateAlternateColorCodes('&', this.value);
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public double getCapability() {
    return this.cap;
  }
  
  public void setCapability(double cap) {
    this.cap = cap;
  }
  
  public ItemType getMainType() {
    return this.type;
  }
  
  public void setMainType(ItemType type) {
    this.type = type;
  }
  
  public boolean isPercent() {
    return this.perc;
  }
  
  public boolean isPlus() {
    return this.plus;
  }
  
  public boolean isMainItem(ItemStack item) {
    if (isPercent() || this.type == ItemType.BOTH)
      return true; 
    if (this.type == ItemType.ARMOR && ItemUtils.isArmor(item))
      return true; 
    if (this.type == ItemType.WEAPON && ItemUtils.isWeapon(item))
      return true; 
    return false;
  }
  
  public double fineValue(double val) {
    if (this == DURABILITY)
      return val; 
    if (this.cap >= 0.0D && val > this.cap)
      val = this.cap; 
    if (val < 0.0D && !this.plus)
      val = 0.0D; 
    return val;
  }
  
  public enum ItemType {
    ARMOR, WEAPON, BOTH;
  }
  
  public StatSettings getSettings() {
    return this.settings;
  }
  
  public void setSettings(StatSettings settings) {
    this.settings = settings;
  }
  
  public double getCost() {
    return this.cost;
  }
  
  public void setCost(double cost) {
    this.cost = cost;
  }
  
  public void setFormat(String format) {
    this.format = format
      .replace("%att_value%", getValue())
      .replace("%att_name%", getName())
      .replace("%att_prefix%", getPrefix());
  }
  
  public String getFormat() {
    return this.format;
  }
}
