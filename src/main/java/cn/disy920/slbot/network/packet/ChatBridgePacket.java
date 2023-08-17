package cn.disy920.slbot.network.packet;

import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChatBridgePacket extends AbstractTwoWayPacket {
    private String identity;
    private String sender;
    private String text;

    public ChatBridgePacket(String identity, String sender, String text) {
        super("chatBridge");
        this.identity = identity;
        this.sender = sender;
        this.text = text;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    @NotNull
    public String genStringPacket() {
        Map<String, Object> chatPacket = new HashMap<>();
        chatPacket.put("type", "chatBridge");

        Map<String, Object> chatTextPacket = new HashMap<>();
        chatTextPacket.put("identity", text);
        chatTextPacket.put("sender", sender);
        chatTextPacket.put("text", text);

        chatPacket.put("args", chatTextPacket);

        chatPacket.put("reqGroup", new long[1]);

        return GsonFactory.getGsonInstance().toJson(chatPacket);
    }

    @Override
    @Nullable
    public ChatBridgePacket genPacketFromJson(String json) {
        JsonObject object = parseJson(json);

        if (object != null) {
            JsonObject chat = object.getAsJsonObject("args");
            String identity = chat.get("identity").getAsString();
            String sender = chat.get("sender").getAsString();
            String text = chat.get("text").getAsString();

            return new ChatBridgePacket(identity, sender, text);
        }

        return null;

    }
}
