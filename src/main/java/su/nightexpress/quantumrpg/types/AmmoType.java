package su.nightexpress.quantumrpg.types;

public enum AmmoType {
  ARROW(true, "&f➶", "Arrow"),
  SNOWBALL(true, "&9❆&f", "Snowball"),
  EGG(true, "&c⚫&f", "Egg"),
  FIREBALL(true, "&c☄&f", "Fireball"),
  WITHER_SKULL(true, "&8☢&f", "Wither Skull"),
  SHULKER_BULLET(true, "&d✦&f", "Shulker Bullet"),
  LLAMA_SPIT(true, "&e☔&f", "Llama Spit"),
  ENDER_PEARL(true, "&b◉", "Ender Peral"),
  EXP_POTION(true, "&e☘", "Exp Potion");
  
  private boolean enabled;
  
  private String prefix;
  
  private String name;
  
  private String format;
  
  AmmoType(boolean enabled, String prefix, String name) {
    setEnabled(enabled);
    setPrefix(prefix);
    setName(name);
    setFormat("&7Ammo Type: %type_prefix% %type_name%");
  }
  
  public String getPrefix() {
    return this.prefix;
  }
  
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isEnabled() {
    return this.enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public void setFormat(String format) {
    this.format = format
      .replace("%type_name%", getName())
      .replace("%type_prefix%", getPrefix());
  }
  
  public String getFormat() {
    return this.format;
  }
}
