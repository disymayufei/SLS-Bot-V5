package cn.disy920.slbot.network.websocket;

import cn.disy920.slbot.Main;
import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.message.At;
import cn.disy920.slbot.bot.message.MessageChainBuilder;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.utils.GsonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.disy920.slbot.Main.LOGGER;
import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;
import static cn.disy920.slbot.bukkit.bot.MessageSender.announceToAllGroups;
import static cn.disy920.slbot.bukkit.bot.MessageSender.sendQueryServerMessage;
import static cn.disy920.slbot.bukkit.database.YamlDatabase.OTHER_DATA;

public class WSServer extends WebSocketServer {

    public static Map<String, WebSocket> connectionMap = new ConcurrentHashMap<>(16);

    private final List<WebSocket> connectionPool = Collections.synchronizedList(new ArrayList<>(128));

    private final List<String> CHECK_SERVER_EXTRA_MSG = Arrays.asList(
            "知道我们还有开黑啦（kook）嘛？可以开麦的哦！传送门：https://kaihei.co/DN8K5U",
            "想让其他玩家更快了解你嘛？来这里来这里：https://sls.wiki/index.php?title=SLS%E6%B4%BB%E8%B7%83%E7%8E%A9%E5%AE%B6",
            "开服开销好大的说...所以腐竹需要你们的捐助！！捐助链接和服务器鸣谢榜：https://www.starlight.cool/server/",
            "想了解更多StarLight的事项？快来我们的wiki：https://sls.wiki",
            "缺少进服的各类资源/Mod？我们的官网提供下载哦：https://www.starlight-server.com/download",
            "还不知道服规？总则章程在这里：https://www.kdocs.cn/l/cmT6lVdryHJu",
            "还不知道服里的领地？来这里看看吧：https://sls.wiki/index.php?title=SLS%E9%87%8D%E8%A6%81%E9%A2%86%E5%9C%B0",
            "不知道IP？看看这里：https://sls.wiki/index.php?title=StarLight-Server，多个线路的IP供你选择！哪个延迟低用哪个！",
            "开服开销好大的说...所以腐竹需要你们的捐助！！群公告可以找到捐助收款码的，捐助还可以上服务器鸣谢榜的：https://www.starlight.cool/server/"
    );

    private final List<String> CHECK_INTERNAL_SERVER_EXTRA_MSG = Arrays.asList(
            "也想加入内服嘛？来挑战一下我们的内服审核吧：https://jq.qq.com/?_wv=1027&k=qrWnXVBU",
            "如有变动，请及时修改内服假人，机器，Mod等信息！总览地址：https://sls.wiki/index.php?title=SLS%E5%86%85%E6%9C%8D"
    );

    private final List<String> CHECK_INTERNAL_SERVER_EXTRA_MSG_FOR_INTERNAL_GROUP = Arrays.asList(
            "内服玩得还愉快嘛？如果体验还不错的话，记得多拉些大佬来哦！我们尤其需要建筑大佬！！",
            "如有变动，请及时修改内服假人，机器，Mod等信息！总览地址：https://sls.wiki/index.php?title=SLS%E5%86%85%E6%9C%8D"
    );

    private final Pattern AT_REGEX = Pattern.compile("\\{@([0-9])*\\}");

    private int checkServerExtraMsgCounter = 0;
    private int checkInternalServerExtraMsgCounter = 0;
    private int checkInternalServerExtraMsgForInternalGroupCounter = 0;

    private StringBuffer internalMsgBuilder = new StringBuffer("内服玩家列表：");
    private int internalPlayerNum = 0;

