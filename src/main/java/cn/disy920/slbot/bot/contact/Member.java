package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.CacheManager;
import cn.disy920.slbot.utils.container.Cache;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class Member extends User {

    protected final Sex sex;
    protected final int age;

    protected final String nameCard;

    public Member(long id, String nickName, String nameCard, int age, Sex sex) {
        super(nickName, id);
        this.nameCard = nameCard;
        this.age = age;
        this.sex = sex;
    }

    @NotNull
    public static String getNameCardOrNick(long id) {
        return getNameCardOrNick(id, false);
    }

    @NotNull
    public static String getNameCardOrNick(long id, boolean useCache) {
        Cache<JsonObject> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("user_id", id);
        params.addProperty("no_cache", !useCache);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_stranger_info", params), code);

        JsonObject response = cache.getCache();

        return response != null && !response.isJsonNull() ? response.get("nickname").getAsString() : "";
    }

    public String getNameCard() {
        return this.nameCard;
    }

    public Sex getSex() {
        return this.sex;
    }

    public int getAge() {
        return this.age;
    }

    public enum Sex {
        MALE,
        FEMALE,
        UNKNOWN;
    }
}
