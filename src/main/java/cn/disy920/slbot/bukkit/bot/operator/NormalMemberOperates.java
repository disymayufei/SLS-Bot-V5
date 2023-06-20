package cn.disy920.slbot.bukkit.bot.operator;

import cn.disy920.slbot.Main;
import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.command.Command;
import cn.disy920.slbot.bot.command.Operate;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.MemberJoinEvent;
import cn.disy920.slbot.bot.event.MemberJoinRequestEvent;
import cn.disy920.slbot.bot.message.At;
import cn.disy920.slbot.bot.message.Image;
import cn.disy920.slbot.bot.message.MessageChainBuilder;
import cn.disy920.slbot.bot.message.QuoteReply;
import cn.disy920.slbot.bot.websocket.WSServer;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.error.BasicError;
import cn.disy920.slbot.error.ErrorPacket;
import cn.disy920.slbot.render.SimpleImageGenerator;
import cn.disy920.slbot.utils.GsonFactory;
import cn.disy920.slbot.utils.LuckRating;
import cn.disy920.slbot.utils.system.DateTime;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static cn.disy920.slbot.Main.LOGGER;
import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;
import static cn.disy920.slbot.bukkit.database.YamlDatabase.DATABASE;

public class NormalMemberOperates {
    public static final NormalMemberOperates NORMAL_MEMBER_OPERATES_INSTANCE = new NormalMemberOperates();

    private static final Gson GSON = GsonFactory.getGsonInstance();

    // TODO: 可通过配置文件修改的存活确认消息
    private static final List<String> ALIVE_MSG = Arrays.asList(
            "我还活着诶！",
            "内个...有什么事嘛？",
            "Hi!",
            "Hello",
            "你好吖！",
            "老叫我我是会烦你的哦",
            "让我看看是哪个屑又在叫我了",
            "你别看我是一个bot，但偶尔我也是会有小情绪的",
            "110010010111101",
            "pong",
            "你知道吗，我是用Java写出来的哦！不是Jvav是Java！",
            "最近有没有什么八卦，和我聊聊！（星星眼）",
            "我觉得这个功能已经被滥用了...",
            "不要想着靠你一个人试出来所有确认我存活后我会说的话，因为这样你会被当做刷屏被管理t掉的！",
            "哼哼哼，啊啊啊啊啊啊啊啊啊啊啊",
            "你说说你，天天不干点正事，老确认我存活干什么！",
            "宝你觉得我今天是不是又瘦一些了",
            "刷屏警告！虽然你也许没想刷屏就是了...",
            "我猜你一定是很无聊吧，要不为啥老确认一个机器人的存活状况...",
            "不要用存活确认刷屏哦，否则管理员会t掉你的！",
            "如果你缺人陪你聊天，你可以等我恢复智能，而不是一直确认存活",
            "真的，有时候我会说胡话，但这并不代表我喜欢你！",
            "Alive！",
            "你好呀，是不是又叫我了",
            "老叫我我会生气的[○･｀Д´･ ○]！",
            "不用确认了，说了我在！",
            "你猜我现在是不是智能bot",
            "我在我在！",
            "叫我干嘛？",
            "天天叫我还不进服玩的屑",
            "一切OK哦！",
            "总确认我存活干嘛？难道说...你喜欢我！",
            "我活着活着活着！！",
            "下次你不如发个ping包给我？",
            "快进服玩！",
            "今天又是个好天气诶...等下，你是不是叫了我！",
            "我已出仓，感觉良好！",
            "在此向你问好哦！"
    );


    /* 预编译的正则，防止接收消息时临时编译，拖慢机器人处理速度 */
    private static final Pattern ID_CHECK_REGEX = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");
    private static final Pattern HELP_REGEX = Pattern.compile("^[#＃](help|帮助|获取帮助)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_HELP_REGEX = Pattern.compile("^[#＃]玩家帮助列表$");

    private final Pattern IMAGE_AND_VIDEO_FILE_REGEX = Pattern.compile("(.jpg|.jpeg|.jiff|.png|.gif|.bmp|.webp|.mp4|.mov|.mkv)$");
    /* 正则部分常量结束 */


    /**
     * 确认机器人是否存活的方法
     * @param event 群聊消息事件
     */
    public static boolean checkAlive(GroupMessageEvent event) {
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(new At(senderID))
                    .append(" ")
                    .append(ALIVE_MSG.get(new Random().nextInt(ALIVE_MSG.size())))
                    .build());
        }

        return Command.SUCCESS;
    }


