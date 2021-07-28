package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.types.DestroyType;
import su.nightexpress.quantumrpg.utils.Utils;

public class SocketSettings {
  private String display;
  
  private List<String> lore;
  
  private DestroyType destroy;
  
  private boolean eff_use;
  
  private String eff_de_value;
  
  private String eff_suc_value;
  
  private boolean sound_use;
  
  private Sound sound_de_value;
  
  private Sound sound_suc_value;
  
  private String header;
  
  private String empty_slot;
  
  private String filled_slot;
  
  SocketSettings(SocketSettings ms) {
    this.display = ms.getDisplay();
    this.lore = ms.getLore();
    this.destroy = ms.getDestroyType();
    this.header = ms.getHeader();
    this.empty_slot = ms.getEmptySlot();
    this.filled_slot = ms.getFilledSlot();
  }
  
  public SocketSettings(String display, List<String> lore, DestroyType destroy, boolean eff_use, String eff_de_value, String eff_suc_value, boolean sound_use, Sound sound_de_value, Sound sound_suc_value, String header, String empty_slot, String filled_slot) {
    setDisplay(display);
    setLore(lore);
    setDestroyType(destroy);
    setUseEffect(eff_use);
    setDestroyEffect(eff_de_value);
    setSuccessEffect(eff_suc_value);
    setUseSound(sound_use);
    setDestroySound(sound_de_value);
    setSuccessSound(sound_suc_value);
    setHeader(header);
    setEmptySlot(empty_slot);
    setFilledSlot(filled_slot);
  }
  
  public String getDisplay() {
    return this.display;
  }
  
  public void setDisplay(String display) {
    this.display = display;
  }
  
  public List<String> getLore() {
    return new ArrayList<>(this.lore);
  }
  
  public void setLore(List<String> lore) {
    this.lore = lore;
  }
  
  public DestroyType getDestroyType() {
    return this.destroy;
  }
  
  public void setDestroyType(DestroyType destroy) {
    this.destroy = destroy;
  }
  
  public boolean useEffect() {
    return this.eff_use;
  }
  
  public void setUseEffect(boolean eff_use) {
    this.eff_use = eff_use;
  }
  
  public String getDestroyEffect() {
    return this.eff_de_value;
  }
  
  public void setDestroyEffect(String eff_de_value) {
    this.eff_de_value = eff_de_value;
  }
  
  public String getSuccessEffect() {
    return this.eff_suc_value;
  }
  
  public void setSuccessEffect(String eff_suc_value) {
    this.eff_suc_value = eff_suc_value;
  }
  
  public boolean useSound() {
    return this.sound_use;
  }
  
  public void setUseSound(boolean sound_use) {
    this.sound_use = sound_use;
  }
  
  public Sound getDestroySound() {
    return this.sound_de_value;
  }
  
  public void setDestroySound(Sound sound_de_value) {
    this.sound_de_value = sound_de_value;
  }
  
  public Sound getSuccessSound() {
    return this.sound_suc_value;
  }
  
  public void setSuccessSound(Sound sound_suc_value) {
    this.sound_suc_value = sound_suc_value;
  }
  
  public void playSound(Player p, boolean success) {
    Sound s;
    if (!useSound())
      return; 
    if (success) {
      s = getSuccessSound();
    } else {
      s = getDestroySound();
    } 
    p.playSound(p.getLocation(), s, 0.8F, 0.8F);
  }
  
  public void playEffect(Player p, boolean success) {
    String eff;
    if (!useEffect())
      return; 
    if (success) {
      eff = getSuccessEffect();
    } else {
      eff = getDestroyEffect();
    } 
    Utils.playEffect(eff, p.getEyeLocation(), 0.3F, 0.0F, 0.3F, 0.3F, 15);
  }
  
  public String getHeader() {
    return this.header;
  }
  
  public void setHeader(String header) {
    this.header = header;
  }
  
  public String getEmptySlot() {
    return this.empty_slot;
  }
  
  public void setEmptySlot(String empty_slot) {
    this.empty_slot = empty_slot;
  }
  
  public String getFilledSlot() {
    return this.filled_slot;
  }
  
  public void setFilledSlot(String filled_slot) {
    this.filled_slot = filled_slot;
  }
}
