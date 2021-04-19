package su.nightexpress.quantumrpg.stats.items.requirements.user;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.DataUT;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;

public abstract class AbstractOwnerRequirement extends DynamicUserRequirement<UUID> {

	public static final UUID DUMMY_ID = UUID.fromString("00a0000a-0000-0000-0000-00aa0000a0a0");
	
	public AbstractOwnerRequirement(
			@NotNull String id, 
			@NotNull String name, 
			@NotNull String format,
			@NotNull String placeholder
			) {
		super(
			id, 
			name, 
			format, 
			placeholder,
			ItemTags.TAG_REQ_USER_OWNER, 
			DataUT.UUID
			);
	}
	
	public boolean add(@NotNull ItemStack item, int line) {
		return this.add(item, DUMMY_ID, line);
	}
	
	public boolean isRequired(@NotNull ItemStack item) {
		UUID uuid = this.getRaw(item);
		return uuid != null && uuid.equals(DUMMY_ID);
	}
}