    public WSServer(InetSocketAddress address){
        super(address);
        this.setReuseAddr(true);  // 防止服务器重启时提示端口被占用的问题
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshakeData) {
        LOGGER.debug("检测到新连接：" + handshakeData.getResourceDescriptor());
        connectionPool.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        for(Map.Entry<String, WebSocket> entry : connectionMap.entrySet()){
            if(entry.getValue().equals(conn)){
                LOGGER.warn(String.format("%s关闭了连接，状态码：%d, 原因：%s", entry.getKey(), code, reason));
                connectionMap.remove(entry.getKey());
                break;
            }
        }
        connectionPool.remove(conn);
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        for(Map.Entry<String, WebSocket> entry : connectionMap.entrySet()){
            if(entry.getValue().equals(conn)){
                LOGGER.warn(String.format("%s的连接出错，错误堆栈信息如下：", entry.getKey()));
                e.printStackTrace();
                connectionMap.remove(entry.getKey());
                break;
            }
        }
        connectionPool.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String msg) {
        LOGGER.debug(msg);

        JsonObject receiveObj;

        try {
            receiveObj = GsonFactory.getGsonInstance().fromJson(msg, JsonObject.class);
        }
        catch (JsonSyntaxException e){
            e.printStackTrace();
            return;
        }


        /*
         * 接收包格式规范：一个JSON字符串，格式如下：{"type": packetType, "args": packetData, "reqGroup": groupArray}
         * packetType的类型为String，表示当前包的类型；packetData类型可变，为包主体内容；groupArray类型为long[]，代表发起请求的群组，若有相关信息会发送至该群聊
         *
         *   其中packetType有如下几种合法的类型：
         *   - helloHandshake: 初始握手包，args为一个String，用于表明自我的身份
         *   - getIP: 获取配置文件中记录的，上一次玩家进入服务器时的IP，args为要查询玩家的ID；返回一个包含IP值的String
         *   - setIP: 设置配置文件中记录的IP值，args为一个Map（{"name": playerName, "IP": playerIP}），其中playerName为玩家的ID，playerIP为记录要设置的IP值；无返回值
         *   - announce：要求向所有交流群广播一条消息，args为一个Map（{"token": token, "msg": announceMsg}），其中token为校验用的密码， announceMsg为相关的更新信息；若groupArray值不为空，且为一组合法的交流群，则仅向groupArray中的群聊发送该广播；无返回值
         *   - onlineModeStatus：代表玩家正版认证状态，args为一个Map（{"status": onlineStatus, "reason": reason, "player": playerName}），onlineStatus，reason与player均为一String，状态修改成功onlineStatus为"success"，reason为null，失败onlineStatus为"failed"，并返回失败原因，playerName为正版认证的玩家名
         *   - playerList：代表目标服务器的玩家列表，args为一个array，包含所有在线玩家ID
         *   - commandResult: 代表目标服务器命令执行后的结果，args为一个map（{"command": cmd, "status": executeStatus}），cmd为一String，表示执行的命令内容; executeStatus为一String，含义与onlineModeStatus中一致
         *   - commandOutput: 代表服务器在执行命令后的输出，args为一个map（{"command": cmd, "message": msg}），cmd为一String，表示执行的命令内容;msg为一String表示控制台的输出内容
         *   - changeExamStatus: 代表要修改某玩家的审核状态，args为一个Map（{"QQNum": num, "passed": isPassed, "token": token}），num为一Number，表示玩家的QQ号，isPassed为一boolean，表示玩家是否过审，token为校验用的密码，返回过审信息。
         *   - chat: 代表某服务器的聊天信息，args为一个Map（{"identity": serverIdentity, "text": chatText}），serverIdentity为一String，表示服务器的身份（会展示在聊天内容的开头），chatText为一String，表示聊天的内容
         *   - chatBridge: 代表需要在子服之间跨服传送的消息，args为一个Map（{""s"": serverIdentity, "sender": sender, "text": chatText}），serverIdentity为一String，表示服务器的身份（会展示在聊天内容的开头），sender为一String，表示消息的发出者，chatText为一String，表示聊天的内容
         *   - getUUID: 获取某ID玩家的UUID，args为一个String，包含该玩家的ID，返回一个包含该玩家UUID值的String
         */

        File dataFile = new File(OTHER_DATA, "Data.yml");

        String packetHeader = receiveObj.get("type").getAsString();
        JsonArray groupArray = receiveObj.getAsJsonArray("reqGroup");

        if(packetHeader != null){

            switch (packetHeader) {
                case "helloHandshake" -> {
                    String identity = receiveObj.get("args").getAsString();
                    if (identity != null) {
                        connectionMap.put(identity, conn);
                        LOGGER.info(String.format("检测到新的连接，连接者身份: %s", identity));
                    }
                }

                case "getIP" -> {
                    String playerNameForGetIP = receiveObj.get("args").getAsString();

                    /* 准备包的基本骨架 */
                    Map<String, Object> sendPacket = new HashMap<>();
                    sendPacket.put("type", "IP");
                    Map<String, String> IPData = new HashMap<>();
                    IPData.put("PlayerName", playerNameForGetIP);

                    /* 插入玩家的IP值 */
                    if (playerNameForGetIP != null && !"".equals(playerNameForGetIP)) {
                        IPData.put("IP", YamlDatabase.INSTANCE.getPlayerIP(playerNameForGetIP));
                    } else {
                        IPData.put("IP", null);
                    }

                    /* 打包及发包 */
                    sendPacket.put("args", IPData);
                    conn.send(GsonFactory.getGsonInstance().toJson(sendPacket));
                }

                case "setIP" -> {
                    JsonObject ipArgs = receiveObj.getAsJsonObject("args");

                    String playerNameForSetIp = ipArgs.get("name").getAsString();
                    String playerIpForSet = ipArgs.get("IP").getAsString();

                    /* 调用方法设定IP */
                    if(playerNameForSetIp != null && !"".equals(playerNameForSetIp)){
                        if(playerIpForSet != null && !"".equals(playerIpForSet)){
                            YamlDatabase.INSTANCE.setPlayerIP(playerNameForSetIp, playerIpForSet);
                        }
                    }
                }

                case "announce" -> {
                    JsonObject packetArg = receiveObj.getAsJsonObject("args");
                    String token = packetArg.get("token").getAsString();
                    if(token.equals(PLUGIN_INSTANCE.getConfig().getString("Announce_Token"))){
                        String rawAnnounceMsg = packetArg.get("msg").getAsString();
                        Matcher m = AT_REGEX.matcher(rawAnnounceMsg);
                        int index = 0;
                        int targetIndex = -1;
                        if(m.find()){
                            targetIndex = m.start();
                        }
                        MessageChainBuilder announceMsgBuilder = new MessageChainBuilder();
                        while (index < rawAnnounceMsg.length()){
                            if(index == targetIndex){
                                index = m.end();
                                String targetQQNumStr = rawAnnounceMsg.substring(m.start(), m.end()).replace("{@", "").replace("}", "");

                                if(m.find()){
                                    targetIndex = m.start();
                                }

                                long targetQQNum;

                                try {
                                    targetQQNum = Long.parseLong(targetQQNumStr);
                                }
                                catch (Exception e){
                                    continue;
                                }

                                announceMsgBuilder.append(new At(targetQQNum));
                            }
                            else {
                                announceMsgBuilder.append(rawAnnounceMsg.charAt(index));
                                index++;
                            }
                        }

                        List<Long> QQGroupList = PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group");

                        if(groupArray != null && groupArray.size() > 0){
                            for (JsonElement groupEle : groupArray){
                                long group = groupEle.getAsLong();
                                if(QQGroupList.contains(group)){
                                    Group allowGroup = Bot.getGroup(group);
                                    if(allowGroup != null){
                                        allowGroup.sendMessage(announceMsgBuilder.build());
                                    }
                                }
                            }
                        }
                        else {
                            for(Long group : QQGroupList){
                                Group allowGroup = Bot.getGroup(group);
                                if(allowGroup != null){
                                    allowGroup.sendMessage(announceMsgBuilder.build());
                                }
                            }
                        }
                    }
                }

                case "onlineModeStatus" -> {
                    JsonObject onlineStatus = receiveObj.getAsJsonObject("args");

                    String playerName = onlineStatus.get("player").getAsString();

                    if(onlineStatus.get("status").getAsString().equals("success")){
                        if(onlineStatus.get("reason").getAsString().contains("取消")){
                            announceToAllGroups(groupArray, String.format("取消%s的正版认证成功了，如有需要请尽快重新认证哦！", playerName));
                            YamlDatabase.INSTANCE.setOnlineMode(playerName, false);
                        }
                        else {
                            announceToAllGroups(groupArray, String.format("%s的正版认证成功啦！快进服体验一下吧！", playerName));
                            YamlDatabase.INSTANCE.setOnlineMode(onlineStatus.get("player").getAsString(), true);
                        }

                    }
                    else {
                        announceToAllGroups(groupArray, onlineStatus.get("reason").getAsString());
                    }
                }

                case "playerList" -> {
                    JsonArray onlinePlayers = receiveObj.getAsJsonArray("args");
                    String clientIdentity = getClientIdentity(conn);
                    if(clientIdentity != null){
                        if(clientIdentity.equals("Bungeecord") || clientIdentity.contains("External")) {
                            if(onlinePlayers.size() == 0){
                                announceToAllGroups(groupArray, "az，好像没有人在外服玩诶");
                            }
                            else {
                                sendQueryServerMessage(onlinePlayers, groupArray, PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg"));
                            }

                            new Thread(() -> {
                                try {
                                    Thread.sleep(2500);
                                } catch (InterruptedException e) {
                                    return;
                                }
                                announceToAllGroups(groupArray, CHECK_SERVER_EXTRA_MSG.get(checkServerExtraMsgCounter));
                                checkServerExtraMsgCounter++;
                                if(checkServerExtraMsgCounter >= CHECK_SERVER_EXTRA_MSG.size()){
                                    checkServerExtraMsgCounter = 0;
                                }
                            }).start();

                        }
                        else if(clientIdentity.contains("Internal")){
                            addInternalServerBuffer(clientIdentity, onlinePlayers, groupArray);
                        }
                    }
                    else {
                        if(onlinePlayers.size() > 0){
                            StringBuilder resultBuilder = new StringBuilder("说到这个服务器呀，有");

                            resultBuilder.append(onlinePlayers.size());
                            resultBuilder.append(" / ");
                            resultBuilder.append(Calendar.getInstance().get(Calendar.YEAR));
                            resultBuilder.append("人在线哦：");

                            for(int i = 0; i < onlinePlayers.size(); i++){
                                resultBuilder.append("\n - ");
                                resultBuilder.append(onlinePlayers.get(i).getAsString());
                            }

                            announceToAllGroups(groupArray, resultBuilder.toString());
                        }
                        else {
                            announceToAllGroups(groupArray, "az，好像没人在这个服务器里玩诶...");
                        }
                    }
                }

                case "commandResult" -> {
                    JsonObject cmdResult = receiveObj.getAsJsonObject("args");

                    if ("success".equals(cmdResult.get("status").getAsString())) {
                        announceToAllGroups(groupArray, String.format("命令\"%s\"执行成功!", cmdResult.get("command").getAsString()));
                    } else {
                        announceToAllGroups(groupArray, String.format("命令\"%s\"执行出错...", cmdResult.get("command").getAsString()));
                    }
                }

                case "commandOutput" -> {
                    JsonObject cmdOutputPacket = receiveObj.getAsJsonObject("args");

                    String cmd = cmdOutputPacket.get("command").getAsString();

                    String cmdOutput = cmdOutputPacket.get("message").getAsString();
                    if(cmdOutput == null){
                        cmdOutput = "";
                    }
                    else if(cmdOutput.length() > 4000){
                        cmdOutput = "返回内容过长，已自动截断:\n" + cmdOutput.substring(4000);
                    }

                    if(cmd != null){
                        announceToAllGroups(groupArray, "命令\"" + cmd + "\"的返回结果为: " + cmdOutput);
                    }
                }

                case "chat" -> {
                    JsonObject chatPacket = receiveObj.getAsJsonObject("args");
                    String text = chatPacket.get("text").getAsString();

                    String chat = String.format("[%s] %s", chatPacket.get("identity").getAsString(), text);
                    announceToAllGroups(groupArray, chat);
                }

                case "chatBridge" -> announceToAllClient(msg, conn);

                case "getUUID" -> {
                    String playerID = receiveObj.get("args").getAsString();

                    Map<String, Object> sendPacket = new HashMap<>();
                    sendPacket.put("type", "UUID");

                    Map<String, Object> params = new HashMap<>();

                    if (YamlDatabase.INSTANCE.isInWhiteList(playerID)) {
                        params.put("errorMsg", "");
                        params.put("whitelisted", true);
                        params.put("ID", playerID);
                        params.put("UUID", YamlDatabase.INSTANCE.getUUIDByID(playerID));
                    }
                    else {
                        params.put("errorMsg", PLUGIN_INSTANCE.getConfig().getString("Kick_Message"));
                        params.put("whitelisted", false);
                        params.put("ID", playerID);
                        params.put("UUID", new UUID(0, 0));
                    }

                    sendPacket.put("args", params);
                    conn.send(GsonFactory.getGsonInstance().toJson(sendPacket));
                }
            }
        }
    }

    @Override
    public void onStart() {
        LOGGER.info(String.format("WebSocket for Bot已经启动，正在监听:ws://%s:%d", this.getAddress().getHostString(), this.getPort()));
    }

    public synchronized void sendCheckInternalServerMsg(JsonArray groupArray){

        sendQueryServerMessage(internalMsgBuilder.toString(), internalPlayerNum, groupArray, PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg"), 2);
        internalMsgBuilder = new StringBuffer("内服玩家列表:");
        internalPlayerNum = 0;

        List<Long> allInternalGroups = PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group");

        JsonArray internalGroup = new JsonArray();
        JsonArray otherGroup = new JsonArray();

        for(JsonElement groupELe : groupArray){
            long groupID = groupELe.getAsLong();
            if (allInternalGroups.contains(groupID)) {
                internalGroup.add(groupELe);
            }
            else {
                otherGroup.add(groupELe);
            }
        }

        if(internalGroup.size() > 0){
            announceToAllGroups(internalGroup, CHECK_INTERNAL_SERVER_EXTRA_MSG_FOR_INTERNAL_GROUP.get(checkInternalServerExtraMsgForInternalGroupCounter));
            checkInternalServerExtraMsgForInternalGroupCounter++;
            if(checkInternalServerExtraMsgForInternalGroupCounter >= CHECK_INTERNAL_SERVER_EXTRA_MSG_FOR_INTERNAL_GROUP.size()){
                checkInternalServerExtraMsgForInternalGroupCounter = 0;
            }
        }

        if(otherGroup.size() > 0){
            announceToAllGroups(otherGroup, CHECK_INTERNAL_SERVER_EXTRA_MSG.get(checkInternalServerExtraMsgCounter));
            checkInternalServerExtraMsgCounter++;
            if(checkInternalServerExtraMsgCounter >= CHECK_INTERNAL_SERVER_EXTRA_MSG.size()){
                checkInternalServerExtraMsgCounter = 0;
            }
        }
    }

    private synchronized void addInternalServerBuffer(String clientIdentity, JsonArray onlinePlayers, JsonArray groupArray){
        internalPlayerNum += onlinePlayers.size();

        internalMsgBuilder.append("\n\n    [");
        internalMsgBuilder.append(clientIdentity);
        internalMsgBuilder.append("] ");

        if(onlinePlayers.size() > 0){
            internalMsgBuilder.append(onlinePlayers.size());
            internalMsgBuilder.append(" / ");
            internalMsgBuilder.append(Calendar.getInstance().get(Calendar.YEAR));
            internalMsgBuilder.append(":");
            for(int i = 0; i < onlinePlayers.size(); i++){
                internalMsgBuilder.append("\n        -  ");
                internalMsgBuilder.append(onlinePlayers.get(i).getAsString());
            }
        }
        else {
            internalMsgBuilder.append("0 / ");
            internalMsgBuilder.append(Calendar.getInstance().get(Calendar.YEAR));
        }

        Main.INTERNAL_SERVER_TIMER_INSTANCE.resetLock(groupArray);
    }

    @Nullable
    private String getClientIdentity(WebSocket conn){
        for(Map.Entry<String, WebSocket> entry : connectionMap.entrySet()){
            if(entry.getValue().equals(conn)){
                return entry.getKey();
            }
        }

        return null;
    }

    private void announceToAllClient(String packet, WebSocket own) {
        for (WebSocket conn : connectionPool) {
            if (!conn.equals(own)) {
                conn.send(packet);
            }
        }
    }
}
