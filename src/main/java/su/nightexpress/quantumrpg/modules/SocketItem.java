package su.nightexpress.quantumrpg.modules;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager.Gem;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.item.ItemSocketRequirement;

public abstract class SocketItem extends RatedItem {

    protected String socketDisplay;
    protected String targetItemSocketId;

    // Creating new config
    @Deprecated
    public SocketItem(@NotNull QuantumRPG plugin, @NotNull String path, @NotNull ModuleSocket<?> module) throws
            InvalidConfigurationException {
        super(plugin, path, module);
    }

    // Load from existent config
    public SocketItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg, @NotNull ModuleSocket<?> module) {
        super(plugin, cfg, module);

        this.socketDisplay = StringUT.color(cfg.getString("socket-display", this.getName()));

        this.targetItemSocketId = cfg.getString("target-requirements.socket", "").toLowerCase();
        if (this.targetItemSocketId == null || this.targetItemSocketId.isEmpty()) {
            throw new IllegalStateException("Socket Item must require Socket Type!");
        }
    }

    @NotNull
    public final String getSocketDisplay(int lvl) {
        String display = this.socketDisplay
                .replace(ItemTags.PLACEHOLDER_ITEM_LEVEL_ROMAN, NumberUT.toRoman(lvl))
                .replace(ItemTags.PLACEHOLDER_ITEM_LEVEL, String.valueOf(lvl));

        if (this instanceof Gem) {
            Gem      g    = (Gem) this;
            BonusMap bMap = g.getBonusMap(lvl);
            if (bMap != null) {
                display = bMap.replacePlaceholders(display);
            }
        }

        return display;
    }

    @NotNull
    public String getTargetSocketIdRequirement() {
        return this.targetItemSocketId;
    }

    @Override
    @NotNull
    protected ItemStack build(int lvl, int uses, int suc) {
        ItemStack item = super.build(lvl, uses, suc);

        boolean               isReq     = false;
        ItemSocketRequirement reqSocket = ItemRequirements.getItemRequirement(ItemSocketRequirement.class);
        if (reqSocket != null) {
            ModuleSocket<?> mod        = (ModuleSocket<?>) this.getModule();
            String          socketType = mod.getSocketType().name();
            isReq = reqSocket.add(item, new String[]{socketType, this.targetItemSocketId}, -1);
        }
        if (!isReq) {
            this.module.error("Attempt to create a Socket Item without the Socket Requirement!");
            return new ItemStack(Material.AIR);
        }

        return item;
    }
}
