package su.nightexpress.quantumrpg.types;

import java.util.List;

public class ArmorType {
  private String id;
  
  private String prefix;
  
  private String name;
  
  private String value;
  
  private boolean percent;
  
  private List<String> bds;
  
  private List<String> bdt;
  
  private String formula;
  
  private double cost;
  
  private String format;
  
  public ArmorType(String id, String prefix, String name, String value, boolean percent, List<String> bds, List<String> bdt, String formula, String format) {
    setId(id);
    setPrefix(prefix);
    setName(name);
    setValue(value);
    setPercent(percent);
    setBlockDamageSources(bds);
    setBlockDamageTypes(bdt);
    setFormula(formula);
    this.cost = 0.0D;
    setFormat(format);
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id.toLowerCase();
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
  
  public String getValue() {
    return this.value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public boolean isPercent() {
    return this.percent;
  }
  
  public void setPercent(boolean percent) {
    this.percent = percent;
  }
  
  public List<String> getBlockDamageSources() {
    return this.bds;
  }
  
  public void setBlockDamageSources(List<String> bds) {
    this.bds = bds;
  }
  
  public List<String> getBlockDamageTypes() {
    return this.bdt;
  }
  
  public void setBlockDamageTypes(List<String> bdt) {
    this.bdt = bdt;
  }
  
  public String getFormula() {
    return this.formula;
  }
  
  public void setFormula(String formula) {
    this.formula = formula;
  }
  
  public double getCost() {
    return this.cost;
  }
  
  public void setCost(double cost) {
    this.cost = cost;
  }
  
  public void setFormat(String format) {
    this.format = format
      .replace("%type_value%", getValue())
      .replace("%type_name%", getName())
      .replace("%type_prefix%", getPrefix());
  }
  
  public String getFormat() {
    return this.format;
  }
}
