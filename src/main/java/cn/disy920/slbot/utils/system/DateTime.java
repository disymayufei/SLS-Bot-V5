package cn.disy920.slbot.utils.system;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 包含用于处理各类时间的方法（效率极低！追求效率者可自行实现！）
 */
public class DateTime {

    private static final Pattern DAY_REGEX = Pattern.compile("(天|日|day)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOUR_REGEX = Pattern.compile("(小时|时|hour)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTE_REGEX = Pattern.compile("(分钟|分|min|minute)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECOND_REGEX = Pattern.compile("(秒钟|秒|sec|second)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_TIME_REGEX = Pattern.compile("^[0-9dhms]+$");
    private static final DateFormat MONTH_AND_DAY_FORMAT = new SimpleDateFormat("MM-dd");

    /**
     * 判断当地是否为愚人节
     * @return 是否为愚人节
     */
    private static boolean isAprilFoolsDay(){
        return MONTH_AND_DAY_FORMAT.format(new Date()).equals("04-01");
    }


    /**
     * 将近似自然语言的时间差字符串转换为时间戳的差
     * @param time 近似自然语言的时间差字符串（如：3小时28分19秒）
     * @return 转换时间戳的差的结果
     */
    public static long parseTime(String time){

        /* 对于空字符串的快速处理 */
        if(time == null) return 0;
        if("".equals(time)) return 0;

        /* 对于纯数字的快速处理 */
        long timeNum;
        try {
            timeNum = Long.parseLong(time);

            if (timeNum > 2592000) {  // 最多容许30天
                return 2592000L;
            }
            else if (timeNum < 0) {
                return 0L;
            }

            return timeNum;
        }
        catch (NumberFormatException ignored){}

        time = DAY_REGEX.matcher(time).replaceFirst("d");
        time = HOUR_REGEX.matcher(time).replaceFirst("h");
        time = MINUTE_REGEX.matcher(time).replaceFirst("m");
        time = SECOND_REGEX.matcher(time).replaceFirst("s");

        /* 判断是否合法 */
        if(!VALID_TIME_REGEX.matcher(time).matches()){
            return 0;  // 不合法的内容直接返回一个0
        }

        /* 给时间赋初值 */
        long day = 0L;
        long hour = 0L;
        long min = 0L;
        long sec = 0L;

        if (time.contains("d")) {  // 转换天数
            try{
                day = Long.parseLong(time.substring(0, time.indexOf("d")));

                if(day > 30){
                    day = 30L;
                }
                else if(day < 0){
                    day = 0L;
                }
            }
            catch (NumberFormatException ignored){}
        }

        if (time.contains("h")) {  // 转换小时数
            try{
                hour = Long.parseLong(time.substring(time.indexOf("d") + 1, time.indexOf("h")));

                if(hour > 720){
                    hour = 720L;
                }
                else if(hour < 0){
                    hour = 0L;
                }
            }
            catch (NumberFormatException ignored){}
        }

        if (time.contains("m")) {  // 转换分钟数
            try{
                min = Long.parseLong(time.substring(time.indexOf("h") + 1, time.indexOf("m")));

                if(min > 1000){
                    min = 1000L;
                }
                else if(min < 0){
                    min = 0L;
                }
            }
            catch (NumberFormatException ignored){}
        }

        if (time.contains("s")) {  // 转换秒数
            try{
                sec = Long.parseLong(time.substring(time.indexOf("m") + 1, time.indexOf("s")));

                if(sec > 1000){
                    sec = 1000L;
                }
                else if(sec < 0){
                    sec = 0L;
                }
            }
            catch (NumberFormatException ignored){}
        }

        return (day * (24L * 60L * 60L)) + (hour * (60L * 60L)) + (min * 60L) + sec;
    }


    /**
     * 通过时间戳的差计算出结束时间
     * @param delta_time_stamp 时间戳的差
     * @return 以当前时间为起始的结束时间（格式：yyyy年MM月dd日 HH:mm:ss）
     */
    public static String convertToDate(long delta_time_stamp){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return sdf.format(new Date((new Date().getTime() + delta_time_stamp * 1000)));
    }
}
