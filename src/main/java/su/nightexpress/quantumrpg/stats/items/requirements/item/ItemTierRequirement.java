package su.nightexpress.quantumrpg.stats.items.requirements.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.ILangMsg;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.ItemRequirement;
import su.nightexpress.quantumrpg.stats.tiers.Tier;

public class ItemTierRequirement extends ItemRequirement<String> {

	public ItemTierRequirement(@NotNull String name, @NotNull String format) {
		super(
			"tier", name, format, 
			ItemTags.PLACEHOLDER_REQ_ITEM_TIER, 
			ItemTags.TAG_REQ_ITEM_TIER, 
			PersistentDataType.STRING);
	}

	@Override
	public boolean canApply(@NotNull ItemStack src, @NotNull ItemStack target) {
		String tierId = this.getRaw(src);
		if (tierId == null) return true;
		
		QModuleDrop<?> module = ItemStats.getModule(target);
		if (module == null) return true;
		
		String itemId = ItemStats.getId(target);
		if (itemId == null) return true;
		
		ModuleItem moduleItem = module.getItemById(itemId);
		if (moduleItem == null) return true;
		
		Tier tier = moduleItem.getTier();
		
		return tier.getId().equalsIgnoreCase(tierId);
	}

	@Override
	public ILangMsg getApplyMessage(@NotNull ItemStack src, @NotNull ItemStack target) {
		String tierId = this.getRaw(src);
		if (tierId == null) return null;
		
		Tier tier = Config.getTier(tierId);
		if (tier == null) return null;
		
		return plugin.lang().Module_Item_Apply_Error_Tier.replace("%tier%", tier.getName());
	}

	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, @NotNull String value) {
		Tier tier = Config.getTier(value);
		if (tier == null) {
			plugin.warn("Invalid Tier requirement provided: '" + value + "' !");
			return "";
		}
		
		return tier.getName();
	}
}
