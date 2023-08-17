package cn.disy920.slbot.network.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTwoWayPacket extends AbstractPacket {
    public AbstractTwoWayPacket(String header) {
        super(header);
    }

    @NotNull
    public abstract String genStringPacket();

    @Nullable
    public abstract AbstractPacket genPacketFromJson(String json);
}
