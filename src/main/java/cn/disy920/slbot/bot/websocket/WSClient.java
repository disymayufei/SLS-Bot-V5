package cn.disy920.slbot.bot.websocket;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.bot.OneBot;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;
import cn.disy920.slbot.bot.contact.Member;
import cn.disy920.slbot.bot.contact.Stranger;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.MemberJoinEvent;
import cn.disy920.slbot.bot.event.MemberJoinRequestEvent;
import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.utils.CacheManager;
import cn.disy920.slbot.utils.GsonFactory;
import cn.disy920.slbot.utils.container.Cache;
import cn.disy920.slbot.utils.container.ValueShield;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetSocketAddress;
import java.net.URI;

import static cn.disy920.slbot.Main.LOGGER;
import static cn.disy920.slbot.Main.RECORDER;

public class WSClient extends WebSocketClient {
    public static WSClient botConnection = null;

    private boolean isReconnecting = false;
    private boolean isActiveClose = false;

    private final Thread currentWebsocketThread = Thread.currentThread();
    private Thread reconnectionThread = null;
    private Thread heartbeatThread = null;

    private final Gson GSON = GsonFactory.getGsonInstance();

    public WSClient(URI serverUri) {
        super(serverUri, new Draft_6455(), null, 2000);

        this.setReuseAddr(true);

        botConnection = this;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        isActiveClose = false;

        launchHeartbeatThread();

        InetSocketAddress socketAddress = this.getRemoteSocketAddress();
        System.out.println(String.format("成功连接至：ws://%s:%s", socketAddress.getHostString(), socketAddress.getPort()));
    }

    @Override
    public void onMessage(String message) {
        JsonObject packet = GSON.fromJson(message, JsonObject.class);

        if (!packet.has("post_type")) {
            analyzeResponsePacket(packet);
        }
        else {
            analyzeEventPacket(packet);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if(!isActiveClose) {
            RECORDER.error("检测到意外的连接断开，请检查Bot端是否正常运行!");
            autoReconnect();
        }
    }


    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        autoReconnect();
    }

    public static JsonObject pack(String header, JsonObject params) {
        JsonObject packet = new JsonObject();
        packet.addProperty("action", header);
        packet.add("params", params);

        return packet;
    }

    public void sendWithEcho(JsonObject packet, String echo) {
        packet.addProperty("echo", echo);
        send(GSON.toJson(packet));
    }

    private void analyzeResponsePacket(JsonObject packet) {
        String echo = packet.has("echo") ? packet.get("echo").getAsString() : null;
        String status = packet.get("status").getAsString();

        if ("ok".equals(status)) {
            JsonElement data = packet.get("data");
            if (data != null) {
                if (echo != null) {
                    try {
                        JsonObject echoPacket = GSON.fromJson(echo, JsonObject.class);
                        String type = echoPacket.get("type").getAsString();

                        switch (type) {
                            case "cache" -> {
                                Cache<JsonElement> cache = CacheManager.getInstance().takeCache(echo, JsonElement.class);
                                if (cache != null) {
                                    cache.putCache(data);
                                }
                            }
                        }
                    } catch (JsonParseException ignored) {}
                }
            }
        }
    }

