package su.nightexpress.quantumrpg.modules.list.classes.object;

import mc.promcteam.engine.utils.random.Rnd;

public class ExpObject {

	private int min;
	private int max;
	private double chance;
	
	public ExpObject(int min, int max, double chance) {
		this.min = min;
		this.max = max;
		this.chance = chance;
	}
	
	public int getMinExp() {
		return this.min;
	}
	
	public int getMaxExp() {
		return this.max;
	}
	
	public double getChance() {
		return this.chance;
	}
	
	public int getExp() {
		if (Rnd.get(true) <= this.getChance()) {
			return Rnd.get(this.getMinExp(), this.getMaxExp());
		}
		return 0;
	}
}
