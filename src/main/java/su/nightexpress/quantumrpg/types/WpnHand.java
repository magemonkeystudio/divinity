package su.nightexpress.quantumrpg.types;

import org.bukkit.ChatColor;

public enum WpnHand {
  ONE("&fOne-handed"),
  TWO("&eTwo-handed");
  
  private boolean e;
  
  private String name;
  
  private String format;
  
  WpnHand(String name) {
    setName(name);
    setFormat("&7Hand: %type_name%");
  }
  
  public void setEnabled(boolean e) {
    this.e = e;
  }
  
  public boolean isEnabled() {
    return this.e;
  }
  
  public void setName(String name) {
    this.name = ChatColor.translateAlternateColorCodes('&', name);
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setFormat(String format) {
    this.format = ChatColor.translateAlternateColorCodes('&', format.replace("%type_name%", this.name));
  }
  
  public String getFormat() {
    return this.format;
  }
}
