package su.nightexpress.quantumrpg.modules;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.attributes.ChargesAttribute;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.util.Map;
import java.util.TreeMap;

public class LimitedItem extends LeveledItem {

    protected TreeMap<Integer, Integer> chargesByLvl;

    // Creating new config
    @Deprecated
    public LimitedItem(QuantumRPG plugin, String path, QModuleDrop<?> module) {
        super(plugin, path, module);
    }

    // Load from config
    public LimitedItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg, @NotNull QModuleDrop<?> module) {
        super(plugin, cfg, module);

        this.chargesByLvl = new TreeMap<>();
        for (String sLvl : cfg.getSection("uses-by-level")) {
            int itemLvl = StringUT.getInteger(sLvl, -1);
            if (itemLvl <= 0) continue;

            int uses = cfg.getInt("uses-by-level." + sLvl);
            this.chargesByLvl.put(itemLvl, uses);
        }
    }

    @NotNull
    public TreeMap<Integer, Integer> getChargesMap() {
        return this.chargesByLvl;
    }

    public final int getCharges(int lvl) {
        Map.Entry<Integer, Integer> e = this.chargesByLvl.floorEntry(lvl);
        if (e == null) return -1;

        return e.getValue();
    }

    @Override
    @NotNull
    public final ItemStack create(int lvl) {
        return this.create(lvl, -1);
    }

    @NotNull
    public ItemStack create(int lvl, int uses) {
        lvl = this.validateLevel(lvl); // Normalize level.
        if (uses < 1) uses = this.getCharges(lvl);

        return this.build(lvl, uses);
    }

    @Override
    @NotNull
    protected final ItemStack build(int lvl) {
        return this.build(lvl, -1);
    }

    @NotNull
    protected ItemStack build(int lvl, int uses) {
        ItemStack item = super.build(lvl);

        ChargesAttribute charges = ItemStats.getAttribute(ChargesAttribute.class);
        if (charges != null && charges.hasPlaceholder(item)) {
            charges.add(item, new int[]{uses, uses}, -1);
        }
        LoreUT.replacePlaceholder(item, ItemTags.PLACEHOLDER_ITEM_CHARGES, null);

        return item;
    }
}
