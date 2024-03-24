package com.promcteam.divinity.manager.effects;

import org.jetbrains.annotations.NotNull;

public abstract class IPeriodicEffect extends IExpirableEffect {

    private long   lastTrigger;
    private double interval;

    protected IPeriodicEffect(@NotNull Builder<?> builder) {
        super(builder);
        this.interval = builder.interval;
    }

    @Override
    public void trigger(boolean force) {
        super.trigger(force);
        this.tick();
    }

    public final double getInterval() {
        return this.interval;
    }

    public final long getLastTriggerTime() {
        return this.lastTrigger;
    }

    public final boolean isReady() {
        return System.currentTimeMillis() > (this.getLastTriggerTime() + (this.getInterval() * 1000D));
    }

    private final void tick() {
        this.lastTrigger = System.currentTimeMillis();
    }

    public abstract static class Builder<B extends Builder<B>> extends IExpirableEffect.Builder<B> {

        private double interval;

        public Builder(double lifeTime, double interval) {
            super(lifeTime);
            this.interval = interval;
        }

        @Override
        @NotNull
        public abstract IPeriodicEffect build();

        @Override
        @NotNull
        protected abstract B self();
    }
}
