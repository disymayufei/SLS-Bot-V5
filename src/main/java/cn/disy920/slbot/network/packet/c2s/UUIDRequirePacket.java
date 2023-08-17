package cn.disy920.slbot.network.packet.c2s;

import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class UUIDRequirePacket extends AbstractC2SPacket {

    private final String playerID;

    public UUIDRequirePacket(String playerID) {
        super("getUUID");

        this.playerID = playerID;
    }

    public String getPlayerID() {
        return playerID;
    }

    @Override
    @Nullable
    public UUIDRequirePacket genPacketFromJson(String json) {
        JsonObject object = parseJson(json);

        if (object != null) {
            String playerID = object.get("args").getAsString();
            return new UUIDRequirePacket(playerID);
        }

        return null;
    }
}
