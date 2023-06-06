package cn.disy920.slbot.bot;

import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.event.manager.EventManager;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.utils.CacheManager;
import cn.disy920.slbot.utils.container.Cache;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class Bot {
    protected static EventManager eventManager = new EventManager();

    public static EventManager getEventManager() {
        return eventManager;
    }

    @Nullable
    public static Group getGroup(long id) {
        int counter = 0;

        while ((WSClient.botConnection == null || !WSClient.botConnection.isOpen()) && counter < 50) {
            try {
                Thread.sleep(100);
                counter++;
            } catch (InterruptedException e) {
                return null;
            }
        }

        if (WSClient.botConnection == null || !WSClient.botConnection.isOpen()) {
            return null;
        }

        Cache<JsonObject> cache = new Cache<>();
        String code = CacheManager.getInstance().addCache(cache);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", id);
        params.addProperty("no_cache", true);

        WSClient.botConnection.sendWithEcho(WSClient.pack("get_group_info", params), code);

        JsonObject response = cache.getCache();

        if (response != null && response.has("group_create_time") && !response.get("group_create_time").isJsonNull()) {
            return response.get("group_create_time").getAsLong() != 0 ? new Group(id) : null;
        }

        return null;
    }
}
