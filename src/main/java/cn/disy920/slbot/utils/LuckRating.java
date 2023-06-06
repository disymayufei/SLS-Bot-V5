package cn.disy920.slbot.utils;

import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.utils.convertor.NumberConvertor;

import java.util.Calendar;
import java.util.Random;

public class LuckRating {

    /**
     * 根据玩家名，获取某玩家当天的幸运值，并且尽量保证同一账号下的不同玩家当天幸运值一样
     * @param playerName 玩家名
     * @return 玩家当天的幸运值，范围为0~100（包含上下限）
     */
    public static int getRating(String playerName) {
        long QQNumber = NumberConvertor.getLong(YamlDatabase.INSTANCE.findQQByID(playerName), playerName.hashCode());

        return getRating(QQNumber);
    }


    /**
     * 根据QQ号，获取某玩家当天的幸运值，保证一天之内幸运值不会改变
     * @param QQNumber 玩家QQ号
     * @return 玩家当天的幸运值，范围为0~100（包含上下限）
     */
    public static int getRating(long QQNumber) {
        Calendar calendar = Calendar.getInstance();
        long seed = QQNumber + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE);

        return new Random(seed).nextInt(0, 101);
    }


    /**
     * 简易的转换器，把玩家的幸运值转化为一个对应的称号
     * @param rating 玩家的幸运值
     * @return 对应的称号
     */

    public static String getString(int rating) {
        // TODO: 转为配置文件设置不同等级对应的文字
        if (rating < 10){
            return "大倒霉蛋";
        }
        else if (rating <= 30){
            return "非酋";
        }
        else if (rating <= 50){
            return "小倒霉蛋";
        }
        else if (rating <= 70){
            return "欧洲人";
        }
        else if (rating <= 95){
            return "欧皇";
        }
        else {
            return "幸运之王";
        }
    }
}
