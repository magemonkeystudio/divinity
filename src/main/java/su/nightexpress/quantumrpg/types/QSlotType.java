package su.nightexpress.quantumrpg.types;

import net.md_5.bungee.api.ChatColor;
import su.nightexpress.quantumrpg.modules.QModuleSocket;

public enum QSlotType {
  GEM(null, "&2ᚐᚑᚒᚓᚔᚍᚎᚏ &a&lGems &2ᚏᚎᚍᚔᚓᚒᚑᚐ", "&a□ (Slot) Gem", "&a▣ Gem: &f"),
  RUNE(null, "&3ᚐᚑᚒᚓᚔᚍᚎᚏ &b&lRunes &3ᚏᚎᚍᚔᚓᚒᚑᚐ", "&b◇ (Slot) Rune", "&b◈ Rune: &f"),
  ESSENCE(null, "&4ᚐᚑᚒᚓᚔᚍᚎᚏ &c&lEssences &4ᚏᚎᚍᚔᚓᚒᚑᚐ", "&c○ (Essence Socket)", "&c◉ Essence: &f");
  
  private QModuleSocket m;
  
  private String head;
  
  private String empty;
  
  private String filled;
  
  private double cost;
  
  QSlotType(QModuleSocket m, String head, String empty, String fill) {
    setModule(m);
    setHeader(head);
    setEmpty(empty);
    setFilled(fill);
  }
  
  public QModuleSocket getModule() {
    return this.m;
  }
  
  public void setModule(QModuleSocket m) {
    this.m = m;
  }
  
  public String getHeader() {
    return this.head;
  }
  
  public void setHeader(String head) {
    this.head = ChatColor.translateAlternateColorCodes('&', head);
  }
  
  public String getEmpty() {
    return this.empty;
  }
  
  public void setEmpty(String empty) {
    this.empty = ChatColor.translateAlternateColorCodes('&', empty);
  }
  
  public String getFilled() {
    return this.filled;
  }
  
  public void setFilled(String filled) {
    this.filled = ChatColor.translateAlternateColorCodes('&', filled);
  }
  
  public double getExtractCost() {
    return this.cost;
  }
  
  public void setExtractCost(double cost) {
    this.cost = cost;
  }
}
