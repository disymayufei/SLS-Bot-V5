package cn.disy920.slbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    public static Gson getGsonInstance() {
        return GSON;
    }
}
