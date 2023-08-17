package cn.disy920.slbot.network.packet.s2c;

import cn.disy920.slbot.network.packet.AbstractPacket;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractS2CPacket extends AbstractPacket {
    public AbstractS2CPacket(String header) {
        super(header);
    }

    @NotNull
    public abstract String genStringPacket();
}
