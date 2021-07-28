package su.nightexpress.quantumrpg.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.api.events.QuantumEntityItemPickupEvent;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.AmmoType;
import su.nightexpress.quantumrpg.types.WpnHand;
import su.nightexpress.quantumrpg.utils.MetaUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.msg.MsgUT;

public class GlobalListener implements Listener {
  @EventHandler(ignoreCancelled = true)
  public void onStatRegen(EntityRegainHealthEvent e) {
    Entity e1 = e.getEntity();
    if (!(e1 instanceof LivingEntity))
      return; 
    LivingEntity li = (LivingEntity)e1;
    double regen = 1.0D + EntityAPI.getItemStat(li, ItemStat.HEALTH_REGEN, null) / 100.0D;
    if (regen > 0.0D)
      e.setAmount(e.getAmount() * regen); 
  }
  
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onBowAmmoType(EntityShootBowEvent e) {
    Snowball snowball;
    Egg egg;
    EnderPearl enderPearl;
    Fireball fireball;
    WitherSkull a;
    ShulkerBullet shulkerBullet;
    LlamaSpit llamaSpit;
    ThrownExpBottle sb;
    ItemStack item;
    LivingEntity ee = e.getEntity();
    ItemStack bow = e.getBow();
    AmmoType at = ItemAPI.getAmmoType(bow);
    switch (at) {
      case SNOWBALL:
        snowball = (Snowball)ee.launchProjectile(Snowball.class);
        e.setProjectile((Entity)snowball);
        break;
      case EGG:
        egg = (Egg)ee.launchProjectile(Egg.class);
        e.setProjectile((Entity)egg);
        break;
      case ENDER_PEARL:
        enderPearl = (EnderPearl)ee.launchProjectile(EnderPearl.class);
        e.setProjectile((Entity)enderPearl);
        break;
      case FIREBALL:
        fireball = (Fireball)ee.launchProjectile(Fireball.class);
        e.setProjectile((Entity)fireball);
        break;
      case WITHER_SKULL:
        a = (WitherSkull)ee.launchProjectile(WitherSkull.class);
        e.setProjectile((Entity)a);
        break;
      case SHULKER_BULLET:
        shulkerBullet = (ShulkerBullet)ee.getWorld().spawnEntity(ee.getEyeLocation().add(ee.getEyeLocation().getDirection()), EntityType.SHULKER_BULLET);
        shulkerBullet.setShooter((ProjectileSource)ee);
        shulkerBullet.setVelocity(ee.getEyeLocation().getDirection().multiply(1.0F + e.getForce()));
        item = new ItemStack(ee.getEquipment().getItemInMainHand());
        MetaUtils.addItemToMeta((Entity)shulkerBullet, item);
        e.setProjectile((Entity)shulkerBullet);
        break;
      case LLAMA_SPIT:
        llamaSpit = (LlamaSpit)ee.getWorld().spawnEntity(ee.getEyeLocation().add(ee.getEyeLocation().getDirection()), EntityType.LLAMA_SPIT);
        llamaSpit.setShooter((ProjectileSource)ee);
        llamaSpit.setBounce(true);
        llamaSpit.setVelocity(ee.getEyeLocation().getDirection().multiply(1.0F + e.getForce()));
        item = new ItemStack(ee.getEquipment().getItemInMainHand());
        MetaUtils.addItemToMeta((Entity)llamaSpit, item);
        e.setProjectile((Entity)llamaSpit);
        break;
      case EXP_POTION:
        sb = (ThrownExpBottle)ee.getWorld().spawnEntity(ee.getEyeLocation().add(ee.getEyeLocation().getDirection()), EntityType.THROWN_EXP_BOTTLE);
        sb.setShooter((ProjectileSource)ee);
        sb.setBounce(true);
        sb.setVelocity(ee.getEyeLocation().getDirection().multiply(1.0F + e.getForce()));
        item = new ItemStack(ee.getEquipment().getItemInMainHand());
        MetaUtils.addItemToMeta((Entity)sb, item);
        e.setProjectile((Entity)sb);
        break;
    } 
  }
  
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onItemDamage(PlayerItemDamageEvent e) {
    ItemStack i = e.getItem();
    if (ItemAPI.getDurabilityMinOrMax(i, 0) != -1) {
      e.setCancelled(true);
      return;
    } 
    if (ItemAPI.getDurabilityMinOrMax(i, 0) == -999) {
      e.setCancelled(true);
      return;
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onDuraBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    ItemStack i = p.getInventory().getItemInMainHand();
    ItemAPI.reduceDurability((LivingEntity)p, i, 1);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onDuraFish(PlayerFishEvent e) {
    Player p = e.getPlayer();
    ItemStack i = p.getInventory().getItemInMainHand();
    if (!ItemAPI.canUse(i, p)) {
      e.setCancelled(true);
      return;
    } 
    ItemAPI.reduceDurability((LivingEntity)p, i, 1);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDuraArmor(EntityDamageEvent e) {
    Entity e1 = e.getEntity();
    if (!(e1 instanceof LivingEntity))
      return; 
    LivingEntity li = (LivingEntity)e1;
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = EntityAPI.getEquipment(li, true)).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      ItemAPI.reduceDurability(li, item, 1);
      b++;
    } 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDuraItem(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof LivingEntity))
      return; 
    LivingEntity li = (LivingEntity)e.getDamager();
    ItemStack i = li.getEquipment().getItemInMainHand();
    ItemAPI.reduceDurability(li, i, 1);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDuraShoot(EntityShootBowEvent e) {
    LivingEntity li = e.getEntity();
    ItemStack i = e.getBow();
    ItemAPI.reduceDurability(li, i, 1);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDuraHoe(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    Block b = e.getClickedBlock();
    if (b != null && (b.getType() == Material.GRASS || b.getType() == Material.DIRT || 
      b.getType() == Material.MYCEL)) {
      ItemStack hoe = p.getInventory().getItemInMainHand();
      if (hoe != null && hoe.getType().name().endsWith("_HOE"))
        ItemAPI.reduceDurability((LivingEntity)p, hoe, 1); 
    } 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRestArmor(InventoryCloseEvent e) {
    Player p = (Player)e.getPlayer();
    if (e.getInventory().getType() == InventoryType.CRAFTING && 
      e.getInventory().getHolder().equals(p))
      EntityAPI.checkForLegitItems(p); 
  }
  
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onRestHit(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player))
      return; 
    Player p = (Player)e.getDamager();
    ItemStack item = p.getInventory().getItemInMainHand();
    if (!ItemAPI.canUse(item, p))
      e.setCancelled(true); 
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void onRestUse(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = e.getItem();
    if (!ItemAPI.canUse(item, p))
      e.setCancelled(true); 
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void onRestClick(InventoryClickEvent e) {
    Player p = (Player)e.getWhoClicked();
    if (e.getInventory().getType() == InventoryType.CRAFTING && e.getInventory().getHolder().equals(p)) {
      int slot = e.getSlot();
      if ((slot >= 36 && slot <= 40) || (slot >= 0 && slot < 9)) {
        ItemStack drag = e.getCursor();
        if (!ItemAPI.canUse(drag, p)) {
          e.setCancelled(true);
          return;
        } 
      } 
      ItemStack item = e.getCurrentItem();
      if (e.getAction() == InventoryAction.HOTBAR_SWAP && !ItemAPI.canUse(item, p) && !Config.g_holdRestrict) {
        e.setCancelled(true);
        return;
      } 
      if (e.isShiftClick() && !ItemAPI.canUse(item, p))
        e.setCancelled(true); 
    } 
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void onRestBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    ItemStack i = p.getInventory().getItemInMainHand();
    if (!ItemAPI.canUse(i, p))
      e.setCancelled(true); 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onPickup(EntityPickupItemEvent e) {
    if (!MetaUtils.isPickable((Entity)e.getItem()))
      e.setCancelled(true); 
    ItemStack item = e.getItem().getItemStack();
    EModule em = ItemAPI.getItemModule(item);
    if (em == null)
      return; 
    QuantumEntityItemPickupEvent ev = new QuantumEntityItemPickupEvent(item, e.getEntity(), em);
    Bukkit.getPluginManager().callEvent((Event)ev);
    e.setCancelled(ev.isCancelled());
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onHandHeld(PlayerItemHeldEvent e) {
    int slot = e.getNewSlot();
    Player p = e.getPlayer();
    ItemStack cu = p.getInventory().getItem(slot);
    if (cu == null || cu.getType() == Material.AIR)
      return; 
    if (!ItemAPI.canUse(cu, p) && !Config.g_holdRestrict) {
      e.setCancelled(true);
      return;
    } 
    ItemStack off = p.getInventory().getItemInOffHand();
    if (off == null || off.getType() == Material.AIR)
      return; 
    WpnHand pickh = ItemAPI.getHandType(cu);
    WpnHand offh = ItemAPI.getHandType(off);
    if (pickh == WpnHand.TWO || offh == WpnHand.TWO) {
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg());
      e.setCancelled(true);
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onHandHeld(PlayerSwapHandItemsEvent e) {
    Player p = e.getPlayer();
    ItemStack off = e.getOffHandItem();
    if (off == null || off.getType() == Material.AIR)
      return; 
    WpnHand offh = ItemAPI.getHandType(off);
    if (offh == WpnHand.TWO) {
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg());
      e.setCancelled(true);
    } 
  }
  
  @EventHandler
  public void onHandClose(InventoryCloseEvent e) {
    Player p = (Player)e.getPlayer();
    ItemStack off = p.getInventory().getItemInOffHand();
    if (off == null || off.getType() == Material.AIR)
      return; 
    ItemStack main = p.getInventory().getItemInMainHand();
    WpnHand offh = ItemAPI.getHandType(off);
    WpnHand mh = ItemAPI.getHandType(main);
    if (offh == WpnHand.TWO || mh == WpnHand.TWO) {
      Utils.addItem(p, off.clone());
      p.getInventory().setItemInOffHand(null);
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg());
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onHoldPickup(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player))
      return; 
    Player p = (Player)e.getEntity();
    ItemStack cu = e.getItem().getItemStack();
    int held = p.getInventory().getHeldItemSlot();
    int first = ItemAPI.getFirstEmptyHotbat(p);
    if (first == held) {
      if (!ItemAPI.canUse(cu, p) && !Config.g_holdRestrict) {
        e.setCancelled(true);
        return;
      } 
      ItemStack off = p.getInventory().getItemInOffHand();
      if (off == null || off.getType() == Material.AIR)
        return; 
      WpnHand pickh = ItemAPI.getHandType(cu);
      WpnHand offh = ItemAPI.getHandType(off);
      if (pickh == WpnHand.TWO || offh == WpnHand.TWO) {
        MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg(), 2);
        e.setCancelled(true);
        return;
      } 
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onHoldOffClick(InventoryClickEvent e) {
    Player p = (Player)e.getWhoClicked();
    Inventory inv = e.getInventory();
    if (inv.getType() == InventoryType.CRAFTING && inv.getHolder().equals(p)) {
      ItemStack cu = e.getCursor();
      if (cu == null || cu.getType() == Material.AIR)
        return; 
      if (e.getSlot() == 40) {
        WpnHand h = ItemAPI.getHandType(cu);
        if (h == WpnHand.TWO) {
          p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg());
          e.setCancelled(true);
          return;
        } 
        if (holdMainTwo(p)) {
          p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Hands_CantHold.toMsg());
          e.setCancelled(true);
        } 
      } 
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onLoreDrop(PlayerDropItemEvent e) {
    ItemStack item = e.getItemDrop().getItemStack();
    ItemAPI.updateLevelRequirement(item);
    ItemAPI.updateClassRequirement(item);
    e.getItemDrop().setItemStack(item);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onLoreOpen(InventoryOpenEvent e) {
    List<ItemStack> list = new ArrayList<>();
    list.addAll(Arrays.asList(e.getInventory().getContents()));
    Player p = (Player)e.getPlayer();
    list.addAll(Arrays.asList(p.getInventory().getArmorContents()));
    for (ItemStack item : list) {
      ItemAPI.updateLevelRequirement(item, (Player)e.getPlayer());
      ItemAPI.updateClassRequirement(item, (Player)e.getPlayer());
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onLoreClose(InventoryCloseEvent e) {
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    List<ItemStack> list = new ArrayList<>();
    list.addAll(Arrays.asList(e.getPlayer().getInventory().getContents()));
    Player p = (Player)e.getPlayer();
    list.addAll(Arrays.asList(p.getInventory().getArmorContents()));
    for (ItemStack item : list) {
      ItemAPI.updateLevelRequirement(item, (Player)e.getPlayer());
      ItemAPI.updateClassRequirement(item, (Player)e.getPlayer());
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onLorePick(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player))
      return; 
    Player p = (Player)e.getEntity();
    ItemStack cu = e.getItem().getItemStack();
    ItemAPI.updateLevelRequirement(cu, p);
    ItemAPI.updateClassRequirement(cu, p);
    e.getItem().setItemStack(cu);
  }
  
  private boolean holdMainTwo(Player p) {
    ItemStack main = p.getInventory().getItemInMainHand();
    WpnHand h = ItemAPI.getHandType(main);
    return (h == WpnHand.TWO);
  }
}
