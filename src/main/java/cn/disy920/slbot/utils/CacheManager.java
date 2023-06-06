package cn.disy920.slbot.utils;

import cn.disy920.slbot.utils.container.Cache;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    public static final CacheManager instance = new CacheManager();

    private long timeStamp = System.currentTimeMillis();
    private long extraCode = 0;

    private final Map<String, Cache<?>> cacheMap = new HashMap<>(512);


    public String addCache(Cache<?> cache) {
        JsonObject codePack = new JsonObject();

        long timeStampNow = System.currentTimeMillis();
        if (timeStampNow == this.timeStamp) {
            extraCode++;
        }
        else {
            this.timeStamp = timeStampNow;
            extraCode = 0;
        }

        codePack.addProperty("time", timeStampNow);
        codePack.addProperty("extra_code", extraCode);
        codePack.addProperty("type", "cache");

        String code = GsonFactory.getGsonInstance().toJson(codePack);

        cacheMap.put(code, cache);

        return code;
    }

    @Nullable
    public Cache<?> takeCache(String code) {
        if (cacheMap.containsKey(code)) {
            Cache<?> cache = cacheMap.get(code);
            cacheMap.remove(code);

            return cache;
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Cache<T> takeCache(String code, Class<T> tClass) {
        return (Cache<T>) takeCache(code);
    }

    public static CacheManager getInstance() {
        return instance;
    }

}
