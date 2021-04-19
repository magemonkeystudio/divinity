package su.nightexpress.quantumrpg.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.LimitedItem;
import su.nightexpress.quantumrpg.modules.ModuleCache;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.RatedItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;

public class QuantumAPI {

	private static QuantumRPG plugin = QuantumRPG.getInstance();
	
	public static ModuleCache getModuleManager() {
		return plugin.getModuleCache();
	}
	
	@Nullable
	public static ModuleItem getModuleItem(@NotNull QModuleDrop<?> e, @NotNull String id) {
		if (!e.isEnabled() || !e.isLoaded()) return null;
		return e.getItemById(id);
	}
	
	@Nullable
	public static ItemStack getItemByModule(
			@NotNull QModuleDrop<?> e, 
			@NotNull String id, 
			int lvl, int uses, int suc) {
		
		ModuleItem mi = getModuleItem(e, id);
		if (mi == null) return null;
		
		if (mi instanceof RatedItem) {
			RatedItem si = (RatedItem) mi;
			return si.create(lvl, uses, suc);
		}
		else if (mi instanceof LimitedItem) {
			LimitedItem si = (LimitedItem) mi;
			return si.create(lvl, suc); // suc = uses here.
		}
		else {
			return mi.create(lvl);
		}
	}
}
