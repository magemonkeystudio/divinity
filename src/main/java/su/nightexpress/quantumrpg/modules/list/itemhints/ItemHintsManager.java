package su.nightexpress.quantumrpg.modules.list.itemhints;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.api.QModule;

public class ItemHintsManager extends QModule {
	
	private boolean glowEnabled;
	private boolean glowIgnoreVanilla;
	private Set<String> glowIgnoredMaterials;
	private Set<String> glowIgnoredNames;
	private Set<String> glowIgnoredLores;
	private Set<String> glowIgnoredModules;
	
	private boolean hintEnabled;
	private String hintFormatSingular;
	private String hintFormatPlural;
	private boolean hintIgnoreVanilla;
	private Set<String> hintIgnoredMaterials;
	private Set<String> hintIgnoredNames;
	private Set<String> hintIgnoredLores;
	private Set<String> hintIgnoredModules;
	
	public ItemHintsManager(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override
	@NotNull
	public String getId() {
		return EModule.ITEM_HINTS;
	}

	@Override
	@NotNull
	public String version() {
		return "1.3.0";
	}
	
	@Override
	public void setup() {
		String path = "glow.";
		if (this.glowEnabled = cfg.getBoolean(path + "enabled")) {
			glowIgnoreVanilla = cfg.getBoolean(path + "ignored-items.vanilla");
			glowIgnoredMaterials = new HashSet<>(cfg.getStringList(path + "ignored-items.by-material"));
			glowIgnoredNames = new HashSet<>(cfg.getStringList(path + "ignored-items.by-name"));
			glowIgnoredLores = new HashSet<>(cfg.getStringList(path + "ignored-items.by-lore"));
			glowIgnoredModules = new HashSet<>(cfg.getStringList(path + "ignored-items.by-module"));
		}
		
		path = "hint.";
		if (this.hintEnabled = cfg.getBoolean(path + "enabled")) {
			hintFormatSingular = StringUT.color(cfg.getString(path + "format.singular", "%name%"));
			hintFormatPlural = StringUT.color(cfg.getString(path + "format.plural", "%name% &7x%amount%"));
			hintIgnoreVanilla = cfg.getBoolean(path + "ignored-items.vanilla");
			hintIgnoredMaterials = new HashSet<>(cfg.getStringList(path + "ignored-items.by-material"));
			hintIgnoredNames = new HashSet<>(cfg.getStringList(path + "ignored-items.by-name"));
			hintIgnoredLores = new HashSet<>(cfg.getStringList(path + "ignored-items.by-lore"));
			hintIgnoredModules = new HashSet<>(cfg.getStringList(path + "ignored-items.by-module"));
		}
	}

	@Override
	public void shutdown() {
		if (this.glowIgnoredMaterials != null) {
			this.glowIgnoredMaterials.clear();
			this.glowIgnoredMaterials = null;
		}
		if (this.glowIgnoredNames != null) {
			this.glowIgnoredNames.clear();
			this.glowIgnoredNames = null;
		}
		if (this.glowIgnoredLores != null) {
			this.glowIgnoredLores.clear();
			this.glowIgnoredLores = null;
		}
		if (this.glowIgnoredModules != null) {
			this.glowIgnoredModules.clear();
			this.glowIgnoredModules = null;
		}
		
		if (this.hintIgnoredMaterials != null) {
			this.hintIgnoredMaterials.clear();
			this.hintIgnoredMaterials = null;
		}
		if (this.hintIgnoredNames != null) {
			this.hintIgnoredNames.clear();
			this.hintIgnoredNames = null;
		}
		if (this.hintIgnoredLores != null) {
			this.hintIgnoredLores.clear();
			this.hintIgnoredLores = null;
		}
		if (this.hintIgnoredModules != null) {
			this.hintIgnoredModules.clear();
			this.hintIgnoredModules = null;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();
		this.setItemHint(item, 0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		Item src = e.getEntity();
		Item target = e.getTarget();
		
		this.setItemHint(target, src.getItemStack().getAmount());
	}
	
	// Glow is added via packets
	public void setItemHint(@NotNull Item i, int addAmount) {
		if (!this.isHint(i)) return;
		
		ItemStack item = i.getItemStack();
		String name = ItemUT.getItemName(item);
		int amount = item.getAmount() + addAmount;
		String format = amount > 1 ? this.hintFormatPlural : this.hintFormatSingular;
		
		String name2 = format.replace("%name%", name).replace("%amount%", String.valueOf(amount));
		i.setCustomName(name2);
		i.setCustomNameVisible(true);
	}
	
	public boolean isGlow() {
		return this.glowEnabled;
	}
	
	public boolean isGlow(@NotNull Item item) {
		return this.isGlow() && this.isAffected(item, true);
	}

	public boolean isHint(@NotNull Item item) {
		return this.hintEnabled && this.isAffected(item, false);
	}

	private boolean isAffected(@NotNull Item item, boolean checkGlow) {
		String name = item.getCustomName();
		if (name != null) {
			for (String blackText : (checkGlow ? this.glowIgnoredNames : this.hintIgnoredNames)) {
				if (name.contains(blackText)) {
					return false;	
				}
			}
		}
		
		return this.isAffected(item.getItemStack(), checkGlow);
	}
	
	private boolean isAffected(@NotNull ItemStack item, boolean checkGlow) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null || (!meta.hasDisplayName() && !meta.hasLore())) {
			if (checkGlow ? this.glowIgnoreVanilla : this.hintIgnoreVanilla) {
				return false;
			}
		}
		
		String type = item.getType().name();
		if ((checkGlow ? this.glowIgnoredMaterials : this.hintIgnoredMaterials).contains(type)) {
			return false;
		}
		
		String name = ItemUT.getItemName(item);
		for (String blackText : (checkGlow ? this.glowIgnoredNames : this.hintIgnoredNames)) {
			if (name.contains(blackText)) {
				return false;	
			}
		}
		
		List<String> lore = meta == null ? null : meta.getLore();
		if (lore == null) return true;
		
		for (String blackText : (checkGlow ? this.glowIgnoredLores : this.hintIgnoredLores)) {
			for (String loreText : lore) {
				if (loreText.contains(blackText)) {
					return false;
				}
			}
		}
		return true;
	}
}
