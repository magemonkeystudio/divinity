package su.nightexpress.quantumrpg.stats.items.attributes;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.StringUT;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;
import su.nightexpress.quantumrpg.modules.list.essences.EssencesManager;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.runes.RuneManager;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.DuplicableItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.Emptible;
import su.nightexpress.quantumrpg.stats.tiers.Tier;
import su.nightexpress.quantumrpg.stats.tiers.Tiered;

public class SocketAttribute extends DuplicableItemLoreStat<String[]> implements Tiered, Emptible<String[]> {

	private Type type;
	private ModuleSocket<?> module;
	private Tier tier;
	
	private String formatValueEmpty;
	private String formatValueFilled;
	
	public SocketAttribute(
			@NotNull Type type,
			
			@NotNull String id,
			@NotNull String name,
			@NotNull String format,
			
			@NotNull Tier tier,
			
			@NotNull String formatValueEmpty,
			@NotNull String formatValueFilled
			) {
		super(id, name, format, "%SOCKET_" + type.name() + "_" + id + "%", ItemTags.TAG_ITEM_SOCKET + type.name(), DataUT.STRING_ARRAY);
		this.type = type;
		this.tier = tier;
		
		this.name = this.getTier().format(this.name);
		this.formatValueEmpty = this.getTier().format(StringUT.color(formatValueEmpty).replace("%name%", this.getName()));
		this.formatValueFilled = this.getTier().format(StringUT.color(formatValueFilled).replace("%name%", this.getName()));
	}
	
	public static enum Type {
	
		GEM,
		RUNE,
		ESSENCE,
		;
		
		@Nullable
		public static SocketAttribute.Type getByModule(@NotNull ModuleSocket<?> mod) {
			if (mod instanceof GemManager) {
				return SocketAttribute.Type.GEM;
			}
			else if (mod instanceof RuneManager) {
				return SocketAttribute.Type.RUNE;
			}
			else if (mod instanceof EssencesManager) {
				return SocketAttribute.Type.ESSENCE;
			}
			else return null;
		}
		
		@Nullable
		public static SocketAttribute.Type getByName(@NotNull String name) {
			try {
				return SocketAttribute.Type.valueOf(name.toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				return null;
			}
		}
		
		@Nullable
		public ModuleSocket<?> getModule() {
			switch(this) {
				case GEM: {
					return plugin.getModuleCache().getGemManager();
				}
				case RUNE: {
					return plugin.getModuleCache().getRuneManager();
				}
				case ESSENCE: {
					return plugin.getModuleCache().getEssenceManager();
				}
			}
			return null;
		}
	}

	@NotNull
	public ModuleSocket<?> getModule() {
		if (this.module == null) {
			this.module = this.type.getModule();
		}
		if (this.module == null) {
			throw new IllegalStateException("NULL module in Socket Attribute!");
		}
		return this.module;
	}
	
	@NotNull
	public SocketAttribute.Type getType() {
		return this.type;
	}

	@Override
	@NotNull
	public Tier getTier() {
		return this.tier;
	}
	
	public final int getFirstEmptyIndex(@NotNull ItemStack item) {
		int total = this.getAmount(item);
		for (int index = 0; index < total; index++) {
			String[] storedValue = this.getRaw(item, index);
			if (storedValue != null && this.isEmpty(storedValue)) {
				return index;
			}
		}
		return -1;
	}

	public final int getEmptyAmount(@NotNull ItemStack item) {
		int total = this.getAmount(item);
		int count = 0;
		
		for (int index = 0; index < total; index++) {
			String[] storedValue = this.getRaw(item, index);
			if (storedValue != null && this.isEmpty(storedValue)) {
				count++;
			}
		}
		return count;
	}
	
	public final int getFilledAmount(@NotNull ItemStack item) {
		int total = this.getAmount(item);
		int count = 0;
		
		for (int index = 0; index < total; index++) {
			String[] storedValue = this.getRaw(item, index);
			if (storedValue != null && !this.isEmpty(storedValue)) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
		String format = this.formatValueEmpty;
		
		if (!this.isEmpty(value)) {
			String itemId = value[0];
			SocketItem mItem = this.getModule().getItemById(itemId);
			if (mItem == null) {
				plugin.error("Invalid socket item '" + itemId + "' for Socket Attribute.");
				return format;
			}
			
			int srcLevel = StringUT.getInteger(value[1], 0);
			return this.formatValueFilled.replace("%value%", mItem.getSocketDisplay(srcLevel));
		}
		return format;
	}

	@Override
	public boolean isEmpty(@NotNull String[] value) {
		return value.length != 2 || value[0].isEmpty() || value[1].isEmpty();
	}

	@Override
	public String[] getDefaultValue() {
		return new String[] {"", ""};
	}
}