    /**
     * 获取当天的幸运指数的方法
     * @param event 群聊消息事件
     */
    public static boolean getLuckRating(GroupMessageEvent event) {
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").contains(groupID)){
            int luckRating = LuckRating.getRating(senderID);

            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(new At(senderID))
                    .append(" 您今日的幸运指数是:%d，看起来是个%s".formatted(luckRating, LuckRating.getString(luckRating)))
                    .build());
        }

        return Command.SUCCESS;
    }


    /**
     * 玩家添加绑定ID的方法
     * @param event 群聊消息事件
     */
    public static boolean addBindID(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {
            if (PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1) {  // 存在审核群
                if (!YamlDatabase.INSTANCE.hadPassedTheExam(senderID)) {
                    String sendingMsg = PLUGIN_INSTANCE.getConfig().getString("Require_Exam_Message");

                    event.getGroup().sendMessage(new MessageChainBuilder()  // 借助消息链构造携带at的消息
                            .append(new At(senderID))
                            .append(" ")
                            .append(sendingMsg == null ? "请先去做审核题哦！" : sendingMsg)
                            .build()
                    );
                    return Command.SUCCESS;
                }
            }

            if (YamlDatabase.INSTANCE.getBindNum(senderID) < 2) {
                String playerID = groupMes.substring(6);
                if (playerID.length() < 3 || playerID.length() > 20) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append("ID的长度仅允许在3~20（含上下限）之间哦！")
                            .build()
                    );
                    return Command.SUCCESS;
                } else if (!ID_CHECK_REGEX.matcher(playerID).matches()) {
                    if (playerID.contains("<") || playerID.contains(">")) {
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ")
                                .append("绑定ID时，不需要加上<>的！这个只是告诉你，这里面的参数可变且必填的哦！")
                                .build()
                        );
                        return Command.SUCCESS;
                    }
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append("ID仅允许英文字母，数字和下划线出现哦！")
                            .build()
                    );
                    return Command.SUCCESS;
                }

                ErrorPacket bindStatus = YamlDatabase.INSTANCE.addBindID(senderID, playerID, senderID);
                if (bindStatus.getError() == BasicError.NONE) {

                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append("绑定ID：")
                            .append(playerID)
                            .append("成功啦！快进服玩玩叭，不知道IP和端口可以看群公告哦！")
                            .build()
                    );
                } else {

                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append(bindStatus.toString())
                            .build()
                    );
                }
            } else {
                if (PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)) {
                    String playerID = groupMes.substring(6);
                    ErrorPacket bindStatus = YamlDatabase.INSTANCE.addBindID(senderID, playerID, senderID);
                    if (bindStatus.getError() == BasicError.NONE) {
                        event.getGroup().sendMessage("腐竹腐竹！绑定ID：" + playerID + "成功啦！");
                    } else {
                        event.getGroup().sendMessage("腐竹腐竹，" + bindStatus.getError().getInfo().replace("请联系腐竹", "快去"));
                    }
                }
                else if (PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)){
                    String playerID = groupMes.substring(6);
                    ErrorPacket bindStatus = YamlDatabase.INSTANCE.addBindID(senderID, playerID, senderID);
                    if (bindStatus.getError() == BasicError.NONE) {
                        event.getGroup().sendMessage("管理管理！绑定ID：" + playerID + "成功啦！");
                    } else {
                        event.getGroup().sendMessage("管理管理，" + bindStatus);
                    }
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append("一个人最多绑定2个ID哦！如果需要删除绑定错误的ID，请回复”#删除ID：你的ID“即可！")
                            .build());
                }
            }
        }
        else {
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new At(senderID))
                    .append(" 绑定或删除ID只能在各交流群进行哦！")
                    .build()
            );
        }

        return Command.SUCCESS;
    }


    /**
     * 玩家删除绑定ID的方法
     * @param event 群聊消息事件
     */
    public static boolean delBindID(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        String playerID = groupMes.substring(6);

        if (PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {
            if (playerID.contains("<") || playerID.contains(">")){
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" ")
                        .append("删除ID时，不需要加上<>的！这个只是告诉你，这里面的参数可变且必填的哦！")
                        .build()
                );
                return Command.SUCCESS;
            }

            if (!PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)){
                if (YamlDatabase.INSTANCE.checkBindID(senderID).contains(playerID)){
                    ErrorPacket cancelBindStatus = YamlDatabase.INSTANCE.delBindID(senderID, playerID);

                    if (cancelBindStatus.getError() == BasicError.NONE){

                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ")
                                .append("删除ID：")
                                .append(playerID)
                                .append("成功啦！如有需要不要忘记重新绑定哦！")
                                .build()
                        );
                    }
                    else {
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ")
                                .append(cancelBindStatus.toString())
                                .build()
                        );
                    }
                }
            }

            else {
                ErrorPacket cancelBindStatus = YamlDatabase.INSTANCE.forceDelBindID(playerID);

                if (cancelBindStatus.getError() == BasicError.NONE){

                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ")
                            .append("删除ID：")
                            .append(playerID)
                            .append("成功啦，腐竹大大！")
                            .build()
                    );
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" 腐竹腐竹！")
                            .append(cancelBindStatus.toString())
                            .build()
                    );
                }
            }
        }

        return Command.SUCCESS;
    }


    /**
     * 查询绑定ID的方法
     * @param event 群聊消息事件
     */
    public static boolean queryID(GroupMessageEvent event) {
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {

            List<String> playerIDList = YamlDatabase.INSTANCE.checkBindID(senderID);

            if (playerIDList.size() == 0){
                event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append("嘛？你没绑定过任何ID哦！").build());
                return Command.SUCCESS;
            }

            MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
            messageChainBuilder.append(new At(senderID)).append("嘛？你绑定了以下ID哦：");

            for (int i = 0; i < playerIDList.size(); i++) {
                String playerID = playerIDList.get(i);
                messageChainBuilder
                        .append("\n槽位")
                        .append(i + 1)
                        .append(": ")
                        .append(playerID);
            }

            event.getGroup().sendMessage(messageChainBuilder.build());
        }

        return Command.SUCCESS;
    }

    /**
     * 免密登录的方法
     * @param event 群聊消息事件
     */
    public static boolean pwdFreeLogin(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group") == groupID) {

            String[] pwdFreeData = groupMes.substring(6).split(" ");
            if (pwdFreeData.length != 2){
                event.getGroup().sendMessage("免密登录格式错误哦！正确的格式是：\"#免密登录:<你的游戏ID> <免密登录可持续的时间>\"(注意ID和时间之间的空格！)");
                return Command.SUCCESS;
            }

            String playerID = pwdFreeData[0];
            String deltaTimeStr = pwdFreeData[1];

            if (!YamlDatabase.INSTANCE.checkBindID(senderID).contains(playerID)){
                event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 这个ID似乎不是你绑定的...").build());
                return Command.SUCCESS;
            }

            long deltaTime = DateTime.parseTime(deltaTimeStr);

            YamlDatabase.INSTANCE.setNoPwdStamp(playerID, new Date().getTime() + deltaTime * 1000);

            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new At(senderID))
                    .append(" 免密登录设置成功！截止到")
                    .append(DateTime.convertToDate(deltaTime))
                    .append("前，您相同IP下的设备均可免密登录哦！")
                    .build()
            );
        }

        return Command.SUCCESS;
    }


    /**
     * 获取帮助的方法
     * @param event 群聊消息事件
     */
    public static boolean requireHelp(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long senderID = event.getSender().getID();

        List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");

        if (HELP_REGEX.matcher(groupMes).matches()){
            if (adminList.contains(senderID)){
                if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg")){
                    try {
                        event.getGroup().sendMessage(new Image(SimpleImageGenerator.gen(getAdminHelpText(), 25)));
                    }
                    catch (Exception e) {
                        event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                        event.getGroup().sendMessage(getAdminHelpText());
                        e.printStackTrace();
                    }
                }
                else {
                    event.getGroup().sendMessage(getAdminHelpText());
                }
            }
            else {
                if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg")){
                    try {
                        event.getGroup().sendMessage(new Image(SimpleImageGenerator.gen(getPlayerHelpText(), 25)));
                    }
                    catch (Exception e) {
                        event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                        event.getGroup().sendMessage(getPlayerHelpText());
                        e.printStackTrace();
                    }
                }
                else {
                    event.getGroup().sendMessage(getPlayerHelpText());
                }
            }
        }

        else if (PLAYER_HELP_REGEX.matcher(groupMes).matches()){
            if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg")){
                try {
                    event.getGroup().sendMessage(new Image(SimpleImageGenerator.gen(getPlayerHelpText(), 25)));
                }
                catch (Exception e) {
                    event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                    event.getGroup().sendMessage(getPlayerHelpText());
                    e.printStackTrace();
                }
            }
            else {
                event.getGroup().sendMessage(getPlayerHelpText());
            }
        }

        return Command.SUCCESS;
    }


    /**
     * 获取正版认证帮助的方法
     * @param event 群聊消息事件
     */
    public static boolean onlineVerifyHelp(GroupMessageEvent event) {
        long senderID = event.getSender().getID();

        event.getGroup().sendMessage(
                new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 警告：如果您的账号不是正版账号，您将面临无法进入服务器的问题！如果您确认自己是正版账号，请再次回复：\"#确认正版认证:<你的ID>\"来完成验证！")
                        .build()
        );

        return Command.SUCCESS;
    }


    /**
     * 修改正版认证状态的方法
     * @param event 群聊消息事件
     */
    public static boolean changeOnlineVerify(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        String playerID = groupMes.substring(8);

        if (!YamlDatabase.INSTANCE.checkBindID(senderID).contains(playerID)){
            event.getGroup().sendMessage(
                    new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" 你又没绑定过这个ID！这让我怎么帮你正版认证嘛...")
                            .build()
            );

            return Command.SUCCESS;
        }

        if (WSServer.connectionMap.containsKey("Bungeecord")){
            WebSocket conn = WSServer.connectionMap.get("Bungeecord");
            if (conn != null && conn.isOpen()){
                Map<String, Object> packet = new HashMap<>();
                packet.put("type", "onlineModeStatus");

                Map<String, Object> packetArgs = new HashMap<>();
                if (groupMes.contains("确认")) {
                    packetArgs.put("enable", true);
                }
                else {
                    packetArgs.put("enable", false);
                }
                packetArgs.put("playerName", playerID);

                packet.put("args", packetArgs);
                packet.put("reqGroup", new long[]{ groupID });

                conn.send(GSON.toJson(packet));

                return Command.SUCCESS;
            }
        }

        event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" Bungeecord端看起来当前不在线呢！可以稍后再试一次！").build());

        return Command.SUCCESS;
    }


    /**
     * 查服的方法
     * @param event 群聊消息事件
     */
    public static boolean queryServer(GroupMessageEvent event) {
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        if (WSServer.connectionMap.containsKey("Bungeecord")){
            WebSocket conn = WSServer.connectionMap.get("Bungeecord");
            if (conn != null && conn.isOpen()){
                Map<String, Object> packet = new HashMap<>();
                packet.put("type", "getOnlinePlayers");
                packet.put("args", null);
                packet.put("reqGroup", new long[]{ groupID });

                conn.send(GSON.toJson(packet));

                return Command.SUCCESS;
            }
        }
        else if (WSServer.connectionMap.containsKey("External-Server")) {
            WebSocket conn = WSServer.connectionMap.get("External-Server");
            if (conn != null && conn.isOpen()){
                Map<String, Object> packet = new HashMap<>();
                packet.put("type", "getOnlinePlayers");
                packet.put("args", null);
                packet.put("reqGroup", new long[]{ groupID });

                conn.send(GSON.toJson(packet));

                return Command.SUCCESS;
            }
        }

        event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" Bungeecord端看起来当前不在线呢！可以稍后再试一次！").build());

        return Command.SUCCESS;
    }


    /**
     * 查内服的方法
     * @param event 群聊消息事件
     */
    public static boolean queryInternalServer(GroupMessageEvent event) {
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

       if (Main.INTERNAL_SERVER_TIMER_INSTANCE.isLocking()){
            event.getGroup().sendMessage("查询太快了诶，先休息一下吧！");
           return Command.SUCCESS;
        }

        Map<String, Object> packet = new HashMap<>();
        packet.put("type", "getOnlinePlayers");
        packet.put("args", null);
        packet.put("reqGroup", new long[]{ groupID });

        for(Map.Entry<String, WebSocket> pair : WSServer.connectionMap.entrySet()){
            if (pair.getKey().contains("Internal")){
                WebSocket conn = pair.getValue();
                if (conn != null && conn.isOpen()){
                    conn.send(GSON.toJson(packet));
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 内服节点[").append(pair.getKey()).append("]看起来当前不在线呢！这个服的玩家可能不会正常显示哦！").build());
                }
            }
        }
        return Command.SUCCESS;
    }


    /**
     * 执行单条指令的方法
     * @param event 群聊消息事件
     */
    public static boolean executeSingleCommand(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();
        long senderID = event.getSender().getID();

        boolean isFakePlayerSpawnCmd = false;
        String fakePlayerName = null;

        String cmd = groupMes.substring(6);
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        if (!cmd.startsWith("player ")) {
            if (cmd.startsWith("ban ") || cmd.startsWith("clone ") || cmd.startsWith("execute ") || cmd.startsWith("fill ") || cmd.startsWith("op ") || cmd.startsWith("deop ") || cmd.startsWith("give ") || cmd.startsWith("kill ") || cmd.startsWith("kick ") || cmd.startsWith("pardon ") || cmd.startsWith("setblock ") || cmd.startsWith("summon ") || cmd.startsWith("whitelist ") || cmd.startsWith("unban ") || cmd.startsWith("stop ")){
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 这么敏感的指令还是请OP们进服亲自执行吧！")
                        .build()
                );

                return Command.SUCCESS;
            }

            if (!PLUGIN_INSTANCE.getConfig().getLongList("Internal_Admins_QQ").contains(event.getSender().getID())){
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 非假人生成命令只能由管理员执行哦！")
                        .build()
                );

                return Command.SUCCESS;
            }
        }
        else {
            if (cmd.contains("shadow")){
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 禁止在群内使用Shadow指令哦！")
                        .build()
                );

                return Command.SUCCESS;
            }

            if (cmd.contains("spawn")) {
                isFakePlayerSpawnCmd = true;  // 群内玩家召唤的假人仅可以是生存模式的
                fakePlayerName = cmd.split(" ")[1];
            }
        }

        Map<String, Object> cmdPacket = new HashMap<>();


        if (WSServer.connectionMap.containsKey("Internal-Survival")){
            WebSocket conn = WSServer.connectionMap.get("Internal-Survival");
            if (conn != null && conn.isOpen()){
                Map<String, Object> packet = new HashMap<>();
                cmdPacket.put("cmd", cmd);

                packet.put("type", "exeCmd");
                packet.put("args", cmdPacket);
                packet.put("reqGroup", new long[]{ groupID });

                conn.send(GSON.toJson(packet));

                if (isFakePlayerSpawnCmd && fakePlayerName != null){
                    Map<String, Object> extraPacket = new HashMap<>();
                    cmdPacket.clear();
                    cmdPacket.put("cmd", String.format("gamemode survival %s", fakePlayerName));

                    extraPacket.put("type", "exeCmd");
                    extraPacket.put("args", cmdPacket);
                    extraPacket.put("reqGroup", null);

                    conn.send(GSON.toJson(extraPacket));
                }

                return Command.SUCCESS;
            }
        }

        event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 内服服务端看起来当前不在线呢！可以稍后再试一次！").build());

        return Command.SUCCESS;
    }


    /**
     * 获取Q & A信息的方法
     * @param event 群聊消息事件
     */
    public static boolean requireQAndA(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long senderID = event.getSender().getID();

        int page;

        if (groupMes.length() < 4){
            event.getGroup().sendMessage(getPlayerQAndAText());
            return Command.SUCCESS;
        }
        else {
            try {
                page = Integer.parseInt(groupMes.substring(3));
            }
            catch (Exception e){
                event.getGroup().sendMessage(getPlayerQAndAText());
                return Command.SUCCESS;
            }
        }

        MessageChainBuilder general_mcb = new MessageChainBuilder().append(new At(senderID)).append(" ");

        switch (page) {
            case 1 ->  // 不知道IP
                    event.getGroup().sendMessage(general_mcb.append("不知道IP嘛？看这里：https://sls.wiki/index.php?title=StarLight-Server#%E6%9C%8D%E5%8A%A1%E5%99%A8IP%E4%B8%8E%E7%AB%AF%E5%8F%A3").build());
            case 2 ->  // 不知道加速IP的作用
                    event.getGroup().sendMessage(general_mcb.append("不知道加速IP的作用嘛？看这里：https://sls.wiki/index.php?title=%E7%9B%B4%E8%BF%9EIP%E4%B8%8E%E5%8A%A0%E9%80%9FIP").build());
            case 3 ->  // 不知道IPv4与IPv6
                    event.getGroup().sendMessage(general_mcb.append("IPv4和IPv6完全搞不懂？看这里：https://sls.wiki/index.php?title=IPv4%E4%B8%8EIPv6").build());
            case 4 ->  // 不会用领地插件
                    event.getGroup().sendMessage(general_mcb.append("不会用领地插件嘛？看这里：https://sls.wiki/index.php?title=Residence").build());
            case 5 ->  // 不知道服规
                    event.getGroup().sendMessage(general_mcb.append("想回顾下服规嘛？看这里：https://www.kdocs.cn/l/cmT6lVdryHJu").build());
            case 6 ->  // 服务器运营模式
                    event.getGroup().sendMessage(general_mcb.append("服务器为纯公益服，日常收益完全依靠玩家的自愿捐助！想了解更多？来看看：https://www.starlight.cool/donation.html").build());
            case 7 ->  // 想和服务器玩家开语音
                    event.getGroup().sendMessage(general_mcb.append("要和玩家一起开语音游玩嘛？加入我们的kook频道：https://www.kookapp.cn/app/channels/7756332831338277").build());
            default -> {
                event.getGroup().sendMessage(getPlayerQAndAText());
                return Command.SUCCESS;
            }
        }

        event.getGroup().sendMessage("注意哦！由于QQ自身的限制，所以这些链接也许无法从QQ直接点开哦！可以先把链接复制到浏览器，然后就能打开啦！");

        return Command.SUCCESS;
    }


    public static boolean shoutInServer(GroupMessageEvent event) {
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getID();

        String chat = groupMes.substring(6);

        Map<String, Object> packet = new HashMap<>();
        packet.put("type", "chat");

        String playerNameCard = event.getSender().getNameCard();
        if (playerNameCard.equals("")){
            playerNameCard = event.getSender().getNick();
        }

        packet.put("args", String.format("[SLS内群-%s] %s", playerNameCard, chat));
        packet.put("reqGroup", new long[]{ groupID });

        for(Map.Entry<String, WebSocket> pair : WSServer.connectionMap.entrySet()){
            if (pair.getKey().contains("Internal")){
                WebSocket conn = pair.getValue();

                if (conn != null && conn.isOpen()){
                    pair.getValue().send(GSON.toJson(packet));
                }
                else {
                    event.getGroup().sendMessage("子服" + pair.getKey() + "的节点当前好像不在线诶，你发送的消息可能无法到达这个子服哦！");
                }
            }
        }

        return Command.SUCCESS;
    }


    public static boolean exchangeSlot(GroupMessageEvent event) {
        long senderID = event.getSender().getID();

        ErrorPacket status = YamlDatabase.INSTANCE.exchangeSlot(senderID);

        if (status.getError() == BasicError.NONE) {
            event.getGroup().sendMessage("槽位交换完成，槽位1与槽位2的ID目前已成功互换！");
        }
        else {
            event.getGroup().sendMessage(status.toString());
        }

        return Command.SUCCESS;
    }

    /**
     * 检查进入主群的时候是否通过审核
     * @param event 申请入群事件
     */
    public static boolean checkLegitimacyWhileJoin(MemberJoinRequestEvent event){
        if (event.getGroupId() == PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group")){
            if (YamlDatabase.INSTANCE.hadPassedTheExam(event.getFromId())){
                event.accept();
            }

            else {
                event.reject("你似乎还没有通过审核诶！");

                Group reviewGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Review_Group"));
                if (reviewGroup != null){
                    reviewGroup.sendMessage(new MessageChainBuilder()
                            .append("滴滴，玩家：")
                            .append(event.getFromNick())
                            .append("（QQ号：")
                            .append(String.valueOf(event.getFromId()))
                            .append(event.getInvitorId() == null ? "" : ("(邀请者QQ号：" + event.getInvitorId() + ")"))
                            .append("）未通过审核，但尝试加主交流群，已被我自动拒绝了哦！")
                            .build()
                    );
                }
            }
        }

        return Operate.SUCCESS;
    }


    /* 群操作方法 */

    /**
     * 处理发送迎接新玩家消息的方法
     * @param event 新成员入群事件
     */
    public static boolean welcomeNewMember(MemberJoinEvent event){
        long groupID = event.getGroup().getID();

        if (PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group") == groupID){
            String rawMsg = getWelcomeMsg();

            int atPosition = rawMsg.indexOf("{@Member}");  // 标记需要at成员的位置

            if (atPosition != -1){  // 如果有at需求
                rawMsg = rawMsg.replace("{@Member}", "");
                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();

                for(int i = 0; i < rawMsg.length(); i++){
                    if (i == atPosition){
                        messageChainBuilder.append(new At(event.getMember().getID()));
                    }
                    messageChainBuilder.append(rawMsg.charAt(i));
                }

                event.getGroup().sendMessage(messageChainBuilder.build());
            }
            else {
                event.getGroup().sendMessage(rawMsg);  // 否则直接发送原始消息
            }

        }
        else if (PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group")  == groupID){
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append("让我们一起欢迎新玩家")
                    .append(new At(event.getMember().getID()))
                    .append("的到来！")
                    .build()
            );
        }

        return Operate.SUCCESS;
    }

    /**
     * 踢掉已经通过审核的玩家
     * 方法为阻塞的，因此请不要与主线程同步运行
     */
    public void kickPassedMember(){
        Group oldGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group"));
        Group newGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group"));

        if (oldGroup != null && newGroup != null){
            LOGGER.info("准备清理旧群人员");

            List<Long> admin_list = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");
            admin_list.add(2739302193L);  // pop猫
            admin_list.add(203701725L);  // Feya
            admin_list.add(1853359516L);  // Parzival
            admin_list.add(2377682530L);  // 棉花

            int counter = 0;

            for(GroupMember member : oldGroup.getMembers()){
                if (newGroup.get(member.getID()) != null){  // 已经在新群了
                    if (member.getRole().equals(GroupMember.Role.MEMBER)) {
                        if (!admin_list.contains(member.getID())){
                            LOGGER.info("应踢出：" + member.getID());
                            while (true){
                                try{
                                    if (member.getRole().equals(GroupMember.Role.MEMBER)) {
                                        member.kick();
                                    }
                                    LOGGER.info("已踢出：" + member.getID());
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }

                                    break;
                                }
                                catch (IllegalStateException e){
                                    LOGGER.error(e.getMessage());
                                    counter++;
                                    if (counter > 5){
                                        counter = 0;
                                        break;
                                    }
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    if (member.getRole().equals(GroupMember.Role.MEMBER)) {
                        if (!admin_list.contains(member.getID())){
                            String memberNameCard = member.getNameCard();
                            if (!memberNameCard.contains("[未审核成员]")){
                                String newNameCard = "[未审核成员] ";
                                if (memberNameCard.equals("")){
                                    newNameCard += member.getNick();
                                }
                                else {
                                    newNameCard += member.getNameCard();
                                }

                                member.setNameCard(newNameCard);
                            }
                        }
                    }
                }
            }

            LOGGER.info("旧群人员清理完成");
        }
    }


    /**
     * 获取玩家Q&A文档的方法
     * @return 玩家Q&A文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    public static String getPlayerQAndAText(){
        File help_data_file_player = new File(DATABASE, "HelpFile/player_q_and_a_msg.txt");

        if (help_data_file_player.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_player), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                LOGGER.error("读取玩家Q&A文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /* 私有方法 */

    /**
     * 获取管理员帮助文档的方法
     * @return 管理员帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    private static String getAdminHelpText(){
        File help_data_file_admin = new File(DATABASE, "HelpFile/admin_help_msg.txt");

        if (help_data_file_admin.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_admin), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                LOGGER.error("读取管理员帮助文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /**
     * 获取玩家帮助文档的方法
     * @return 玩家帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    private static String getPlayerHelpText(){
        File help_data_file_player = new File(DATABASE, "HelpFile/player_help_msg.txt");

        if (help_data_file_player.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_player), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                LOGGER.error("读取玩家帮助文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /**
     * 获取欢迎新成员信息的方法
     * @return 欢迎新成员的信息
     */
    private static String getWelcomeMsg(){
        File welcome_file = new File(DATABASE, "HelpFile/welcome_msg.txt");

        if (welcome_file.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(welcome_file), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                LOGGER.error("读取欢迎文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }
}
