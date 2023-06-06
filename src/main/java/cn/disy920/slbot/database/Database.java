package cn.disy920.slbot.database;

import cn.disy920.slbot.error.ErrorPacket;

import java.util.List;

public interface Database {

    /**
     * 将指定ID加入白名单
     * @param playerID 玩家ID
     * @return 错误信息，如果没有错误则返回BasicBasicErrorPacket.None
     */
    ErrorPacket addWhiteList(String playerID);


    /**
     * 将指定ID移出白名单
     * @param playerID 玩家ID
     * @return 错误信息，如果没有错误则返回BasicErrorPacket.None
     */
    ErrorPacket delWhiteList(String playerID);


    /**
     * 将多个指定ID移出白名单
     * @param playerIDList 多个玩家ID的数组
     * @return 错误信息，如果没有错误则返回BasicErrorPacket.None
     */
    ErrorPacket delWhiteList(List<String> playerIDList);


    /**
     * 判断一个人是否在白名单中
     * @param playerID 该玩家的id
     * @return 一个boolean值，表示玩家是否在白名单中
     */
    boolean isInWhiteList(String playerID);


    /**
     * 获取白名单列表
     * @return 一个List，包含全部处于白名单的玩家ID
     */
    List<String> getWhiteList();


    /**
     * 判断一个人是否通过了审核
     * @param QQNumber 该玩家的QQ号
     * @return 一个boolean值，表示玩家是否通过了审核
     */
    boolean hadPassedTheExam(long QQNumber);


    /**
     * 使指定QQ通过审核
     * @param QQNumber 过审的玩家QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket passExam(long QQNumber);


    /**
     * 拒绝指定QQ的审核
     * @param QQNumber 玩家QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket denyExam(long QQNumber);


    /**
     * 撤销指定QQ的审核
     * @param QQNumber 玩家QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket withdrawExam(long QQNumber);


    /**
     * 设置指定QQ为Bot的管理员（职责为管理Bot，而非群管理）
     * @param QQNumber 待添加管理的QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket addAdmin(long QQNumber);


    /**
     * 撤去指定QQ的Bot管理权限
     * @param QQNumber 待添加管理的QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket delAdmin(long QQNumber);


    /**
     * 获取某QQ是否为Bot管理
     * @param QQNumber 待获取的QQ号
     * @return 一个boolean值，表示该用户是否为Bot管理
     */
    boolean isAdmin(long QQNumber);


    /**
     * 删除一个人的全部信息
     * @param QQNumber 要删除的用户的QQ号
     * @return 错误信息，若未发生错误则返回BasicErrorPacket.None
     */
    ErrorPacket clearUserData(long QQNumber);
}
