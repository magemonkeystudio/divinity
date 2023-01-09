package su.nightexpress.quantumrpg.modules.list.identify.event;

import mc.promcteam.engine.manager.api.event.ICancellableEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.identify.IdentifyManager.IdentifyTome;
import su.nightexpress.quantumrpg.modules.list.identify.IdentifyManager.UnidentifiedItem;

public class PlayerIdentifyItemEvent extends ICancellableEvent {

    private UnidentifiedItem target;
    private IdentifyTome     tome;
    private ItemStack        result;
    private Player           player;

    public PlayerIdentifyItemEvent(
            @NotNull UnidentifiedItem target,
            @NotNull IdentifyTome tome,
            @NotNull ItemStack result,
            @NotNull Player player
    ) {
        this.target = target;
        this.tome = tome;
        this.result = result;
        this.player = player;
    }

    @NotNull
    public IdentifyTome getTome() {
        return this.tome;
    }

    @NotNull
    public UnidentifiedItem getTargetItem() {
        return this.target;
    }

    @NotNull
    public ItemStack getResult() {
        return this.result;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }
}
