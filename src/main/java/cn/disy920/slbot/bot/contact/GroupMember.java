package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;

public class GroupMember extends Member {

    protected final String level;  // 群等级
    protected final Role role;  // 群权限

    protected final String title;  // 群头衔

    protected final Group group;

    protected final Stranger stranger;

    public GroupMember(long id, Group group, String nickName, String nameCard, int age, Sex sex, String level, Role role, String title) {
        super(id, nickName, nameCard, age, sex);

        this.group = group;
        this.level = level;
        this.role = role;
        this.title = title;

        this.stranger = new Stranger(this.group, this.nickName, this.id);
    }

    public String getLevel() {
        return this.level;
    }

    public Role getRole() {
        return this.role;
    }

    public String getTitle() {
        return this.title;
    }

    public void setNameCard(String nameCard) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.group.getID());
        params.addProperty("user_id", this.id);
        params.addProperty("card", nameCard);

        JsonObject packet = WSClient.pack("set_group_card", params);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(packet));
    }

    public void kick() {
        kick(false);
    }

    public void kick(boolean blacklist) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.group.getID());
        params.addProperty("user_id", this.id);
        params.addProperty("reject_add_request", blacklist);

        JsonObject packet = WSClient.pack("set_group_kick", params);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(packet));
    }

    public Stranger getAsStranger() {
        return this.stranger;
    }

    @Override
    public void sendMessage(MessageChain messages) {
        this.stranger.sendMessage(messages);
    }

    public enum Role {
        OWNER,  // 群主
        ADMIN,  // 管理员
        MEMBER,  // 普通群员
        UNKNOWN
    }
}
