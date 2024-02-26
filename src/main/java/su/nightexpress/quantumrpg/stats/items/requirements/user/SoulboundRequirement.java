package su.nightexpress.quantumrpg.stats.items.requirements.user;

import mc.promcteam.engine.config.api.ILangMsg;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.stats.items.ItemTags;

import java.util.UUID;

public class SoulboundRequirement extends AbstractOwnerRequirement {

    public SoulboundRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "soulbound",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_SOUL
        );
    }

    @Override
    @NotNull
    public String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_SOULBOUND;
    }

    @Override
    public boolean canUse(@NotNull Player player, @Nullable UUID value) {
        return value == null || player.getUniqueId().equals(value);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull UUID value) {
        if (value.equals(DUMMY_ID)) {
            return SoulboundManager.SOULBOUND_FORMAT_FREE;
        }
        String user = QuantumRPG.getInstance().getServer().getOfflinePlayer(value).getName();
        if (user == null) user = "null";

        return SoulboundManager.SOULBOUND_FORMAT_APPLIED.replace("%player%", user);
    }

    @Override
    @NotNull
    public ILangMsg getDenyMessage(@NotNull Player p, @NotNull ItemStack src) {
        if (this.isRequired(src)) {
            return QuantumRPG.getInstance().lang().Module_Item_Interact_Error_Soulbound;
        }
        UUID   value = this.getRaw(src);
        String user  = "null";
        if (value != null) {
            String name = QuantumRPG.getInstance().getServer().getOfflinePlayer(value).getName();
            if (name != null) user = name;
        }
        return QuantumRPG.getInstance().lang().Module_Item_Interact_Error_Owner.replace("%owner%", user);
    }
}
