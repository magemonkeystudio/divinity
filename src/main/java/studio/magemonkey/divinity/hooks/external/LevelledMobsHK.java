package studio.magemonkey.divinity.hooks.external;

import org.bukkit.Bukkit;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.hooks.HookMobLevel;

import java.util.Objects;

public class LevelledMobsHK extends NHook<Divinity> implements HookMobLevel {

    public LevelledMobsHK(@NotNull Divinity plugin) {
        super(plugin);

        Plugin levelledMobsPlugin = Bukkit.getPluginManager().getPlugin("LevelledMobs");
        levelledMobsIsInstalled = levelledMobsPlugin != null && levelledMobsPlugin.isEnabled();

        if (levelledMobsIsInstalled){
            key = new NamespacedKey(levelledMobsPlugin, "level");
        }
    }

    private final Boolean levelledMobsIsInstalled;
    private NamespacedKey key;

    @Override
    @NotNull
    protected HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {}

    public boolean hasLevelledMobsInstalled(){
        return levelledMobsIsInstalled != null && levelledMobsIsInstalled;
    }

    @Override
    public double getMobLevel(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity) || !hasLevelledMobsInstalled()) return 0.0D;

        Integer mobLevel = entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return Objects.requireNonNullElse(mobLevel, 0);
    }
}
