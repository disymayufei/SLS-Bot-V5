package cn.disy920.slbot.network.packet.c2s;

import cn.disy920.slbot.network.packet.AbstractPacket;

public abstract class AbstractC2SPacket extends AbstractPacket {
    public AbstractC2SPacket(String header) {
        super(header);
    }

    public abstract AbstractPacket genPacketFromJson(String json);
}
