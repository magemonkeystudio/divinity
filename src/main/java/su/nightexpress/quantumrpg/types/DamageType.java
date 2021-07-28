package su.nightexpress.quantumrpg.types;

import java.util.HashMap;
import java.util.List;

public class DamageType {
  private String id;
  
  private boolean def;
  
  private String prefix;
  
  private String name;
  
  private String value;
  
  private List<String> actions;
  
  private HashMap<String, Double> biome;
  
  private double cost;
  
  private String format;
  
  public DamageType(String id, boolean def, String prefix, String name, String value, List<String> actions, HashMap<String, Double> biome, String format) {
    setId(id);
    setDefault(def);
    setPrefix(prefix);
    setName(name);
    setValue(value);
    setActions(actions);
    setBiomeDamageModifiers(biome);
    this.cost = 0.0D;
    setFormat(format);
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setId(String id) {
    this.id = id.toLowerCase();
  }
  
  public boolean isDefault() {
    return this.def;
  }
  
  public void setDefault(boolean def) {
    this.def = def;
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
  
  public List<String> getActions() {
    return this.actions;
  }
  
  public void setActions(List<String> actions) {
    this.actions = actions;
  }
  
  public HashMap<String, Double> getBiomeDamageModifiers() {
    return this.biome;
  }
  
  public void setBiomeDamageModifiers(HashMap<String, Double> biome) {
    this.biome = biome;
  }
  
  public double getDamageModifierByBiome(String b) {
    if (this.biome.containsKey(b.toUpperCase()))
      return ((Double)this.biome.get(b.toUpperCase())).doubleValue(); 
    return 1.0D;
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
