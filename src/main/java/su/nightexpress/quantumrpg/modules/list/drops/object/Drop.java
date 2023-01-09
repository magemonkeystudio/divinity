package su.nightexpress.quantumrpg.modules.list.drops.object;

import mc.promcteam.engine.utils.random.Rnd;
import org.jetbrains.annotations.NotNull;

public class Drop {

	private final DropItem dropConfig;
	private       int count = 0;
	
	public Drop(@NotNull DropItem dropTemplate) {
		this.dropConfig = dropTemplate;
	}

	/**
	 * Regenerates item count upon each call
	 */
	public void calculateCount() {
		count = Rnd.get(this.dropConfig.getMinAmount(), this.dropConfig.getMaxAmount());
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@NotNull
	public DropItem getDropConfig() {
		return dropConfig;
	}
}