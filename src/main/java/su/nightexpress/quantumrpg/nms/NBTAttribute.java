package su.nightexpress.quantumrpg.nms;

public enum NBTAttribute {
  armor("armor"),
  armorToughness("armorToughness"),
  attackDamage("attackDamage"),
  attackSpeed("attackSpeed"),
  movementSpeed("movementSpeed"),
  maxHealth("maxHealth"),
  knockbackResistance("knockbackResistance");
  
  private String s;
  
  NBTAttribute(String s) {
    this.s = s;
  }
  
  public String att() {
    return this.s;
  }
}
