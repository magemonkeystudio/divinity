package su.nightexpress.quantumrpg.cmds.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;

public class SetCommand extends ICmd {
  public SetCommand(QuantumRPG plugin) {}
  
  public String getLabel() {
    return "set";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Set.";
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    // Byte code:
    //   0: aload_3
    //   1: arraylength
    //   2: iconst_1
    //   3: if_icmpgt -> 53
    //   6: getstatic su/nightexpress/quantumrpg/config/Lang.Help_Set : Lsu/nightexpress/quantumrpg/config/Lang;
    //   9: invokevirtual getList : ()Ljava/util/List;
    //   12: invokeinterface iterator : ()Ljava/util/Iterator;
    //   17: astore #5
    //   19: goto -> 42
    //   22: aload #5
    //   24: invokeinterface next : ()Ljava/lang/Object;
    //   29: checkcast java/lang/String
    //   32: astore #4
    //   34: aload_1
    //   35: aload #4
    //   37: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   42: aload #5
    //   44: invokeinterface hasNext : ()Z
    //   49: ifne -> 22
    //   52: return
    //   53: aload_1
    //   54: checkcast org/bukkit/entity/Player
    //   57: astore #4
    //   59: aload #4
    //   61: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   66: invokeinterface getItemInMainHand : ()Lorg/bukkit/inventory/ItemStack;
    //   71: astore #5
    //   73: aload #5
    //   75: ifnull -> 89
    //   78: aload #5
    //   80: invokevirtual getType : ()Lorg/bukkit/Material;
    //   83: getstatic org/bukkit/Material.AIR : Lorg/bukkit/Material;
    //   86: if_acmpne -> 125
    //   89: aload #4
    //   91: new java/lang/StringBuilder
    //   94: dup
    //   95: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   98: invokevirtual toMsg : ()Ljava/lang/String;
    //   101: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   104: invokespecial <init> : (Ljava/lang/String;)V
    //   107: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidItem : Lsu/nightexpress/quantumrpg/config/Lang;
    //   110: invokevirtual toMsg : ()Ljava/lang/String;
    //   113: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   116: invokevirtual toString : ()Ljava/lang/String;
    //   119: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   124: return
    //   125: iconst_m1
    //   126: istore #6
    //   128: aload_3
    //   129: iconst_1
    //   130: aaload
    //   131: invokevirtual toLowerCase : ()Ljava/lang/String;
    //   134: dup
    //   135: astore #7
    //   137: invokevirtual hashCode : ()I
    //   140: lookupswitch default -> 1454, -1773941975 -> 240, -1565772024 -> 253, -1172162311 -> 266, -897048717 -> 279, 2326569 -> 292, 3533310 -> 305, 3540564 -> 318, 13085340 -> 331, 94742904 -> 344, 102865796 -> 357, 509279610 -> 370
    //   240: aload #7
    //   242: ldc 'damagetype'
    //   244: invokevirtual equals : (Ljava/lang/Object;)Z
    //   247: ifne -> 655
    //   250: goto -> 1454
    //   253: aload #7
    //   255: ldc 'ammotype'
    //   257: invokevirtual equals : (Ljava/lang/Object;)Z
    //   260: ifne -> 1003
    //   263: goto -> 1454
    //   266: aload #7
    //   268: ldc 'armortype'
    //   270: invokevirtual equals : (Ljava/lang/Object;)Z
    //   273: ifne -> 847
    //   276: goto -> 1454
    //   279: aload #7
    //   281: ldc 'socket'
    //   283: invokevirtual equals : (Ljava/lang/Object;)Z
    //   286: ifne -> 454
    //   289: goto -> 1454
    //   292: aload #7
    //   294: ldc 'handtype'
    //   296: invokevirtual equals : (Ljava/lang/Object;)Z
    //   299: ifne -> 1129
    //   302: goto -> 1454
    //   305: aload #7
    //   307: ldc 'slot'
    //   309: invokevirtual equals : (Ljava/lang/Object;)Z
    //   312: ifne -> 454
    //   315: goto -> 1454
    //   318: aload #7
    //   320: ldc 'stat'
    //   322: invokevirtual equals : (Ljava/lang/Object;)Z
    //   325: ifne -> 1256
    //   328: goto -> 1454
    //   331: aload #7
    //   333: ldc 'attribute'
    //   335: invokevirtual equals : (Ljava/lang/Object;)Z
    //   338: ifne -> 1256
    //   341: goto -> 1454
    //   344: aload #7
    //   346: ldc 'class'
    //   348: invokevirtual equals : (Ljava/lang/Object;)Z
    //   351: ifne -> 585
    //   354: goto -> 1454
    //   357: aload #7
    //   359: ldc 'level'
    //   361: invokevirtual equals : (Ljava/lang/Object;)Z
    //   364: ifne -> 383
    //   367: goto -> 1454
    //   370: aload #7
    //   372: ldc 'defensetype'
    //   374: invokevirtual equals : (Ljava/lang/Object;)Z
    //   377: ifne -> 847
    //   380: goto -> 1454
    //   383: iconst_m1
    //   384: istore #8
    //   386: aload_3
    //   387: iconst_2
    //   388: aaload
    //   389: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   392: ifeq -> 403
    //   395: aload_3
    //   396: iconst_2
    //   397: aaload
    //   398: invokestatic parseInt : (Ljava/lang/String;)I
    //   401: istore #8
    //   403: aload_3
    //   404: arraylength
    //   405: iconst_4
    //   406: if_icmpne -> 426
    //   409: aload_3
    //   410: iconst_3
    //   411: aaload
    //   412: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   415: ifeq -> 426
    //   418: aload_3
    //   419: iconst_3
    //   420: aaload
    //   421: invokestatic parseInt : (Ljava/lang/String;)I
    //   424: istore #6
    //   426: aload #5
    //   428: iload #8
    //   430: iload #6
    //   432: invokestatic setLevelRequirement : (Lorg/bukkit/inventory/ItemStack;II)Lorg/bukkit/inventory/ItemStack;
    //   435: astore #5
    //   437: aload #4
    //   439: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   444: aload #5
    //   446: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   451: goto -> 1461
    //   454: aload_3
    //   455: arraylength
    //   456: iconst_3
    //   457: if_icmpge -> 467
    //   460: aload_0
    //   461: aload #4
    //   463: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   466: return
    //   467: aconst_null
    //   468: astore #8
    //   470: aload_3
    //   471: iconst_2
    //   472: aaload
    //   473: invokevirtual toUpperCase : ()Ljava/lang/String;
    //   476: invokestatic valueOf : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/types/QSlotType;
    //   479: astore #8
    //   481: goto -> 536
    //   484: astore #9
    //   486: aload #4
    //   488: new java/lang/StringBuilder
    //   491: dup
    //   492: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   495: invokevirtual toMsg : ()Ljava/lang/String;
    //   498: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   501: invokespecial <init> : (Ljava/lang/String;)V
    //   504: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidType : Lsu/nightexpress/quantumrpg/config/Lang;
    //   507: invokevirtual toMsg : ()Ljava/lang/String;
    //   510: ldc '%s'
    //   512: ldc su/nightexpress/quantumrpg/types/QSlotType
    //   514: ldc '&c'
    //   516: ldc '&7'
    //   518: invokestatic getEnums : (Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   521: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   524: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   527: invokevirtual toString : ()Ljava/lang/String;
    //   530: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   535: return
    //   536: aload_3
    //   537: arraylength
    //   538: iconst_4
    //   539: if_icmpne -> 559
    //   542: aload_3
    //   543: iconst_3
    //   544: aaload
    //   545: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   548: ifeq -> 559
    //   551: aload_3
    //   552: iconst_3
    //   553: aaload
    //   554: invokestatic parseInt : (Ljava/lang/String;)I
    //   557: istore #6
    //   559: aload #5
    //   561: aload #8
    //   563: iload #6
    //   565: invokestatic addDivineSlot : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/types/QSlotType;I)V
    //   568: aload #4
    //   570: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   575: aload #5
    //   577: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   582: goto -> 1461
    //   585: aload_3
    //   586: arraylength
    //   587: iconst_3
    //   588: if_icmpge -> 598
    //   591: aload_0
    //   592: aload #4
    //   594: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   597: return
    //   598: aload_3
    //   599: arraylength
    //   600: iconst_4
    //   601: if_icmpne -> 621
    //   604: aload_3
    //   605: iconst_3
    //   606: aaload
    //   607: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   610: ifeq -> 621
    //   613: aload_3
    //   614: iconst_3
    //   615: aaload
    //   616: invokestatic parseInt : (Ljava/lang/String;)I
    //   619: istore #6
    //   621: aload #5
    //   623: aload_3
    //   624: iconst_2
    //   625: aaload
    //   626: ldc ','
    //   628: invokevirtual split : (Ljava/lang/String;)[Ljava/lang/String;
    //   631: iload #6
    //   633: invokestatic setClassRequirement : (Lorg/bukkit/inventory/ItemStack;[Ljava/lang/String;I)Lorg/bukkit/inventory/ItemStack;
    //   636: astore #5
    //   638: aload #4
    //   640: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   645: aload #5
    //   647: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   652: goto -> 1461
    //   655: aload_3
    //   656: arraylength
    //   657: iconst_5
    //   658: if_icmpge -> 668
    //   661: aload_0
    //   662: aload #4
    //   664: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   667: return
    //   668: dconst_0
    //   669: dstore #8
    //   671: dconst_0
    //   672: dstore #10
    //   674: aload_3
    //   675: iconst_3
    //   676: aaload
    //   677: invokestatic parseDouble : (Ljava/lang/String;)D
    //   680: dstore #8
    //   682: aload_3
    //   683: iconst_4
    //   684: aaload
    //   685: invokestatic parseDouble : (Ljava/lang/String;)D
    //   688: dstore #10
    //   690: goto -> 763
    //   693: astore #12
    //   695: aload #4
    //   697: new java/lang/StringBuilder
    //   700: dup
    //   701: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   704: invokevirtual toMsg : ()Ljava/lang/String;
    //   707: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   710: invokespecial <init> : (Ljava/lang/String;)V
    //   713: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidNumber : Lsu/nightexpress/quantumrpg/config/Lang;
    //   716: invokevirtual toMsg : ()Ljava/lang/String;
    //   719: ldc '%s'
    //   721: new java/lang/StringBuilder
    //   724: dup
    //   725: aload_3
    //   726: iconst_3
    //   727: aaload
    //   728: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   731: invokespecial <init> : (Ljava/lang/String;)V
    //   734: ldc ' or '
    //   736: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   739: aload_3
    //   740: iconst_4
    //   741: aaload
    //   742: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   745: invokevirtual toString : ()Ljava/lang/String;
    //   748: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   751: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   754: invokevirtual toString : ()Ljava/lang/String;
    //   757: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   762: return
    //   763: aload_3
    //   764: arraylength
    //   765: bipush #6
    //   767: if_icmpne -> 787
    //   770: aload_3
    //   771: iconst_5
    //   772: aaload
    //   773: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   776: ifeq -> 787
    //   779: aload_3
    //   780: iconst_5
    //   781: aaload
    //   782: invokestatic parseInt : (Ljava/lang/String;)I
    //   785: istore #6
    //   787: aload_3
    //   788: iconst_2
    //   789: aaload
    //   790: invokestatic getDamageTypeById : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/types/DamageType;
    //   793: astore #12
    //   795: aload #12
    //   797: ifnonnull -> 831
    //   800: aload_1
    //   801: new java/lang/StringBuilder
    //   804: dup
    //   805: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   808: invokevirtual toMsg : ()Ljava/lang/String;
    //   811: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   814: invokespecial <init> : (Ljava/lang/String;)V
    //   817: ldc 'Invalid damage type!'
    //   819: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   822: invokevirtual toString : ()Ljava/lang/String;
    //   825: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   830: return
    //   831: aload #5
    //   833: aload #12
    //   835: dload #8
    //   837: dload #10
    //   839: iload #6
    //   841: invokestatic addDamageType : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/types/DamageType;DDI)V
    //   844: goto -> 1461
    //   847: aload_3
    //   848: arraylength
    //   849: iconst_4
    //   850: if_icmpge -> 860
    //   853: aload_0
    //   854: aload #4
    //   856: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   859: return
    //   860: ldc2_w -1.0
    //   863: dstore #8
    //   865: aload_3
    //   866: iconst_3
    //   867: aaload
    //   868: invokestatic parseDouble : (Ljava/lang/String;)D
    //   871: dstore #8
    //   873: goto -> 922
    //   876: astore #10
    //   878: aload #4
    //   880: new java/lang/StringBuilder
    //   883: dup
    //   884: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   887: invokevirtual toMsg : ()Ljava/lang/String;
    //   890: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   893: invokespecial <init> : (Ljava/lang/String;)V
    //   896: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidNumber : Lsu/nightexpress/quantumrpg/config/Lang;
    //   899: invokevirtual toMsg : ()Ljava/lang/String;
    //   902: ldc '%s'
    //   904: aload_3
    //   905: iconst_3
    //   906: aaload
    //   907: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   910: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   913: invokevirtual toString : ()Ljava/lang/String;
    //   916: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   921: return
    //   922: aload_3
    //   923: arraylength
    //   924: iconst_5
    //   925: if_icmpne -> 945
    //   928: aload_3
    //   929: iconst_4
    //   930: aaload
    //   931: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   934: ifeq -> 945
    //   937: aload_3
    //   938: iconst_4
    //   939: aaload
    //   940: invokestatic parseInt : (Ljava/lang/String;)I
    //   943: istore #6
    //   945: aload_3
    //   946: iconst_2
    //   947: aaload
    //   948: invokestatic getArmorTypeById : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/types/ArmorType;
    //   951: astore #10
    //   953: aload #10
    //   955: ifnonnull -> 989
    //   958: aload_1
    //   959: new java/lang/StringBuilder
    //   962: dup
    //   963: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   966: invokevirtual toMsg : ()Ljava/lang/String;
    //   969: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   972: invokespecial <init> : (Ljava/lang/String;)V
    //   975: ldc 'Invalid armor type!'
    //   977: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   980: invokevirtual toString : ()Ljava/lang/String;
    //   983: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   988: return
    //   989: aload #5
    //   991: aload #10
    //   993: dload #8
    //   995: iload #6
    //   997: invokestatic addDefenseType : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/types/ArmorType;DI)V
    //   1000: goto -> 1461
    //   1003: aload_3
    //   1004: arraylength
    //   1005: iconst_3
    //   1006: if_icmpge -> 1016
    //   1009: aload_0
    //   1010: aload #4
    //   1012: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   1015: return
    //   1016: aload_3
    //   1017: iconst_2
    //   1018: aaload
    //   1019: invokevirtual toUpperCase : ()Ljava/lang/String;
    //   1022: invokestatic valueOf : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/types/AmmoType;
    //   1025: astore #8
    //   1027: goto -> 1082
    //   1030: astore #9
    //   1032: aload #4
    //   1034: new java/lang/StringBuilder
    //   1037: dup
    //   1038: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1041: invokevirtual toMsg : ()Ljava/lang/String;
    //   1044: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   1047: invokespecial <init> : (Ljava/lang/String;)V
    //   1050: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidType : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1053: invokevirtual toMsg : ()Ljava/lang/String;
    //   1056: ldc '%s'
    //   1058: ldc su/nightexpress/quantumrpg/types/AmmoType
    //   1060: ldc '&a'
    //   1062: ldc '&7'
    //   1064: invokestatic getEnums : (Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   1067: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   1070: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1073: invokevirtual toString : ()Ljava/lang/String;
    //   1076: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   1081: return
    //   1082: aload_3
    //   1083: arraylength
    //   1084: iconst_4
    //   1085: if_icmpne -> 1105
    //   1088: aload_3
    //   1089: iconst_3
    //   1090: aaload
    //   1091: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   1094: ifeq -> 1105
    //   1097: aload_3
    //   1098: iconst_3
    //   1099: aaload
    //   1100: invokestatic parseInt : (Ljava/lang/String;)I
    //   1103: istore #6
    //   1105: aload #4
    //   1107: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   1112: aload #5
    //   1114: aload #8
    //   1116: iload #6
    //   1118: invokestatic setAmmoType : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/types/AmmoType;I)Lorg/bukkit/inventory/ItemStack;
    //   1121: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   1126: goto -> 1461
    //   1129: aload_3
    //   1130: arraylength
    //   1131: iconst_3
    //   1132: if_icmpge -> 1142
    //   1135: aload_0
    //   1136: aload #4
    //   1138: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   1141: return
    //   1142: aload_3
    //   1143: iconst_2
    //   1144: aaload
    //   1145: invokevirtual toUpperCase : ()Ljava/lang/String;
    //   1148: invokestatic valueOf : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/types/WpnHand;
    //   1151: astore #8
    //   1153: goto -> 1209
    //   1156: astore #9
    //   1158: aload #4
    //   1160: new java/lang/StringBuilder
    //   1163: dup
    //   1164: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1167: invokevirtual toMsg : ()Ljava/lang/String;
    //   1170: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   1173: invokespecial <init> : (Ljava/lang/String;)V
    //   1176: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidType : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1179: invokevirtual toMsg : ()Ljava/lang/String;
    //   1182: ldc '%s'
    //   1184: ldc_w su/nightexpress/quantumrpg/types/WpnHand
    //   1187: ldc '&a'
    //   1189: ldc '&7'
    //   1191: invokestatic getEnums : (Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   1194: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   1197: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1200: invokevirtual toString : ()Ljava/lang/String;
    //   1203: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   1208: return
    //   1209: aload_3
    //   1210: arraylength
    //   1211: iconst_4
    //   1212: if_icmpne -> 1232
    //   1215: aload_3
    //   1216: iconst_3
    //   1217: aaload
    //   1218: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   1221: ifeq -> 1232
    //   1224: aload_3
    //   1225: iconst_3
    //   1226: aaload
    //   1227: invokestatic parseInt : (Ljava/lang/String;)I
    //   1230: istore #6
    //   1232: aload #4
    //   1234: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   1239: aload #5
    //   1241: aload #8
    //   1243: iload #6
    //   1245: invokestatic setHandType : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/types/WpnHand;I)Lorg/bukkit/inventory/ItemStack;
    //   1248: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   1253: goto -> 1461
    //   1256: aload_3
    //   1257: arraylength
    //   1258: iconst_4
    //   1259: if_icmpge -> 1269
    //   1262: aload_0
    //   1263: aload #4
    //   1265: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   1268: return
    //   1269: aconst_null
    //   1270: astore #8
    //   1272: aload_3
    //   1273: iconst_2
    //   1274: aaload
    //   1275: invokevirtual toUpperCase : ()Ljava/lang/String;
    //   1278: invokestatic valueOf : (Ljava/lang/String;)Lsu/nightexpress/quantumrpg/stats/ItemStat;
    //   1281: astore #8
    //   1283: goto -> 1341
    //   1286: astore #9
    //   1288: aload #4
    //   1290: new java/lang/StringBuilder
    //   1293: dup
    //   1294: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1297: invokevirtual toMsg : ()Ljava/lang/String;
    //   1300: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   1303: invokespecial <init> : (Ljava/lang/String;)V
    //   1306: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidType : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1309: invokevirtual toMsg : ()Ljava/lang/String;
    //   1312: ldc '%s'
    //   1314: ldc_w su/nightexpress/quantumrpg/stats/ItemStat
    //   1317: ldc_w '§a'
    //   1320: ldc_w '§7'
    //   1323: invokestatic getEnums : (Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   1326: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   1329: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1332: invokevirtual toString : ()Ljava/lang/String;
    //   1335: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   1340: return
    //   1341: dconst_0
    //   1342: dstore #9
    //   1344: aload_3
    //   1345: iconst_3
    //   1346: aaload
    //   1347: invokestatic parseDouble : (Ljava/lang/String;)D
    //   1350: dstore #9
    //   1352: goto -> 1401
    //   1355: astore #11
    //   1357: aload #4
    //   1359: new java/lang/StringBuilder
    //   1362: dup
    //   1363: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1366: invokevirtual toMsg : ()Ljava/lang/String;
    //   1369: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   1372: invokespecial <init> : (Ljava/lang/String;)V
    //   1375: getstatic su/nightexpress/quantumrpg/config/Lang.Other_InvalidNumber : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1378: invokevirtual toMsg : ()Ljava/lang/String;
    //   1381: ldc '%s'
    //   1383: aload_3
    //   1384: iconst_3
    //   1385: aaload
    //   1386: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   1389: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1392: invokevirtual toString : ()Ljava/lang/String;
    //   1395: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   1400: return
    //   1401: aload_3
    //   1402: arraylength
    //   1403: iconst_5
    //   1404: if_icmpne -> 1424
    //   1407: aload_3
    //   1408: iconst_4
    //   1409: aaload
    //   1410: invokestatic isNumeric : (Ljava/lang/String;)Z
    //   1413: ifeq -> 1424
    //   1416: aload_3
    //   1417: iconst_4
    //   1418: aaload
    //   1419: invokestatic parseInt : (Ljava/lang/String;)I
    //   1422: istore #6
    //   1424: aload #5
    //   1426: aload #8
    //   1428: dload #9
    //   1430: iload #6
    //   1432: invokestatic addItemStat : (Lorg/bukkit/inventory/ItemStack;Lsu/nightexpress/quantumrpg/stats/ItemStat;DI)Lorg/bukkit/inventory/ItemStack;
    //   1435: astore #5
    //   1437: aload #4
    //   1439: invokeinterface getInventory : ()Lorg/bukkit/inventory/PlayerInventory;
    //   1444: aload #5
    //   1446: invokeinterface setItemInMainHand : (Lorg/bukkit/inventory/ItemStack;)V
    //   1451: goto -> 1461
    //   1454: aload_0
    //   1455: aload #4
    //   1457: invokespecial printHelp : (Lorg/bukkit/entity/Player;)V
    //   1460: return
    //   1461: aload_1
    //   1462: new java/lang/StringBuilder
    //   1465: dup
    //   1466: getstatic su/nightexpress/quantumrpg/config/Lang.Prefix : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1469: invokevirtual toMsg : ()Ljava/lang/String;
    //   1472: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   1475: invokespecial <init> : (Ljava/lang/String;)V
    //   1478: getstatic su/nightexpress/quantumrpg/config/Lang.Admin_Set : Lsu/nightexpress/quantumrpg/config/Lang;
    //   1481: invokevirtual toMsg : ()Ljava/lang/String;
    //   1484: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1487: invokevirtual toString : ()Ljava/lang/String;
    //   1490: invokeinterface sendMessage : (Ljava/lang/String;)V
    //   1495: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #45	-> 0
    //   #46	-> 6
    //   #47	-> 34
    //   #46	-> 42
    //   #49	-> 52
    //   #52	-> 53
    //   #53	-> 59
    //   #55	-> 73
    //   #56	-> 89
    //   #57	-> 124
    //   #60	-> 125
    //   #62	-> 128
    //   #64	-> 383
    //   #65	-> 386
    //   #66	-> 395
    //   #68	-> 403
    //   #69	-> 418
    //   #71	-> 426
    //   #72	-> 437
    //   #74	-> 451
    //   #78	-> 454
    //   #79	-> 460
    //   #80	-> 466
    //   #83	-> 467
    //   #85	-> 470
    //   #86	-> 481
    //   #87	-> 484
    //   #88	-> 486
    //   #89	-> 535
    //   #92	-> 536
    //   #93	-> 551
    //   #96	-> 559
    //   #97	-> 568
    //   #99	-> 582
    //   #102	-> 585
    //   #103	-> 591
    //   #104	-> 597
    //   #106	-> 598
    //   #107	-> 613
    //   #110	-> 621
    //   #111	-> 638
    //   #112	-> 652
    //   #115	-> 655
    //   #116	-> 661
    //   #117	-> 667
    //   #120	-> 668
    //   #121	-> 671
    //   #123	-> 674
    //   #124	-> 682
    //   #125	-> 690
    //   #126	-> 693
    //   #127	-> 695
    //   #128	-> 762
    //   #131	-> 763
    //   #132	-> 779
    //   #135	-> 787
    //   #136	-> 795
    //   #137	-> 800
    //   #138	-> 830
    //   #141	-> 831
    //   #143	-> 844
    //   #147	-> 847
    //   #148	-> 853
    //   #149	-> 859
    //   #152	-> 860
    //   #154	-> 865
    //   #155	-> 873
    //   #156	-> 876
    //   #157	-> 878
    //   #158	-> 921
    //   #161	-> 922
    //   #162	-> 937
    //   #165	-> 945
    //   #166	-> 953
    //   #167	-> 958
    //   #168	-> 988
    //   #171	-> 989
    //   #173	-> 1000
    //   #176	-> 1003
    //   #177	-> 1009
    //   #178	-> 1015
    //   #183	-> 1016
    //   #184	-> 1027
    //   #185	-> 1030
    //   #186	-> 1032
    //   #187	-> 1081
    //   #190	-> 1082
    //   #191	-> 1097
    //   #194	-> 1105
    //   #195	-> 1126
    //   #198	-> 1129
    //   #199	-> 1135
    //   #200	-> 1141
    //   #205	-> 1142
    //   #206	-> 1153
    //   #207	-> 1156
    //   #208	-> 1158
    //   #209	-> 1208
    //   #212	-> 1209
    //   #213	-> 1224
    //   #216	-> 1232
    //   #217	-> 1253
    //   #221	-> 1256
    //   #222	-> 1262
    //   #223	-> 1268
    //   #226	-> 1269
    //   #228	-> 1272
    //   #229	-> 1283
    //   #230	-> 1286
    //   #231	-> 1288
    //   #232	-> 1340
    //   #235	-> 1341
    //   #237	-> 1344
    //   #238	-> 1352
    //   #239	-> 1355
    //   #240	-> 1357
    //   #241	-> 1400
    //   #244	-> 1401
    //   #245	-> 1416
    //   #248	-> 1424
    //   #249	-> 1437
    //   #251	-> 1451
    //   #254	-> 1454
    //   #255	-> 1460
    //   #258	-> 1461
    //   #259	-> 1495
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	1496	0	this	Lsu/nightexpress/quantumrpg/cmds/list/SetCommand;
    //   0	1496	1	sender	Lorg/bukkit/command/CommandSender;
    //   0	1496	2	label	Ljava/lang/String;
    //   0	1496	3	args	[Ljava/lang/String;
    //   34	8	4	s	Ljava/lang/String;
    //   59	1437	4	p	Lorg/bukkit/entity/Player;
    //   73	1423	5	item	Lorg/bukkit/inventory/ItemStack;
    //   128	1368	6	line	I
    //   386	68	8	level	I
    //   470	115	8	st	Lsu/nightexpress/quantumrpg/types/QSlotType;
    //   486	50	9	ex	Ljava/lang/IllegalArgumentException;
    //   671	176	8	val1	D
    //   674	173	10	val2	D
    //   695	68	12	ex	Ljava/lang/NumberFormatException;
    //   795	52	12	dt	Lsu/nightexpress/quantumrpg/types/DamageType;
    //   865	138	8	amount	D
    //   878	44	10	ex	Ljava/lang/NumberFormatException;
    //   953	50	10	dt	Lsu/nightexpress/quantumrpg/types/ArmorType;
    //   1027	3	8	dt	Lsu/nightexpress/quantumrpg/types/AmmoType;
    //   1082	47	8	dt	Lsu/nightexpress/quantumrpg/types/AmmoType;
    //   1032	50	9	ex	Ljava/lang/IllegalArgumentException;
    //   1153	3	8	dt	Lsu/nightexpress/quantumrpg/types/WpnHand;
    //   1209	47	8	dt	Lsu/nightexpress/quantumrpg/types/WpnHand;
    //   1158	51	9	ex	Ljava/lang/IllegalArgumentException;
    //   1272	182	8	at	Lsu/nightexpress/quantumrpg/stats/ItemStat;
    //   1288	53	9	ex	Ljava/lang/IllegalArgumentException;
    //   1344	110	9	val	D
    //   1357	44	11	ex	Ljava/lang/NumberFormatException;
    // Exception table:
    //   from	to	target	type
    //   470	481	484	java/lang/IllegalArgumentException
    //   674	690	693	java/lang/NumberFormatException
    //   865	873	876	java/lang/NumberFormatException
    //   1016	1027	1030	java/lang/IllegalArgumentException
    //   1142	1153	1156	java/lang/IllegalArgumentException
    //   1272	1283	1286	java/lang/IllegalArgumentException
    //   1344	1352	1355	java/lang/NumberFormatException
  }
  
  private void printHelp(Player p) {
    for (String s : Lang.Help_Set.getList())
      p.sendMessage(s); 
  }
  
  public String getPermission() {
    return "qrpg.admin";
  }
  
  public boolean playersOnly() {
    return true;
  }
}
