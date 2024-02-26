package su.nightexpress.quantumrpg.stats.items.attributes.api;

import mc.promcteam.engine.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.HookClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

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
            if (complex.percent) container.set(PERCENT, BOOLEAN, true);
            for (Predicate<Player> condition : complex.userConditions) {
                if (condition instanceof ClassCondition) {
                    Set<String> classes = new LinkedHashSet<>(((ClassCondition) condition).getClasses());
                    container.set(CLASS_CONDITION, DataUT.STRING_ARRAY, classes.toArray(new String[classes.size()]));
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

            List<Predicate<Player>> conditions = new ArrayList<>();
            if (primitive.has(CLASS_CONDITION, DataUT.STRING_ARRAY)) {
                String[] classCondition = primitive.get(CLASS_CONDITION, DataUT.STRING_ARRAY);
                if (classCondition != null) {
                    conditions.add(new ClassCondition(classCondition));
                }
            }
            return new StatBonus(array, array.length > 1 ? primitive.getOrDefault(PERCENT, BOOLEAN, false) : false, conditions);
        }
    };

    private final double[] value;
    private final boolean  percent;
    private final List<Predicate<Player>> userConditions;

    public StatBonus(double[] value, boolean percent, List<Predicate<Player>> userConditions) {
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
        this.userConditions = List.copyOf(userConditions);
    }

    public double[] getValue() {return value;}

    public boolean isPercent() {return percent;}

    public boolean meetsRequirements(Player player) {
        return userConditions.stream().allMatch(playerPredicate -> playerPredicate.test(player));
    }

    public static final class ClassCondition implements Predicate<Player> {
        private final List<String> classes;

        public ClassCondition(String[] classes) {
            this.classes = List.of(classes);
        }

        public List<String> getClasses() {return this.classes;}

        @Override
        public boolean test(Player player) {
            HookClass classPlugin = EngineCfg.HOOK_PLAYER_CLASS_PLUGIN;
            if (classPlugin == null) return false;
            String playerClass = classPlugin.getClass(player);
            return this.classes.stream().anyMatch(playerClass::equalsIgnoreCase);
        }
    }
}
