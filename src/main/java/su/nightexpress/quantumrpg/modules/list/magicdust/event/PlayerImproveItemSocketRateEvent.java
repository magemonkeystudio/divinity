package su.nightexpress.quantumrpg.modules.list.magicdust.event;

import mc.promcteam.engine.manager.api.event.ICancellableEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerImproveItemSocketRateEvent extends ICancellableEvent {

    private Player    player;
    private ItemStack item;
    private int       rateOld;
    private int       rateNew;

    public PlayerImproveItemSocketRateEvent(
            @NotNull Player player,
            @NotNull ItemStack item,
            int rateOld,
            int rateNew
    ) {
        this.player = player;
        this.item = item;
        this.rateOld = rateOld;
        this.rateNew = rateNew;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public ItemStack getItem() {
        return this.item;
    }

    public int getRatePrevious() {
        return this.rateOld;
    }

    public int getRateNew() {
        return this.rateNew;
    }
}
