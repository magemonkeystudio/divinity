package su.nightexpress.quantumrpg.modules.list.itemgenerator.generators;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.api.AbstractAttributeGenerator;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

public class SingleAttributeGenerator<A extends ItemLoreStat<String>> extends AbstractAttributeGenerator {
	
	private Map<A, Double> attributes;
	
	public SingleAttributeGenerator(
			@NotNull QuantumRPG plugin,
			@NotNull GeneratorItem generatorItem,
			@NotNull String path,
			@NotNull Collection<@NotNull A> attributesAll, 
			@NotNull String placeholder) {
		super(plugin, generatorItem, placeholder);
		JYML cfg = this.generatorItem.getConfig();
		
		this.minAmount = 1;
		this.maxAmount = 1;
		
		this.attributes = new HashMap<>();
		attributesAll.forEach(att -> {
			double chance = cfg.getDouble(path + att.getId().toUpperCase());
			if (chance <= 0) return;
			
			this.attributes.put(att, chance);
		});
	}

	@Override
	public void generate(@NotNull ItemStack item, int itemLevel) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
		
		if (!ItemUtils.isWeapon(item) || this.attributes.isEmpty()) {
			LoreUT.replacePlaceholder(item, this.placeholder, null);
			return;
		}

		@Nullable A handAtt = Rnd.getRandomItem(this.attributes, true);
		if (handAtt != null) {
			handAtt.add(item, handAtt.getName(), -1);
		}
		LoreUT.replacePlaceholder(item, this.placeholder, null);
	}
}
