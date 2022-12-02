package su.nightexpress.quantumrpg.hooks.external.mythicmobs;

public abstract class AbstractMythicEntity<T> {
    public abstract String getInternalName();

    public abstract String getFaction();

    public abstract T getMob();
}