    private void analyzeEventPacket(JsonObject packet) {
        String header = packet.get("post_type").getAsString();

        switch (header) {
            case "message" -> {
                String type = packet.get("message_type").getAsString();

                if (type.equals("group")) {
                    JsonObject senderInfo = packet.get("sender").getAsJsonObject();

                    Group group = new Group(packet.get("group_id").getAsLong());
                    int messageID = packet.get("message_id").getAsInt();

                    long id = senderInfo.get("user_id").getAsLong();
                    int age = new ValueShield<JsonElement, Integer>(senderInfo.get("age")).runOrReturn(JsonElement::getAsInt, 0);
                    String nameCard = new ValueShield<JsonElement, String>(senderInfo.get("card")).runOrReturn(JsonElement::getAsString, "");
                    String level = new ValueShield<JsonElement, String>(senderInfo.get("level")).runOrReturn(JsonElement::getAsString, "0");
                    String nickName = new ValueShield<JsonElement, String>(senderInfo.get("nickname")).runOrReturn(JsonElement::getAsString, "");
                    String title = new ValueShield<JsonElement, String>(senderInfo.get("title")).runOrReturn(JsonElement::getAsString, "");
                    GroupMember.Role role = new ValueShield<JsonElement, GroupMember.Role>(senderInfo.get("role")).runOrReturn(ele -> GroupMember.Role.valueOf(ele.getAsString().toUpperCase()), GroupMember.Role.UNKNOWN);
                    Member.Sex sex = new ValueShield<JsonElement, Member.Sex>(senderInfo.get("sex")).runOrReturn(ele -> Member.Sex.valueOf(ele.getAsString().toUpperCase()), Member.Sex.UNKNOWN);

                    GroupMember sender = new GroupMember(id, group, nickName, nameCard, age, sex, level, role, title);
                    MessageChain messageChain = MessageChain.deserializeFromJson(packet.get("message").getAsJsonArray(), messageID);

                    long timeStamp = packet.get("time").getAsLong();
                    long botID = packet.get("self_id").getAsLong();

                    GroupMessageEvent groupMessageEvent = new GroupMessageEvent(timeStamp, botID, sender, messageChain, group);
                    Bot.getEventManager().addEvent(groupMessageEvent);
                }
            }

            case "request" -> {
                String type = packet.get("request_type").getAsString();

                if (type.equals("group")) {

                    Group group = new Group(packet.get("group_id").getAsLong());
                    long id = packet.get("user_id").getAsLong();
                    String nickName = Member.getNameCardOrNick(id);
                    Stranger from = new Stranger(group, nickName, id);

                    long timeStamp = packet.get("time").getAsLong();
                    long botID = packet.get("self_id").getAsLong();

                    String flag = packet.get("flag").getAsString();
                    String sudType = packet.get("sub_type").getAsString();

                    MemberJoinRequestEvent memberJoinRequestEvent = new MemberJoinRequestEvent(timeStamp, botID, group, from, null, flag, sudType);
                    Bot.getEventManager().addEvent(memberJoinRequestEvent);
                }
            }

            case "notice" -> {
                String noticeType = packet.get("notice_type").getAsString();

                if (noticeType.equals("group_increase")) {  // 群人数增加
                    Group group = new Group(packet.get("group_id").getAsLong());
                    GroupMember member = group.get(packet.get("user_id").getAsLong());

                    long timeStamp = packet.get("time").getAsLong();
                    long botID = packet.get("self_id").getAsLong();

                    MemberJoinEvent memberJoinEvent = new MemberJoinEvent(timeStamp, botID, group, member);
                    Bot.getEventManager().addEvent(memberJoinEvent);
                }
            }
        }
    }

    @Override
    public void close(){
        this.isActiveClose = true;
        if(heartbeatThread != null){
            heartbeatThread.interrupt();
        }
        if(reconnectionThread != null){
            reconnectionThread.interrupt();
        }
        super.close();

    }

    private void launchHeartbeatThread(){
        heartbeatThread = new Thread(() -> {
            while (isOpen() && !Thread.currentThread().isInterrupted()){
                sendPing();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }

            if(!Thread.currentThread().isInterrupted()){
                autoReconnect();
            }
        }, "Heartbeat-Thread");

        heartbeatThread.start();
    }

    private void autoReconnect(){
        reconnectionThread = new Thread(() -> {
            if(!isReconnecting){
                isReconnecting = true;
                while (!currentWebsocketThread.isInterrupted()){
                    try {
                        reconnect();
                        isReconnecting = false;
                        break;
                    }
                    catch (Exception e){
                        try{
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException ie){
                            break;
                        }
                    }
                }
            }
        });

        reconnectionThread.start();
    }
}
