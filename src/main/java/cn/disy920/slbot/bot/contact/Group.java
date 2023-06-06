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
        Cache<JsonObject> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.id);
        params.addProperty("user_id", id);
        params.addProperty("no_cache", true);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_group_member_info", params), code);

        JsonObject response = cache.getCache();
        
        if (response != null && !response.isJsonNull()) {
            long userID = response.get("user_id").getAsLong();
            int age = new ValueShield<JsonElement, Integer>(response.get("age")).runOrReturn(JsonElement::getAsInt, 0);
            String nameCard = new ValueShield<JsonElement, String>(response.get("card")).runOrReturn(JsonElement::getAsString, "");
            String level = new ValueShield<JsonElement, String>(response.get("level")).runOrReturn(JsonElement::getAsString, "0");
            String nickName = new ValueShield<JsonElement, String>(response.get("nickname")).runOrReturn(JsonElement::getAsString, "");
            String title = new ValueShield<JsonElement, String>(response.get("title")).runOrReturn(JsonElement::getAsString, "");
            GroupMember.Role role = new ValueShield<JsonElement, GroupMember.Role>(response.get("role")).runOrReturn(ele -> GroupMember.Role.valueOf(ele.getAsString().toUpperCase()), GroupMember.Role.UNKNOWN);
            Member.Sex sex = new ValueShield<JsonElement, Member.Sex>(response.get("sex")).runOrReturn(ele -> Member.Sex.valueOf(ele.getAsString().toUpperCase()), Member.Sex.UNKNOWN);

            return new GroupMember(userID, this, nickName, nameCard, age, sex, level, role, title);
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

        JsonArray response = cache.getCache();

        if (response != null) {
            for (JsonElement memberEle : response) {
                if (memberEle instanceof JsonObject memberObj) {

                    long userID = memberObj.get("user_id").getAsLong();
                    int age = new ValueShield<JsonElement, Integer>(memberObj.get("age")).runOrReturn(JsonElement::getAsInt, 0);
                    String nameCard = new ValueShield<JsonElement, String>(memberObj.get("card")).runOrReturn(JsonElement::getAsString, "");
                    String level = new ValueShield<JsonElement, String>(memberObj.get("level")).runOrReturn(JsonElement::getAsString, "0");
                    String nickName = new ValueShield<JsonElement, String>(memberObj.get("nickname")).runOrReturn(JsonElement::getAsString, "");
                    String title = new ValueShield<JsonElement, String>(memberObj.get("title")).runOrReturn(JsonElement::getAsString, "");
                    GroupMember.Role role = new ValueShield<JsonElement, GroupMember.Role>(memberObj.get("role")).runOrReturn(ele -> GroupMember.Role.valueOf(ele.getAsString().toUpperCase()), GroupMember.Role.UNKNOWN);
                    Member.Sex sex = new ValueShield<JsonElement, Member.Sex>(memberObj.get("sex")).runOrReturn(ele -> Member.Sex.valueOf(ele.getAsString().toUpperCase()), Member.Sex.UNKNOWN);


                    result.add(new GroupMember(userID, this, nickName, nameCard, age, sex, level, role, title));
                }
            }
        }

        return result;
    }

    public boolean contains(long id) {
        Cache<JsonObject> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.id);
        params.addProperty("user_id", id);
        params.addProperty("no_cache", true);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_group_member_info", params), code);

        JsonObject response = cache.getCache();

        return response != null && !response.isJsonNull();
    }

    @Override
    public void sendMessage(MessageChain messages) {
        JsonObject message = messages.serializeToJson();

        JsonObject params = new JsonObject();
        params.addProperty("group_id", this.id);
        params.add("message", message);

        JsonObject packet = new JsonObject();
        packet.addProperty("action", "send_group_msg");
        packet.add("params", params);

        WSClient.botConnection.send(GsonFactory.getGsonInstance().toJson(packet));
    }
}
