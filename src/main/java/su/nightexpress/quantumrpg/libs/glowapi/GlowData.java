package su.nightexpress.quantumrpg.libs.glowapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlowData {
  public Map<UUID, GlowAPI.Color> colorMap = new HashMap<>();
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    GlowData glowData = (GlowData)o;
    return (this.colorMap != null) ? this.colorMap.equals(glowData.colorMap) : ((glowData.colorMap == null));
  }
  
  public int hashCode() {
    return (this.colorMap != null) ? this.colorMap.hashCode() : 0;
  }
}
