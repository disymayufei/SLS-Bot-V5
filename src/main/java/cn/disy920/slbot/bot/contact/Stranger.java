package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;

public class Stranger extends User {

    protected final Group fromGroup;

    public Stranger(Group fromGroup, String nickName, long id) {
        super(nickName, id);

        this.fromGroup = fromGroup;
    }

    @Override
    public void sendMessage(MessageChain messages) {
        JsonObject params = new JsonObject();
        params.addProperty("user_id", this.id);
        params.addProperty("group_id", this.fromGroup.getID());
        params.add("message", messages.serializeToJson());

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(WSClient.pack("send_private_msg", params)));
    }
}
