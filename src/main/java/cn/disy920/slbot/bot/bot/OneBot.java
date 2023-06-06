package cn.disy920.slbot.bot.bot;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.command.Command;
import cn.disy920.slbot.bot.command.CommandTreeBuilder;
import cn.disy920.slbot.bot.command.Operate;
import cn.disy920.slbot.bot.command.OperateTreeBuilder;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.MemberJoinEvent;
import cn.disy920.slbot.bot.event.MemberJoinRequestEvent;
import cn.disy920.slbot.bot.message.At;
import cn.disy920.slbot.bot.message.MessageChainBuilder;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.bukkit.bot.operator.AdminOperates;
import cn.disy920.slbot.bukkit.bot.operator.NormalMemberOperates;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.error.BasicError;
import cn.disy920.slbot.error.ErrorPacket;
import cn.disy920.slbot.utils.convertor.NumberConvertor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static cn.disy920.slbot.Main.*;
import static cn.disy920.slbot.bukkit.bot.operator.NormalMemberOperates.NORMAL_MEMBER_OPERATES_INSTANCE;
import static cn.disy920.slbot.bukkit.database.YamlDatabase.QQ_DATA;

public class OneBot implements RunnableBot{

    public static Thread botThread = null;

    @Override
    public void registerAllEvents() {
        // Debug用
        OperateTreeBuilder<Event> debugOperateTreeBuilder = new OperateTreeBuilder<>(Event.class);

        debugOperateTreeBuilder
                .addOperate(debugOperateTreeBuilder.createNode()
                        .executes(event -> {
                            LOGGER.debug("触发事件：" + event.getClass() + ", 事件详细信息:" + event);
                            return Operate.SUCCESS;
                        }))
                .register();


        // 欢迎新成员相关
        OperateTreeBuilder<MemberJoinEvent> joinOperateTreeBuilder = new OperateTreeBuilder<>(MemberJoinEvent.class);

        joinOperateTreeBuilder
                .addOperate(joinOperateTreeBuilder.createNode()
                        .executes(NormalMemberOperates::welcomeNewMember)
                )
                .register();


        // 加群时自动验证是否通过审核
        OperateTreeBuilder<MemberJoinRequestEvent> requestJoinOperateTreeBuilder = new OperateTreeBuilder<>(MemberJoinRequestEvent.class);

        requestJoinOperateTreeBuilder
                .addOperate(requestJoinOperateTreeBuilder.createNode()
                        .executes(NormalMemberOperates::checkLegitimacyWhileJoin)
                )
                .register();


        // 普通成员事件注册
        CommandTreeBuilder<GroupMessageEvent> normalCommandTreeBuilder = new CommandTreeBuilder<>(GroupMessageEvent.class);

        normalCommandTreeBuilder
                .addCommand(
                        normalCommandTreeBuilder.createNode("存活确认")
                                .executes(NormalMemberOperates::checkAlive)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("幸运指数")
                                .alias("获取幸运指数")
                                .executes(NormalMemberOperates::getLuckRating)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("绑定ID")
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::addBindID)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("删除ID")
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::delBindID)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("查询绑定ID")
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::queryID)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("免密登录")
                                .alias("免密登陆")
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::pwdFreeLogin)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("帮助")
                                .alias(List.of("玩家帮助列表", "获取帮助", "菜单", "help", "?", "？"))
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::requireHelp)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("正版认证")
                                .alias("正版验证")
                                .executes(event -> {event.getGroup().sendMessage(
                                        new MessageChainBuilder()
                                                .append(new At(event.getSender().getID()))
                                                .append(" 你没有输入你要正版认证的ID诶，要不我随便猜一个？")
                                                .build()
                                );
                                    return Command.SUCCESS;
                                })
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("正版认证")
                                .alias("正版验证")
                                .needArgs(true)
                                .executes(NormalMemberOperates::onlineVerifyHelp)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("确认正版认证")
                                .alias(List.of("确认正版验证", "取消正版认证", "取消正版验证"))
                                .needArgs(true)
                                .executes(NormalMemberOperates::changeOnlineVerify)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("查服")
                                .executes(NormalMemberOperates::queryServer)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("查内服")
                                .require(event -> !PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").isEmpty())  // 仅在有内服时生效
                                .executes(NormalMemberOperates::queryInternalServer)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("执行命令")
                                .alias(List.of("执行", "执行指令", "cmd"))
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").contains(event.getGroup().getID()))  // 仅限内服执行
                                .setCaseInsensitive(true)
                                .needArgs(true)
                                .executes(NormalMemberOperates::executeSingleCommand)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("Q")
                                .alias(List.of("Q & A", "Q&A"))
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(NormalMemberOperates::requireQAndA)
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("Q")
                                .setCaseInsensitive(true)
                                .executes(event -> {
                                    event.getGroup().sendMessage(NormalMemberOperates.getPlayerQAndAText());
                                    return Command.SUCCESS;
                                })
                )
                .addCommand(
                        normalCommandTreeBuilder.createNode("内服喊话")
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").contains(event.getGroup().getID()))  // 仅限内服执行
                                .setCaseInsensitive(true)
                                .needArgs(true)
                                .executes(NormalMemberOperates::shoutInServer)
                )
                .register();


        // 管理员事件注册
        CommandTreeBuilder<GroupMessageEvent> adminCommandTreeBuilder = new CommandTreeBuilder<>(GroupMessageEvent.class);

        adminCommandTreeBuilder
                .require(event -> PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(event.getSender().getID()))  // 必须是配置文件中设置的管理员
                .executeWhenFailed(event -> event.getGroup().sendMessage(new MessageChainBuilder()
                        .append("不是管理却还想要执行管理指令的")
                        .append(new At(event.getSender().getID()))
                        .append("是屑！")
                        .build()
                ))
                .addCommand(
                        adminCommandTreeBuilder.createNode("审核通过")
                                .alias(List.of("过审", "通过", "通过审核"))
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1)  // 存在审核群
                                .executesWhenFailed(event -> event.getGroup().sendMessage("本服务器未启用审核机制，因此审核指令不可用哦！"))
                                .needArgs(true)
                                .executes(AdminOperates::passExam)
                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("静默过审")
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1)  // 存在审核群
                                .executesWhenFailed(event -> event.getGroup().sendMessage("本服务器未启用审核机制，因此审核指令不可用哦！"))
                                .needArgs(true)
                                .executes(AdminOperates::silentPassExam)
                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("拒绝审核")
                                .alias(List.of("拒绝", "不通过"))
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1)  // 存在审核群
                                .executesWhenFailed(event -> event.getGroup().sendMessage("本服务器未启用审核机制，因此审核指令不可用哦！"))
                                .needArgs(true)
                                .executes(AdminOperates::denyExam)

                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("查询未过审玩家")
                                .alias(List.of("查询未过审", "查询拒绝列表", "查询黑名单"))
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1)  // 存在审核群
                                .executesWhenFailed(event -> event.getGroup().sendMessage("本服务器未启用审核机制，因此审核指令不可用哦！"))
                                .executes(AdminOperates::checkDenyList)

                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("撤销审核")
                                .alias(List.of("撤销过审"))
                                .require(event -> PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group", -1) != -1)  // 存在审核群
                                .executesWhenFailed(event -> event.getGroup().sendMessage("本服务器未启用审核机制，因此审核指令不可用哦！"))
                                .needArgs(true)
                                .executes(AdminOperates::withdrawExam)
                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("计算离线UUID")
                                .alias(List.of("离线UUID", "计算UUID", "获取离线UUID", "获取UUID"))
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(AdminOperates::calcUUID)
                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("反查ID")
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(AdminOperates::findQQByID)
                )
                .addCommand(
                        adminCommandTreeBuilder.createNode("查询ID")
                                .needArgs(true)
                                .setCaseInsensitive(true)
                                .executes(AdminOperates::findIDsByQQ)
                )
                .register();


        // 服主事件注册
        CommandTreeBuilder<GroupMessageEvent> ownerCommandTreeBuilder = new CommandTreeBuilder<>(GroupMessageEvent.class);

        ownerCommandTreeBuilder
                .require(event -> PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(event.getSender().getID()))
                .executeWhenFailed(event -> {event.getGroup().sendMessage(new MessageChainBuilder()
                        .append("不是腐竹却还想要乱用腐竹专用指令的")
                        .append(new At(event.getSender().getID()))
                        .append("是屑！")
                        .build()
                );})
                .addCommand(
                        ownerCommandTreeBuilder.createNode("添加管理")
                                .needArgs(true)
                                .executes(AdminOperates::addAdmin)
                )
                .addCommand(
                        ownerCommandTreeBuilder.createNode("删除管理")
                                .needArgs(true)
                                .executes(AdminOperates::delAdmin)
                )
                .register();
    }

    public static void launchBot() {
        botThread = new Thread(new OneBot(), "Bot");
        botThread.start();
    }

    @Override
    public void run() {
        LOGGER.info("机器人正在启动...");

        try {
            int botClientPort = PLUGIN_INSTANCE.getConfig().getInt("WS_Bot_Port");
            if (botClientPort <= 0 || botClientPort >= 65536){
                RECORDER.warn("检测到不合法的Bot WebSocket端口，已自动替换为默认端口！");
                botClientPort = 16124;
            }

            WSClient.botConnection = new WSClient(new URI("ws://127.0.0.1:" + botClientPort));

            WSClient.botConnection.connect();

            YamlDatabase.INSTANCE.recoverAllData();  // 初始化时自动同步数据，防止白名与绑定ID的不一致

            /* 注册所有Bot事件 */
            registerAllEvents();
            kickAllDeadMember();  // 移除所有退群玩家的ID

            /* 清除旧群残留玩家 */
            // new Thread(NORMAL_MEMBER_OPERATES_INSTANCE::kickPassedMember, "Kick-Member-Thread").start();

            LOGGER.info("机器人启动完成！");
        } catch (URISyntaxException e) {
            LOGGER.error("错误的Bot端地址，请检查配置文件中WS_Bot_Port项端口是否输入正确！");
        }
    }


    /**
     * 将全部不在服务器群中的人的白名单撤销
     */
    public static void kickAllDeadMember(){
        LOGGER.info("准备清理退群人员白名");

        File[] QQList = QQ_DATA.listFiles();
        long mainServerGroup = PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group");
        long examServerGroup = PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group");

        if(QQList != null){
            for(File playerData : QQList){
                if(!playerData.isDirectory()){
                    if(playerData.getName().contains(".yml")){
                        long playerQQNum = NumberConvertor.getLong(playerData.getName().replace(".yml", ""));
                        // 通过数据库文件名转换对应到玩家QQ号
                        if(playerQQNum != 0){
                            Group serverGroup = Bot.getGroup(mainServerGroup);
                            Group examGroup = Bot.getGroup(examServerGroup);
                            if(serverGroup != null && examGroup != null){
                                if(serverGroup.get(playerQQNum) == null && examGroup.get(playerQQNum) == null){
                                    ErrorPacket afterRemoveErr = YamlDatabase.INSTANCE.clearUserData(playerQQNum);
                                    if(afterRemoveErr.getError() == BasicError.NONE){
                                        LOGGER.warn(playerQQNum + "已不在群中，其个人信息已被移除");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LOGGER.info("清理退群人员白名工作完成！");
    }
}
