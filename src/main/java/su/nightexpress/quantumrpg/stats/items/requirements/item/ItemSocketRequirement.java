package su.nightexpress.quantumrpg.stats.items.requirements.item;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.DataUT;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;
import su.nightexpress.quantumrpg.stats.items.requirements.api.ItemRequirement;

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
		ILangMsg msg = plugin.lang().Module_Item_Apply_Error_Socket;
		
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
			plugin.error("Empty/Incomplete arguments for Socket Requirement!");
			return "";
		}
		
		SocketAttribute.Type type = SocketAttribute.Type.getByName(arr[0]);
		if (type == null) {
			plugin.error("Invalid Socket Type '" + arr[0] + "' for Socket Requirement!");
			return "";
		}
		
		SocketAttribute socket = ItemStats.getSocket(type, arr[1]);
		if (socket == null) {
			plugin.error("Invalid Socket Attribute Id '" + arr[1] + "' for Socket Requirement!");
			return "";
		}
		
		return socket.getName();
	}

}
