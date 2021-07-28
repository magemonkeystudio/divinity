package su.nightexpress.quantumrpg.modules.consumes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.consumes.events.QuantumPlayerConsumeItemEvent;
import su.nightexpress.quantumrpg.nms.VersionUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class ConsumeManager extends QModuleDrop {
  private MyConfig consCfg;
  
  private ConsumeSettings settings;
  
  private HashMap<String, List<ConsumeCD>> cd;
  
  public ConsumeManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.CONSUMABLES;
  }
  
  public String name() {
    return "Consumables";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.cd = new HashMap<>();
    this.consCfg = new MyConfig((JavaPlugin)this.plugin, getPath(), "consumables.yml");
    setupMain();
  }
  
  public void shutdown() {
    this.cd = null;
    this.consCfg = null;
    this.settings = null;
  }
  
  private void setupMain() {
    setupSettings();
    setupConsumes();
  }
  
  private void setupSettings() {
    JYML jYML = this.cfg.getConfig();
    boolean hp = jYML.getBoolean("general.allow-eat-on-full-health");
    boolean hunger = jYML.getBoolean("general.allow-eat-on-full-hunger");
    this.settings = new ConsumeSettings(hp, hunger);
  }
  
  private void setupConsumes() {
    JYML jYML = this.consCfg.getConfig();
    if (!jYML.isConfigurationSection("consumables"))
      return; 
    for (String id : jYML.getConfigurationSection("consumables").getKeys(false)) {
      String path = "consumables." + id + ".";
      int cd = jYML.getInt(String.valueOf(path) + "cooldown");
      String color = jYML.getString(String.valueOf(path) + "item.extras.color");
      double hp = jYML.getDouble(String.valueOf(path) + "effects.health");
      double hunger = jYML.getDouble(String.valueOf(path) + "effects.hunger");
      List<String> cmds = jYML.getStringList(String.valueOf(path) + "effects.actions");
      boolean wb = jYML.getBoolean(String.valueOf(path) + "craft.workbench.enabled");
      List<String> wb_rec = jYML.getStringList(String.valueOf(path) + "craft.workbench.template");
      boolean fur = jYML.getBoolean(String.valueOf(path) + "craft.furnace.enabled");
      String fur_rec = jYML.getString(String.valueOf(path) + "craft.furnace.input");
      double fur_exp = jYML.getDouble(String.valueOf(path) + "craft.furnace.experience");
      int fur_time = jYML.getInt(String.valueOf(path) + "craft.furnace.cook-time");
      Consume cc = new Consume(
          id, 
          String.valueOf(path) + "item.", 
          (FileConfiguration)jYML, 
          type(), 
          
          cd, 
          
          color, 
          
          hp, 
          hunger, 
          cmds, 
          
          wb, 
          wb_rec, 
          
          fur, 
          fur_rec, 
          fur_exp, 
          fur_time);
      cc.registerRecipes();
      this.items.put(cc.getId(), cc);
    } 
  }
  
  private boolean isOnCooldown(Player p, String id) {
    if (!this.cd.containsKey(p.getName()))
      return false; 
    for (ConsumeCD fc : this.cd.get(p.getName())) {
      if (fc.getFoodId().equalsIgnoreCase(id)) {
        if (System.currentTimeMillis() > fc.getTimeEnd()) {
          ((List)this.cd.get(p.getName())).remove(fc);
          return false;
        } 
        return true;
      } 
    } 
    return false;
  }
  
  private String getCooldownTime(Player p, String id) {
    for (ConsumeCD fc : this.cd.get(p.getName())) {
      if (fc.getFoodId().equalsIgnoreCase(id)) {
        long l2 = System.currentTimeMillis();
        long l1 = fc.getTimeEnd();
        return Utils.getTimeLeft(l1, l2);
      } 
    } 
    return Utils.getTimeLeft(System.currentTimeMillis(), System.currentTimeMillis());
  }
  
  private void setCooldown(Player p, Consume f) {
    List<ConsumeCD> list = new ArrayList<>();
    if (this.cd.containsKey(p.getName()))
      list = new ArrayList<>(this.cd.get(p.getName())); 
    ConsumeCD fc = new ConsumeCD(f.getId(), System.currentTimeMillis() + f.getCooldown() * 1000L);
    list.add(fc);
    this.cd.put(p.getName(), list);
  }
  
  private boolean consume(ItemStack i, Player p) {
    String id = getItemId(i);
    Consume f = (Consume)getItemById(id, Consume.class);
    if (f == null)
      return true; 
    QuantumPlayerConsumeItemEvent eve = new QuantumPlayerConsumeItemEvent(i, p, f);
    this.plugin.getPluginManager().callEvent((Event)eve);
    if (eve.isCancelled())
      return false; 
    if (isOnCooldown(p, id)) {
      out((Entity)p, Lang.Consumables_Cooldown.toMsg()
          .replace("%item%", Utils.getItemName(i))
          .replace("%time%", getCooldownTime(p, id)));
      return false;
    } 
    double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    if (f.getHealth() > 0.0D && !this.settings.eat_full_hp && p.getHealth() >= max) {
      out((Entity)p, Lang.Consumables_FullHp.toMsg().replace("%item%", Utils.getItemName(i)));
      return false;
    } 
    if (f.getHunger() > 0.0D && !this.settings.eat_full_food && p.getFoodLevel() >= 20) {
      out((Entity)p, Lang.Consumables_FullHunger.toMsg().replace("%item%", Utils.getItemName(i)));
      return false;
    } 
    f.applyEffects(p);
    if (f.getCooldown() > 0)
      setCooldown(p, f); 
    return true;
  }
  
  @EventHandler
  public void onConsume(PlayerItemConsumeEvent e) {
    ItemStack i = e.getItem();
    if (!isItemOfThisModule(i))
      return; 
    if (!consume(i, e.getPlayer()))
      e.setCancelled(true); 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onConsume(PlayerInteractEvent e) {
    if (e.getHand() == EquipmentSlot.OFF_HAND)
      return; 
    ItemStack i = e.getItem();
    if (i == null || i.getType() == Material.AIR)
      return; 
    if (i.getType().isEdible())
      return; 
    if (!isItemOfThisModule(i))
      return; 
    e.setCancelled(true);
    if (consume(i, e.getPlayer()))
      i.setAmount(i.getAmount() - 1); 
  }
  
  public class ConsumeSettings {
    private boolean eat_full_hp;
    
    private boolean eat_full_food;
    
    public ConsumeSettings(boolean eat_full_hp, boolean eat_full_food) {
      setEatFullHealth(eat_full_hp);
      setEatFullHunger(eat_full_food);
    }
    
    public boolean canEatFullHealth() {
      return this.eat_full_hp;
    }
    
    public void setEatFullHealth(boolean b) {
      this.eat_full_hp = b;
    }
    
    public boolean canEatFullHunger() {
      return this.eat_full_food;
    }
    
    public void setEatFullHunger(boolean b) {
      this.eat_full_food = b;
    }
  }
  
  public class Consume extends ModuleItem {
    private int cd;
    
    private String color;
    
    private double hp;
    
    private double hunger;
    
    private List<String> actions;
    
    private boolean wb;
    
    private List<String> wb_rec;
    
    private boolean fur;
    
    private String fur_rec;
    
    private double fur_exp;
    
    private int fur_time;
    
    public Consume(String id, String path, FileConfiguration cfg, EModule module, int cd, String color, double hp, double hunger, List<String> actions, boolean wb, List<String> wb_rec, boolean fur, String fur_rec, double fur_exp, int fur_time) {
      super(id, path, cfg, ConsumeManager.this.type());
      setCooldown(cd);
      setColor(color);
      setHealth(hp);
      setHunger(hunger);
      setActions(actions);
      setWorkbench(wb);
      setWorkbenchRecipe(wb_rec);
      setFurnace(fur);
      setFurnaceRecipe(fur_rec);
      setFurnaceExp(fur_exp);
      setFurnaceTime(fur_time);
    }
    
    public int getCooldown() {
      return this.cd;
    }
    
    public void setCooldown(int cd) {
      this.cd = cd;
    }
    
    public String getColor() {
      return this.color;
    }
    
    public void setColor(String color) {
      this.color = color;
    }
    
    public double getHealth() {
      return this.hp;
    }
    
    public void setHealth(double hp) {
      this.hp = hp;
    }
    
    public double getHunger() {
      return this.hunger;
    }
    
    public void setHunger(double hunger) {
      this.hunger = hunger;
    }
    
    public List<String> getActions() {
      return this.actions;
    }
    
    public void setActions(List<String> actions) {
      this.actions = actions;
    }
    
    public boolean isWorkbench() {
      return this.wb;
    }
    
    public void setWorkbench(boolean wb) {
      this.wb = wb;
    }
    
    public List<String> getWorkbenchRecicpe() {
      return this.wb_rec;
    }
    
    public void setWorkbenchRecipe(List<String> wb_rec) {
      this.wb_rec = wb_rec;
    }
    
    public boolean isFurnace() {
      return this.fur;
    }
    
    public void setFurnace(boolean fur) {
      this.fur = fur;
    }
    
    public String getFurnaceRecipe() {
      return this.fur_rec;
    }
    
    public void setFurnaceRecipe(String fur_rec) {
      this.fur_rec = fur_rec;
    }
    
    public double getFurnaceExp() {
      return this.fur_exp;
    }
    
    public void setFurnaceExp(double fur_exp) {
      this.fur_exp = fur_exp;
    }
    
    public int getFurnaceTime() {
      return this.fur_time;
    }
    
    public void setFurnaceTime(int fur_time) {
      this.fur_time = fur_time;
    }
    
    protected ItemStack build() {
      ItemStack item = super.build();
      if (item.getType() == Material.AIR)
        return item; 
      if (item.getType().name().contains("POTION")) {
        PotionMeta pm = (PotionMeta)item.getItemMeta();
        String[] cc = getColor().split(",");
        int r = Integer.parseInt(cc[0]);
        int g = Integer.parseInt(cc[1]);
        int b = Integer.parseInt(cc[2]);
        pm.setColor(Color.fromRGB(r, g, b));
        item.setItemMeta((ItemMeta)pm);
      } 
      return item;
    }
    
    public void applyEffects(Player p) {
      double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
      p.setHealth(Math.min(p.getHealth() + getHealth(), max));
      p.setFoodLevel((int)(p.getFoodLevel() + getHunger()));
      DivineItemsAPI.executeActions((Entity)p, this.actions, null);
    }
    
    public void registerRecipes() {
      ItemStack food = create();
      ConsumeManager.this.plugin.getServer().getRecipesFor(food).clear();
      if (isWorkbench()) {
        ShapedRecipe sr;
        if (VersionUtils.get().isHigher(VersionUtils.Version.v1_11_R1)) {
          NamespacedKey key;
          try {
            key = NamespacedKey.minecraft(this.id);
          } catch (IllegalArgumentException ex) {
            String id2 = "qrpg_consume_" + Utils.randInt(0, 3000);
            key = NamespacedKey.minecraft(id2);
          } 
          sr = new ShapedRecipe(key, food);
        } else {
          sr = new ShapedRecipe(food);
        } 
        sr.shape(new String[] { "ABC", "DEF", "GHI" });
        char[] ziga = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' };
        int j = 0;
        label41: for (String s0 : getWorkbenchRecicpe()) {
          String[] l1 = s0.split(" , ");
          byte b;
          int i;
          String[] arrayOfString1;
          for (i = (arrayOfString1 = l1).length, b = 0; b < i; ) {
            String s1 = arrayOfString1[b];
            ItemStack i1 = Utils.buildItem(s1);
            if (i1 == null) {
              sr = null;
              ConsumeManager.this.log("Invalid material '" + s1 + "' for workbench recipe '" + this.id + "'!", LogType.ERROR);
              break label41;
            } 
            sr.setIngredient(ziga[j], i1.getData());
            j++;
            b++;
          } 
        } 
        if (sr != null)
          try {
            ConsumeManager.this.plugin.getServer().addRecipe((Recipe)sr);
          } catch (IllegalStateException ex) {
            ConsumeManager.this.log("Unable to register workbench recipe for consumable '" + this.id + "'!", LogType.ERROR);
          }  
      } 
      if (isFurnace()) {
        String s2 = getFurnaceRecipe();
        ItemStack i1 = Utils.buildItem(s2);
        if (i1 == null) {
          ConsumeManager.this.log("Invalid material '" + this.fur_rec + "' for furnace recipe '" + this.id + "'!", LogType.ERROR);
          return;
        } 
        FurnaceRecipe fr = new FurnaceRecipe(food, i1.getType());
        try {
          ConsumeManager.this.plugin.getServer().addRecipe((Recipe)fr);
        } catch (IllegalStateException ex) {
          ConsumeManager.this.log("Unable to register furnace recipe for consumable '" + this.id + "'!", LogType.ERROR);
        } 
      } 
    }
  }
  
  public class ConsumeCD {
    private String id;
    
    private long time;
    
    public ConsumeCD(String id, long time) {
      this.id = id;
      this.time = time;
    }
    
    public String getFoodId() {
      return this.id;
    }
    
    public long getTimeEnd() {
      return this.time;
    }
  }
}
