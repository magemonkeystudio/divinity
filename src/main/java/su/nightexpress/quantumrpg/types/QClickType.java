package su.nightexpress.quantumrpg.types;

import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum QClickType {
    RIGHT, LEFT, SHIFT_RIGHT, SHIFT_LEFT;

    @Nullable
    public static QClickType getFromAction(@NotNull Action action, boolean shift) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
            return shift ? SHIFT_RIGHT : RIGHT;
        return shift ? SHIFT_LEFT : LEFT;
    }
}
