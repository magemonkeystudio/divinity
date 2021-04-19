package su.nightexpress.quantumrpg.stats;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.quantumrpg.QuantumRPG;

public class ProjectileStats {

	private static QuantumRPG plugin = QuantumRPG.getInstance();
	
	private static final String PROJECTILE_SOURCE_WEAPON = 	"QRPG_PJ_WEAPON";
	private static final String PROJECTILE_PICKABLE = 		"QRPG_PJ_PICKABLE";
	private static final String PROJECTILE_LAUNCH_POWER = 	"QRPG_PJ_POWER";
	
	public static void setSrcWeapon(@NotNull Projectile pp, @NotNull ItemStack item) {
		pp.setMetadata(PROJECTILE_SOURCE_WEAPON, new FixedMetadataValue(plugin, item));
	}

	@Nullable
	public static ItemStack getSrcWeapon(@NotNull Projectile e) {
		if (!e.hasMetadata(PROJECTILE_SOURCE_WEAPON)) return null;
		
		Object val = e.getMetadata(PROJECTILE_SOURCE_WEAPON).get(0).value();
		return (ItemStack) val;
	}

	public static void setPower(@NotNull Projectile e, double power) {
		e.setMetadata(PROJECTILE_LAUNCH_POWER, new FixedMetadataValue(plugin, power));
	}

	public static double getPower(@NotNull Projectile e) {
		if (!e.hasMetadata(PROJECTILE_LAUNCH_POWER)) return 1D;
		
		return e.getMetadata(PROJECTILE_LAUNCH_POWER).get(0).asDouble();
	}

	public static void setPickable(@NotNull Entity pp, boolean b) {
		pp.setMetadata(PROJECTILE_PICKABLE, new FixedMetadataValue(plugin, b));
	}
	
	public static boolean isPickable(@NotNull Entity pp) {
		if (!pp.hasMetadata(PROJECTILE_PICKABLE)) return true;
		
		return pp.getMetadata(PROJECTILE_PICKABLE).get(0).asBoolean();
	}
}
