package su.nightexpress.quantumrpg.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.HookUtils;
import su.nightexpress.quantumrpg.hooks.external.RPGInvHook;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.arrows.ArrowManager;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.modules.gems.GemManager;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.BonusType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.MetaUtils;

public class EntityAPI {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  public static void damageWithPercent(LivingEntity target, LivingEntity damager, double dmg, double amount) {
    MetaUtils.addDamagePercent(target, amount);
    target.damage(dmg, (Entity)damager);
  }
  
  public static void damageWithAdditional(LivingEntity target, LivingEntity damager, double dmg, double amount) {
    MetaUtils.addDamageAdditional(target, amount);
    target.damage(dmg, (Entity)damager);
  }
  
  public static LivingEntity getTarget(Entity entity) {
    return getTargetByRange(entity, Config.getMaxTargetDistance());
  }
  
  public static LivingEntity getTargetByRange(Entity entity, double range) {
    LivingEntity target = null;
    Location start = entity.getLocation();
    if (entity instanceof LivingEntity)
      start = ((LivingEntity)entity).getEyeLocation(); 
    Vector increase = start.getDirection();
    for (int counter = 0; counter < range; counter++) {
      Location point = start.add(increase);
      if (point.getBlock() != null && point.getBlock().getType() != Material.AIR)
        break; 
      byte b;
      int i;
      Entity[] arrayOfEntity;
      for (i = (arrayOfEntity = point.getChunk().getEntities()).length, b = 0; b < i; ) {
        Entity e = arrayOfEntity[b];
        if (!(e instanceof LivingEntity) || 
          e instanceof org.bukkit.entity.ArmorStand || 
          !e.getWorld().equals(point.getWorld()) || 
          e.getLocation().distance(point) > 1.5D || 
          !HookUtils.canFights(entity, e)) {
          b++;
          continue;
        } 
        return (LivingEntity)e;
      } 
    } 
    return target;
  }
  
  public static double getTotalDamageByType(String type, LivingEntity li) {
    DamageType dtype = Config.getDamageTypeById(type);
    double value = 0.0D;
    if (dtype == null)
      return value; 
    Map<DamageType, Double> map = getDamageTypes(li, li.getEquipment().getItemInMainHand(), null);
    if (map.containsKey(dtype))
      return ((Double)map.get(dtype)).doubleValue(); 
    return value;
  }
  
