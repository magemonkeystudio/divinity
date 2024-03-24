package com.promcteam.divinity.stats.bonus;

import com.promcteam.codex.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.stats.items.requirements.ItemRequirements;
import com.promcteam.divinity.stats.items.requirements.api.DynamicUserRequirement;
import com.promcteam.divinity.stats.items.requirements.api.UserRequirement;
import com.promcteam.divinity.stats.items.requirements.user.ClassRequirement;

import java.util.Arrays;
import java.util.Objects;

public class StatBonus {
    private static final NamespacedKey VALUE           = new NamespacedKey(QuantumRPG.getInstance(), "value");
    private static final NamespacedKey PERCENT         = new NamespacedKey(QuantumRPG.getInstance(), "percent");
    private static final NamespacedKey CLASS_CONDITION = new NamespacedKey(QuantumRPG.getInstance(), "class");

    public static PersistentDataType<PersistentDataContainer,StatBonus> DATA_TYPE = new PersistentDataType<>() {
        @NotNull
        @Override
        public Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @NotNull
        @Override
        public Class<StatBonus> getComplexType() {
            return StatBonus.class;
        }

        @NotNull
        @Override
        public PersistentDataContainer toPrimitive(@NotNull StatBonus complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = DataUT.itemPersistentDataContainer();
            if (complex.value.length == 1) {
                container.set(VALUE, PersistentDataType.DOUBLE, complex.value[0]);
            } else if (complex.value.length == 2) {
                container.set(VALUE, DataUT.DOUBLE_ARRAY, complex.value);
            }
            if (complex.percent) container.set(PERCENT, DataUT.BOOLEAN, true);
            if (complex.condition != null) {
                if (complex.condition.requirement instanceof ClassRequirement) {
                    container.set(complex.condition.requirement.getKey(), DataUT.STRING_ARRAY, (String[]) complex.condition.value);
                }
            }
            return container;
        }

        @NotNull
        @Override
        public StatBonus fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            double[] array = null;
            if (primitive.has(VALUE, DataUT.DOUBLE_ARRAY)) {
                array = primitive.get(VALUE, DataUT.DOUBLE_ARRAY);
            }
            if (array == null && primitive.has(VALUE, PersistentDataType.DOUBLE)) {
                Double simple = primitive.get(VALUE, PersistentDataType.DOUBLE);
                array = simple == null ? new double[]{0} : new double[]{simple};
            }
            if (array == null) array = new double[] {0, 0};

            Condition<?> condition = null;
            for (UserRequirement<?> requirement : ItemRequirements.getUserRequirements()) {
                if (!(requirement instanceof DynamicUserRequirement)) continue;
                if (primitive.has(requirement.getKey())) {
                }
            }


            if (primitive.has(CLASS_CONDITION, DataUT.STRING_ARRAY)) {
                String[] classCondition = primitive.get(CLASS_CONDITION, DataUT.STRING_ARRAY);
                if (classCondition != null) {
                    condition = new Condition<>(ItemRequirements.getUserRequirement(ClassRequirement.class), classCondition);
                }
            }
            return new StatBonus(array, array.length == 1 ? primitive.getOrDefault(PERCENT, DataUT.BOOLEAN, false) : false, condition);
        }
    };

    private final double[] value;
    private final boolean      percent;
    @Nullable
    private final Condition<?> condition;

    public StatBonus(double[] value, boolean percent, @Nullable Condition<?> condition) {
        if (value.length == 2) {
            if (value[0] == value[1]) {
                this.value = new double[]{value[0]};
            } else {
                this.value = new double[]{Math.min(value[0], value[1]), Math.max(value[0], value[1])};
            }
        } else if (value.length == 1) {
            this.value = new double[]{value[0]};
        } else throw new IllegalArgumentException();
        this.percent = percent;
        this.condition = condition;
    }

    public double[] getValue() {return value;}

    public boolean isPercent() {return percent;}

    @Nullable
    public Condition<?> getCondition() {
        return this.condition;
    }

    public boolean meetsRequirement(@Nullable Player player) {
        return this.condition == null || (player != null && this.condition.meetsRequirement(player));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatBonus statBonus = (StatBonus) o;
        return percent == statBonus.percent && Arrays.equals(value, statBonus.value) && Objects.equals(condition, statBonus.condition);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(percent, condition);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    public static final class Condition<Z> {
        private final DynamicUserRequirement<Z> requirement;
        private final Z                         value;

        public Condition(DynamicUserRequirement<Z> requirement, Z value) {
            this.requirement = requirement;
            this.value = value;
        }

        public boolean meetsRequirement(@NotNull Player p) {
            return this.requirement.canUse(p, this.value);
        }

        @NotNull
        public String getFormat(@Nullable Player p, @NotNull ItemStack item) {
            return this.requirement.getFormat(p, item, this.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Condition<?> condition = (Condition<?>) o;
            if (this.requirement != condition.requirement) return false;
            if (this.value instanceof long[] && condition.value instanceof long[]) {
                return Arrays.equals((long[]) this.value, (long[]) condition.value);
            } else if (this.value instanceof int[] && condition.value instanceof int[]) {
                return Arrays.equals((int[]) this.value, (int[]) condition.value);
            } else if (this.value instanceof short[] && condition.value instanceof short[]) {
                return Arrays.equals((short[]) this.value, (short[]) condition.value);
            } else if (this.value instanceof char[] && condition.value instanceof char[]) {
                return Arrays.equals((char[]) this.value, (char[]) condition.value);
            } else if (this.value instanceof byte[] && condition.value instanceof byte[]) {
                return Arrays.equals((byte[]) this.value, (byte[]) condition.value);
            } else if (this.value instanceof boolean[] && condition.value instanceof boolean[]) {
                return Arrays.equals((boolean[]) this.value, (boolean[]) condition.value);
            } else if (this.value instanceof double[] && condition.value instanceof double[]) {
                return Arrays.equals((double[]) this.value, (double[]) condition.value);
            } else if (this.value instanceof float[] && condition.value instanceof float[]) {
                return Arrays.equals((float[]) this.value, (float[]) condition.value);
            } else if (this.value instanceof Object[] && condition.value instanceof Object[]) {
                return Arrays.equals((Object[]) this.value, (Object[]) condition.value);
            } else {
                return Objects.equals(value, condition.value);
            }
        }

        @Override
        public int hashCode() {
            int result = 31;
            if (this.value instanceof long[]) {
                result += Arrays.hashCode((long[]) this.value);
            } else if (this.value instanceof int[]) {
                result += Arrays.hashCode((int[]) this.value);
            } else if (this.value instanceof short[]) {
                result += Arrays.hashCode((short[]) this.value);
            } else if (this.value instanceof char[]) {
                result += Arrays.hashCode((char[]) this.value);
            } else if (this.value instanceof byte[]) {
                result += Arrays.hashCode((byte[]) this.value);
            } else if (this.value instanceof boolean[]) {
                result += Arrays.hashCode((boolean[]) this.value);
            } else if (this.value instanceof double[]) {
                result += Arrays.hashCode((double[]) this.value);
            } else if (this.value instanceof float[]) {
                result += Arrays.hashCode((float[]) this.value);
            } else if (this.value instanceof Object[]) {
                result += Arrays.hashCode((Object[]) this.value);
            } else {
                result += this.value.hashCode();
            }
            return 31 * result + requirement.hashCode();
        }
    }
}
