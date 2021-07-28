package su.nightexpress.quantumrpg.config;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public enum Lang {
  Prefix("&aQuantumRPG &8›› &7"),
  Help_Main("&8&m-----------&8&l[ &aQuantum RPG &8&l]&8&m-----------\n&2> &a/qrpg set &7- Add item attributes.\n&2> &a/qrpg modify &7- Modify the item in hand.\n&2> &a/qrpg reload &7- Reload the plugin and modules.\n&2> &a/qrpg info &7- Plugin info."),
  Help_Modify("&8&m-----------&8&l[ &aQuantum RPG &7- &aModify &8&l]&8&m-----------\n&7&l&nCommand List:\n&2> &a/qrpg modify name <Name> &7- Change display name of the item.\n&2> &a/qrpg modify lore add [text] [line] &7- Add lore line.\n&2> &a/qrpg modify lore del [line] &7- Remove lore line.\n&2> &a/qrpg modify lore clear &7- Clear item lore.\n&2> &a/qrpg modify flag add <flag> &7- Add a flag to item.\n&2> &a/qrpg modify flag del <flag> &7- Remove a flag from the item.\n&2> &a/qrpg modify nbt add <tag> <value> &7- Add an NBT Tag to item.\n&2> &a/qrpg modify nbt del <tag> &7- Remove NBT Tag from item.\n&2> &a/qrpg modify enchant <enchantment> <Level> &7- Enchants item.\n&2> &a/qrpg modify potion <effect> <level> <duration> [ambient(true/false)] [particles(true/false)] [icon(true/false)] &7- Modify potion effects.\n&2> &a/qrpg modify eggtype <entity type> &7- Change the monster egg type.\n&2> &a/qrpg modify color <r,g,b> &7- Change the color of leather armor."),
  Help_Set("&8&m-----------&8&l[ &aQuantum RPG &7- &aSet &8&l]&8&m-----------\n&7&l&nCommand List:\n&2> &a/qrpg set attribute/stat <type> <value> [line] &7- Set the attribute to item.\n&2> &a/qrpg set bonus <type> <value> [line] &7- Set the bonus stat to item.\n&2> &a/qrpg set level <number> [line] &7- Add level requirements to item.\n&2> &a/qrpg set class <Class1,Class2,Etc> [line] &7- Add class requirements to item.\n&2> &a/qrpg set damagetype <type> <min> <max> [line] &7- Add the damage type to item.\n&2> &a/qrpg set defensetype <type> <value> [line] &7- Add the defense type to item.\n&2> &a/qrpg set ammotype <type> [line] &7- Set the type of ammo to bow.\n&2> &a/qrpg set slot/socket <type> [line] &7- Add an empty slot to item."),
  Module_Cmd_Help_List(
    "&8&m-----------&8&l[ &aQuantum RPG &7- &a%module% &8&l]&8&m-----------\n%cmds%"),
  Module_Cmd_Get_Done("You get &ax%amount% &7of &a%item%&7!"),
  Module_Cmd_Give_Done("You gave &ax%amount% &7of &a%item%&7 to &a%player%&7!"),
  Module_Cmd_Drop_Done("You dropped &ax%amount% &7of &a%item%&7 in &a%w%&7, %x%&7, &a%y%&7, &a%z%&7!"),
  Module_Cmd_Help_Format("&2> &a/%cmd% %label% %usage% &7- %desc%"),
  Module_Cmd_Usage("&cUsage: &f/%cmd% %label% %usage%"),
  Module_Cmd_Reload("Module &a%module% &7reloaded!"),
  Buffs_Get("You just got the %type% buff for %time% seconds. Your %type% has been increased for %value%%"),
  Buffs_End("Your %type% buff's time ended."),
  Buffs_Give("You just gave the %value%% %mod% buff for %time% seconds to %p."),
  Buffs_Invalid("It seems you did a mistake in buff name."),
  Consumables_Cooldown("&cYou must wait &f%time% &cto consume %item% &cagain."),
  Consumables_FullHunger("&cYou can not consume %item% &cbecause your food level is full."),
  Consumables_FullHp("&cYou can not consume %item% &cbecause your health is full."),
  CustomItems_Cooldown("&cYou must wait &e%time%&c to use &f%item% &cagain!"),
  Essences_AlreadyHave("%item% &7already have this essence."),
  Essences_Enchanting_NoSlots("&c&lOops! &7This item do not contains empty essnece sockets!"),
  Essences_Enchanting_InvalidType("&c&lOops! &7This essence can not be applied to this item type!"),
  Essences_Enchanting_BadLevel("&c&lOops! &7This item level is too low or high for this essence!"),
  Essences_Enchanting_Cancel("&4&lCanceled!"),
  Essences_Enchanting_Success("&a&lSuccess! &7Your item have been improved!"),
  Essences_Enchanting_Failure_Item("&c&lFailure! &7Your item have been destroyed!"),
  Essences_Enchanting_Failure_Source("&c&lFailure! &7Your essence have been destroyed!"),
  Essences_Enchanting_Failure_Both("&c&lFailure! &7Your item and essence has been destroyed!"),
  Essences_Enchanting_Failure_Clear("&c&lFailure! &7Your item have lost all active essences!"),
  Extractor_Select("&c&lPlease, select the socket type and item to extract!"),
  Extractor_TooExpensive("&c&lYou can't afford the extraction!"),
  Extractor_Done("&a&lExtraction complete!"),
  Extractor_Cancel("&4&lCancelled."),
  Gems_Enchanting_MultipleNotAllowed("Your item is already have gem &c%gem%&7!"),
  Gems_Enchanting_InvalidType("&c&lOops! &7This gem can not be applied to this item type!"),
  Gems_Enchanting_BadLevel("&c&lOops! &7This item level is too low or high for this gem!"),
  Gems_Enchanting_NoSlots("&c&lOops! &7This item do not contains empty gem slots!"),
  Gems_Enchanting_Cancel("&4&lCanceled!"),
  Gems_Enchanting_Success("&a&lSuccess! &7Your item have been improved!"),
  Gems_Enchanting_Failure_Item("&c&lFailure! &7Your item have been destroyed!"),
  Gems_Enchanting_Failure_Source("&c&lFailure! &7Your gem have been destroyed!"),
  Gems_Enchanting_Failure_Both("&c&lFailure! &7Your item and gem has been destroyed!"),
  Gems_Enchanting_Failure_Clear("&c&lFailure! &7Your item have lost all active gems!"),
  Identify_Cooldown("&cYou must wait &e%time%&c to use &f%item% &cagain!"),
  Identify_WrongTome("&clOops! &7This &cIdentify Tome &7can not identify this item."),
  Identify_NoEquip("You can not equip the unidentified item!"),
  MagicDust_Done("&a&lSuccess! &7The success rate have been increased!"),
  MagicDust_Maximum("The success rate of the item equals &c%rate%%&7. You cant add more!"),
  MagicDust_NoStack("You can not apply Magic Dust on more than 1 item in stack!"),
  Party_Create("Created a new party &a%party%&7. Invite players: &a/party invite <player>&7."),
  Party_CreateIn("You're already in party! Leave it: &e/party leave&7."),
  Party_CreateExist("There is already party with this name."),
  Party_JoinIn("You're already in party! Leave it: &e/party leave&7."),
  Party_Join("&8*** &7You have joined the &d%party% &7party. &8***"),
  Party_JoinNew("&8*** &a%player% &7has joined the party. &8***"),
  Party_LeaderNew("&8*** &7You're now the new party leader. &8***"),
  Party_Invite_Already("This player is already invited to your party."),
  Party_Invite_Send("Invite sended to &e%player%&7."),
  Party_Invite_Get("&8*** &e%leader% &7invites you to the party &e%party%&7. Type &e/party join %party% &8***"),
  Party_Invite_Another("You don't have invite to this party."),
  Party_MaxSize("There are maximum players in party."),
  Party_Disband("Party disbanded: &c%party%&7."),
  Party_Leave("&8*** &7You have left &d%party% &7party. &8***"),
  Party_LeaveNew("&8*** &c%player% &7left the party. &8***"),
  Party_LeaveDisband("&c%leader% &7disbands the party."),
  Party_Kick("&d%leader% &7kicked you from the party."),
  Party_KickSelf("Unable to kick self."),
  Party_KickOther("&c%leader% &7kicked &c%player% &7from the party."),
  Party_AlreadyIn("&c%player% &7is already in a party."),
  Party_PlayerNotIn("Player is not in party."),
  Party_NotIn("You're not in the party."),
  Party_NotLeader("Only party leader can do this."),
  Party_QuitMember("&7*** &d%player% &5has left the game. &7***"),
  Party_BackMember("&7*** &e%player% &6joined the game. &7***"),
  Party_TpSelf("Unable to teleport to self!"),
  Party_TpTo("&8*** &7Teleport to &a%player%&7. &8***"),
  Party_TpFrom("&8*** &a%player% &7teleported to you. &8***"),
  Party_TpCooldown("Teleport cooldown: &c%time%"),
  Party_ChatOn("Party chat: &aEnabled"),
  Party_ChatOff("Party chat: &cDisabled"),
  Party_Drop_FREE("&aFree"),
  Party_Drop_LEADER("&cLeader"),
  Party_Drop_AUTO("&fAuto"),
  Party_Drop_ROLL("&eRoll"),
  Party_Drop_Mode("&8*** &7Drop mode: &e%mode% &8***"),
  Refine_Enchanting_InvalidType("&c&lOops! &7This gem can not be applied to this item type!"),
  Refine_Enchanting_BadLevel("&c&lOops! &7This item level is too low or high for this stone!"),
  Refine_Enchanting_Cancel("&4&lCanceled!"),
  Refine_Enchanting_Success("&a&lSuccess! &7Your item have been improved!"),
  Refine_Enchanting_Failure("&c&lFailure! &7Your item has been downgraded!"),
  Repair_Cancel("&4&lCancelled."),
  Repair_Select("&cPlease, select the repair type!"),
  Repair_NoItem("&cYou must hold an item to repair!"),
  Repair_InvalidItem("&c%item% can not be repaired."),
  Repair_NotDamaged("&c%item% &eis at full durability."),
  Repair_Done("&aYour item have been repaired."),
  Repair_TooExpensive("&cYou can't afford the repair!"),
  Resolve_Done("&a%item% &7resolving complete!"),
  Resolve_Invalid("&c%item% &7can not be resolved!"),
  Resolve_Cancel("&4&lCancelled"),
  Restrictions_NotOwner("&7You &ccan not &7use this item because you are not the owner of them."),
  Restrictions_Level("&7Your level is too low &7(&6%s&7) for using this item."),
  Restrictions_Class("&7Your class &c(%s) &7is not allowed to use this item!"),
  Restrictions_NoCommands("&cYou can not type this command while holding untradable item."),
  Restrictions_Usage("&7You must set the &6Soulbound&7 to use this item. Do &6Right-Click &7on this item in your inventory (press E)."),
  Restrictions_SoulAccept("&7Soulbound have been set &asuccessfully&7!"),
  Restrictions_SoulDecline("&4&lCanceled."),
  Restrictions_Hands_CantHold("Your weapon is two-handed! You can't hold items in off hand!"),
  Runes_Enchanting_InvalidType("&c&lOops! &7This rune can not be applied to this item type!"),
  Runes_Enchanting_BadLevel("&c&lOops! &7This item level is too low or high for this rune!"),
  Runes_Enchanting_NoSlots("&c&lOops! &7This item do not contains empty rune slots!"),
  Runes_Enchanting_AlreadyHave("%item% &7already have this rune!"),
  Runes_Enchanting_Cancel("&4&lCanceled!"),
  Runes_Enchanting_Success("&a&lSuccess! &7Your item have been enchanted!"),
  Runes_Enchanting_Failure_Item("&c&lFailure! &7Your item have been destroyed!"),
  Runes_Enchanting_Failure_Source("&c&lFailure! &7Your rune have been destroyed!"),
  Runes_Enchanting_Failure_Both("&c&lFailure! &7Your item and rune has been destroyed!"),
  Runes_Enchanting_Failure_Clear("&c&lFailure! &7Your item have lost all active runes!"),
  Sell_Sell("Sold for &a$%cost%&7!"),
  Sell_Cancel("&4&lCancelled."),
  Scrolls_Cooldown("Scroll is on cooldown. You need to wait &c%s &7seconds to use it again."),
  Scrolls_Using("&r&lUsing scroll... Don't move."),
  Scrolls_Cancelled("&4&lCanceled."),
  Sets_Invalid("&cSet &7%s &cdoes not exitst."),
  Soulbound_Cmd_Set_Done("Item soulbound: %state%"),
  Soulbound_Cmd_Untradable_Done("Item untradable: %state%"),
  Soulbound_NoDrop("&cYou can not drop untradeable item!"),
  Soulbound_Error_Pickup("&cYou can't pickup item you don't own."),
  Lore_State_true("&a&l✓ &r&a"),
  Lore_State_false("&c&l✗ &r&c"),
  Other_true("&aTrue"),
  Other_false("&cFalse"),
  Other_Right("&fRight"),
  Other_Left("&fLeft"),
  Other_BrokenItem("Your item is broken! You can not use it!"),
  Other_Broadcast("Player &6%p &7found the item &6%item%&7!"),
  Other_Disabled("This item is disabled!"),
  Other_NoPerm("You dont have permissions to do that!"),
  Other_Get("You received: &7<%item%&7>"),
  Other_NotAPotion("The item must be a potion!"),
  Other_NotAnEgg("The item must be an monster egg!"),
  Other_NotALeather("The item must be a leather armor!"),
  Other_InvalidType("&cInvalid type! Available types: %s"),
  Other_InvalidPlayer("&cPlayer not found!"),
  Other_InvalidNumber("&f%s &cis invalid number!"),
  Other_InvalidWorld("&cWorld &7%s &cdoes not exists!"),
  Other_InvalidSender("&cYou must be a player!"),
  Other_InvalidItem("&cYou must hold an item!"),
  Other_InvalidCoordinates("&7%s &care not a valid coordinates!"),
  Other_InvalidRGB("&7%s &care not a valid RGB colors!"),
  Other_Internal("&cInternal error! Contact administration."),
  Time_Sec("sec."),
  Time_Min("m."),
  Time_Hour("h."),
  Admin_NBTSet("NBT Tag &a%s &7successfully setted!"),
  Admin_AttributeSet("Attribute &a%s &7successfully setted!"),
  Admin_Set("Done!"),
  Admin_WrongUsage("Wrong usage! Type &a/di help&7 for help."),
  Admin_Reload("Reload complete!");
  
  private String msg;
  
  private static MyConfig config;
  
  Lang(String msg) {
    this.msg = msg;
  }
  
  public String getPath() {
    return name().replace("_", ".");
  }
  
  public String getMsg() {
    return this.msg;
  }
  
  public String toMsg() {
    return ChatColor.translateAlternateColorCodes('&', config.getConfig().getString(getPath()));
  }
  
  public List<String> getList() {
    List<String> list = new ArrayList<>();
    for (String s : config.getConfig().getStringList(getPath()))
      list.add(ChatColor.translateAlternateColorCodes('&', s)); 
    return list;
  }
  
  public static boolean hasPath(String path) {
    return config.getConfig().contains(path);
  }
  
  public static String getCustom(String path) {
    String s = config.getConfig().getString(path);
    if (s == null)
      return ""; 
    return ChatColor.translateAlternateColorCodes('&', s);
  }
  
  public static void setup(MyConfig config) {
    Lang.config = config;
    load();
  }
  
  public static String getBool(boolean b) {
    return ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("Other." + String.valueOf(b)));
  }
  
  private static void load() {
    byte b;
    int i;
    Lang[] arrayOfLang;
    for (i = (arrayOfLang = values()).length, b = 0; b < i; ) {
      Lang lang = arrayOfLang[b];
      if (config.getConfig().getString(lang.getPath()) == null)
        if (lang.getMsg().contains("\n")) {
          List<String> list = new ArrayList<>();
          String[] ss = lang.getMsg().split("\n");
          byte b1;
          int j;
          String[] arrayOfString1;
          for (j = (arrayOfString1 = ss).length, b1 = 0; b1 < j; ) {
            String s = arrayOfString1[b1];
            list.add(s);
            b1++;
          } 
          config.getConfig().set(lang.getPath(), list);
        } else {
          config.getConfig().set(lang.getPath(), lang.getMsg());
        }  
      b++;
    } 
    EntityType[] arrayOfEntityType;
    for (i = (arrayOfEntityType = EntityType.values()).length, b = 0; b < i; ) {
      EntityType m = arrayOfEntityType[b];
      String n = m.name();
      if (m.isAlive() && config.getConfig().getString("EntityNames." + n) == null)
        config.getConfig().set("EntityNames." + n, WordUtils.capitalizeFully(n.replace("_", " "))); 
      b++;
    } 
    config.save();
  }
}
