package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

import java.util.Set;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface DropCalculator {
  int dropCalculator(Player paramPlayer, LivingEntity paramLivingEntity, Set<DropItem> paramSet, int paramInt, float paramFloat);
}
