package su.nightexpress.quantumrpg.modules.tiers.resources;

import java.util.List;

public class Res {
  private String type;
  
  private List<String> prefix;
  
  private List<String> suffix;
  
  public Res(String type, List<String> prefix, List<String> suffix) {
    setType(type);
    setPrefixes(prefix);
    setSuffixes(suffix);
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public List<String> getPrefixes() {
    return this.prefix;
  }
  
  public void setPrefixes(List<String> prefix) {
    this.prefix = prefix;
  }
  
  public List<String> getSuffixes() {
    return this.suffix;
  }
  
  public void setSuffixes(List<String> suffix) {
    this.suffix = suffix;
  }
}
