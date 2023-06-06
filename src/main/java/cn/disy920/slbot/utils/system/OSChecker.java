package cn.disy920.slbot.utils.system;

import static cn.disy920.slbot.utils.system.OSChecker.OSType.*;

public class OSChecker {

    public enum OSType{
        LINUX,
        WINDOWS,
        MACOS,
        OTHER;
    }


    public static boolean isLinux(){
        return System.getProperty("os.name").startsWith("Linux");
    }

    public static boolean isWindows(){
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isMacOS(){
        return System.getProperty("os.name").startsWith("Mac");
    }

    public static OSType checkOS(){
        String type = System.getProperty("os.name");

        if (type.startsWith("Linux")){
            return LINUX;
        }
        else if (type.startsWith("Windows")){
            return WINDOWS;
        }
        else if (type.startsWith("Mac")){
            return MACOS;
        }
        else {
            return OTHER;
        }
    }
}
