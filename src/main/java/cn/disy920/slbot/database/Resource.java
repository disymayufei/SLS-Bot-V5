package cn.disy920.slbot.database;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Resource {

    /**
     * 获取玩家发送“#存活确认”后机器人可用的消息
     * @return 包含所有可用消息字符串的列表
     */
    @NotNull List<String> getAliveMsg();


    /**
     * 获取查服时Bot给出的额外提示信息
     * @param type 额外提示信息的类型
     * @return 包含全部可用额外消息的列表，若不存在消息或获取失败，则返回空列表
     */
    @NotNull List<String> getExtraMsg(ExtraMsgType type);


    /**
     * 获取管理员帮助文档的方法
     * @return 管理员帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    @NotNull String getAdminHelpText();


    /**
     * 获取玩家帮助文档的方法
     * @return 玩家帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    @NotNull String getPlayerHelpText();


    /**
     * 获取玩家Q&A文档的方法
     * @return 玩家Q&A文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    @NotNull String getPlayerQAndAText();


    /**
     * 获取欢迎新成员信息的方法
     * @return 欢迎新成员的信息，如果获取失败则返回一个不包含任何字符的字符串
     */
    @NotNull String getWelcomeMsg();


    enum ExtraMsgType{
        EXTERNAL,  // 适用于任何群聊的外服查服消息
        INTERNAL_EXCLUSIVE,  // 适用于除内服交流群外，其他群的额外查服消息
        INTERNAL_UNIVERSAL  // 专用于内服交流群的查服额外消息
    }

}
