package su.nightexpress.quantumrpg.nms.engine;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PMS {
    
    float getAttackCooldown(@NotNull Player p);
    
    void changeSkull(Block b, String hash);
    
    double getDefaultDamage(@NotNull ItemStack itemStack);
    
    double getDefaultSpeed(@NotNull ItemStack itemStack);
    
    double getDefaultArmor(@NotNull ItemStack itemStack);
    
    double getDefaultToughness(@NotNull ItemStack itemStack);
}
