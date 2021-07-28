package su.nightexpress.quantumrpg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.MetaUtils;

public class DamageListener implements Listener {
  private QuantumRPG plugin;
  
  public DamageListener(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler
  public void onRangeDamage(PlayerInteractEvent e) {
    if (e.getHand() == EquipmentSlot.OFF_HAND)
      return; 
    if (e.getAction().toString().contains("RIGHT"))
      return; 
    Player p = e.getPlayer();
    ItemStack wpn = p.getInventory().getItemInMainHand();
    if (wpn == null || wpn.getType() == Material.BOW || !ItemUtils.isWeapon(wpn))
      return; 
    double range = EntityAPI.getItemStat((LivingEntity)p, ItemStat.RANGE, null);
    if (EModule.BUFFS.isEnabled()) {
      double buff = ((BuffManager)this.plugin.getMM().getModule(BuffManager.class)).getBuffValue((Entity)p, BuffManager.BuffType.ITEM_STAT, ItemStat.RANGE.name());
      if (buff > 0.0D) {
        if (range <= 0.0D)
          range = 3.0D; 
        range *= 1.0D + buff / 100.0D;
      } 
    } 
    if (range <= 3.0D)
      return; 
    LivingEntity target = EntityAPI.getTargetByRange((Entity)p, range);
    if (target != null) {
      double dmg = ItemAPI.getDefaultDamage(wpn);
      target.damage(dmg, (Entity)p);
    } 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onMeleeDamage(EntityDamageEvent ex) {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual getEntity : ()Lorg/bukkit/entity/Entity;
    //   4: astore_2
    //   5: aload_2
    //   6: instanceof org/bukkit/entity/LivingEntity
    //   9: ifne -> 13
    //   12: return
    //   13: aload_1
    //   14: invokevirtual getDamage : ()D
    //   17: dstore_3
    //   18: aconst_null
    //   19: astore #5
    //   21: aconst_null
    //   22: astore #6
    //   24: aload_2
    //   25: checkcast org/bukkit/entity/LivingEntity
    //   28: astore #7
    //   30: new su/nightexpress/quantumrpg/listeners/DamageMeta
    //   33: dup
    //   34: aload #5
    //   36: aload #6
    //   38: aload #7
    //   40: invokespecial <init> : (Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;)V
    //   43: astore #8
    //   45: aload #8
    //   47: dload_3
    //   48: invokevirtual setDefaultDamage : (D)V
    //   51: aload_1
    //   52: instanceof org/bukkit/event/entity/EntityDamageByEntityEvent
    //   55: istore #9
    //   57: iload #9
    //   59: ifeq -> 512
    //   62: aload_1
    //   63: checkcast org/bukkit/event/entity/EntityDamageByEntityEvent
    //   66: astore #10
    //   68: aload #10
    //   70: invokevirtual getDamager : ()Lorg/bukkit/entity/Entity;
    //   73: astore #11
    //   75: aload #11
    //   77: instanceof org/bukkit/entity/LivingEntity
    //   80: ifeq -> 266
    //   83: aload #11
    //   85: aload_2
    //   86: invokestatic canFights : (Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;)Z
    //   89: ifne -> 99
    //   92: aload #10
    //   94: iconst_1
    //   95: invokevirtual setCancelled : (Z)V
    //   98: return
    //   99: aload #11
    //   101: checkcast org/bukkit/entity/LivingEntity
    //   104: astore #6
    //   106: aload #8
    //   108: aload #6
    //   110: invokevirtual setDamager : (Lorg/bukkit/entity/Entity;)V
    //   113: aload #6
    //   115: invokeinterface getEquipment : ()Lorg/bukkit/inventory/EntityEquipment;
    //   120: invokeinterface getItemInMainHand : ()Lorg/bukkit/inventory/ItemStack;
    //   125: ifnull -> 149
    //   128: new org/bukkit/inventory/ItemStack
    //   131: dup
    //   132: aload #6
    //   134: invokeinterface getEquipment : ()Lorg/bukkit/inventory/EntityEquipment;
    //   139: invokeinterface getItemInMainHand : ()Lorg/bukkit/inventory/ItemStack;
    //   144: invokespecial <init> : (Lorg/bukkit/inventory/ItemStack;)V
    //   147: astore #5
    //   149: aload #5
    //   151: ifnull -> 174
    //   154: aload #5
    //   156: invokevirtual getType : ()Lorg/bukkit/Material;
    //   159: getstatic org/bukkit/Material.BOW : Lorg/bukkit/Material;
    //   162: if_acmpne -> 174
    //   165: invokestatic bowsMeleeDmg : ()Z
    //   168: ifne -> 174
    //   171: goto -> 677
    //   174: aload #10
    //   176: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   179: getstatic org/bukkit/event/entity/EntityDamageEvent$DamageCause.ENTITY_SWEEP_ATTACK : Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   182: if_acmpne -> 198
    //   185: invokestatic noSweepAtk : ()Z
    //   188: ifeq -> 198
    //   191: aload #10
    //   193: iconst_1
    //   194: invokevirtual setCancelled : (Z)V
    //   197: return
    //   198: aload #6
    //   200: getstatic su/nightexpress/quantumrpg/stats/ItemStat.RANGE : Lsu/nightexpress/quantumrpg/stats/ItemStat;
    //   203: aconst_null
    //   204: invokestatic getItemStat : (Lorg/bukkit/entity/LivingEntity;Lsu/nightexpress/quantumrpg/stats/ItemStat;Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow;)D
    //   207: dstore #12
    //   209: dload #12
    //   211: dconst_0
    //   212: dcmpl
    //   213: ifle -> 482
    //   216: aload #6
    //   218: invokeinterface getWorld : ()Lorg/bukkit/World;
    //   223: aload #7
    //   225: invokeinterface getWorld : ()Lorg/bukkit/World;
    //   230: invokevirtual equals : (Ljava/lang/Object;)Z
    //   233: ifeq -> 482
    //   236: aload #6
    //   238: invokeinterface getLocation : ()Lorg/bukkit/Location;
    //   243: aload #7
    //   245: invokeinterface getLocation : ()Lorg/bukkit/Location;
    //   250: invokevirtual distance : (Lorg/bukkit/Location;)D
    //   253: dload #12
    //   255: dcmpl
    //   256: ifle -> 482
    //   259: aload #10
    //   261: iconst_1
    //   262: invokevirtual setCancelled : (Z)V
    //   265: return
    //   266: aload #11
    //   268: instanceof org/bukkit/entity/Projectile
    //   271: ifeq -> 677
    //   274: aload #11
    //   276: checkcast org/bukkit/entity/Projectile
    //   279: astore #12
    //   281: aload #12
    //   283: instanceof org/bukkit/entity/EnderPearl
    //   286: ifeq -> 330
    //   289: aload_2
    //   290: instanceof org/bukkit/entity/Player
    //   293: ifeq -> 330
    //   296: aload_2
    //   297: checkcast org/bukkit/entity/Player
    //   300: astore #13
    //   302: aload #12
    //   304: invokeinterface getShooter : ()Lorg/bukkit/projectiles/ProjectileSource;
    //   309: ifnull -> 330
    //   312: aload #12
    //   314: invokeinterface getShooter : ()Lorg/bukkit/projectiles/ProjectileSource;
    //   319: aload #13
    //   321: invokevirtual equals : (Ljava/lang/Object;)Z
    //   324: ifeq -> 330
    //   327: goto -> 677
    //   330: aload #12
    //   332: invokestatic hasItem : (Lorg/bukkit/entity/Entity;)Z
    //   335: ifeq -> 677
    //   338: aload #12
    //   340: invokeinterface getShooter : ()Lorg/bukkit/projectiles/ProjectileSource;
    //   345: instanceof org/bukkit/entity/LivingEntity
    //   348: ifne -> 354
    //   351: goto -> 677
    //   354: aload #12
    //   356: invokeinterface getShooter : ()Lorg/bukkit/projectiles/ProjectileSource;
    //   361: checkcast org/bukkit/entity/LivingEntity
    //   364: astore #6
    //   366: aload #6
    //   368: aload_2
    //   369: invokestatic canFights : (Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;)Z
    //   372: ifne -> 382
    //   375: aload #10
    //   377: iconst_1
    //   378: invokevirtual setCancelled : (Z)V
    //   381: return
    //   382: aload #12
    //   384: invokestatic hasPower : (Lorg/bukkit/entity/Projectile;)Z
    //   387: ifeq -> 408
    //   390: aload #12
    //   392: invokestatic getProjectilePower : (Lorg/bukkit/entity/Projectile;)D
    //   395: ldc2_w 100.0
    //   398: dmul
    //   399: dstore #13
    //   401: aload #7
    //   403: dload #13
    //   405: invokestatic addDamagePercent : (Lorg/bukkit/entity/LivingEntity;D)V
    //   408: aload #8
    //   410: aload #6
    //   412: invokevirtual setDamager : (Lorg/bukkit/entity/Entity;)V
    //   415: getstatic su/nightexpress/quantumrpg/modules/EModule.ARROWS : Lsu/nightexpress/quantumrpg/modules/EModule;
    //   418: invokevirtual isEnabled : ()Z
    //   421: ifeq -> 475
    //   424: aload_0
    //   425: getfield plugin : Lsu/nightexpress/quantumrpg/QuantumRPG;
    //   428: invokevirtual getMM : ()Lsu/nightexpress/quantumrpg/modules/ModuleManager;
    //   431: getstatic su/nightexpress/quantumrpg/modules/EModule.ARROWS : Lsu/nightexpress/quantumrpg/modules/EModule;
    //   434: invokevirtual getModule : (Lsu/nightexpress/quantumrpg/modules/EModule;)Lsu/nightexpress/quantumrpg/modules/QModule;
    //   437: checkcast su/nightexpress/quantumrpg/modules/arrows/ArrowManager
    //   440: astore #13
    //   442: aload #13
    //   444: aload #12
    //   446: invokevirtual isArrow : (Lorg/bukkit/entity/Projectile;)Z
    //   449: ifeq -> 475
    //   452: aload #8
    //   454: aload #13
    //   456: aload #13
    //   458: aload #12
    //   460: invokevirtual getArrowId : (Lorg/bukkit/entity/Projectile;)Ljava/lang/String;
    //   463: ldc_w su/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow
    //   466: invokevirtual getItemById : (Ljava/lang/String;Ljava/lang/Class;)Lsu/nightexpress/quantumrpg/modules/ModuleItem;
    //   469: checkcast su/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow
    //   472: invokevirtual setArrow : (Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow;)V
    //   475: aload #12
    //   477: invokestatic getItemFromMeta : (Lorg/bukkit/entity/Entity;)Lorg/bukkit/inventory/ItemStack;
    //   480: astore #5
    //   482: aload #7
    //   484: aload #6
    //   486: dload_3
    //   487: aload #5
    //   489: aload #10
    //   491: aload #8
    //   493: invokestatic procDmg : (Lorg/bukkit/entity/LivingEntity;Lorg/bukkit/entity/LivingEntity;DLorg/bukkit/inventory/ItemStack;Lorg/bukkit/event/entity/EntityDamageByEntityEvent;Lsu/nightexpress/quantumrpg/listeners/DamageMeta;)D
    //   496: dstore_3
    //   497: aload #10
    //   499: dload_3
    //   500: invokevirtual setDamage : (D)V
    //   503: aload #8
    //   505: dload_3
    //   506: invokevirtual setDefaultDamage : (D)V
    //   509: goto -> 677
    //   512: aload_2
    //   513: instanceof org/bukkit/entity/Player
    //   516: ifne -> 525
    //   519: invokestatic allowAttributesToMobs : ()Z
    //   522: ifeq -> 667
    //   525: aload_1
    //   526: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   529: invokevirtual name : ()Ljava/lang/String;
    //   532: invokevirtual toLowerCase : ()Ljava/lang/String;
    //   535: astore #10
    //   537: aload #7
    //   539: aconst_null
    //   540: invokestatic getDefenseTypes : (Lorg/bukkit/entity/LivingEntity;Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow;)Ljava/util/Map;
    //   543: astore #11
    //   545: aload #11
    //   547: invokeinterface keySet : ()Ljava/util/Set;
    //   552: invokeinterface iterator : ()Ljava/util/Iterator;
    //   557: astore #13
    //   559: goto -> 657
    //   562: aload #13
    //   564: invokeinterface next : ()Ljava/lang/Object;
    //   569: checkcast su/nightexpress/quantumrpg/types/ArmorType
    //   572: astore #12
    //   574: aload #12
    //   576: invokevirtual getBlockDamageSources : ()Ljava/util/List;
    //   579: aload #10
    //   581: invokeinterface contains : (Ljava/lang/Object;)Z
    //   586: ifne -> 592
    //   589: goto -> 657
    //   592: aload #11
    //   594: aload #12
    //   596: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
    //   601: checkcast java/lang/Double
    //   604: invokevirtual doubleValue : ()D
    //   607: dstore #14
    //   609: aload #12
    //   611: invokevirtual getFormula : ()Ljava/lang/String;
    //   614: ldc_w '%def%'
    //   617: dload #14
    //   619: invokestatic valueOf : (D)Ljava/lang/String;
    //   622: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   625: ldc_w '%dmg%'
    //   628: dload_3
    //   629: invokestatic valueOf : (D)Ljava/lang/String;
    //   632: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   635: ldc_w '%penetrate%'
    //   638: ldc_w '0'
    //   641: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   644: astore #16
    //   646: aload #16
    //   648: invokestatic eval : (Ljava/lang/String;)D
    //   651: dstore_3
    //   652: aload_1
    //   653: dload_3
    //   654: invokevirtual setDamage : (D)V
    //   657: aload #13
    //   659: invokeinterface hasNext : ()Z
    //   664: ifne -> 562
    //   667: aload #8
    //   669: aload_1
    //   670: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   673: dload_3
    //   674: invokevirtual setDamageCause : (Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;D)V
    //   677: dconst_1
    //   678: dstore #10
    //   680: aload #7
    //   682: instanceof org/bukkit/entity/Player
    //   685: ifeq -> 726
    //   688: invokestatic getPlayerDmgModifiers : ()Ljava/util/Map;
    //   691: aload_1
    //   692: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   695: invokeinterface containsKey : (Ljava/lang/Object;)Z
    //   700: ifeq -> 761
    //   703: invokestatic getPlayerDmgModifiers : ()Ljava/util/Map;
    //   706: aload_1
    //   707: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   710: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
    //   715: checkcast java/lang/Double
    //   718: invokevirtual doubleValue : ()D
    //   721: dstore #10
    //   723: goto -> 761
    //   726: invokestatic getMobDmgModifiers : ()Ljava/util/Map;
    //   729: aload_1
    //   730: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   733: invokeinterface containsKey : (Ljava/lang/Object;)Z
    //   738: ifeq -> 761
    //   741: invokestatic getMobDmgModifiers : ()Ljava/util/Map;
    //   744: aload_1
    //   745: invokevirtual getCause : ()Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;
    //   748: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
    //   753: checkcast java/lang/Double
    //   756: invokevirtual doubleValue : ()D
    //   759: dstore #10
    //   761: aload #8
    //   763: dload #10
    //   765: invokevirtual multiply : (D)V
    //   768: aload_1
    //   769: aload #8
    //   771: invokevirtual getTotalDamage : ()D
    //   774: invokevirtual setDamage : (D)V
    //   777: iload #9
    //   779: ifeq -> 921
    //   782: aload #6
    //   784: ifnull -> 921
    //   787: getstatic su/nightexpress/quantumrpg/hooks/EHook.MYTHIC_MOBS : Lsu/nightexpress/quantumrpg/hooks/EHook;
    //   790: invokevirtual isEnabled : ()Z
    //   793: ifeq -> 815
    //   796: ldc_w su/nightexpress/quantumrpg/hooks/external/MythicMobsHook
    //   799: invokestatic getHook : (Ljava/lang/Class;)Lsu/nightexpress/quantumrpg/hooks/Hook;
    //   802: checkcast su/nightexpress/quantumrpg/hooks/external/MythicMobsHook
    //   805: aload #6
    //   807: aload #8
    //   809: invokevirtual getTotalDamage : ()D
    //   812: invokevirtual setSkillDamage : (Lorg/bukkit/entity/Entity;D)V
    //   815: dconst_0
    //   816: aload #8
    //   818: invokevirtual getTotalDamage : ()D
    //   821: aload #6
    //   823: getstatic su/nightexpress/quantumrpg/stats/ItemStat.VAMPIRISM : Lsu/nightexpress/quantumrpg/stats/ItemStat;
    //   826: aload #8
    //   828: invokevirtual getArrow : ()Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow;
    //   831: invokestatic getItemStat : (Lorg/bukkit/entity/LivingEntity;Lsu/nightexpress/quantumrpg/stats/ItemStat;Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager$QArrow;)D
    //   834: ldc2_w 100.0
    //   837: ddiv
    //   838: dmul
    //   839: invokestatic max : (DD)D
    //   842: dstore #12
    //   844: new org/bukkit/event/entity/EntityRegainHealthEvent
    //   847: dup
    //   848: aload #6
    //   850: dload #12
    //   852: getstatic org/bukkit/event/entity/EntityRegainHealthEvent$RegainReason.CUSTOM : Lorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;
    //   855: invokespecial <init> : (Lorg/bukkit/entity/Entity;DLorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;)V
    //   858: astore #14
    //   860: aload_0
    //   861: getfield plugin : Lsu/nightexpress/quantumrpg/QuantumRPG;
    //   864: invokevirtual getPluginManager : ()Lorg/bukkit/plugin/PluginManager;
    //   867: aload #14
    //   869: invokeinterface callEvent : (Lorg/bukkit/event/Event;)V
    //   874: aload #14
    //   876: invokevirtual isCancelled : ()Z
    //   879: ifne -> 921
    //   882: aload #6
    //   884: getstatic org/bukkit/attribute/Attribute.GENERIC_MAX_HEALTH : Lorg/bukkit/attribute/Attribute;
    //   887: invokeinterface getAttribute : (Lorg/bukkit/attribute/Attribute;)Lorg/bukkit/attribute/AttributeInstance;
    //   892: invokeinterface getValue : ()D
    //   897: dstore #15
    //   899: aload #6
    //   901: dload #15
    //   903: aload #6
    //   905: invokeinterface getHealth : ()D
    //   910: dload #12
    //   912: dadd
    //   913: invokestatic min : (DD)D
    //   916: invokeinterface setHealth : (D)V
    //   921: aload_1
    //   922: invokevirtual getFinalDamage : ()D
    //   925: aload_1
    //   926: invokevirtual getDamage : ()D
    //   929: dcmpl
    //   930: ifeq -> 1002
    //   933: invokestatic values : ()[Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;
    //   936: dup
    //   937: astore #15
    //   939: arraylength
    //   940: istore #14
    //   942: iconst_0
    //   943: istore #13
    //   945: goto -> 995
    //   948: aload #15
    //   950: iload #13
    //   952: aaload
    //   953: astore #12
    //   955: aload_1
    //   956: aload #12
    //   958: invokevirtual isApplicable : (Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;)Z
    //   961: ifeq -> 992
    //   964: aload #12
    //   966: getstatic org/bukkit/event/entity/EntityDamageEvent$DamageModifier.BASE : Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;
    //   969: if_acmpne -> 985
    //   972: aload_1
    //   973: aload #12
    //   975: aload_1
    //   976: invokevirtual getDamage : ()D
    //   979: invokevirtual setDamage : (Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;D)V
    //   982: goto -> 992
    //   985: aload_1
    //   986: aload #12
    //   988: dconst_0
    //   989: invokevirtual setDamage : (Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;D)V
    //   992: iinc #13, 1
    //   995: iload #13
    //   997: iload #14
    //   999: if_icmplt -> 948
    //   1002: aload #7
    //   1004: aload #8
    //   1006: invokestatic addDamageMeta : (Lorg/bukkit/entity/Entity;Lsu/nightexpress/quantumrpg/listeners/DamageMeta;)V
    //   1009: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #87	-> 0
    //   #88	-> 5
    //   #89	-> 13
    //   #91	-> 18
    //   #92	-> 21
    //   #93	-> 24
    //   #95	-> 30
    //   #96	-> 45
    //   #98	-> 51
    //   #102	-> 57
    //   #103	-> 62
    //   #104	-> 68
    //   #106	-> 75
    //   #109	-> 83
    //   #110	-> 92
    //   #111	-> 98
    //   #115	-> 99
    //   #116	-> 106
    //   #118	-> 113
    //   #119	-> 128
    //   #121	-> 149
    //   #123	-> 174
    //   #124	-> 191
    //   #125	-> 197
    //   #128	-> 198
    //   #129	-> 209
    //   #130	-> 216
    //   #131	-> 259
    //   #132	-> 265
    //   #137	-> 266
    //   #138	-> 274
    //   #141	-> 281
    //   #142	-> 296
    //   #143	-> 302
    //   #144	-> 327
    //   #149	-> 330
    //   #150	-> 351
    //   #153	-> 354
    //   #155	-> 366
    //   #156	-> 375
    //   #157	-> 381
    //   #161	-> 382
    //   #162	-> 390
    //   #163	-> 401
    //   #166	-> 408
    //   #168	-> 415
    //   #169	-> 424
    //   #170	-> 442
    //   #171	-> 452
    //   #175	-> 475
    //   #181	-> 482
    //   #182	-> 497
    //   #183	-> 503
    //   #184	-> 509
    //   #187	-> 512
    //   #188	-> 525
    //   #189	-> 537
    //   #190	-> 545
    //   #191	-> 574
    //   #192	-> 589
    //   #194	-> 592
    //   #195	-> 609
    //   #196	-> 614
    //   #197	-> 625
    //   #198	-> 635
    //   #195	-> 644
    //   #199	-> 646
    //   #200	-> 652
    //   #190	-> 657
    //   #203	-> 667
    //   #207	-> 677
    //   #208	-> 680
    //   #209	-> 688
    //   #210	-> 703
    //   #212	-> 723
    //   #214	-> 726
    //   #215	-> 741
    //   #219	-> 761
    //   #220	-> 768
    //   #226	-> 777
    //   #227	-> 787
    //   #228	-> 796
    //   #231	-> 815
    //   #232	-> 844
    //   #233	-> 860
    //   #234	-> 874
    //   #235	-> 882
    //   #236	-> 899
    //   #241	-> 921
    //   #242	-> 933
    //   #243	-> 955
    //   #244	-> 964
    //   #245	-> 972
    //   #246	-> 982
    //   #248	-> 985
    //   #242	-> 992
    //   #254	-> 1002
    //   #256	-> 1009
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	1010	0	this	Lsu/nightexpress/quantumrpg/listeners/DamageListener;
    //   0	1010	1	ex	Lorg/bukkit/event/entity/EntityDamageEvent;
    //   5	1005	2	e1	Lorg/bukkit/entity/Entity;
    //   18	992	3	dd	D
    //   21	989	5	item	Lorg/bukkit/inventory/ItemStack;
    //   24	986	6	damager	Lorg/bukkit/entity/LivingEntity;
    //   30	980	7	zertva	Lorg/bukkit/entity/LivingEntity;
    //   45	965	8	q	Lsu/nightexpress/quantumrpg/listeners/DamageMeta;
    //   57	953	9	ede	Z
    //   68	441	10	e	Lorg/bukkit/event/entity/EntityDamageByEntityEvent;
    //   75	434	11	e2	Lorg/bukkit/entity/Entity;
    //   209	57	12	range	D
    //   281	201	12	pp	Lorg/bukkit/entity/Projectile;
    //   302	28	13	p	Lorg/bukkit/entity/Player;
    //   401	7	13	power	D
    //   442	33	13	arr	Lsu/nightexpress/quantumrpg/modules/arrows/ArrowManager;
    //   537	130	10	source	Ljava/lang/String;
    //   545	122	11	armors	Ljava/util/Map;
    //   574	83	12	at	Lsu/nightexpress/quantumrpg/types/ArmorType;
    //   609	48	14	def	D
    //   646	11	16	f1	Ljava/lang/String;
    //   680	330	10	mult	D
    //   844	77	12	vamp	D
    //   860	61	14	ev	Lorg/bukkit/event/entity/EntityRegainHealthEvent;
    //   899	22	15	max	D
    //   955	37	12	dm	Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   545	122	11	armors	Ljava/util/Map<Lsu/nightexpress/quantumrpg/types/ArmorType;Ljava/lang/Double;>;
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onProjectLaunch(ProjectileLaunchEvent e) {
    Projectile projectile1 = e.getEntity();
    if (!(projectile1 instanceof Projectile))
      return; 
    Projectile pp = projectile1;
    if (!(pp.getShooter() instanceof LivingEntity))
      return; 
    LivingEntity e2 = (LivingEntity)pp.getShooter();
    if (!(e2 instanceof Player) && 
      !Config.allowAttributesToMobs())
      return; 
    String name = pp.getType().name();
    ItemStack off = e2.getEquipment().getItemInOffHand();
    if (off != null && off.getType().name().equalsIgnoreCase(name))
      return; 
    ItemStack launcher = e2.getEquipment().getItemInMainHand();
    if (launcher == null || launcher.getType() == Material.AIR)
      return; 
    ItemStack item = new ItemStack(launcher);
    MetaUtils.addItemToMeta((Entity)pp, item);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBowAddPower(EntityShootBowEvent e) {
    Entity e1 = e.getProjectile();
    if (!(e1 instanceof Projectile))
      return; 
    Projectile pp = (Projectile)e1;
    MetaUtils.setProjectilePower(pp, e.getForce());
    e.setProjectile((Entity)pp);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onFish(PlayerFishEvent e) {
    if (!Config.allowFishHookDamage())
      return; 
    Entity e1 = e.getCaught();
    if (!(e1 instanceof LivingEntity))
      return; 
    Player p = e.getPlayer();
    LivingEntity li = (LivingEntity)e1;
    ItemStack rod = p.getInventory().getItemInMainHand();
    double itemdmg = ItemAPI.getAllDamage(rod);
    double power = p.getLocation().distance(li.getLocation());
    if (power < 10.0D) {
      power /= 10.0D;
    } else {
      power = 1.5D;
    } 
    itemdmg *= power;
    li.damage(itemdmg, (Entity)p);
  }
}
