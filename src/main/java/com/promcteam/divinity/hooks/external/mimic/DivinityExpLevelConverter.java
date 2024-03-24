package com.promcteam.divinity.hooks.external.mimic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.level.ExpLevelConverter;
import ru.endlesscode.mimic.util.ExistingWeakReference;
import com.promcteam.divinity.api.QuantumAPI;
import com.promcteam.divinity.modules.list.classes.ClassManager;
import com.promcteam.divinity.modules.list.classes.api.RPGClass;
import com.promcteam.divinity.modules.list.classes.api.UserClassData;

import java.util.Objects;

public class DivinityExpLevelConverter implements ExpLevelConverter {

    private final ExistingWeakReference<Player> playerRef;
    private final ClassManager                  classManager;

    public DivinityExpLevelConverter(Player player) {
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
