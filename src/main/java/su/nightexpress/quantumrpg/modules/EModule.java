package su.nightexpress.quantumrpg.modules;

public enum EModule {
  ACTIVE_ITEMS, ARROWS, BUFFS, COMBAT_LOG, CONSUMABLES, CUSTOM_ITEMS, DROPS, ESSENCES, EXTRACTOR, GEMS, IDENTIFY, ITEM_HINTS, MAGIC_DUST, NOTIFICATIONS, PARTY, REFINE, REPAIR, RESOLVE, RUNES, SELL, SETS, SOULBOUND, TIERS;
  
  private boolean e;
  
  private boolean hint;
  
  EModule() {
    this.e = false;
    this.hint = false;
  }
  
  public void setEnabled(boolean e) {
    this.e = e;
  }
  
  public boolean isEnabled() {
    return this.e;
  }
  
  public void setHint(boolean hint) {
    this.hint = hint;
  }
  
  public boolean isHinted() {
    return this.hint;
  }
}
