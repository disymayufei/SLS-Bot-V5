package cn.disy920.slbot.bukkit.bot;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.message.Image;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.util.Calendar;

import static cn.disy920.slbot.Main.richImageGenerator;

public class MessageSender {

    public static void sendQueryServerMessage(JsonArray onlinePlayers, JsonArray groupArray, boolean tryImage) {
        if (tryImage) {
            if (richImageGenerator != null) {
                StringBuilder infoBuilder = new StringBuilder("外服玩家列表：");

                for (int i = 0; i < onlinePlayers.size(); i++) {
                    infoBuilder.append("\n    - ");
                    infoBuilder.append(onlinePlayers.get(i).getAsString());
                }

                int imageHeight = 900 + 40 * (onlinePlayers.size() + 1);
                InputStream queryServerImage = richImageGenerator.genQueryServerImage(onlinePlayers.size(), Calendar.getInstance().get(Calendar.YEAR), imageHeight, infoBuilder.toString(), 1);

                if (queryServerImage != null) {
                    sendImageToAllGroups(groupArray, queryServerImage);
                }
                else {
                    announceToAllGroups(groupArray, "图片生成失败，尝试发送文字版查服信息...");

                    sendPlainQueryServerMessage(onlinePlayers, groupArray);
                }
            }
            else {
                announceToAllGroups(groupArray, "图片生成失败，尝试发送文字版查服信息...");

                sendPlainQueryServerMessage(onlinePlayers, groupArray);
            }
        }
        else {
            sendPlainQueryServerMessage(onlinePlayers, groupArray);
        }
    }


    public static void sendQueryServerMessage(String text, int currentPlayerNum, JsonArray groupArray, boolean tryImage, int theme) {
        if (tryImage) {
            if (richImageGenerator != null) {

                int imageHeight = 900 + 40 * (text.split("\n").length);
                InputStream queryServerImage = richImageGenerator.genQueryServerImage(currentPlayerNum, Calendar.getInstance().get(Calendar.YEAR), imageHeight, text, theme);

                if (queryServerImage != null) {
                    sendImageToAllGroups(groupArray, queryServerImage);
                }
                else {
                    announceToAllGroups(groupArray, "图片生成失败，尝试发送文字版查服信息...");

                    sendPlainQueryServerMessage(text, groupArray);
                }
            }
            else {
                announceToAllGroups(groupArray, "图片生成失败，尝试发送文字版查服信息...");

                sendPlainQueryServerMessage(text, groupArray);
            }
        }
        else {
            sendPlainQueryServerMessage(text, groupArray);
        }
    }


    public static void sendPlainQueryServerMessage(JsonArray onlinePlayers, JsonArray groupArray) {
        StringBuilder resultBuilder = new StringBuilder("说到外服呀，有");

        resultBuilder.append(onlinePlayers.size());
        resultBuilder.append(" / ");
        resultBuilder.append(Calendar.getInstance().get(Calendar.YEAR));
        resultBuilder.append("人在线哦：");

        for(int i = 0; i < onlinePlayers.size(); i++) {
            resultBuilder.append("\n - ");
            resultBuilder.append(onlinePlayers.get(i).getAsString());
        }

        announceToAllGroups(groupArray, resultBuilder.toString());
    }


    public static void sendPlainQueryServerMessage(String text, JsonArray groupArray) {
        announceToAllGroups(groupArray, text);
    }


    /**
     * 将某条消息群发至群聊（会自动忽略机器人未加入的群聊）
     * @param groupArray 包含全部群聊的JSON数组
     * @param msg 待发送的文字消息
     */
    public static void announceToAllGroups(JsonArray groupArray, String msg) {
        if(groupArray != null && groupArray.size() > 0) {
            for (JsonElement groupEle : groupArray) {
                long groupNum = groupEle.getAsLong();
                Group group = Bot.getGroup(groupNum);
                if(group != null) {
                    group.sendMessage(msg);
                }
            }
        }
    }


    /**
     * 将某个图片群发至群聊（会自动忽略机器人未加入的群聊）
     * @param groupArray 包含全部群聊的JSON数组
     * @param imageStream 待发送的图片二级制数据输入流
     */
    public static void sendImageToAllGroups(JsonArray groupArray, InputStream imageStream) {
        if(groupArray != null && groupArray.size() > 0) {
            for (JsonElement groupEle : groupArray) {
                long groupNum = groupEle.getAsLong();
                Group group = Bot.getGroup(groupNum);
                if(group != null) {
                    group.sendMessage(new Image(imageStream));
                }
            }
        }
    }
}
