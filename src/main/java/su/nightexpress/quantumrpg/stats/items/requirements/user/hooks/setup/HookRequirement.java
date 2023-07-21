package su.nightexpress.quantumrpg.stats.items.requirements.user.hooks.setup;

public class HookRequirement<K, V> {

    public enum HookRequirementType {
        MCMMO,
        JOBS_REBORN,

    }

    private final HookRequirementType hooK;
    private final K key;
    private final V value;

    public HookRequirement(HookRequirementType hook, K key, V value) {
        this.hooK = hook;
        this.key = key;
        this.value = value;
    }

    public HookRequirementType getHooK() {
        return hooK;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
