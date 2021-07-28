package su.nightexpress.quantumrpg.modules.party;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUI;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.nbt.NBTItem;

public class PartyGUI extends GUI {
  private PartyManager m;
  
  private final String NBT_MEM = "PARTY_MEMBER";
  
  public PartyGUI(PartyManager m, String title, int size, LinkedHashMap<String, GUIItem> items) {
    super(m.pl(), title, size, items);
    this.m = m;
  }
  
  public void openPartyGUI(Player p, PartyManager.Party party) {
    p.openInventory(build(new Object[] { party }));
  }
  
  public Inventory build(Object... val) {
    PartyManager.Party party = (PartyManager.Party)val[0];
    Inventory inv = getInventory();
    for (GUIItem gi : this.items.values()) {
      ItemStack item = gi.getItem().clone();
      item = replacePartyVars(item, party);
      if (gi.getType() == ContentType.PARTY_MEMBER) {
        int size = party.getMembers().size();
        List<PartyManager.PartyMember> list = new ArrayList<>(party.getMembers());
        for (int j = 0; j < size; j++) {
          PartyManager.PartyMember pm = list.get(j);
          ItemStack item2 = new ItemStack(item);
          item2 = setMemberTag(item2, pm);
          item2 = replaceMemberVars(item2, pm);
          int slot = gi.getSlots()[j];
          inv.setItem(slot, item2);
        } 
        continue;
      } 
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        inv.setItem(j, item);
        b++;
      } 
    } 
    return inv;
  }
  
  private ItemStack replaceMemberVars(ItemStack item, PartyManager.PartyMember pm) {
    ItemMeta meta = item.getItemMeta();
    if (meta.hasDisplayName()) {
      String n = meta.getDisplayName()
        .replace("%online%", Lang.getBool((pm.getPlayer() != null)))
        .replace("%name%", pm.getName());
      meta.setDisplayName(n);
    } 
    if (meta.hasLore()) {
      List<String> lore = new ArrayList<>();
      for (String s : meta.getLore())
        lore.add(s
            .replace("%online%", Lang.getBool((pm.getPlayer() != null)))
            .replace("%name%", pm.getName())); 
      meta.setLore(lore);
    } 
    item.setItemMeta(meta);
    if (item.getType() == Material.SKULL_ITEM) {
      SkullMeta sm = (SkullMeta)item.getItemMeta();
      sm.setOwner(pm.getName());
      item.setItemMeta(meta);
    } 
    return item;
  }
  
  private ItemStack replacePartyVars(ItemStack item, PartyManager.Party p) {
    ItemMeta meta = item.getItemMeta();
    if (meta.hasDisplayName()) {
      String n = rep(meta.getDisplayName(), p);
      meta.setDisplayName(n);
    } 
    if (meta.hasLore()) {
      List<String> lore = new ArrayList<>();
      for (String s : meta.getLore())
        lore.add(rep(s, p)); 
      meta.setLore(lore);
    } 
    item.setItemMeta(meta);
    return item;
  }
  
  private String rep(String s, PartyManager.Party party) {
    return s.replace("%party_drop%", Lang.getCustom("Party.Drop." + party.getDropMode().name()))
      .replace("%party_leader%", party.getLeader().getName())
      .replace("%party_members%", String.valueOf(party.getMembers().size()))
      .replace("%party_name%", party.getIdName())
      .replace("%party_size%", String.valueOf(party.getSize()));
  }
  
  private ItemStack setMemberTag(ItemStack item, PartyManager.PartyMember pm) {
    NBTItem nbt = new NBTItem(item);
    nbt.setString("PARTY_MEMBER", pm.getUUID().toString());
    return nbt.getItem();
  }
  
  public UUID getMemberUid(ItemStack item) {
    NBTItem nbt = new NBTItem(item);
    return UUID.fromString(nbt.getString("PARTY_MEMBER"));
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    PartyManager.Party party;
    PartyManager.PartyMember pm;
    if (!this.m.isInParty(p)) {
      p.closeInventory();
      return false;
    } 
    switch (type) {
      case PARTY_DISBAND:
        this.m.disbandParty(p);
        p.closeInventory();
        break;
      case PARTY_LEAVE:
        this.m.leaveParty(p);
        p.closeInventory();
        break;
      case PARTY_DROP:
        this.m.togglePartyDrop(p);
        this.m.openPartyGUI(p);
        break;
      case PARTY_CHAT:
        this.m.toggleChat(p);
        break;
      case PARTY_MEMBER:
        party = this.m.getPlayerParty(p);
        pm = party.getMember(getMemberUid(item));
        if (e.isLeftClick()) {
          this.m.teleport(p, pm.getPlayer());
          break;
        } 
        if (e.isRightClick()) {
          this.m.kickFromParty(p, pm.getPlayer());
          this.m.openPartyGUI(p);
        } 
        break;
      case EXIT:
        p.closeInventory();
        break;
    } 
    return false;
  }
}
