package su.nightexpress.quantumrpg.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.listeners.DamageMeta;

public class MetaUtils {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  private static final String M_ITEM = "QRPG_META_ITEM";
  
  private static final String NO_PICK = "QRPG_NOPICK";
  
  private static final String ATK_POWER = "QRPG_ATK_POWER";
  
  private static final String ATK_PERCENT = "QRPG_ATK_PERCENT";
  
  private static final String ATK_ADD = "QRPG_ATK_ADD";
  
  private static final String BOW_POWER = "QRPG_BOW_POWER";
  
  public static final String DAMAGE_META = "QRPG_DAMAGE";
  
  public static final String RF_SPACE = "§r§r§8§8§r§r §r§r§8§8§r§r";
  
  public static void addItemToMeta(Entity pp, ItemStack item) {
    pp.setMetadata("QRPG_META_ITEM", (MetadataValue)new FixedMetadataValue((Plugin)plugin, item));
  }
  
  public static boolean hasItem(Entity e) {
    return e.hasMetadata("QRPG_META_ITEM");
  }
  
  public static ItemStack getItemFromMeta(Entity e) {
    return (ItemStack)((MetadataValue)e.getMetadata("QRPG_META_ITEM").get(0)).value();
  }
  
  public static void setProjectilePower(Projectile pp, double power) {
    pp.setMetadata("QRPG_BOW_POWER", (MetadataValue)new FixedMetadataValue((Plugin)plugin, Double.valueOf(power)));
  }
  
  public static boolean hasPower(Projectile e) {
    return e.hasMetadata("QRPG_BOW_POWER");
  }
  
  public static double getProjectilePower(Projectile e) {
    if (!hasPower(e))
      return 1.0D; 
    double d = ((MetadataValue)e.getMetadata("QRPG_BOW_POWER").get(0)).asDouble();
    e.removeMetadata("QRPG_BOW_POWER", (Plugin)plugin);
    return d;
  }
  
  public static void noPickProjectile(Entity pp) {
    pp.setMetadata("QRPG_NOPICK", (MetadataValue)new FixedMetadataValue((Plugin)plugin, "x"));
  }
  
  public static boolean isPickable(Entity pp) {
    return !pp.hasMetadata("QRPG_NOPICK");
  }
  
  public static void addDamageMeta(Entity e, DamageMeta meta) {
    e.setMetadata("QRPG_DAMAGE", (MetadataValue)new FixedMetadataValue((Plugin)plugin, meta));
  }
  
  public static DamageMeta getDamageMeta(Entity e) {
    if (e.hasMetadata("QRPG_DAMAGE"))
      return (DamageMeta)((MetadataValue)e.getMetadata("QRPG_DAMAGE").get(0)).value(); 
    return null;
  }
  
  public static void removeDamageMeta(Entity e) {
    e.removeMetadata("QRPG_DAMAGE", (Plugin)plugin);
  }
  
  public static void storeAtkPower(Player p) {
    double power = plugin.getNMS().getAttackCooldown(p);
    if (power >= 1.0D && getAtkPower(p) >= 1.0D)
      return; 
    p.setMetadata("QRPG_ATK_POWER", (MetadataValue)new FixedMetadataValue((Plugin)plugin, Double.valueOf(power)));
  }
  
  public static double getAtkPower(Player p) {
    if (p.hasMetadata("QRPG_ATK_POWER"))
      return ((MetadataValue)p.getMetadata("QRPG_ATK_POWER").get(0)).asDouble(); 
    return 1.0D;
  }
  
  public static void addDamagePercent(LivingEntity e, double amount) {
    e.setMetadata("QRPG_ATK_PERCENT", (MetadataValue)new FixedMetadataValue((Plugin)plugin, Double.valueOf(amount)));
  }
  
  public static double getDamagePercent(LivingEntity e) {
    if (e.hasMetadata("QRPG_ATK_PERCENT")) {
      double d = ((MetadataValue)e.getMetadata("QRPG_ATK_PERCENT").get(0)).asDouble() / 100.0D;
      e.removeMetadata("QRPG_ATK_PERCENT", (Plugin)plugin);
      return d;
    } 
    return 1.0D;
  }
  
  public static void addDamageAdditional(LivingEntity e, double amount) {
    e.setMetadata("QRPG_ATK_PERCENT", (MetadataValue)new FixedMetadataValue((Plugin)plugin, Double.valueOf(amount)));
  }
  
  public static double getDamageAdditional(LivingEntity e) {
    if (e.hasMetadata("QRPG_ATK_ADD")) {
      double d = ((MetadataValue)e.getMetadata("QRPG_ATK_ADD").get(0)).asDouble();
      e.removeMetadata("QRPG_ATK_ADD", (Plugin)plugin);
      return d;
    } 
    return 0.0D;
  }
}
