package su.nightexpress.quantumrpg.modules.list.drops.object;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.random.Rnd;

public class Drop {

	private int index = 0;
	private int count = 0;
	private DropItem dropConfig;
	
	public Drop(@NotNull DropItem dropTemplate) {
		this.dropConfig = dropTemplate;
	}

	/**
	 * Regenerates item count upon each call
	 */
	public void calculateCount() {
		count = Rnd.get(this.dropConfig.getMinAmount(), this.dropConfig.getMaxAmount());
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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