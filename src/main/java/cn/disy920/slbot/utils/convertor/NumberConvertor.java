package cn.disy920.slbot.utils.convertor;

public class NumberConvertor {

    public static int getInt(String str){
        return getInt(str, 0);
    }

    public static int getInt(String str, int def){
        try{
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e){
            return def;
        }
    }

    public static long getLong(String str){
        return getLong(str, 0L);
    }

    public static long getLong(String str, long def){
        try{
            return Long.parseLong(str);
        }
        catch (NumberFormatException e){
            return def;
        }
    }
}
