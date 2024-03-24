package com.promcteam.divinity.manager.interactions.api;

import lombok.Data;
import lombok.experimental.Accessors;
import com.promcteam.codex.manager.api.task.ITask;
import com.promcteam.codex.utils.StringUT;
import com.promcteam.codex.utils.random.Rnd;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;

import java.util.function.Function;

public class AnimatedSuccessBar extends ICustomInteraction {

    private final String    title;
    private final String    barChar;
    private final String    barFormat;
    private final ChatColor barColorNeutral;
    private final ChatColor barColorGood;
    private final ChatColor barColorBad;
    private final int       barSize;

    private final long fillInterval;
    private final int  fillAmount;

    private final double                  chance;
    private final int                     minSuccess;
    private final Function<Boolean, Void> result;
    private       int                     succ, unsucc;

    private AnimatedSuccessBar(@NotNull Builder builder) {
        super(builder.plugin);

        this.title = builder.barTitle;
        this.barChar = builder.barChar;
        this.barFormat = builder.barFormat;
        this.barColorNeutral = builder.colorNeutral;
        this.barColorGood = builder.colorSuccess;
        this.barColorBad = builder.colorUnsuccess;
        this.barSize = builder.barSize;
        this.fillInterval = builder.fillInterval;
        this.fillAmount = builder.fillAmount;
        this.chance = builder.chance;
        this.minSuccess = builder.minSuccess;
        this.result = builder.result;

        this.succ = 0;
        this.unsucc = 0;
    }

    @Override
    protected boolean doAction() {
        new Task().start();
        return true;
    }

    private void display() {
        double oneFillSucc = 100D / (double) this.barSize; // 10 succ = 1 fill

        StringBuilder barBuilder = new StringBuilder();
        for (int count = 0; count < this.barSize; count++) {
            if (this.succ >= oneFillSucc * count) {
                barBuilder.append(this.barColorGood);
            } else if (this.unsucc >= (this.barSize - count) * oneFillSucc) {
                barBuilder.append(this.barColorBad);
            } else {
                barBuilder.append(this.barColorNeutral);
            }
            barBuilder.append(this.barChar);
        }

        String bar = this.barFormat
                .replace("%failure%", String.valueOf(unsucc))
                .replace("%success%", String.valueOf(succ))
                .replace("%bar%", barBuilder.toString());

        boolean isFirst = succ + unsucc == 0;
        player.sendTitle(title, bar, isFirst ? 10 : 0, (int) fillInterval + 20, 40);
    }

    @Data
    @Accessors(chain = true)
    public static class Builder {

        private final QuantumRPG plugin;

        private final String    barTitle;
        private final String    barChar;
        private       String    barFormat;
        private       int       barSize;
        private       ChatColor colorNeutral;
        private       ChatColor colorSuccess;
        private       ChatColor colorUnsuccess;

        private long fillInterval;
        private int  fillAmount;

        private double chance;
        private int    minSuccess;

        private Function<Boolean, Void> result;

        public Builder(@NotNull QuantumRPG plugin, @NotNull String title, @NotNull String barChar) {
            this.plugin = plugin;
            this.barTitle = StringUT.color(title);
            this.barChar = StringUT.color(barChar);
            this.setBarFormat("%bar%");
            this.setColorNeutral(ChatColor.DARK_GRAY);
            this.setColorSuccess(ChatColor.GREEN);
            this.setColorUnsuccess(ChatColor.RED);
            this.setBarSize(20);
            this.setFillInterval(1);
            this.setFillAmount(1);
            this.setChance(50);
            this.setMinSuccess(50);
            this.setResult(b -> null);
        }

        @NotNull
        public Builder setBarFormat(@NotNull String format) {
            this.barFormat = StringUT.color(format);
            return this;
        }

        @NotNull
        @Override
        public Builder clone() {
            Builder clone = new Builder(plugin, barTitle, barChar);
            clone.barFormat = barFormat;
            clone.colorNeutral = colorNeutral;
            clone.colorSuccess = colorSuccess;
            clone.colorUnsuccess = colorUnsuccess;
            clone.barSize = barSize;
            clone.fillInterval = fillInterval;
            clone.fillAmount = fillAmount;
            clone.chance = chance;
            clone.minSuccess = minSuccess;
            clone.result = result;
            return clone;
        }

        @NotNull
        public AnimatedSuccessBar build() {
            return new AnimatedSuccessBar(this);
        }
    }

    class Task extends ITask<QuantumRPG> {

        Task() {
            super(AnimatedSuccessBar.this.plugin, AnimatedSuccessBar.this.fillInterval, true);
        }

        @Override
        public void action() {
            if (player == null || player.isDead()) {
                this.stop();
                return;
            }

            display();

            if (succ + unsucc >= 100) {
                plugin.getServer().getScheduler().runTask(plugin, () -> result.apply(succ >= minSuccess));
                endAction();
                this.stop();
                return;
            }

            if (Rnd.get(true) < chance) succ += fillAmount;
            else unsucc += fillAmount;
        }
    }
}
