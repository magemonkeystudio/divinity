package su.nightexpress.quantumrpg.modules.list.itemgenerator.api;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IAttributeGenerator {

	public int getMinAmount();
	
	public int getMaxAmount();
	
	@NotNull
	public List<String> getLoreFormat();
	
	@NotNull
	public String getPlaceholder();
	
	public void generate(@NotNull ItemStack item, int itemLevel);
}
