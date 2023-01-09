package su.nightexpress.quantumrpg.nms.engine;

import mc.promcteam.engine.utils.reflection.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PMS {

    public float getAttackCooldown(@NotNull Player p) {
        return ReflectionUtil.getAttackCooldown(p);
    }

    public void changeSkull(Block b, String hash) {
        ReflectionUtil.changeSkull(b, hash);
    }

    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultDamage(itemStack);
    }

    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultSpeed(itemStack);
    }

    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultArmor(itemStack);
    }

    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return ReflectionUtil.getDefaultToughness(itemStack);
    }
}
