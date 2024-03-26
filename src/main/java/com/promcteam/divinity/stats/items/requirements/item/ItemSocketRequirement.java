package com.promcteam.divinity.stats.items.requirements.item;

import com.promcteam.codex.config.api.ILangMsg;
import com.promcteam.codex.util.DataUT;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.stats.items.ItemStats;
import com.promcteam.divinity.stats.items.ItemTags;
import com.promcteam.divinity.stats.items.attributes.SocketAttribute;
import com.promcteam.divinity.stats.items.requirements.api.ItemRequirement;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemSocketRequirement extends ItemRequirement<String[]> {

    public ItemSocketRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "socket",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_ITEM_SOCKET,
                ItemTags.TAG_REQ_ITEM_SOCKET,
                DataUT.STRING_ARRAY);

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_req_item_socketsocket"));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_req_item_socketsocket"));
    }

    @Override
    @NotNull
    public Class<String[]> getParameterClass() {
        return String[].class;
    }

    @Override
    public boolean canApply(@NotNull ItemStack src, @NotNull ItemStack target) {
        String[] req = this.getRaw(src);
        if (req == null || req.length != 2 || req[0].isEmpty() || req[1].isEmpty()) return false;

        SocketAttribute.Type type = SocketAttribute.Type.getByName(req[0]);
        if (type == null) return false;

        SocketAttribute socket = ItemStats.getSocket(type, req[1]);
        if (socket == null) return false;

        return socket.getFirstEmptyIndex(target) >= 0;
    }

    @Override
    public ILangMsg getApplyMessage(@NotNull ItemStack src, @NotNull ItemStack target) {
        ILangMsg msg = Divinity.getInstance().lang().Module_Item_Apply_Error_Socket;

        String[] arr = this.getRaw(src);
        if (arr == null || ArrayUtils.isEmpty(arr) || arr.length != 2) return msg;

        SocketAttribute.Type type = SocketAttribute.Type.getByName(arr[0]);
        if (type == null) return msg;

        SocketAttribute socket = ItemStats.getSocket(type, arr[1]);
        if (socket == null) return msg;

        return msg.replace("%socket%", socket.getName());
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] arr) {
        if (ArrayUtils.isEmpty(arr) || arr.length != 2) {
            Divinity.getInstance().error("Empty/Incomplete arguments for Socket Requirement!");
            return "";
        }

        SocketAttribute.Type type = SocketAttribute.Type.getByName(arr[0]);
        if (type == null) {
            Divinity.getInstance().error("Invalid Socket Type '" + arr[0] + "' for Socket Requirement!");
            return "";
        }

        SocketAttribute socket = ItemStats.getSocket(type, arr[1]);
        if (socket == null) {
            Divinity.getInstance().error("Invalid Socket Attribute Id '" + arr[1] + "' for Socket Requirement!");
            return "";
        }

        return socket.getName();
    }

}
