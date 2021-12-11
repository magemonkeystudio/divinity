package su.nightexpress.quantumrpg.hooks.external.mimic;

import mc.promcteam.engine.utils.StringUT;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.classes.BukkitClassSystem;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProRpgItemsClassSystem extends BukkitClassSystem {

    private static final String ID = "prorpgitems";

    private final ClassManager classManager;

    public ProRpgItemsClassSystem(@NotNull Player player) {
        super(player);
        classManager = QuantumAPI.getModuleManager().getClassManager();
    }

    @NotNull
    @Override
    public List<String> getClasses() {
        RPGClass playerClass = getClassData();
        if (playerClass == null) return Collections.emptyList();

        List<RPGClass> classes = new ArrayList<>();
        classes.add(playerClass);
        classes.addAll(playerClass.getParents());

        return classes.stream()
                .map(RPGClass::getName)
                .map(StringUT::colorOff)
                .collect(Collectors.toList());
    }

    @Nullable
    private RPGClass getClassData() {
        return Objects.requireNonNull(classManager.getUserData(getPlayer()))
                .getPlayerClass();
    }

    public static class Provider extends BukkitClassSystem.Provider {

        public Provider() {
            super(ID);
        }

        @NotNull
        @Override
        public BukkitClassSystem getSystem(@NotNull Player player) {
            return new ProRpgItemsClassSystem(player);
        }
    }
}
