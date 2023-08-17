package cn.disy920.slbot.network.packet;

import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPacket {
    public final String header;

    public AbstractPacket(String header) {
        this.header = header;
    }

    public String getHeader() {
        return this.header;
    }

    @Nullable
    protected JsonObject parseJson(String json) {
        JsonObject parseObj;

        try {
            parseObj = GsonFactory.getGsonInstance().fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException e) {
            return null;
        }

        return parseObj;
    }
}