  public static Map<DamageType, Double> getDamageTypes(LivingEntity li, ItemStack wpn, ArrowManager.QArrow da) {
    Map<DamageType, Double> map = new HashMap<>();
    Block b = li.getLocation().getBlock();
    String bio = b.getBiome().name();
    BuffManager bs = (BuffManager)plugin.getMM().getModule(EModule.BUFFS);
    GemManager gems = (GemManager)plugin.getMM().getModule(EModule.GEMS);
    SetManager sm = (SetManager)plugin.getMM().getModule(EModule.SETS);
    Map<DamageType, Double> set_values = new HashMap<>();
    Map<DamageType, Double> set_bonuses = new HashMap<>();
    List<BuffManager.Buff> buffs = new ArrayList<>();
    if (bs.isActive())
      buffs = bs.getBuffs((Entity)li, BuffManager.BuffType.DAMAGE); 
    for (DamageType dt : Config.getDamageTypes().values()) {
      double set_value = 0.0D;
      double set_bonus = 0.0D;
      if (da != null)
        set_value += da.getAddDamage(dt.getId()); 
      if (sm.isActive()) {
        set_value += sm.getSetBonus(li, BonusType.DAMAGE, dt.getId(), false);
        set_bonus += sm.getSetBonus(li, BonusType.DAMAGE, dt.getId(), true);
      } 
      for (BuffManager.Buff bb : buffs) {
        if (bb.getValue().equalsIgnoreCase(dt.getId())) {
          set_bonus += bb.getModifier();
          break;
        } 
      } 
      if (set_values.containsKey(dt))
        set_value += ((Double)set_values.get(dt)).doubleValue(); 
      set_values.put(dt, Double.valueOf(set_value));
      if (set_bonuses.containsKey(dt))
        set_bonus += ((Double)set_bonuses.get(dt)).doubleValue(); 
      set_bonuses.put(dt, Double.valueOf(set_bonus));
    } 
    ItemStack[] equip = getEquipment(li, false);
    equip[0] = wpn;
    byte b1;
    int i;
    ItemStack[] arrayOfItemStack1;
    for (i = (arrayOfItemStack1 = getEquipment(li, false)).length, b1 = 0; b1 < i; ) {
      ItemStack item = arrayOfItemStack1[b1];
      if (item != null && item.getType() != Material.AIR)
        for (DamageType dt : Config.getDamageTypes().values()) {
          double value = 0.0D;
          double bonus = 0.0D;
          if (gems.isActive()) {
            value += gems.getItemGemDamage(item, dt.getId(), false);
            bonus += gems.getItemGemDamage(item, dt.getId(), true);
          } 
          if (ItemAPI.hasDamageType(dt.getId(), item)) {
            value += ItemAPI.getDamageByType(dt.getId(), item);
            value *= dt.getDamageModifierByBiome(bio);
          } 
          if (set_values.containsKey(dt))
            value += ((Double)set_values.get(dt)).doubleValue(); 
          set_values.put(dt, Double.valueOf(value));
          if (set_bonuses.containsKey(dt))
            bonus += ((Double)set_bonuses.get(dt)).doubleValue(); 
          set_bonuses.put(dt, Double.valueOf(bonus));
        }  
      b1++;
    } 
    for (DamageType dt : Config.getDamageTypes().values()) {
      double value = 0.0D;
      double bonus = 0.0D;
      if (set_values.containsKey(dt))
        value = ((Double)set_values.get(dt)).doubleValue(); 
      if (set_bonuses.containsKey(dt))
        bonus = ((Double)set_bonuses.get(dt)).doubleValue(); 
      value *= 1.0D + bonus / 100.0D;
      if (value > 0.0D)
        map.put(dt, Double.valueOf(value)); 
    } 
    if (map.isEmpty()) {
      DamageType dt = Config.getDamageTypeByDefault();
      double bonus = 0.0D;
      double value = 1.0D;
      if (set_bonuses.containsKey(dt))
        bonus = ((Double)set_bonuses.get(dt)).doubleValue(); 
      if (wpn != null && wpn.getType() != Material.AIR)
        value = ItemAPI.getDamageByType(dt.getId(), wpn); 
      value *= 1.0D + bonus / 100.0D;
      map.put(dt, Double.valueOf(value));
    } 
    return map;
  }
  
