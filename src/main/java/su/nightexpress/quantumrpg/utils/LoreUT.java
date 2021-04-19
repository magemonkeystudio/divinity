package su.nightexpress.quantumrpg.utils;

import java.util.List;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.quantumrpg.QuantumRPG;

public class LoreUT {

	private static QuantumRPG plugin;
	
	static {
		plugin = QuantumRPG.getInstance();
	}
	
	@Deprecated
	public static final String TAG_SPLITTER = "__x__";
	
	public static void addOrReplace(
			@NotNull List<String> lore, int orig, int line, @NotNull String format) {
		
    	if (orig < 0) {
    		if (line < 0 || line >= lore.size()) {
    			lore.add(format);
    		}
    		else {
    			lore.add(line, format);
    		}
    	}
    	else {
	    	if (line < 0 || line >= lore.size()) {
	    		lore.set(orig, format);
	    	}
	    	else {
	    		lore.remove(orig);
	    		lore.add(line, format);
	    	}
    	}
	}
	
	public static int addToLore(@NotNull List<String> lore, int pos, @NotNull String value) {
		if (pos >= lore.size()) {
			lore.add(value);
		}
		else {
			lore.add(pos, value);
		}
		return pos + 1;
	}
	
	@NotNull
    public static String getStrSeparated(
    		@NotNull Set<String> values, @NotNull String sep, @NotNull String color) {
		return LoreUT.getStrSeparated(values.toArray(new String[values.size()]), sep, color);
	}
	
	@NotNull
    public static String getStrSeparated(
    		@NotNull String[] values, @NotNull String sep, @NotNull String color) {
		
    	StringBuilder builder = new StringBuilder("");
    	for (String clazz : values) {
    		if (clazz.isEmpty()) continue;
    		if (builder.length() > 0) {
    			builder.append(sep);
    		}
    		builder.append(color);
    		builder.append(clazz);
    	}
    	
    	return builder.toString();
    }
    
	public static void replacePlaceholder(
			@NotNull ItemStack item, @NotNull String placeholder, @Nullable String replacer) {
		
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
		
		int pos = lore.indexOf(placeholder);
		if (pos < 0) return;
		
		if (replacer != null && !replacer.isEmpty()) {
			lore.set(pos, replacer);
		}
		else {
			lore.remove(pos);
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public static void replaceEnchants(@NotNull ItemStack item) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
		
		int pos = lore.indexOf("%ENCHANTS%");
		if (pos < 0) return;
		
		for (Enchantment e : meta.getEnchants().keySet()) {
			String value = plugin.lang().getEnchantment(e) + " " + NumberUT.toRoman(meta.getEnchantLevel(e));
			lore.add(pos, value);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		replacePlaceholder(item, "%ENCHANTS%", null);
	}
}
