package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;
import cn.disy920.slbot.bot.contact.Stranger;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class MemberJoinRequestEvent extends AbstractEvent implements Event {
    protected final Group group;
    protected final Stranger from;
    protected final GroupMember invitor;

    protected final String flag;  // 事件的唯一标识码，用于处理该请求

    protected final Type type;  // 事件类型


    protected MemberJoinRequestEvent(long timeStamp, long botID, Group group, Stranger from, @Nullable GroupMember invitor, String flag, String type) {
        super(timeStamp, botID);

        this.group = group;
        this.from = from;
        this.invitor = invitor;
        this.flag = flag;
        this.type = Type.valueOf(type.toUpperCase());
    }

    public long getGroupId() {
        return this.group.getID();
    }

    public Group getGroup() {
        return this.group;
    }

    public long getFromId() {
        return this.from.getID();
    }

    public String getFromNick() {
        return this.from.getNick();
    }

    public Stranger getFrom() {
        return this.from;
    }

    @Nullable
    public GroupMember getInvitor() {
        return this.invitor;
    }

    public Long getInvitorId() {
        return this.invitor != null ? this.invitor.getID() : null;
    }

    public Type getType() {
        return this.type;
    }

    public void accept() {
        JsonObject params = new JsonObject();
        params.addProperty("flag", this.flag);
        params.addProperty("sub_type", this.type.toString());
        params.addProperty("approve", true);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(WSClient.pack("set_group_add_request", params)));
    }

    public void reject() {
        reject("");
    }

    public void reject(String reason) {
        JsonObject params = new JsonObject();
        params.addProperty("flag", this.flag);
        params.addProperty("sub_type", this.type.toString());
        params.addProperty("approve", false);
        params.addProperty("reason", reason);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(WSClient.pack("set_group_add_request", params)));
    }

    public enum Type {
        ADD("add"),
        INVITE("invite");

        final String type;
        Type(String type) {
            this.type = type;
        }

        public String toString() {
            return this.type;
        }
    }
}
