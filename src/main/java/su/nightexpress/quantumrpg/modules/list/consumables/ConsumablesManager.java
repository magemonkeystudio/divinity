package su.nightexpress.quantumrpg.modules.list.consumables;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.QuantumPlayerItemUseEvent;
import su.nightexpress.quantumrpg.modules.LimitedItem;
import su.nightexpress.quantumrpg.modules.UsableItem;
import su.nightexpress.quantumrpg.modules.api.QModuleUsage;
import su.nightexpress.quantumrpg.stats.EntityStats;

public class ConsumablesManager extends QModuleUsage<ConsumablesManager.Consume> {
    private boolean allowConsumeFullHealth;

    private boolean allowConsumeFullFood;

    public ConsumablesManager(@NotNull QuantumRPG plugin) {
        super(plugin, Consume.class);
    }

    @NotNull
    public String getId() {
        return "consumables";
    }

    @NotNull
    public String version() {
        return "1.8.0";
    }

    public void setup() {
        this.allowConsumeFullHealth = this.cfg.getBoolean("consuming.allow-on-full-health");
        this.allowConsumeFullFood = this.cfg.getBoolean("consuming.allow-on-full-food");
    }

    public void shutdown() {
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(QuantumPlayerItemUseEvent e) {
        LimitedItem uItem = e.getItem();
        if (!(uItem instanceof Consume))
            return;
        Player p = e.getPlayer();
        Consume c = (Consume) uItem;
        ItemStack i = e.getItemStack();
        double maxHealth = EntityStats.getEntityMaxHealth(p);
        if (c.getHealth() > 0.0D && !isConsumingAllowedOnFullHealth() && p.getHealth() >= maxHealth) {
            (this.plugin.lang()).Consumables_Consume_Error_HealthLevel.replace("%item%", ItemUT.getItemName(i)).send(p);
            e.setCancelled(true);
            return;
        }
        if (c.getHunger() > 0.0D && !isConsumingAllowedOnFullFood() && p.getFoodLevel() >= 20) {
            (this.plugin.lang()).Consumables_Consume_Error_FoodLevel.replace("%item%", ItemUT.getItemName(i)).send(p);
            e.setCancelled(true);
            return;
        }
        c.applyEffects(p);
    }

    public boolean isConsumingAllowedOnFullHealth() {
        return this.allowConsumeFullHealth;
    }

    public boolean isConsumingAllowedOnFullFood() {
        return this.allowConsumeFullFood;
    }

    public class Consume extends UsableItem {
        private final double hp;

        private final double hunger;

        public Consume(@NotNull QuantumRPG plugin, JYML cfg) {
            super(plugin, cfg, ConsumablesManager.this);
            this.hp = cfg.getDouble("effects.health");
            this.hunger = cfg.getDouble("effects.hunger");
        }

        public double getHealth() {
            return this.hp;
        }

        public double getHunger() {
            return this.hunger;
        }

        public void applyEffects(@NotNull Player p) {
            double max = EntityStats.getEntityMaxHealth(p);
            int food = (int) Math.min(20.0D, p.getFoodLevel() + getHunger());
            int saturation = 20 - food;
            p.setHealth(Math.min(p.getHealth() + getHealth(), max));
            p.setFoodLevel(food);
            p.setSaturation(saturation);
        }
    }
}
