package su.nightexpress.quantumrpg.hooks.external.mimic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.level.ExpLevelConverter;
import ru.endlesscode.mimic.util.ExistingWeakReference;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;

import java.util.Objects;

public class ProRpgItemsExpLevelConverter implements ExpLevelConverter {

    private final ExistingWeakReference<Player> playerRef;
    private final ClassManager                  classManager;

    public ProRpgItemsExpLevelConverter(Player player) {
        playerRef = new ExistingWeakReference<>(player);
        classManager = QuantumAPI.getModuleManager().getClassManager();
        Objects.requireNonNull(classManager);
    }

    @Override
    public double getExpToReachLevel(int level) {
        if (level <= 0) return 0.0;
        RPGClass playerClass = getPlayerClass();
        return playerClass != null ? playerClass.getNeedExpForLevel(level) : -1.0;
    }

    @Nullable
    private RPGClass getPlayerClass() {
        UserClassData userData = classManager.getUserData(playerRef.get());
        return userData != null ? userData.getPlayerClass() : null;
    }
}
