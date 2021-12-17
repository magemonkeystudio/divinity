package su.nightexpress.quantumrpg.modules;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

import java.util.Map;
import java.util.TreeMap;

public abstract class RatedItem extends LimitedItem {

    protected TreeMap<Integer, String[]> successRateExpr;

    // Creating new config
    @Deprecated
    public RatedItem(QuantumRPG plugin, String path, ModuleSocket<?> module) {
        super(plugin, path, module);
    }

    // Load from existent config
    public RatedItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg, @NotNull QModuleDrop<?> module) {
        super(plugin, cfg, module);

        if (!cfg.contains("success-rate-by-level")) {
            cfg.set("success-rate-by-level.1", "75");
            cfg.set("success-rate-by-level.2", "30 * %ITEM_LEVEL%");
            cfg.set("success-rate-by-level.3", "30:50");
        }

        this.successRateExpr = new TreeMap<>();
        for (String rLvl : cfg.getSection("success-rate-by-level")) {
            int itemLvl = StringUT.getInteger(rLvl, -1);
            if (itemLvl <= 0) continue;

            String raw = cfg.getString("success-rate-by-level." + rLvl);
            if (raw == null || raw.isEmpty()) continue;

            this.successRateExpr.put(itemLvl, raw.split(":"));
        }
    }

    public final int[] getSuccessRate(int itemLvl) {
        Map.Entry<Integer, String[]> e = this.successRateExpr.floorEntry(itemLvl);
        if (e == null) return new int[]{100, 100};

        return this.doMathExpression(itemLvl, e.getValue());
    }

    @Override
    @NotNull
    public ItemStack create(int lvl, int uses) {
        return this.create(lvl, uses, -1);
    }

    @Override
    @NotNull
    protected final ItemStack build(int lvl, int uses) {
        return this.build(lvl, uses, -1);
    }

    @NotNull
    public ItemStack create(int lvl, int uses, int success) {
        lvl = this.validateLevel(lvl);
        if (uses < 1) uses = this.getCharges(lvl);

        if (success < 0) {
            int[] minMax = this.getSuccessRate(lvl);
            success = Rnd.get(minMax[0], minMax[1]);
        }

        return this.build(lvl, uses, Math.min(100, success));
    }

    @NotNull
    protected ItemStack build(int lvl, int uses, int suc) {
        ItemStack item = super.build(lvl, uses);

        ItemStats.setSocketRate(item, suc);

        replacePlaceholders(item);

        return item;
    }
}
