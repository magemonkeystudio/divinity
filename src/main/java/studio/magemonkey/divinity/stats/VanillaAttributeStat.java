package studio.magemonkey.divinity.stats;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.util.Locale;
import java.util.UUID;

/**
 * Item stats that map 1:1 onto a vanilla attribute added in MC 1.20.5+/1.21+. These live outside codex's
 * {@code NBTAttribute} because older server versions don't have them; {@link EntityStats} resolves each one
 * against the running server and skips the ones that don't exist.
 * <p>
 * Each constant shares its name with both its {@link TypedStat.Type} and the vanilla attribute it drives.
 * Stats whose type {@link TypedStat.Type#isPercent() is a percentage} are configured in percent (50 = +0.5);
 * the rest are configured in the attribute's own units (blocks, hearts, etc.).
 */
public enum VanillaAttributeStat {
    SCALE(0x01),
    WATER_MOVEMENT_EFFICIENCY(0x02),
    MOVEMENT_EFFICIENCY(0x03),
    SNEAKING_SPEED(0x04),
    BLOCK_BREAK_SPEED(0x05),
    BLOCK_INTERACTION_RANGE(0x06),
    ENTITY_INTERACTION_RANGE(0x07),
    EXPLOSION_KNOCKBACK_RESISTANCE(0x08),
    FALL_DAMAGE_MULTIPLIER(0x09),
    FLYING_SPEED(0x0a),
    GRAVITY(0x0b),
    JUMP_STRENGTH(0x0c),
    MAX_ABSORPTION(0x0d),
    MINING_EFFICIENCY(0x0e),
    OXYGEN_BONUS(0x0f),
    SAFE_FALL_DISTANCE(0x10),
    STEP_HEIGHT(0x11),
    SUBMERGED_MINING_SPEED(0x12),
    ;

    private final TypedStat.Type statType;
    private final UUID           modifierId;
    private final String         modifierName;

    /**
     * @param modifierSuffix last segment of the modifier UUID. Assigned explicitly rather than from the
     *                       ordinal so the UUID never changes: it's how a previously applied modifier is
     *                       found and replaced on an entity.
     */
    VanillaAttributeStat(int modifierSuffix) {
        this.statType = TypedStat.Type.valueOf(this.name());
        this.modifierId = UUID.fromString(String.format("d141e000-5ca1-4000-0000-%012x", modifierSuffix));
        this.modifierName = "divinity." + this.name().toLowerCase(Locale.ROOT);
    }

    @NotNull
    public TypedStat.Type getStatType() {
        return this.statType;
    }

    /** Name passed to {@code NMS#getAttribute(String)}. */
    @NotNull
    public String getAttributeName() {
        return this.name();
    }

    @NotNull
    public UUID getModifierId() {
        return this.modifierId;
    }

    @NotNull
    public String getModifierName() {
        return this.modifierName;
    }

    /**
     * Converts a configured stat value into the amount added to the vanilla attribute.
     */
    public double toAttributeAmount(double statValue) {
        return this.statType.isPercent() ? statValue / 100D : statValue;
    }
}
