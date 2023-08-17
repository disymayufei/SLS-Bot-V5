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

    public static GroupMember getEmptyGroupMember(long id, Group group) {
        return new GroupMember(id, group, Member.getNameCardOrNick(id), "", 0, Sex.UNKNOWN, "0", Role.MEMBER, "");
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


    /**
     * 踢出该群员，并不将其纳入黑名单
     */
    public void kick() {
        kick(false);
    }


    /**
     * 踢出该群员
     * @param blacklist 是否纳入黑名单
     */
    public void kick(boolean blacklist) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.group.getID());
        params.addProperty("user_id", this.id);
        params.addProperty("reject_add_request", blacklist);

        JsonObject packet = WSClient.pack("set_group_kick", params);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(packet));
    }


    /**
     * 以临时会话人的身份获取该群员
     * @return 该群员对应的临时会话人
     */
    public Stranger getAsStranger() {
        return this.stranger;
    }


    /**
     * 对该群员发送一条私聊消息
     * @param messages 待发送的消息链
     */
    @Override
    public void sendMessage(MessageChain messages) {
        this.stranger.sendMessage(messages);
    }


    /**
     * 封禁该群员
     * @param duration 封禁时长，单位秒，设置为小于等于0的数值则为解除禁言
     */
    public void mute(long duration) {
        if (duration < 0) {
            duration = 0;
        }
        else if (duration > 4294967295L) {
            duration = 4294967295L;
        }

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.group.getID());
        params.addProperty("user_id", this.id);
        params.addProperty("duration", duration);

        JsonObject packet = WSClient.pack("set_group_ban", params);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(packet));
    }

    /**
     * 用于表示该群员权限
     */
    public enum Role {
        OWNER,  // 群主
        ADMIN,  // 管理员
        MEMBER,  // 普通群员
        UNKNOWN  // 未知权限
    }
}
