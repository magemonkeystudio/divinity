package studio.magemonkey.codex.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TEST extends NMS {
    @Getter
    private final NMS testNms = mock(NMS.class);

    @Override
    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        String json = testNms.toJSON(item);
        return json == null ? "" : json;
    }

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player p) {
        Channel channel = testNms.getChannel(p);
        if (channel == null) {
            channel = mock(Channel.class);
            when(channel.pipeline())
                    .thenReturn(mock(ChannelPipeline.class));
        }
        return channel;
    }

    @Override
    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        return testNms.toBase64(item);
    }

    @Override
    public ItemStack fromBase64(@NotNull String data) {
        return testNms.fromBase64(data);
    }

    @Override
    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        return testNms.getNbtString(item);
    }

    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        String result = testNms.fixColors(str);
        return result == null ? str : result;
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return testNms.getDefaultSpeed(itemStack);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return testNms.getDefaultArmor(itemStack);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return testNms.getDefaultToughness(itemStack);
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        return testNms.isWeapon(itemStack);
    }
}
