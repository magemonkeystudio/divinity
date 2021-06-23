package su.nightexpress.quantumrpg.stats.items.requirements;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mc.promcteam.engine.utils.ItemUT;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.api.ItemRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.api.UserRequirement;

public class ItemRequirements {

	public static final Map<String, UserRequirement<?>> USER_REQUIREMENTS;
	public static final Map<String, ItemRequirement<?>> ITEM_REQUIREMENTS;
	
	static {
		USER_REQUIREMENTS = new HashMap<>();
		ITEM_REQUIREMENTS = new HashMap<>();
	}
	
	public static void clear() {
		USER_REQUIREMENTS.clear();
		ITEM_REQUIREMENTS.clear();
	}
	
	public static void registerUserRequirement(@NotNull UserRequirement<?> req) {
		USER_REQUIREMENTS.put(req.getId(), req);
	}
	
	@NotNull
	public static Collection<UserRequirement<?>> getUserRequirements() {
		return USER_REQUIREMENTS.values();
	}
	
	@Nullable
	public static UserRequirement<?> getUserRequirement(@NotNull String id) {
		return USER_REQUIREMENTS.get(id.toLowerCase());
	}

	public static <T extends UserRequirement<?>> boolean isRegisteredUser(@NotNull Class<T> clazz) {
		return getUserRequirement(clazz) != null;
	}
	
	public static <T extends ItemRequirement<?>> boolean isRegisteredItem(@NotNull Class<T> clazz) {
		return ItemRequirements.getItemRequirement(clazz) != null;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T getUserRequirement(@NotNull Class<T> clazz) {
		for (UserRequirement<?> stat : ItemRequirements.getUserRequirements()) {
			Class<?> clazz2 = stat.getClass();
			if (clazz.isAssignableFrom(clazz2)) {
				return (T) stat;
			}
		}
		return null;
	}
	
	public static void registerItemRequirement(@NotNull ItemRequirement<?> req) {
		ITEM_REQUIREMENTS.put(req.getId(), req);
	}
	
	@NotNull
	public static Collection<ItemRequirement<?>> getItemRequirements() {
		return ITEM_REQUIREMENTS.values();
	}
	
	@Nullable
	public static ItemRequirement<?> getItemRequirement(@NotNull String id) {
		return ITEM_REQUIREMENTS.get(id.toLowerCase());
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends ItemRequirement<?>> T getItemRequirement(@NotNull Class<T> clazz) {
		for (ItemRequirement<?> stat : ItemRequirements.getItemRequirements()) {
			Class<?> clazz2 = stat.getClass();
			if (clazz.isAssignableFrom(clazz2)) {
				return (T) stat;
			}
		}
		return null;
	}
	
	public static void updateItem(@Nullable Player p, @NotNull ItemStack item) {
		if (!EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE) return;
		
		for (UserRequirement<?> req : getUserRequirements()) {
			if (req.isDynamic()) {
				DynamicUserRequirement<?> dReq = (DynamicUserRequirement<?>) req;
				dReq.updateItem(p, item);
			}
		}
	}
	
	public static boolean canApply(
			@NotNull Player p, @NotNull ItemStack src, @NotNull ItemStack target) {
		
		for (ItemRequirement<?> req : ItemRequirements.getItemRequirements()) {
			if (req.isApplied(src) && !req.canApply(src, target)) {
				req.getApplyMessage(src, target)
					.replace("%target%", ItemUT.getItemName(target))
					.replace("%source%", ItemUT.getItemName(src))
					.replace("%player%", p.getName())
					.send(p);
				return false;
			}
		}
		return true;
	}
}
