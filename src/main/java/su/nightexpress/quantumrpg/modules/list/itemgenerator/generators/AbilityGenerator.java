package su.nightexpress.quantumrpg.modules.list.itemgenerator.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.types.ClickType;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemAbilityHandler;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.api.AbstractAttributeGenerator;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

public class AbilityGenerator extends AbstractAttributeGenerator {
	
	private TreeMap<Integer, Map<String, Object>> varsLvl;
	private Set<ClickType> allowedClicks;
	private Map<String, AbilityGenerator.Ability> abilitiesRef;
	private Map<AbilityGenerator.Ability, Double> abilities;
	
	public AbilityGenerator(@NotNull QuantumRPG plugin, @NotNull GeneratorItem generatorItem, @NotNull String placeholder) {
		super(plugin, generatorItem, placeholder);
		
		JYML cfg = this.generatorItem.getConfig();
		String path = "generator.abilities.";
		
		this.minAmount = cfg.getInt(path + "minimum");
		this.maxAmount = Math.min(cfg.getInt(path + "maximum"), ClickType.values().length);
		
		this.varsLvl = new TreeMap<>();
		for (String sLvl : cfg.getSection(path + "variables-by-level")) {
			int lvl = StringUT.getInteger(sLvl, -1);
			if (lvl <= 0) continue;
			
			Map<String, Object> vars = new HashMap<>();
			for (String var : cfg.getSection(path + "variables-by-level." + sLvl)) {
				Object varVal = cfg.get(path + "variables-by-level." + sLvl + "." + var);
				vars.put(var.toLowerCase(), varVal);
			}
			this.varsLvl.put(lvl, vars);
		}
		
		this.allowedClicks = new HashSet<>();
		for (String sType : cfg.getStringList(path + "allowed-clicks")) {
			ClickType clickType = CollectionsUT.getEnum(sType, ClickType.class);
			if (clickType == null) continue;
			this.allowedClicks.add(clickType);
		}
		
		this.abilities = new HashMap<>();
		this.abilitiesRef = new HashMap<>();
		for (String abilityId : cfg.getSection(path + "list")) {
			String path2 = path + "list." + abilityId + ".";
			
			double chance = cfg.getDouble(path2 + "chance");
			if (chance <= 0) continue;
			
			double cooldown = cfg.getDouble(path2 + "cooldown");
			List<String> aLoreFormat = cfg.getStringList(path2 + "lore-format");
			ActionManipulator actions = new ActionManipulator(plugin, cfg, path2 + "actions");
			
			AbilityGenerator.Ability ability = new Ability(abilityId, cooldown, aLoreFormat, actions);
			this.abilities.put(ability, chance);
			this.abilitiesRef.put(ability.getId(), ability);
		}
	}
	
	@Nullable
	public Ability getAbility(@NotNull String id) {
		return this.abilitiesRef.get(id.toLowerCase());
	}
	
	@NotNull
	public TreeMap<Integer, Map<String, Object>> getVariables() {
		return this.varsLvl;
	}
	
	@NotNull
	private final String replaceVars(@NotNull String str, int lvl) {
		Map.Entry<Integer, Map<String, Object>> e = this.varsLvl.floorEntry(lvl);
		if (e == null) return str;
		
		Map<String, Object> vars = e.getValue();
		for (Map.Entry<String, Object> eVar : vars.entrySet()) {
			Object value = eVar.getValue();
			String valueFormat = value.toString();
			if (value instanceof Number) {
				valueFormat = NumberUT.format(StringUT.getDouble(valueFormat, -1));
			}
			str = str.replace("%var_" + eVar.getKey() + "%", valueFormat);
		}
		return str;
	}
	
	@Override
	public void generate(@NotNull ItemStack item, int itemLevel) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
		
		if (!ItemUtils.isWeapon(item)) {
			LoreUT.replacePlaceholder(item, placeholder, null);
			return;
		}
		
		List<ClickType> allowedClicks = new ArrayList<>(this.allowedClicks);
		int pos = lore.indexOf(this.placeholder);
		int min = this.getMinAmount();
		int max = Math.min(allowedClicks.size(), this.getMaxAmount());

