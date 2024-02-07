package su.nightexpress.quantumrpg.stats.items.requirements.user;

import mc.promcteam.engine.config.api.ILangMsg;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.stats.items.ItemTags;

import java.util.UUID;

public class UntradeableRequirement extends AbstractOwnerRequirement {

    public UntradeableRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "untradeable",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_ITEM_UNTRADEABLE
        );
    }

    @Override
    @NotNull
    public String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_UNTRADEABLE;
    }

    @Override
    public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
        UUID uuid = this.getRaw(item);
        return uuid == null || uuid.equals(DUMMY_ID) || player.getUniqueId().equals(uuid);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull UUID value) {
        if (value.equals(DUMMY_ID)) {
            return SoulboundManager.UNTRADE_FORMAT_FREE;
        }
        String user = QuantumRPG.getInstance().getServer().getOfflinePlayer(value).getName();
        if (user == null) user = "null";

        return SoulboundManager.UNTRADE_FORMAT_APPLIED.replace("%player%", user);
    }

    @Override
    @NotNull
    public ILangMsg getDenyMessage(@NotNull Player player, @NotNull ItemStack src) {
        UUID   value = this.getRaw(src);
        String user  = "null";
        if (value != null) {
            String name = QuantumRPG.getInstance().getServer().getOfflinePlayer(value).getName();
            if (name != null) user = name;
        }
        return QuantumRPG.getInstance().lang().Module_Item_Interact_Error_Owner.replace("%owner%", user);
    }
}
