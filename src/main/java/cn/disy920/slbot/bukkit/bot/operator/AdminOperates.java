package cn.disy920.slbot.bukkit.bot.operator;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.command.Command;
import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.message.At;
import cn.disy920.slbot.bot.message.Image;
import cn.disy920.slbot.bot.message.Message;
import cn.disy920.slbot.bot.message.MessageChainBuilder;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.error.BasicError;
import cn.disy920.slbot.error.ErrorPacket;
import cn.disy920.slbot.render.SimpleImageGenerator;
import cn.disy920.slbot.utils.UUIDTool;
import cn.disy920.slbot.utils.container.Pair;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static cn.disy920.slbot.Main.LOGGER;
import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;
import static cn.disy920.slbot.bukkit.database.YamlDatabase.DATABASE;

public class AdminOperates {
    public static boolean addAdmin(GroupMessageEvent event){
        String msg = event.getMessage().contentToString();

        String playerQQNum = msg.substring(6);

        try {
            long QQNum = Long.parseLong(playerQQNum);
            ErrorPacket status = YamlDatabase.INSTANCE.addAdmin(QQNum);

            if (event.getGroup().contains(QQNum)) {
                if (status.getError() == BasicError.NONE) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("腐竹大大，已经添加")
                            .append(new At(QQNum))
                            .append("进管理行列啦！")
                            .build()
                    );
                } else {
                    event.getGroup().sendMessage(status.toString());
                }

            } else {
                event.getGroup().sendMessage(status.getError() == BasicError.NONE ? "腐竹大大，已经添加" + QQNum + "进管理行列啦！" : status.toString());
            }
        } catch (Exception e) {
            event.getGroup().sendMessage("腐竹腐竹，你添加的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean delAdmin(GroupMessageEvent event){
        String msg = event.getMessage().contentToString();

        String player_qq_num = msg.substring(6);

        try {
            long QQNum = Long.parseLong(player_qq_num);
            ErrorPacket status = YamlDatabase.INSTANCE.delAdmin(QQNum);

            if (event.getGroup().contains(QQNum)) {
                if (status.getError() == BasicError.NONE) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("腐竹大大，已经将")
                            .append(new At(QQNum))
                            .append("清出管理行列啦！")
                            .build()
                    );
                } else {
                    event.getGroup().sendMessage(status.toString());
                }

            } else {
                event.getGroup().sendMessage(status.getError() == BasicError.NONE ? "腐竹大大，已经将" + QQNum + "清出管理行列啦！" : status.toString());
            }
        } catch (Exception e) {
            event.getGroup().sendMessage("腐竹腐竹，你取消的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean passExam(GroupMessageEvent event){
        String msg = event.getMessage().contentToString();
        String playerQQNum = msg.substring(6);

        try {
            long QQNum = Long.parseLong(playerQQNum);
            if(QQNum < 9999){
                event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                return Command.SUCCESS;
            }

            ErrorPacket status = YamlDatabase.INSTANCE.passExam(QQNum);

            if (event.getGroup().contains(QQNum)) {
                if (status.getError() == BasicError.NONE) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("管理大大，已经同意")
                            .append(new At(QQNum))
                            .append("绑定白名单啦！")
                            .build()
                    );
                } else {
                    event.getGroup().sendMessage(status.toString());
                    return Command.SUCCESS;
                }

            } else {
                event.getGroup().sendMessage(status.getError() == BasicError.NONE ? "管理大大，已经同意" + QQNum + "绑定白名单啦！" : status.toString());
            }

            if (status.getError() == BasicError.NONE) {
                Group forReviewGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group"));
                if(forReviewGroup != null){
                    GroupMember newPlayer = forReviewGroup.get(QQNum);
                    if(newPlayer != null){
                        forReviewGroup.sendMessage(
                                new MessageChainBuilder()
                                        .append(new At(QQNum))
                                        .append(" 你已经通过我们的审核了，请注意我的私聊消息有加群二维码哦！")
                                        .build()
                        );

                        // TODO: 可变的审核提示语
                        newPlayer.sendMessage("[StarLight - Bot] 你已经通过审核了，快点击这个链接加入我们的服务器交流群吧：\"https://jq.qq.com/?_wv=1027&k=BwfeiIBU\"");
                        newPlayer.sendMessage(new Image(SimpleImageGenerator.gen("[StarLight - Bot] 你已经通过审核了，如果你无法扫码，那就试试手动在浏览器里输入这个链接加群吧：\"https://jq.qq.com/?_wv=1027&k=BwfeiIBU\"", 30)));

                        if(PLUGIN_INSTANCE.getConfig().getBoolean("Send_Pic_While_Invite")){
                            File picFile = new File(DATABASE, "HelpFile/invite.png");
                            if(!picFile.exists() || picFile.isDirectory()){
                                LOGGER.warn("没有入群图片可以发送哦，记得将要邀请的图片命名为invite.png后放在配置文件夹中DataBase/HelpFile文件下，或在配置文件中关闭发送图片的功能");
                                return Command.SUCCESS;
                            }

                            newPlayer.sendMessage(new Image(picFile));
                        }
                    }
                    else {
                        Group review_group = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Review_Group"));
                        if(review_group != null){
                            review_group.sendMessage(new MessageChainBuilder()
                                    .append("滴滴，我尝试帮你们邀请玩家：")
                                    .append(String.valueOf(QQNum))
                                    .append("，但发送邀请消息失败了诶！")
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean silentPassExam(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        String playerQQNum = msg.substring(6);

        try {
            long QQNum = Long.parseLong(playerQQNum);
            if(QQNum < 9999){
                event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                return Command.SUCCESS;
            }
            ErrorPacket status = YamlDatabase.INSTANCE.passExam(QQNum);

            if (event.getGroup().contains(QQNum)) {
                if (status.getError() == BasicError.NONE) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("管理大大，已经同意")
                            .append(new At(QQNum))
                            .append("绑定白名单啦！")
                            .build()
                    );
                } else {
                    event.getGroup().sendMessage(status.toString());
                }

            } else {
                event.getGroup().sendMessage(status.getError() == BasicError.NONE ? "管理大大，已经同意" + QQNum + "绑定白名单啦！" : status.toString());
            }
        } catch (Exception e) {
            event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean denyExam(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        String[] textArray = msg.substring(6).split(" ");

        if(textArray.length == 0){
            event.getGroup().sendMessage("管理管理，你输入的格式不太对哦！正确格式：#拒绝审核 <QQ号> [原因]");
        }

        String playerQQNum = textArray[0];
        String reason = null;

        if(textArray.length >= 2){
            reason = textArray[1];
        }
        try {
            long QQNum = Long.parseLong(playerQQNum);
            if (QQNum < 9999) {
                event.getGroup().sendMessage("管理大大！你拒绝的真的是个QQ号嘛？");
                return Command.SUCCESS;
            }

            Group forReviewGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group"));
            if(forReviewGroup != null) {
                GroupMember newPlayer = forReviewGroup.get(QQNum);
                if (newPlayer != null) {
                    forReviewGroup.sendMessage(
                            new MessageChainBuilder()
                                    .append(new At(QQNum))
                                    .append(" 很抱歉，您的审核未通过")
                                    .append(reason != null ? ("，原因：" + reason) : "")
                                    .append("，若无异议稍后就可以退群了诶")
                                    .build()
                    );

                    YamlDatabase.INSTANCE.denyExam(QQNum);

                    newPlayer.setNameCard("[审核未通过] " + newPlayer.getNick());

                    event.getGroup().sendMessage("管理管理，我已经拒绝掉这个玩家的审核了哦！");
                }
                else {
                    event.getGroup().sendMessage("玩家" + QQNum + "已经不在审核群了诶...");
                }
            }
            else {
                event.getGroup().sendMessage("机器人好像还不在审核群诶！快来先把机器人拉入审核群吧！");
            }
        }
        catch (Exception e){
            event.getGroup().sendMessage("管理大大！你拒绝的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean checkDenyList(GroupMessageEvent event) {
        List<Long> denyList = YamlDatabase.INSTANCE.getDenyList();

        if(denyList.isEmpty()){
            event.getGroup().sendMessage("目前群里没有审核被拒绝的玩家了诶");
        }
        else {
            Group forReviewGroup = Bot.getGroup(PLUGIN_INSTANCE.getConfig().getLong("Exam_Server_Group"));
            MessageChainBuilder resultMsg = new MessageChainBuilder().append("当前审核群内还有以下玩家审核被拒绝诶：");

            if(forReviewGroup != null){
                for(long QQNum : denyList){
                    if(forReviewGroup.get(QQNum) != null){
                        resultMsg.append("\n- ");
                        resultMsg.append(String.valueOf(QQNum));
                    }
                    else {
                        YamlDatabase.INSTANCE.delDenyList(QQNum);
                    }
                }

                event.getGroup().sendMessage(resultMsg.build());
            }
            else {
                event.getGroup().sendMessage("机器人好像还不在审核群诶！快来先把机器人拉入审核群吧！");
            }
        }

        return Command.SUCCESS;
    }


    public static boolean withdrawExam(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        String playerQQNum = msg.substring(6);

        try {
            long qqNum = Long.parseLong(playerQQNum);
            ErrorPacket status = YamlDatabase.INSTANCE.withdrawExam(qqNum);

            if (event.getGroup().contains(qqNum)) {
                if (status.getError() == BasicError.NONE) {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("管理大大，已经撤销")
                            .append(new At(qqNum))
                            .append("绑定白名单的权利啦！")
                            .build()
                    );
                } else {
                    event.getGroup().sendMessage(status.toString());
                }

            } else {
                event.getGroup().sendMessage(status.getError() == BasicError.NONE ? "管理大大，已经撤销" + qqNum + "绑定白名单的权利啦！" : status.toString());
            }
        } catch (Exception e) {
            event.getGroup().sendMessage("管理管理，你撤销的真的是个QQ号嘛？");
        }

        return Command.SUCCESS;
    }


    public static boolean calcUUID(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        String playerID = msg.substring(10);

        Pair<UUID, Boolean> uuidPair = UUIDTool.safeGetOfflineUUID(playerID);

        event.getGroup().sendMessage(new MessageChainBuilder()
                .append(new At(event.getSender().getID()))
                .append(" 玩家")
                .append(playerID)
                .append("的离线UUID为：\n")
                .append(uuidPair.getFirst().toString())
                .build()
        );

        if(!uuidPair.getSecond()){
            event.getGroup().sendMessage("请注意，该玩家尚未绑定本服的白名单，请务必再次检查ID输入是否正确！如您确认正确，请忽略本警告。");
        }

        return Command.SUCCESS;
    }


    public static boolean findQQByID(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        long senderID = event.getSender().getID();

        String playerID = msg.substring(6);
        String QQID = YamlDatabase.INSTANCE.findQQByID(playerID);
        long qq_number;

        if(QQID != null){
            try {
                qq_number = Long.parseLong(QQID);
                if(event.getGroup().contains(qq_number)){
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" ID：")
                            .append(playerID)
                            .append("绑定的QQ号为：")
                            .append(QQID)
                            .append("，对应群员为：")
                            .append(new At(qq_number))
                            .build()
                    );

                    return Command.SUCCESS;
                }
            }
            catch (Exception ignored){}
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new At(senderID))
                    .append(" ID：")
                    .append(playerID)
                    .append("绑定的QQ号为：")
                    .append(QQID)
                    .build()
            );
        }
        else {
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append(new At(senderID))
                    .append(" ID：")
                    .append(playerID)
                    .append("还没有人绑定过诶！")
                    .build()
            );
        }

        return Command.SUCCESS;
    }


    public static boolean findIDsByQQ(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        long senderID = event.getSender().getID();

        long playerQQ = -1;

        for(Message m : event.getMessage()){
            if(m instanceof At){
                playerQQ = ((At) m).getTarget();
                break;
            }
        }

        if(playerQQ == -1){
            try {
                playerQQ = Long.parseLong(msg.substring(6));
            }
            catch (Exception e){
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 管理管理，你查询的真的是一个QQ号嘛？")
                        .build()
                );

                return Command.SUCCESS;
            }
        }

        List<String> playerBindID = YamlDatabase.INSTANCE.checkBindIDWithOwner(playerQQ);

        if(playerBindID.size() > 0){
            MessageChainBuilder mcb = new MessageChainBuilder();
            mcb.append("玩家：").append(new At(playerQQ)).append("绑定的ID有：");
            for(String id : playerBindID){
                mcb.append("\n").append("- ").append(id);
            }

            event.getGroup().sendMessage(mcb.build());

            return Command.SUCCESS;
        }

        event.getGroup().sendMessage(new MessageChainBuilder()
                .append("玩家：")
                .append(new At(playerQQ))
                .append("没有绑定过任何ID哦！")
                .build()
        );

        return Command.SUCCESS;
    }
}
