package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.CacheManager;
import cn.disy920.slbot.utils.GsonFactory;
import cn.disy920.slbot.utils.container.Cache;
import cn.disy920.slbot.utils.container.ValueShield;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Group implements Contact {
    private final long id;

    public Group(long id) {
        this.id = id;
    }

    public long getID() {
        return this.id;
    }
    
    public GroupMember get(long id) {
        Cache<JsonObject> cache = requireMemberInfo(id, this.id);

        JsonObject response = cache.getCache(1200);
        
        if (response != null && !response.isJsonNull()) {
            return jsonObjectToMember(response, this);
        }
        else {
            return null;
        }
    }

    public List<GroupMember> getMembers() {
        List<GroupMember> result = new ArrayList<>(2000);

        Cache<JsonArray> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.id);
        params.addProperty("no_cache", true);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_group_member_list", params), code);

        JsonArray response = cache.getCache(2000);

        if (response != null) {
            for (JsonElement memberEle : response) {
                if (memberEle instanceof JsonObject memberObj) {
                    result.add(jsonObjectToMember(memberObj, this));
                }
            }
        }

        return result;
    }

    public boolean contains(long id) {
        Cache<JsonObject> cache = requireMemberInfo(id, this.id);
        JsonObject response = cache.getCache(600);

        return response != null && !response.isJsonNull();
    }

    @Override
    public void sendMessage(MessageChain messages) {
        JsonArray message = messages.serializeToJson();

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.id);
        params.add("message", message);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(WSClient.pack("send_group_msg", params)));
    }

    public static Cache<JsonObject> requireMemberInfo(long userID, long groupID) {
        Cache<JsonObject> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupID);
        params.addProperty("user_id", userID);
        params.addProperty("no_cache", true);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_group_member_info", params), code);

        return cache;
    }

    public static GroupMember jsonObjectToMember(JsonObject jsonObj, Group group) {
        long userID = jsonObj.get("user_id").getAsLong();
        int age = new ValueShield<JsonElement, Integer>(jsonObj.get("age")).runOrReturn(JsonElement::getAsInt, 0);
        String nameCard = new ValueShield<JsonElement, String>(jsonObj.get("card")).runOrReturn(JsonElement::getAsString, "");
        String level = new ValueShield<JsonElement, String>(jsonObj.get("level")).runOrReturn(JsonElement::getAsString, "0");
        String nickName = new ValueShield<JsonElement, String>(jsonObj.get("nickname")).runOrReturn(JsonElement::getAsString, "");
        String title = new ValueShield<JsonElement, String>(jsonObj.get("title")).runOrReturn(JsonElement::getAsString, "");
        GroupMember.Role role = new ValueShield<JsonElement, GroupMember.Role>(jsonObj.get("role")).runOrReturn(ele -> GroupMember.Role.valueOf(ele.getAsString().toUpperCase()), GroupMember.Role.UNKNOWN);
        Member.Sex sex = new ValueShield<JsonElement, Member.Sex>(jsonObj.get("sex")).runOrReturn(ele -> Member.Sex.valueOf(ele.getAsString().toUpperCase()), Member.Sex.UNKNOWN);

        return new GroupMember(userID, group, nickName, nameCard, age, sex, level, role, title);
    }
}
