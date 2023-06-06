package cn.disy920.slbot.utils.logger;

import cn.disy920.slbot.Main;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordLogger extends SimpleLogger implements Logger {
    private final File logDir;
    private final File normalLogDir;
    private final File errLogDir;
    private final File debugLogDir;

    private final SimpleDateFormat STANDARD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat STANDARD_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RecordLogger(JavaPlugin plugin) {
        super(plugin);

        this.logDir = new File(plugin.getDataFolder(), "logs");

        if (!this.logDir.exists() || !this.logDir.isDirectory()) {
            this.logDir.mkdirs();
        }

        normalLogDir = new File(logDir, "log");
        if (!this.normalLogDir.exists() || !this.normalLogDir.isDirectory()) {
            this.normalLogDir.mkdirs();
        }

        errLogDir = new File(logDir, "errors");
        if (!this.errLogDir.exists() || !this.errLogDir.isDirectory()) {
            this.errLogDir.mkdirs();
        }

        debugLogDir = new File(logDir, "debug");
        if (!this.debugLogDir.exists() || !this.debugLogDir.isDirectory()) {
            this.debugLogDir.mkdirs();
        }
    }

    @Override
    public void info(Object msg) {
        super.info(msg);
        writeLog(String.valueOf(msg), Level.INFO);
    }

    @Override
    public void warn(Object msg) {
        super.warn(msg);
        writeLog(String.valueOf(msg), Level.WARN);
    }

    @Override
    public void error(Object msg) {
        super.error(msg);

        writeErr(String.valueOf(msg));
    }

    @Override
    public void debug(Object msg) {
        if(Main.PLUGIN_INSTANCE.getConfig().getBoolean("Debug_Mode")){
            this.logger.warning("[" + this.pluginName + " - DEBUG] " + msg);

            writeDebug(String.valueOf(msg));
        }
    }

    protected void writeLog(String log, Level level) {
        final String fileName = String.format("%s.log", STANDARD_DATE_FORMAT.format(new Date()));
        final File logFile = new File(normalLogDir, fileName);

        if (!logFile.exists() || !logFile.isFile()) {
            try {
                logFile.createNewFile();
            }
            catch (Exception e) {
                System.err.println("创建记录文件时出现错误，以下是错误的堆栈信息: ");
                e.printStackTrace();
            }
        }

        try {
            Files.writeString(logFile.toPath(), String.format("\n[%s - %s] %s", STANDARD_TIME_FORMAT.format(new Date()), level.getLevel(), log), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.err.println("写入记录文件时出现错误，以下是错误的堆栈信息: ");
            e.printStackTrace();
        }
    }

    protected void writeErr(String log) {
        final File logFile = new File(normalLogDir, "errors.log");

        if (!logFile.exists() || !logFile.isFile()) {
            try {
                logFile.createNewFile();
            }
            catch (Exception e) {
                System.err.println("创建错误记录文件时出现错误，以下是错误的堆栈信息: ");
                e.printStackTrace();
            }
        }

        try {
            Files.writeString(logFile.toPath(), String.format("\n[%s] %s", STANDARD_TIME_FORMAT.format(new Date()), log), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            this.logger.warning("写入错误记录文件时出现错误，以下是错误的堆栈信息: ");
            e.printStackTrace();
        }
    }

    protected void writeDebug(String log) {
        final String fileName = String.format("%s.log", STANDARD_DATE_FORMAT.format(new Date()));
        final File logFile = new File(debugLogDir, fileName);

        if (!logFile.exists() || !logFile.isFile()) {
            try {
                logFile.createNewFile();
            }
            catch (Exception e) {
                this.logger.warning("创建Debug文件时出现错误，以下是错误的堆栈信息: ");
                e.printStackTrace();
            }
        }

        try {
            Files.writeString(logFile.toPath(), String.format("\n[%s] %s", STANDARD_TIME_FORMAT.format(new Date()), log), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            this.logger.warning("写入Debug文件时出现错误，以下是错误的堆栈信息: ");
            e.printStackTrace();
        }
    }

    protected enum Level {
        INFO("INFO"),
        WARN("WARN");

        private final String level;

        Level(String str) {
            this.level = str;
        }

        public String getLevel() {
            return this.level;
        }
    }
}
