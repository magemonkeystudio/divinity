package studio.magemonkey.divinity.manager.effects;

import org.jetbrains.annotations.NotNull;

public abstract class IExpirableEffect extends IEffect {

    protected     long   endTime;
    private final double lifeTime;

    protected IExpirableEffect(@NotNull Builder<?> builder) {
        super(builder);
        this.lifeTime = builder.lifeTime;
        this.endTime = System.currentTimeMillis() + (long) (int) (this.getLifeTime() * 1000D);
    }

    @Override
    public void trigger(boolean force) {
        super.trigger(force);
        if (this.isExpired()) {
            if (this.msgExpire != null) {
                this.target.sendMessage(this.msgExpire);
            }
            return;
        }
    }

    public final double getLifeTime() {
        return this.lifeTime;
    }

    public final boolean isPermanent() {
        return this.getLifeTime() < 0D && this.getCharges() < 0;
    }

    @Override
    public final boolean isExpired() {
        if (this.isPermanent()) return false;
        return super.isExpired() || (this.getLifeTime() > 0 && System.currentTimeMillis() >= this.endTime);
    }

    public abstract static class Builder<B extends Builder<B>> extends IEffect.Builder<B> {

        private double lifeTime;

        public Builder(double lifeTime) {
            this.lifeTime = lifeTime;
        }

        @Override
        @NotNull
        public abstract IExpirableEffect build();

        @Override
        @NotNull
        protected abstract B self();
    }
}
