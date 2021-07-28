package su.nightexpress.quantumrpg.modules.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.HookUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.party.listeners.SkillAPIListener;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class PartyManager extends QModule {
  private HashMap<String, Party> parts;
  
  private HashMap<String, List<PartyInvite>> invites;
  
  private HashMap<String, Long> tp;
  
  private HashMap<Player, Integer> tasks;
  
  private SkillAPIListener lis_sapi;
  
  private PartyGUI gui;
  
  private PartySettings ss;
  
  public PartyManager(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public EModule type() {
    return EModule.PARTY;
  }
  
  public String name() {
    return "Party";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.parts = new HashMap<>();
    this.invites = new HashMap<>();
    this.tp = new HashMap<>();
    this.tasks = new HashMap<>();
    if (EHook.SKILL_API.isEnabled()) {
      this.lis_sapi = new SkillAPIListener(this);
      this.plugin.getPluginManager().registerEvents((Listener)this.lis_sapi, (Plugin)this.plugin);
    } 
    setupCfg();
  }
  
  public void shutdown() {
    for (Iterator<Integer> iterator = this.tasks.values().iterator(); iterator.hasNext(); ) {
      int i = ((Integer)iterator.next()).intValue();
      this.plugin.getServer().getScheduler().cancelTask(i);
    } 
    this.parts = null;
    this.invites = null;
    this.tp = null;
    this.tasks = null;
    if (EHook.SKILL_API.isEnabled() && this.lis_sapi != null)
      HandlerList.unregisterAll((Listener)this.lis_sapi); 
    this.ss = null;
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  private void setupCfg() {
    JYML jYML = this.cfg.getConfig();
    String path = "party.";
    int p_max_size = jYML.getInt(String.valueOf(path) + "max-size");
    boolean p_lead_quit_disband = jYML.getBoolean(String.valueOf(path) + "leader-leave-disband");
    int p_invite_time = jYML.getInt(String.valueOf(path) + "invite-timeout");
    boolean p_quit_inst_leave = jYML.getBoolean(String.valueOf(path) + "quit-auto-leave");
    int p_quit_time = jYML.getInt(String.valueOf(path) + "quit-timeout");
    int p_tp_cd = jYML.getInt(String.valueOf(path) + "tp-cooldown");
    path = "chat.";
    boolean p_chat = jYML.getBoolean(String.valueOf(path) + "enabled");
    String p_chat_format = jYML.getString(String.valueOf(path) + "format");
    path = "exp-balance.decreasing.";
    boolean exp_dec = jYML.getBoolean(String.valueOf(path) + "enabled");
    double exp_dec_amount = jYML.getDouble(String.valueOf(path) + "by-percent");
    int exp_dec_for = jYML.getInt(String.valueOf(path) + "for-each");
    path = "exp-balance.increasing.";
    boolean exp_inc = jYML.getBoolean(String.valueOf(path) + "enabled");
    double exp_inc_amount = jYML.getDouble(String.valueOf(path) + "by-percent");
    int exp_inc_for = jYML.getInt(String.valueOf(path) + "for-each");
    Map<String, Integer> size_perms = new LinkedHashMap<>();
    if (jYML.isConfigurationSection("size-permissions"))
      for (String rank : jYML.getConfigurationSection("size-permissions").getKeys(false)) {
        int size = jYML.getInt("size-permissions." + rank);
        size_perms.put(rank, Integer.valueOf(size));
      }  
    int q_distance = jYML.getInt("quests.mob-kill-distance");
    Map<PartyAction, Sound> sounds = new HashMap<>();
    byte b;
    int i;
    PartyAction[] arrayOfPartyAction;
    for (i = (arrayOfPartyAction = PartyAction.values()).length, b = 0; b < i; ) {
      PartyAction pa = arrayOfPartyAction[b];
      if (jYML.contains("sounds." + pa.name())) {
        Sound s = null;
        String s2 = jYML.getString("sounds." + pa.name()).toUpperCase();
        try {
          s = Sound.valueOf(s2);
        } catch (IllegalArgumentException ex) {
          log("Invalid sound '" + s2 + "'!", LogType.WARN);
        } 
        sounds.put(pa, s);
      } 
      b++;
    } 
    this.ss = new PartySettings(
        p_max_size, 
        p_lead_quit_disband, 
        p_invite_time, 
        p_quit_inst_leave, 
        p_quit_time, 
        p_tp_cd, 
        
        p_chat, 
        p_chat_format, 
        
        exp_dec, 
        exp_dec_amount, 
        exp_dec_for, 
        
        exp_inc, 
        exp_inc_amount, 
        exp_inc_for, 
        
        size_perms, 
        
        q_distance, 
        
        sounds);
    path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    this.gui = new PartyGUI(this, g_title, g_size, items);
  }
  
  public void toggleChat(Player p) {
    if (!isInParty(p)) {
      out((Entity)p, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(p);
    PartyMember pm = party.getMember(p.getUniqueId());
    pm.toggleChat();
  }
  
  public boolean isInParty(Player p) {
    for (Party pp : getParties()) {
      if (pp != null && pp.isMember(p))
        return true; 
    } 
    return false;
  }
  
  public Party getPlayerParty(Player p) {
    for (Party party : getParties()) {
      if (party.isMember(p))
        return party; 
    } 
    return null;
  }
  
  public void leaveParty(Player p) {
    if (!isInParty(p)) {
      out((Entity)p, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(p);
    party.leave(p);
  }
  
  public void createParty(Player creator, String name) {
    if (isInParty(creator)) {
      out((Entity)creator, Lang.Party_CreateIn.toMsg());
      return;
    } 
    if (name == null)
      name = creator.getName(); 
    if (!isIdFree(name)) {
      out((Entity)creator, Lang.Party_CreateExist.toMsg());
      return;
    } 
    int size = this.ss.getPartyPermSize(creator);
    Party party = new Party(name, creator, size);
    party.addMember(creator);
    this.parts.put(party.getIdName(), party);
    out((Entity)creator, Lang.Party_Create.toMsg().replace("%party%", name));
    this.ss.playSound(creator, PartyAction.CREATE);
  }
  
  public void joinParty(Player p, String id) {
    updateInvites(p);
    if (isInParty(p)) {
      out((Entity)p, Lang.Party_JoinIn.toMsg());
      return;
    } 
    Party party = getPartyById(id);
    if (!isInvited(p, party)) {
      out((Entity)p, Lang.Party_Invite_Another.toMsg());
      return;
    } 
    if (party.getMembers().size() >= party.getSize()) {
      out((Entity)p, Lang.Party_MaxSize.toMsg());
      return;
    } 
    out((Entity)p, Lang.Party_Join.toMsg().replace("%party%", party.getIdName()));
    for (PartyMember pm : party.getMembers()) {
      Player p1 = pm.getPlayer();
      if (p1 == null)
        continue; 
      out((Entity)p1, Lang.Party_JoinNew.toMsg().replace("%player%", p.getName()));
      this.ss.playSound(p1, PartyAction.JOIN);
    } 
    party.addMember(p);
    updateInvites(p);
  }
  
  public void invitePlayer(Player from, Player to) {
    if (to == null) {
      out((Entity)from, Lang.Other_InvalidPlayer.toMsg());
      return;
    } 
    if (!isInParty(from)) {
      out((Entity)from, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(from);
    if (!party.isLeader(from)) {
      out((Entity)from, Lang.Party_NotLeader.toMsg());
      return;
    } 
    if (party.getMembers().size() >= party.getSize()) {
      out((Entity)from, Lang.Party_MaxSize.toMsg());
      return;
    } 
    if (isInParty(to)) {
      out((Entity)from, Lang.Party_AlreadyIn.toMsg().replace("%player%", to.getName()));
      return;
    } 
    if (isInvited(to, party)) {
      out((Entity)from, Lang.Party_Invite_Already.toMsg());
      return;
    } 
    PartyInvite inv = new PartyInvite(party);
    addInvite(to, inv);
    out((Entity)from, Lang.Party_Invite_Send.toMsg().replace("%player%", to.getName()));
    out((Entity)to, Lang.Party_Invite_Get.toMsg().replace("%leader%", from.getName()).replace("%party%", party.getIdName()));
    this.ss.playSound(to, PartyAction.INVITE);
  }
  
  private void addInvite(Player p, PartyInvite pi) {
    String s = p.getName();
    List<PartyInvite> list = null;
    if (this.invites.containsKey(s))
      list = this.invites.get(s); 
    if (list == null)
      list = new ArrayList<>(); 
    list.add(pi);
    this.invites.put(s, list);
  }
  
  public boolean isInvited(Player p, Party party) {
    updateInvites(p);
    if (party == null)
      return false; 
    String s = p.getName();
    if (this.invites.containsKey(s))
      for (PartyInvite pi : this.invites.get(s)) {
        if (pi.getPartyId().equalsIgnoreCase(party.getIdName())) {
          if (!pi.isExpired())
            return true; 
          return false;
        } 
      }  
    return false;
  }
  
  private void updateInvites(Player p) {
    String s = p.getName();
    List<PartyInvite> list = new ArrayList<>();
    if (this.invites.containsKey(s))
      for (PartyInvite pi : this.invites.get(s)) {
        Party party = getPartyById(pi.getPartyId());
        if (party != null && !party.isMember(p) && 
          !pi.isExpired())
          list.add(pi); 
      }  
    this.invites.put(s, list);
  }
  
  public void kickFromParty(Player lead, Player who) {
    if (!isInParty(lead)) {
      out((Entity)lead, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(lead);
    if (!party.isLeader(lead)) {
      out((Entity)lead, Lang.Party_NotLeader.toMsg());
      return;
    } 
    if (who == null) {
      out((Entity)lead, Lang.Other_InvalidPlayer.toMsg());
      return;
    } 
    if (!party.isMember(who)) {
      out((Entity)lead, Lang.Party_PlayerNotIn.toMsg().replace("%player%", who.getName()));
      return;
    } 
    if (lead.equals(who)) {
      out((Entity)lead, Lang.Party_KickSelf.toMsg());
      return;
    } 
    party.delMember(who);
    for (PartyMember pm : party.getMembers()) {
      Player p1 = pm.getPlayer();
      if (p1 == null)
        continue; 
      out((Entity)p1, Lang.Party_KickOther.toMsg().replace("%leader%", lead.getName()).replace("%player%", who.getName()));
      this.ss.playSound(p1, PartyAction.KICK);
    } 
    out((Entity)who, Lang.Party_Kick.toMsg().replace("%leader%", lead.getName()).replace("%party%", party.getIdName()));
    this.ss.playSound(who, PartyAction.KICK);
  }
  
  public void disbandParty(Player pl) {
    if (!isInParty(pl)) {
      out((Entity)pl, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party p = getPlayerParty(pl);
    if (!p.isLeader(pl)) {
      out((Entity)pl, Lang.Party_NotLeader.toMsg());
      return;
    } 
    p.disband();
    p = null;
  }
  
  public void teleport(Player from, Player to) {
    if (!isInParty(from)) {
      out((Entity)from, Lang.Party_NotIn.toMsg());
      return;
    } 
    long time = System.currentTimeMillis();
    if (this.tp.containsKey(from.getName())) {
      long time_cd = ((Long)this.tp.get(from.getName())).longValue();
      if (time < time_cd) {
        String lost = Utils.getTimeLeft(time_cd, time);
        out((Entity)from, Lang.Party_TpCooldown.toMsg().replace("%time%", lost));
        return;
      } 
    } 
    if (to == null) {
      out((Entity)from, Lang.Other_InvalidPlayer.toMsg());
      return;
    } 
    if (from.equals(to)) {
      out((Entity)from, Lang.Party_TpSelf.toMsg());
      return;
    } 
    Party pp = getPlayerParty(from);
    if (!pp.isMember(to)) {
      out((Entity)from, Lang.Party_PlayerNotIn.toMsg().replace("%player%", to.getName()));
      return;
    } 
  }
  
  public void togglePartyDrop(Player p) {
    if (!isInParty(p)) {
      out((Entity)p, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(p);
    if (!party.isLeader(p)) {
      out((Entity)p, Lang.Party_NotLeader.toMsg());
      return;
    } 
    party.toggleDrop();
  }
  
  public boolean isIdFree(String id) {
    for (String id2 : getPartyIds()) {
      if (id2.equalsIgnoreCase(id))
        return false; 
    } 
    return true;
  }
  
  public void addParty(Party p) {
    this.parts.put(p.getIdName(), p);
  }
  
  public Party getPartyById(String id) {
    return this.parts.get(id);
  }
  
  public Collection<String> getPartyIds() {
    return this.parts.keySet();
  }
  
  public Collection<Party> getParties() {
    return this.parts.values();
  }
  
  public double getExpPercentByLevel(Player p, Entity e) {
    double reduce_exp = 100.0D;
    double inc_exp = 100.0D;
    double m_lvl = 0.0D;
    double p_lvl = Config.getLevelPlugin().getLevel(p);
    if (m_lvl == p_lvl)
      return 1.0D; 
    if (m_lvl < p_lvl) {
      if (this.ss.isDecreaseExp())
        reduce_exp = Math.min(100.0D, (p_lvl - m_lvl) / this.ss.getDecExpFor() * this.ss.getDecExpPercent()); 
      return 1.0D - reduce_exp / 100.0D;
    } 
    if (this.ss.isIncreaseExp())
      inc_exp = Math.min(100.0D, (m_lvl - p_lvl) / this.ss.getIncExpFor() * this.ss.getIncExpPercent()); 
    if (inc_exp == 100.0D)
      return 1.0D; 
    return 1.0D + inc_exp / 100.0D;
  }
  
  public void openPartyGUI(Player p) {
    if (!isInParty(p)) {
      out((Entity)p, Lang.Party_NotIn.toMsg());
      return;
    } 
    Party party = getPlayerParty(p);
    this.gui.openPartyGUI(p, party);
  }
  
  public PartySettings getSettings() {
    return this.ss;
  }
  
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    if (!isInParty(p))
      return; 
    Party party = getPlayerParty(p);
    PartyMember pm = party.getMember(p.getUniqueId());
    if (!pm.isInChat())
      return; 
    e.getRecipients().clear();
    for (PartyMember pm2 : party.getMembers()) {
      Player p1 = pm2.getPlayer();
      if (p1 == null)
        continue; 
      e.getRecipients().add(p1);
    } 
    String format = String.valueOf(this.ss.getChatFormat().replace("%p", p.getName())) + e.getMessage();
    if (EHook.PLACEHOLDER_API.isEnabled())
      format = PlaceholderAPI.setPlaceholders(p, format); 
    e.setFormat(format);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onDamage(EntityDamageByEntityEvent e) {
    Entity e1 = e.getEntity();
    Entity e2 = e.getDamager();
    if (e1 == null || e2 == null)
      return; 
    if (!(e1 instanceof Player))
      return; 
    if (!(e2 instanceof Player))
      return; 
    Player p1 = (Player)e1;
    Player p2 = (Player)e2;
    if (!isInParty(p1) || !isInParty(p2))
      return; 
    for (Party pp : getParties()) {
      if (pp.isMember(p1) && pp.isMember(p2)) {
        e.setCancelled(true);
        return;
      } 
    } 
  }
  
  @EventHandler
  public void onLeave(PlayerQuitEvent e) {
    final Player p = e.getPlayer();
    if (!isInParty(p))
      return; 
    Party party = getPlayerParty(p);
    for (PartyMember pm : party.getMembers()) {
      Player p1 = pm.getPlayer();
      if (p1 == null || p1.equals(p))
        continue; 
      out((Entity)p1, Lang.Party_QuitMember.toMsg().replace("%player%", p.getName()));
      this.ss.playSound(p1, PartyAction.QUIT);
    } 
    if (this.ss.autoLeaveOnQuit()) {
      leaveParty(p);
    } else {
      final Party party2 = party;
      int time = this.ss.getQuitTimeout();
      int taskId = this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, new Runnable() {
            public void run() {
              if (!p.isOnline() && party2 != null) {
                PartyManager.this.leaveParty(p);
                PartyManager.this.tasks.remove(p);
              } 
            }
          },  1200L * time).getTaskId();
      this.tasks.put(p, Integer.valueOf(taskId));
    } 
  }
  
  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    if (this.tasks.containsKey(p)) {
      this.plugin.getServer().getScheduler().cancelTask(((Integer)this.tasks.get(p)).intValue());
      this.tasks.remove(p);
    } 
    if (!isInParty(p))
      return; 
    Party party = getPlayerParty(p);
    if (party == null)
      return; 
    if (party.getLeader() != null && party.getLeader().getName().equals(p.getName()))
      party.setLeader(p); 
    for (PartyMember pm : party.getMembers()) {
      Player p1 = pm.getPlayer();
      if (p1 == null || p1.equals(p))
        continue; 
      out((Entity)p1, Lang.Party_BackMember.toMsg().replace("%player%", p.getName()));
      this.ss.playSound(p1, PartyAction.BACK);
    } 
  }
  
  public class PartyInvite {
    private String name;
    
    private long time;
    
    public PartyInvite(PartyManager.Party p) {
      this.name = p.getIdName();
      this.time = System.currentTimeMillis() + PartyManager.this.ss.getPartyInviteTime() * 1000L;
    }
    
    public String getPartyId() {
      return this.name;
    }
    
    public long getTimeout() {
      return this.time;
    }
    
    public boolean isExpired() {
      return (this.time < System.currentTimeMillis());
    }
  }
  
  public class PartyMember {
    private UUID uuid;
    
    private String name;
    
    private boolean chat;
    
    public PartyMember(Player p) {
      this.uuid = p.getUniqueId();
      this.name = p.getName();
      this.chat = false;
    }
    
    public UUID getUUID() {
      return this.uuid;
    }
    
    public String getName() {
      return this.name;
    }
    
    public boolean isInChat() {
      return this.chat;
    }
    
    public void toggleChat() {
      this.chat = !this.chat;
      Player p = getPlayer();
      if (isInChat()) {
        PartyManager.this.out((Entity)p, Lang.Party_ChatOff.toMsg());
      } else {
        PartyManager.this.out((Entity)p, Lang.Party_ChatOn.toMsg());
      } 
    }
    
    public Player getPlayer() {
      return PartyManager.this.plugin.getServer().getPlayer(this.uuid);
    }
  }
  
  public class Party {
    private String id;
    
    private Player leader;
    
    private Map<UUID, PartyManager.PartyMember> members;
    
    private int size;
    
    private PartyManager.PartyDropMode drop;
    
    public Party(String id, Player leader, int size) {
      setIdName(id);
      setLeader(leader);
      this.members = new LinkedHashMap<>();
      setSize(size);
      this.drop = PartyManager.PartyDropMode.FREE;
    }
    
    public String getIdName() {
      return this.id;
    }
    
    public void setIdName(String id) {
      this.id = id;
    }
    
    public Player getLeader() {
      return this.leader;
    }
    
    public void setLeader(Player leader) {
      this.leader = leader;
    }
    
    public Collection<PartyManager.PartyMember> getMembers() {
      return this.members.values();
    }
    
    public PartyManager.PartyMember getMember(UUID uuid) {
      return this.members.get(uuid);
    }
    
    public int getSize() {
      return this.size;
    }
    
    public void setSize(int size) {
      this.size = size;
    }
    
    public PartyManager.PartyDropMode getDropMode() {
      return this.drop;
    }
    
    public void setDropMode(PartyManager.PartyDropMode drop) {
      this.drop = drop;
    }
    
    public boolean isLeader(Player p) {
      return !(!p.equals(this.leader) && !p.getUniqueId().equals(this.leader.getUniqueId()));
    }
    
    public boolean isMember(Player p) {
      return this.members.containsKey(p.getUniqueId());
    }
    
    public void addMember(Player p) {
      PartyManager.PartyMember pm = new PartyManager.PartyMember(p);
      this.members.put(pm.getUUID(), pm);
    }
    
    public void delMember(Player p) {
      this.members.remove(p.getUniqueId());
    }
    
    public void delMember(UUID uuid) {
      this.members.remove(uuid);
    }
    
    public void leave(Player p) {
      if (!isMember(p))
        return; 
      delMember(p);
      PartyManager.this.out((Entity)p, Lang.Party_Leave.toMsg().replace("%party%", getIdName()));
      if (getMembers().size() <= 0 || (PartyManager.this.ss.disbandOnLeaderQuit() && getLeader().equals(p))) {
        disband();
        return;
      } 
      if (getLeader().equals(p))
        for (PartyManager.PartyMember pm : getMembers()) {
          Player p1 = pm.getPlayer();
          if (p1 == null)
            continue; 
          setLeader(p1);
          PartyManager.this.out((Entity)p1, Lang.Party_LeaderNew.toMsg());
          break;
        }  
      for (PartyManager.PartyMember pm : getMembers()) {
        Player p1 = pm.getPlayer();
        if (p1 == null)
          continue; 
        PartyManager.this.out((Entity)p1, Lang.Party_LeaveNew.toMsg().replace("%player%", p.getName()));
        PartyManager.this.ss.playSound(p1, PartyManager.PartyAction.LEAVE);
      } 
    }
    
    public void disband() {
      PartyManager.this.out((Entity)this.leader, Lang.Party_Disband.toMsg().replace("%party%", getIdName()));
      for (PartyManager.PartyMember pm : getMembers()) {
        Player p = pm.getPlayer();
        if (p == null)
          continue; 
        if (!isLeader(p))
          PartyManager.this.out((Entity)p, Lang.Party_LeaveDisband.toMsg().replace("%leader%", getLeader().getName())); 
        PartyManager.this.ss.playSound(p, PartyManager.PartyAction.DISBAND);
      } 
      this.members = null;
      PartyManager.this.parts.remove(getIdName());
    }
    
    public void teleport(Player from, Player to) {
      from.teleport((Entity)to);
      PartyManager.this.out((Entity)from, Lang.Party_TpTo.toMsg().replace("%player%", to.getName()));
      PartyManager.this.out((Entity)to, Lang.Party_TpFrom.toMsg().replace("%player%", from.getName()));
      PartyManager.this.ss.playSound(from, PartyManager.PartyAction.TP);
      PartyManager.this.ss.playSound(to, PartyManager.PartyAction.TP);
      PartyManager.this.tp.put(from.getName(), Long.valueOf(System.currentTimeMillis() + 1000L * PartyManager.this.ss.getTeleportCooldown()));
    }
    
    public void toggleDrop() {
      this.drop = this.drop.toggle();
      for (PartyManager.PartyMember pm : getMembers()) {
        Player p = pm.getPlayer();
        if (p == null)
          continue; 
        String s = Lang.getCustom("Party.Drop." + this.drop.name());
        PartyManager.this.out((Entity)p, Lang.Party_Drop_Mode.toMsg().replace("%mode%", s));
      } 
    }
  }
  
  public class PartySettings {
    private int p_max_size;
    
    private boolean p_lead_quit_disband;
    
    private int p_invite_time;
    
    private boolean p_quit_inst_leave;
    
    private int p_quit_time;
    
    private int p_tp_cd;
    
    private boolean p_chat;
    
    private String p_chat_format;
    
    private boolean exp_dec;
    
    private double exp_dec_amount;
    
    private int exp_dec_for;
    
    private boolean exp_inc;
    
    private double exp_inc_amount;
    
    private int exp_inc_for;
    
    private Map<String, Integer> size_perms;
    
    private int q_distance;
    
    Map<PartyManager.PartyAction, Sound> sounds;
    
    public PartySettings(int p_max_size, boolean p_lead_quit_disband, int p_invite_time, boolean p_quit_inst_leave, int p_quit_time, int p_tp_cd, boolean p_chat, String p_chat_format, boolean exp_dec, double exp_dec_amount, int exp_dec_for, boolean exp_inc, double exp_inc_amount, int exp_inc_for, Map<String, Integer> size_perms, int q_distance, Map<PartyManager.PartyAction, Sound> sounds) {
      this.p_max_size = p_max_size;
      this.p_lead_quit_disband = p_lead_quit_disband;
      this.p_invite_time = p_invite_time;
      this.p_quit_inst_leave = p_quit_inst_leave;
      this.p_quit_time = p_quit_time;
      this.p_tp_cd = p_tp_cd;
      this.p_chat = p_chat;
      this.p_chat_format = p_chat_format;
      this.exp_dec = exp_dec;
      this.exp_dec_amount = exp_dec_amount;
      this.exp_dec_for = exp_dec_for;
      this.exp_inc = exp_inc;
      this.exp_inc_amount = exp_inc_amount;
      this.exp_inc_for = exp_inc_for;
      this.size_perms = size_perms;
      this.q_distance = q_distance;
      this.sounds = sounds;
    }
    
    public int getPartyMaxSize() {
      return this.p_max_size;
    }
    
    public boolean disbandOnLeaderQuit() {
      return this.p_lead_quit_disband;
    }
    
    public int getPartyInviteTime() {
      return this.p_invite_time;
    }
    
    public boolean autoLeaveOnQuit() {
      return this.p_quit_inst_leave;
    }
    
    public int getQuitTimeout() {
      return this.p_quit_time;
    }
    
    public int getTeleportCooldown() {
      return this.p_tp_cd;
    }
    
    public boolean isChatEnabled() {
      return this.p_chat;
    }
    
    public String getChatFormat() {
      return this.p_chat_format;
    }
    
    public boolean isDecreaseExp() {
      return this.exp_dec;
    }
    
    public double getDecExpPercent() {
      return this.exp_dec_amount;
    }
    
    public int getDecExpFor() {
      return this.exp_dec_for;
    }
    
    public boolean isIncreaseExp() {
      return this.exp_inc;
    }
    
    public double getIncExpPercent() {
      return this.exp_inc_amount;
    }
    
    public int getIncExpFor() {
      return this.exp_inc_for;
    }
    
    public Map<String, Integer> getSizePerms() {
      return this.size_perms;
    }
    
    public int getQuestsKillDistance() {
      return this.q_distance;
    }
    
    public void playSound(Player p, PartyManager.PartyAction a) {
      Sound s = this.sounds.get(a);
      if (s != null)
        p.playSound(p.getLocation(), s, 0.8F, 0.8F); 
    }
    
    public int getPartyPermSize(Player p) {
      int i = getPartyMaxSize();
      String group = HookUtils.getGroup(p);
      for (String s : this.size_perms.keySet()) {
        if (s.equalsIgnoreCase(group))
          i = ((Integer)this.size_perms.get(s)).intValue(); 
      } 
      return i;
    }
  }
  
  enum PartyAction {
    CREATE, INVITE, JOIN, LEAVE, KICK, DISBAND, QUIT, BACK, TP;
  }
  
  enum PartyDropMode {
    FREE, LEADER, AUTO, ROLL;
    
    PartyDropMode toggle() {
      if (this == FREE)
        return LEADER; 
      if (this == LEADER)
        return AUTO; 
      if (this == AUTO)
        return ROLL; 
      return FREE;
    }
  }
}
