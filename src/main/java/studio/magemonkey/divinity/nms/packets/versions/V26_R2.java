package studio.magemonkey.divinity.nms.packets.versions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.events.EnginePlayerPacketEvent;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.divinity.Divinity;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * 26.3 packet handling.
 * <p>
 * 26.3 turned ClientboundLevelParticlesPacket into a record: its accessors lost the
 * "get" prefix, and its final fields can no longer be written reflectively, so a
 * modified copy of the packet is sent instead.
 */
public class V26_R2 extends V26_R1 {
    public V26_R2(@NotNull Divinity plugin) {super(plugin);}

    @Override
    @Nullable
    protected Object getParticle(@NotNull Object packet) {
        return Reflex.invokeMethod(
                Reflex.getMethod(packet.getClass(), "particle"),
                packet
        );
    }

    @Override
    @Nullable
    protected Integer getParticleCount(@NotNull Object packet) {
        return (Integer) Reflex.invokeMethod(
                Reflex.getMethod(packet.getClass(), FIELD_COUNT),
                packet
        );
    }

    @Override
    protected void setParticleCount(@NotNull EnginePlayerPacketEvent event, @NotNull Object packet, int count) {
        Object copy = copyRecord(packet, FIELD_COUNT, count);
        if (copy != null) {
            event.setPacket(copy);
        }
    }

    /**
     * Creates a copy of the given record with a single component replaced.
     *
     * @return the copy, or null if the object is not a record or the copy failed
     */
    @Nullable
    private Object copyRecord(@NotNull Object record, @NotNull String component, @Nullable Object value) {
        Class<?>          clazz      = record.getClass();
        RecordComponent[] components = clazz.getRecordComponents();
        if (components == null) return null;

        try {
            Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            Object[]   args  = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                args[i] = components[i].getName().equals(component)
                        ? value
                        : components[i].getAccessor().invoke(record);
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            plugin.error("Could not copy packet " + clazz.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
}