  public static Map<ArmorType, Double> getDefenseTypes(LivingEntity li, ArrowManager.QArrow da) {
    Map<ArmorType, Double> map = new HashMap<>();
    BuffManager bs = (BuffManager)plugin.getMM().getModule(EModule.BUFFS);
    GemManager gems = (GemManager)plugin.getMM().getModule(EModule.GEMS);
    SetManager sm = (SetManager)plugin.getMM().getModule(EModule.SETS);
    Map<ArmorType, Double> set_values = new HashMap<>();
    Map<ArmorType, Double> set_bonuses = new HashMap<>();
    List<BuffManager.Buff> buffs = new ArrayList<>();
    if (bs.isActive())
      buffs = bs.getBuffs((Entity)li, BuffManager.BuffType.DEFENSE); 
    for (ArmorType dt : Config.getArmorTypes().values()) {
      double set_value = 0.0D;
      double set_bonus = 0.0D;
      if (da != null)
        set_value = Math.max(0.0D, set_value - da.getDefIgnore(dt.getId())); 
      if (sm.isActive()) {
        set_value += sm.getSetBonus(li, BonusType.DEFENSE, dt.getId(), false);
        set_bonus += sm.getSetBonus(li, BonusType.DEFENSE, dt.getId(), true);
      } 
      for (BuffManager.Buff bb : buffs) {
        if (bb.getValue().equalsIgnoreCase(dt.getId())) {
          set_bonus += bb.getModifier();
          break;
        } 
      } 
      if (set_values.containsKey(dt))
        set_value += ((Double)set_values.get(dt)).doubleValue(); 
      set_values.put(dt, Double.valueOf(set_value));
      if (set_bonuses.containsKey(dt))
        set_bonus += ((Double)set_bonuses.get(dt)).doubleValue(); 
      set_bonuses.put(dt, Double.valueOf(set_bonus));
    } 
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = getEquipment(li, true)).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      if (item != null && item.getType() != Material.AIR)
        for (ArmorType dt : Config.getArmorTypes().values()) {
          double value = ItemAPI.getDefenseByType(dt.getId(), item);
          double bonus = 0.0D;
          if (gems.isActive()) {
            value += gems.getItemGemDefense(item, dt.getId(), false);
            bonus += gems.getItemGemDefense(item, dt.getId(), true);
          } 
          if (set_values.containsKey(dt))
            value += ((Double)set_values.get(dt)).doubleValue(); 
          set_values.put(dt, Double.valueOf(value));
          if (set_bonuses.containsKey(dt))
            bonus += ((Double)set_bonuses.get(dt)).doubleValue(); 
          set_bonuses.put(dt, Double.valueOf(bonus));
        }  
      b++;
    } 
    for (ArmorType dt : Config.getArmorTypes().values()) {
      double value = 0.0D;
      double bonus = 0.0D;
      if (set_values.containsKey(dt))
        value = ((Double)set_values.get(dt)).doubleValue(); 
      if (set_bonuses.containsKey(dt))
        bonus = ((Double)set_bonuses.get(dt)).doubleValue(); 
      value *= 1.0D + bonus / 100.0D;
      if (value > 0.0D)
        map.put(dt, Double.valueOf(value)); 
    } 
    if (map.isEmpty()) {
      DamageType dt = Config.getDamageTypeByDefault();
      if (dt == null)
        return map; 
      double value = ItemAPI.getDefaultDefense(li);
      for (ArmorType at : Config.getArmorTypes().values()) {
        if (at.getBlockDamageTypes().contains(dt.getId())) {
          double bonus = 0.0D;
          if (set_bonuses.containsKey(at))
            bonus = ((Double)set_bonuses.get(at)).doubleValue(); 
          value *= 1.0D + bonus / 100.0D;
          map.put(at, Double.valueOf(value));
        } 
      } 
    } 
    return map;
  }
  
  public static double getEnchantedDefense(Entity damager, LivingEntity li) {
    double def = 0.0D;
    if (damager instanceof org.bukkit.entity.Creeper) {
      byte b;
      int i;
      ItemStack[] arrayOfItemStack;
      for (i = (arrayOfItemStack = getEquipment(li, true)).length, b = 0; b < i; ) {
        ItemStack itemStack = arrayOfItemStack[b];
        if (itemStack != null && itemStack.getType() != Material.AIR && 
          itemStack.containsEnchantment(Enchantment.PROTECTION_EXPLOSIONS))
          def += (itemStack.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) * 2); 
        b++;
      } 
    } else if (damager instanceof org.bukkit.entity.Projectile) {
      byte b;
      int i;
      ItemStack[] arrayOfItemStack;
      for (i = (arrayOfItemStack = getEquipment(li, true)).length, b = 0; b < i; ) {
        ItemStack itemStack = arrayOfItemStack[b];
        if (itemStack != null && itemStack.getType() != Material.AIR && 
          itemStack.containsEnchantment(Enchantment.PROTECTION_PROJECTILE))
          def += (itemStack.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE) * 2); 
        b++;
      } 
    } else {
      byte b;
      int i;
      ItemStack[] arrayOfItemStack;
      for (i = (arrayOfItemStack = getEquipment(li, true)).length, b = 0; b < i; ) {
        ItemStack itemStack = arrayOfItemStack[b];
        if (itemStack != null && itemStack.getType() != Material.AIR && 
          itemStack.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL))
          def += itemStack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL); 
        b++;
      } 
    } 
    return def;
  }
  
  public static double getPvPEDamage(LivingEntity e1, LivingEntity e2, ArrowManager.QArrow da) {
    double get = 0.0D;
    if (e2 instanceof Player)
      if (e1 instanceof Player) {
        get = getItemStat(e2, ItemStat.PVP_DAMAGE, da);
      } else {
        get = getItemStat(e2, ItemStat.PVE_DAMAGE, da);
      }  
    return get;
  }
  
  public static double getItemStat(LivingEntity e1, ItemStat type, ArrowManager.QArrow da) {
    if (!(e1 instanceof Player) && 
      !Config.allowAttributesToMobs())
      return 0.0D; 
    double value = getBaseStat(e1, type);
    if ((type == ItemStat.BLOCK_RATE || type == ItemStat.BLOCK_DAMAGE) && 
      e1 instanceof Player) {
      Player p = (Player)e1;
      if (p.isBlocking() && p.getCooldown(Material.SHIELD) <= 0)
        if (type == ItemStat.BLOCK_RATE) {
          value = Config.g_combat_shield_rate;
        } else {
          value = Config.g_combat_shield_dmg;
        }  
    } 
    if (da != null)
      value += da.getAddStat(type); 
    if (EModule.BUFFS.isEnabled())
      value += ((BuffManager)plugin.getMM().getModule(BuffManager.class)).getBuffValue((Entity)e1, BuffManager.BuffType.ITEM_STAT, type.name()); 
    if (type.getCapability() >= 0.0D && 
      value > type.getCapability())
      value = type.getCapability(); 
    return value;
  }
  
  private static double getBaseStat(LivingEntity e1, ItemStat type) {
    double value = 0.0D;
    double bonus = 0.0D;
    GemManager gems = null;
    SetManager sets = null;
    if (EModule.GEMS.isEnabled())
      gems = (GemManager)plugin.getMM().getModule(EModule.GEMS); 
    if (EModule.SETS.isEnabled()) {
      sets = (SetManager)plugin.getModule(SetManager.class);
      if (sets != null)
        value += sets.getSetBonus(e1, BonusType.ITEM_STAT, type.name(), false); 
    } 
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = getEquipment(e1, false)).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      double raw = ItemAPI.getStatOnItem(item, type);
      if (gems != null) {
        value += gems.getItemGemStat(item, type, false);
        bonus += gems.getItemGemStat(item, type, true);
      } 
      if (type.isMainItem(item)) {
        value += raw;
      } else {
        bonus += raw;
      } 
      b++;
    } 
    if (bonus != 0.0D)
      value *= 1.0D + bonus / 100.0D; 
    return value;
  }
  
  public static ItemStack[] getEquipment(LivingEntity e, boolean armorOnly) {
    ItemStack[] armor = new ItemStack[6];
    if (e instanceof Player && EHook.RPG_INVENTORY.isEnabled()) {
      Player p = (Player)e;
      RPGInvHook ri = (RPGInvHook)EHook.RPG_INVENTORY.getHook();
      armor = ri.getEquip(p);
    } else if (e != null && e.getEquipment() != null) {
      for (int j = 2; j < 6; j++)
        armor[j] = e.getEquipment().getArmorContents()[j - 2]; 
      if (e.getEquipment().getItemInOffHand() != null && (
        Config.allowAttributesToOffHand() || e.getEquipment().getItemInOffHand().getType() == Material.SHIELD))
        armor[1] = e.getEquipment().getItemInOffHand(); 
      if (e.getEquipment().getItemInMainHand() != null) {
        ItemStack main = e.getEquipment().getItemInMainHand();
        if (!ItemUtils.isArmor(main))
          if (armorOnly) {
            armor[0] = new ItemStack(Material.AIR);
          } else {
            armor[0] = e.getEquipment().getItemInMainHand();
          }  
      } 
    } 
    for (int i = 0; i < armor.length; i++) {
      ItemStack it = armor[i];
      if (it == null || ItemAPI.isSoulboundRequired(it))
        armor[i] = new ItemStack(Material.AIR); 
    } 
    return armor;
  }
  
  public static void checkForLegitItems(Player p) {
    int i = 0;
    ItemStack[] ara = p.getInventory().getArmorContents();
    byte b;
    int j;
    ItemStack[] arrayOfItemStack1;
    for (j = (arrayOfItemStack1 = ara).length, b = 0; b < j; ) {
      ItemStack itt = arrayOfItemStack1[b];
      if (itt != null && 
        !ItemAPI.canUse(itt, p)) {
        if (i == 2) {
          p.getInventory().setChestplate(null);
        } else if (i == 1) {
          p.getInventory().setLeggings(null);
        } else if (i == 0) {
          p.getInventory().setBoots(null);
        } else {
          p.getInventory().setHelmet(null);
        } 
        if (p.getInventory().firstEmpty() != -1) {
          p.getInventory().addItem(new ItemStack[] { itt });
        } else {
          p.getWorld().dropItem(p.getLocation(), itt).setPickupDelay(40);
        } 
      } 
      i++;
      b++;
    } 
  }
}
