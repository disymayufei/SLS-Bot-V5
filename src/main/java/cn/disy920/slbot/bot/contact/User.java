package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;

/**
 * 一个普通的用户，可能是机器人或者普通群员
 */
public class User implements Contact {

    protected String nickName;
    protected long id;

    public User(String nickName, long id) {
        this.nickName = nickName;
        this.id = id;
    }

    public String getNick() {
        return this.nickName;
    }

    public long getID() {
        return this.id;
    }

    @Override
    public void sendMessage(MessageChain messages) {
        JsonObject params = new JsonObject();
        params.addProperty("user_id", this.id);
        params.add("message", messages.serializeToJson());

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(WSClient.pack("send_private_msg", params)));
    }
}
