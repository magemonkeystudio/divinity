package studio.magemonkey.divinity.manager.damage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * The typed amount of a {@code DIVINITY_<type>} hit that {@link DamageManager#dealDamage} deals while a dealer's
 * Codex request is already open on the target, as a Fabled skill with a {@code DIVINITY_<type>} classifier does.
 * <p>
 * Opening a request of Divinity's own there would hide the dealer's, and with it the weapon-stats mode the
 * dealer chose. So instead the damage listener claims the dealer's request as usual and takes the typed amount
 * from here. A deal is claimed by the same rule as a Codex request: the first damage event of the target that
 * asks takes the newest deal not yet taken, and asking again with that event returns the same deal.
 * <p>
 * Not thread safe. Server thread only, like Codex's {@code DamageContexts}.
 */
public final class TypedDeals {
    private static final Map<UUID, Deque<Deal>> OPEN = new HashMap<>();

    private TypedDeals() {}

    /**
     * Announces a typed hit on the target. Close the returned deal when the call to {@code damage()} returns.
     *
     * @param type   the damage type exactly as the dealer named it, such as {@code DIVINITY_fire}
     * @param amount the amount, 0 or more
     */
    @NotNull
    static Deal open(@NotNull Entity target, @NotNull String type, double amount) {
        Deal deal = new Deal(target.getUniqueId(), type, amount);
        OPEN.computeIfAbsent(deal.target, id -> new ArrayDeque<>()).push(deal);
        return deal;
    }

    /**
     * @return the typed hit this event is, or null if it is not one
     */
    @Nullable
    public static Deal claim(@NotNull EntityDamageEvent event) {
        Deque<Deal> deals = OPEN.get(event.getEntity().getUniqueId());
        if (deals == null) return null;
        for (Deal deal : deals) {
            if (deal.claimedBy == event) return deal;
        }
        for (Deal deal : deals) {
            if (deal.claimedBy == null) {
                deal.claimedBy = event;
                return deal;
            }
        }
        return null;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Deal implements AutoCloseable {
        private final UUID              target;
        /** The damage type exactly as the dealer named it. */
        @Getter
        @NotNull
        private final String            type;
        @Getter
        private final double            amount;
        private       EntityDamageEvent claimedBy;
        private       boolean           closed;

        public boolean isClaimed() {
            return claimedBy != null;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            Deque<Deal> deals = OPEN.get(target);
            if (deals == null) return;
            for (Iterator<Deal> it = deals.iterator(); it.hasNext(); ) {
                if (it.next() == this) {
                    it.remove();
                    break;
                }
            }
            if (deals.isEmpty()) OPEN.remove(target);
        }
    }
}