		if (pos < 0 || max == 0 || allowedClicks.isEmpty() || this.abilities.isEmpty()) {
			LoreUT.replacePlaceholder(item, placeholder, null);
			return;
		}
		
		Map<AbilityGenerator.Ability, Double> abilityMap = new HashMap<>(this.abilities);
		
		// Min: -1, Max: 5
		// Roll: <=5
		//
		// Min: -1, Max: -1
		// Roll: Unlimited
		//
		// Min: 3, Max: -1
		// Roll: >3
		//
		// Min: 2, Max: 5
		// Roll: (2, 5);
		boolean isMaxUnlimited = (max < 0);
		boolean isMinUnlimited = (min < 0);
		int maxSize = allowedClicks.size();
		int rollMax = isMaxUnlimited ? maxSize : max;
		int rollMin = (isMinUnlimited || (isMinUnlimited && isMaxUnlimited)) ? Rnd.get(rollMax+1) : min;
		int roll = Rnd.get((isMaxUnlimited ? rollMax : rollMin), rollMax);
		
		// If get stats number is 0
		// Remove all stat placeholders
		if (roll <= 0) {
			LoreUT.replacePlaceholder(item, this.placeholder, null);
			return;
		}
		
		Map<ClickType, AbilityGenerator.Ability> abilityAdd = new HashMap<>();
		
		for (int count = 0; count < roll; count++) {
			ClickType clickType = Rnd.get(allowedClicks);
			if (clickType == null || abilityMap.isEmpty()) break;
			
			AbilityGenerator.Ability ability = Rnd.getRandomItem(abilityMap, true);
			if (ability == null) break;
			
			// Minimal stats are added, so we can process chances
			if (count >= rollMin) {
				// If stats are not limited, then we will check
				// for a chance to apply on item manually.
				double chance = abilityMap.get(ability);
				if (Rnd.get(true) > chance) {
					abilityMap.remove(ability);
					continue;
				}
			}
			
			// Add ability lore with variables.
			for (String format : ability.getLoreFormat()) {
				format = format.replace("%key%", plugin.lang().getEnum(clickType));
				pos = LoreUT.addToLore(lore, pos, this.replaceVars(format, itemLevel));
			}
			lore.remove(this.placeholder);
			
			abilityAdd.put(clickType, ability);
			abilityMap.remove(ability);
			allowedClicks.remove(clickType);
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		abilityAdd.forEach((key, ability) -> {
			DataUT.setData(item, ItemAbilityHandler.ABILITY_KEYS.get(key), ability.getId());
		});
	}
	
	public class Ability {
		
		private String id;
		private double cooldown;
		private List<String> loreFormat;
		
		private static final String ACTION_ENGINE_ID = "itemgen-ability-";
		
		public Ability(
				@NotNull String id,
				double cd, 
				@NotNull List<String> loreFormat,
				@NotNull ActionManipulator manipulator
				) {
			this.id = id.toLowerCase();
			this.cooldown = cd;
			this.loreFormat = StringUT.color(loreFormat);
			
			getVariables().keySet().forEach(lvl -> {
				ActionManipulator am = manipulator.replace(str -> replaceVars(str, lvl));
				plugin.getActionsManager().registerManipulator(this.getActionEngineId(lvl), am);
			});
		}
		
		@NotNull
		public String getId() {
			return id;
		}
		
		public double getCooldown() {
			return cooldown;
		}
		
		public long getCooldownDate() {
			return System.currentTimeMillis() + (long)(1000D * cooldown);
		}
		
		@NotNull
		public List<String> getLoreFormat() {
			return loreFormat;
		}
		
		@NotNull
		private String getActionEngineId(int level) {
			return ACTION_ENGINE_ID + this.getId() + "-" + level;
		}
		
		@Nullable
		public ActionManipulator getActions(int level) {
	    	Integer abilityLvl = getVariables().floorKey(level);
	    	if (abilityLvl == null) return null;
	    	
	    	ActionManipulator manipulator = plugin.getActionsManager().getManipulator(this.getActionEngineId(abilityLvl));
	    	return manipulator;
		}
	}
}
