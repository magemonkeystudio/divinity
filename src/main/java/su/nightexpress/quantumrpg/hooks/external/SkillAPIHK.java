package su.nightexpress.quantumrpg.hooks.external;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerCastSkillEvent;
import com.sucy.skill.api.event.PlayerManaGainEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.manager.AttributeManager;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AbilityGenerator;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.SkillAPIAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;

import java.util.*;

public class SkillAPIHK extends NHook<QuantumRPG> implements HookLevel, HookClass {

    public SkillAPIHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        this.registerListeners();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();
    }

    @Override
    public int getLevel(Player player) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.hasClass() ? playerData.getMainClass().getLevel() : 0;
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        playerData.giveExp(amount, ExpSource.SPECIAL);
    }

    @Override
    @NotNull
    public String getClass(Player player) {
        PlayerData data = SkillAPI.getPlayerData(player);
        if (data.hasClass()) {
            return StringUT.colorOff(data.getMainClass().getData().getName());
        } else {
            return "";
        }
    }

    @EventHandler
    public void onSkillCast(PlayerCastSkillEvent e) {
        if (!EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_SKILL_API) return;
        Player    p    = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        DurabilityStat duraStat = ItemStats.getStat(DurabilityStat.class);
        if (duraStat != null) {
            duraStat.reduceDurability(p, item, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegen(PlayerManaGainEvent e) {
        if (e.getPlayerData() == null) return;
        Player player = e.getPlayerData().getPlayer();
        if (player == null) return;

        double regen = 1D + EntityStats.get(player).getItemStat(AbstractStat.Type.MANA_REGEN, false) / 100D;
        if (regen > 0) {
            e.setAmount(e.getAmount() * regen);
        }
    }

    @Override
    public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
        PlayerData data = SkillAPI.getPlayerData(player);
        if (data == null) return;

        double cur = data.getMana();

        if (ofMax) {
            double max = data.getMaxMana();
            amount = max / 100D * amount;
        }

        data.setMana(Math.max(0, cur - amount));
    }

    private final List<UUID> exempt = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void skillDamage(SkillDamageEvent event) {
        LivingEntity player = event.getDamager();

        if (event.isKnockback())
            exempt.add(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void damage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity) || !exempt.contains(event.getDamager().getUniqueId()))
            return;

        exempt.remove(event.getDamager().getUniqueId());
    }

    public boolean isExempt(LivingEntity player) {
        return exempt.contains(player.getUniqueId());
    }

    public void addSkill(Player player, String skillId, int level) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        Skill skill = SkillAPI.getSkill(skillId);
        if (skill == null) {
            plugin.warn("Could not find skill \""+skillId+"\" to add to the item");
            return;
        }
        playerData.addSkillExternally(skill, playerData.getMainClass(), AbilityGenerator.ABILITY_KEY, level);
    }

    public void removeSkill(Player player, String skillId) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        Skill skill = SkillAPI.getSkill(skillId);
        if (skill == null) { return; }
        playerData.removeSkillExternally(skill, AbilityGenerator.ABILITY_KEY);
    }

    public Set<String> getSkills() { return SkillAPI.getSkills().keySet(); }

    public ItemStack getSkillIndicator(String skillId) {
        Skill skill = SkillAPI.getSkill(skillId);
        if (skill == null) { return new ItemStack(Material.JACK_O_LANTERN); }
        return skill.getIndicator();
    }

    public Collection<SkillAPIAttribute> getAttributes() {
        List<SkillAPIAttribute> list = new ArrayList<>();
        String format;
        {
            String baseFormat = SkillAPI.getSettings().getAttrGiveText("{attr}");
            int index = baseFormat.indexOf('{');
            String attrPre = baseFormat.substring(0, index);
            String attrPost = baseFormat.substring(index + "{attr}".length());
            format = EngineCfg.LORE_STYLE_SKILLAPI_ATTRIBUTE_FORMAT
                    .replace("%attrPre%", attrPre)
                    .replace("%attrPost%", attrPost)
                    +"%value%";
        }
        for (Map.Entry<String,AttributeManager.Attribute> entry : SkillAPI.getAttributeManager().getAttributes().entrySet()) {
            list.add(new SkillAPIAttribute(entry.getKey(), entry.getValue().getName(), format));
        }
        return list;
    }

    public ItemStack getAttributeIndicator(String attributeId) {
        AttributeManager.Attribute attribute = SkillAPI.getAttributeManager().getAttribute(attributeId);
        if (attribute == null) {
            ItemStack itemStack = new ItemStack(Material.DIRT);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(attributeId);
                itemStack.setItemMeta(meta);
            }
            return itemStack;
        }
        return attribute.getToolIcon();
    }

    private Map<String,Integer> getAbilities(ItemStack item) {
        Map<String,Integer> map = new HashMap<>();
        if (item == null) { return map; }
        String[] stringAbilities = DataUT.getStringArrayData(item, AbilityGenerator.ABILITY_KEY);
        if (stringAbilities == null) { return map; }
        for (String stringAbility : stringAbilities) {
            int i = stringAbility.lastIndexOf(':');
            int level;
            try {
                level = Integer.parseInt(stringAbility.substring(i+1));
            } catch (NumberFormatException e) {
                continue;
            }
            map.put(stringAbility.substring(0, i), level);
        }
        return map;
    }

    public void updateSkills(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {

                Map<String,Integer> skills    = new HashMap<>();
                PlayerInventory     inventory = player.getInventory();
                for (int i : new int[]{inventory.getHeldItemSlot(), 36, 37, 38, 39, 40}) {
                    for (Map.Entry<String,Integer> entry : getAbilities(inventory.getItem(i)).entrySet()) {
                        String id = entry.getKey();
                        int level = entry.getValue();
                        if (!skills.containsKey(id) || level > skills.get(id)) {
                            skills.put(id, level);
                        }
                    }
                }
                Set<PlayerData.ExternallyAddedSkill> prevSkills = new HashSet<>(SkillAPI.getPlayerData(player).getExternallyAddedSkills());
                for (PlayerData.ExternallyAddedSkill prevSkill : prevSkills) {
                    if (!prevSkill.getKey().equals(AbilityGenerator.ABILITY_KEY)) { continue; }
                    String id = prevSkill.getId();
                    Integer level = skills.get(id);
                    if (level == null) { // Removed skill
                        removeSkill(player, id);
                    } else if (level != prevSkill.getLevel()) { // Update changed level
                        addSkill(player, id, level);
                    }
                }
                for (Map.Entry<String,Integer> entry : skills.entrySet()) {
                    String id = entry.getKey();
                    if (prevSkills.stream().noneMatch(extSkill -> extSkill.getKey().equals(AbilityGenerator.ABILITY_KEY) && extSkill.getId().equals(id))) {
                        addSkill(player, id, entry.getValue());
                    }
                }
            }
        }.runTask(plugin);
    }
}
