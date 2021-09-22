package su.nightexpress.quantumrpg.modules.list.drops;

import com.google.common.collect.Sets;
import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.manager.types.MobGroup;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.hooks.external.MyPetHK;
import su.nightexpress.quantumrpg.hooks.external.MythicMobsHK;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.api.QModule;
import su.nightexpress.quantumrpg.modules.list.drops.object.Drop;
import su.nightexpress.quantumrpg.modules.list.drops.object.DropItem;
import su.nightexpress.quantumrpg.modules.list.drops.object.DropMob;
import su.nightexpress.quantumrpg.modules.list.drops.object.DropTable;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

import java.util.*;

public class DropManager extends QModule {

    private static final String                 META_DROP_MOB = "QRPG_NO_MOB_DROP";
    private              Map<String, Float>     dropModifier;
    private              Map<String, DropMob>   dropNpc;
    private              Map<String, DropTable> dropTables;
    private              MythicMobsHK           mmHook;
    private              MyPetHK                myPetHook;

    public DropManager(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.DROPS;
    }

    @Override
    @NotNull
    public String version() {
        return "2.1.0";
    }

    @Override
    public void setup() {
        this.plugin.getConfigManager().extract(this.getPath() + "mobs");
        this.plugin.getConfigManager().extract(this.getPath() + "tables");

        this.dropModifier = new HashMap<>();
        this.dropNpc = new HashMap<>();
        this.dropTables = new HashMap<>();

        JYML cfg_main = this.cfg;
        for (String s : cfg_main.getSection("multipliers")) {
            double d = cfg_main.getDouble("multipliers." + s);
            this.dropModifier.put(s.toLowerCase(), (float) d);
        }

        for (JYML cfg : JYML.loadAll(this.getFullPath() + "/tables/", true)) {
            try {
                DropTable dropTable = new DropTable(plugin, cfg);
                this.dropTables.put(dropTable.getId(), dropTable);
            } catch (Exception ex) {
                error("Could not load Drop Table: " + cfg.getFile().getName());
                ex.printStackTrace();
            }
        }

        for (JYML cfg : JYML.loadAll(this.getFullPath() + "/mobs/", true)) {
            try {
                DropMob npc = new DropMob(plugin, cfg, this);
                this.dropNpc.put(npc.getId(), npc);
            } catch (Exception ex) {
                error("Could not load Mob Table: " + cfg.getFile().getName());
                ex.printStackTrace();
            }
        }

        this.mmHook = plugin.getHook(MythicMobsHK.class);
        this.myPetHook = plugin.getHook(MyPetHK.class);
    }

    @Override
    public void shutdown() {
        if (this.dropModifier != null) {
            this.dropModifier.clear();
            this.dropModifier = null;
        }
        if (this.dropNpc != null) {
            this.dropNpc.clear();
            this.dropNpc = null;
        }
        if (this.dropTables != null) {
            this.dropTables.clear();
            this.dropTables = null;
        }
    }

    @Nullable
    public DropTable getTableById(@NotNull String id) {
        return this.dropTables.get(id.toLowerCase());
    }

    @NotNull
    private Set<DropMob> getDropsForEntity(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity)) return Collections.emptySet();

        String  mobType;
        boolean isMythic = false;

        if (this.mmHook != null && this.mmHook.isMythicMob(entity)) {
            mobType = this.mmHook.getMythicNameByEntity(entity);
            isMythic = true;
        } else {
            mobType = entity.getType().name();
        }

        Set<DropMob> tables = new HashSet<>();

        for (DropMob dropNpc : this.dropNpc.values()) {
            Set<String> mobList;
            if (isMythic) mobList = dropNpc.getMythic();
            else mobList = dropNpc.getEntities();

            if (mobList.contains(JStrings.MASK_ANY)) {
                tables.add(dropNpc);
                continue;
            }

            if (!isMythic) {
                MobGroup group = MobGroup.getMobGroup(entity);
                if (mobList.contains(group.name())) {
                    tables.add(dropNpc);
                    continue;
                }
            }

            if (mobList.contains(mobType)) {
                tables.add(dropNpc);
                continue;
            }
        }

        return tables;
    }

    private float getMultiplier(@NotNull Player player, @NotNull LivingEntity dead) {
        float mult      = 0.0f;
        float multGroup = 0.0f;
        float multStat  = 0.0f;

        String group = Hooks.getPermGroup(player);
        if (this.dropModifier.containsKey(group)) {
            multGroup += ((this.dropModifier.get(group) - 1f) * 100f);
        }

        multStat += EntityStats.get(player).getItemStat(AbstractStat.Type.LOOT_RATE, false);
        mult = 1f + ((multGroup + multStat) / 100f);

        return mult;
    }

    @NotNull
    private List<ItemStack> methodRoll(@NotNull Player killer, @NotNull LivingEntity dead) {
        List<ItemStack> loot     = new ArrayList<>();
        Set<DropMob>    mobs     = this.getDropsForEntity(dead);
        float           modifier = this.getMultiplier(killer, dead);

        // TODO
        // Fast implementation for Drop Conditions.
        // Must be remaked a lot
        Map<String, Set<Entity>> mapTarget = new HashMap<>();
        mapTarget.put("player", Sets.newHashSet(killer));
        mapTarget.put("entity", Sets.newHashSet(dead));

        for (DropMob dropNpc : mobs) {
            Set<Drop> drop  = new HashSet<>();
            int       index = 0;
            index = dropNpc.dropCalculator(killer, dead, drop, index, modifier);

            for (Drop dropItem : drop) {
                DropItem     dropConfig     = dropItem.getDropConfig();
                List<String> dropConditions = dropConfig.getConditions();
                if (!ActionManipulator.processConditions(plugin, killer, dropConditions, mapTarget)) continue;

                String itemId  = dropConfig.getItemId();
                int    itemLvl = dropConfig.getLevel(killer, dead);

                ItemStack dropStack = QuantumAPI.getItemByModule(dropConfig.getModuleId(), itemId, itemLvl, -1, -1);
                if (dropStack == null || dropStack.getType() == Material.AIR) continue;

                dropConfig.executeActions(killer, mapTarget);
                loot.add(dropStack);
            }
        }
        return loot;
    }

    // ---------------------------------------------------- //

    @EventHandler(priority = EventPriority.HIGH)
    public void onDropDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.hasMetadata(META_DROP_MOB)) return;

        Player killer = dead.getKiller();
        if (killer == null) {
            // Support for MyPet kills.
            if (this.myPetHook != null) {
                EntityDamageEvent e2 = dead.getLastDamageCause();
                if (e2 instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent ede     = (EntityDamageByEntityEvent) e2;
                    Entity                    damager = ede.getDamager();
                    if (this.myPetHook.isPet(damager)) {
                        killer = this.myPetHook.getPetOwner(damager);
                    }
                }
            }
            return;
        }

        e.getDrops().addAll(this.methodRoll(killer, dead));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDropSpawn(CreatureSpawnEvent e) {
        LivingEntity entity = e.getEntity();
        Set<DropMob> mobs   = this.getDropsForEntity(entity);
        if (mobs.isEmpty()) return;

        String reason = e.getSpawnReason().name();
        for (DropMob dropNpc : mobs) {
            if (dropNpc.getReasons().contains(reason)) {
                entity.setMetadata(META_DROP_MOB, new FixedMetadataValue(plugin, "yes"));
                break;
            }
        }
    }
}
