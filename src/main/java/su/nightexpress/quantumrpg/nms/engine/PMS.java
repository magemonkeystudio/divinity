package su.nightexpress.quantumrpg.nms.engine;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import mc.promcteam.engine.utils.reflection.ReflectionUtil;

public interface PMS {

    default float getAttackCooldown(@NotNull Player p) {
        return ReflectionUtil.getAttackCooldown(p);
    }

    default void changeSkull(Block b, String hash) {
        ReflectionUtil.changeSkull(b, hash);
    }

    default double getDefaultDamage(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultDamage(itemStack);
    }

    default double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultSpeed(itemStack);
    }

    default double getDefaultArmor(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultArmor(itemStack);
    }

    default double getDefaultToughness(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultToughness(itemStack);
    }
}
