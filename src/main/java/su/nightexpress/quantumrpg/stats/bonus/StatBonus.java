package su.nightexpress.quantumrpg.stats.bonus;

import mc.promcteam.engine.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.api.UserRequirement;
import su.nightexpress.quantumrpg.stats.items.requirements.user.ClassRequirement;

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
    }
}
