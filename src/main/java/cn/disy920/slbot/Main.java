package cn.disy920.slbot;

import cn.disy920.slbot.bot.bot.OneBot;
import cn.disy920.slbot.bot.websocket.WSClient;
import cn.disy920.slbot.bot.websocket.WSServer;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.bukkit.listeners.OnChat;
import cn.disy920.slbot.bukkit.listeners.OnPlayerJoin;
import cn.disy920.slbot.render.RichImageGenerator;
import cn.disy920.slbot.selenium.ChromeRichImageGenerator;
import cn.disy920.slbot.selenium.FirefoxRichImageGenerator;
import cn.disy920.slbot.timer.InternalServerTimer;
import cn.disy920.slbot.utils.logger.RecordLogger;
import cn.disy920.slbot.utils.logger.SimpleLogger;
import cn.disy920.slbot.utils.safe.PasswordGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;

public final class Main extends JavaPlugin {

    public static Main PLUGIN_INSTANCE;
    public static SimpleLogger LOGGER;
    public static RecordLogger RECORDER;

    public static WSServer WEBSOCKET_INSTANCE = null;  // Websocket服务端实例
    public static InternalServerTimer INTERNAL_SERVER_TIMER_INSTANCE = null;  // 内服消息计时器实例

    public static Thread WSS_THREAD = null;
    public static Thread INTERNAL_CHECK_TIMER_THREAD = null;

    public static final String pluginVersion = "5.0.0";

    public static String ACCESS_TOKEN = null;

    public static String serverName = "";
    public static RichImageGenerator richImageGenerator = null;

    @Override
    public void onEnable() {
        PLUGIN_INSTANCE = this;

        LOGGER = new SimpleLogger(this);
        RECORDER = new RecordLogger(this);

        /* 使用Yaml数据库 */
        YamlDatabase.INSTANCE.init();
        initConfig();

        OneBot.launchBot();

        /* 初始化复杂图片生成器 */
        if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Image_Msg")){
            switch (PLUGIN_INSTANCE.getConfig().getString("Image_Generator", "").toUpperCase()) {
                case "CHROME", "DEFAULT" -> richImageGenerator = new ChromeRichImageGenerator();
                case "FIREFOX" -> richImageGenerator = new FirefoxRichImageGenerator();
                default -> RECORDER.warn("您可能未设定图片生成器的类型，或类型错误，请检查配置文件中Image_Generator的值！");
            }
        }

        int listenedPort = this.getConfig().getInt("WS_Server_Port");
        if(listenedPort <= 0 || listenedPort >= 65536){
            RECORDER.warn("检测到不合法的WebSocket Server端口，已自动替换为默认端口！");
            listenedPort = 16123;
        }

        WEBSOCKET_INSTANCE = new WSServer(new InetSocketAddress("0.0.0.0", listenedPort));

        WSS_THREAD = new Thread(WEBSOCKET_INSTANCE);
        WSS_THREAD.start();

        INTERNAL_SERVER_TIMER_INSTANCE = new InternalServerTimer();
        INTERNAL_CHECK_TIMER_THREAD = new Thread(INTERNAL_SERVER_TIMER_INSTANCE);
        INTERNAL_CHECK_TIMER_THREAD.start();

        Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new OnChat(), this);

        LOGGER.info("SLS Bot插件已加载，版本V" + pluginVersion);
        LOGGER.info("Bot准备加载！");
    }

    @Override
    public void onDisable() {
        if (WSS_THREAD != null){
            WSS_THREAD.interrupt();
            WSS_THREAD = null;
        }

        if (INTERNAL_CHECK_TIMER_THREAD != null){
            INTERNAL_CHECK_TIMER_THREAD.interrupt();
            INTERNAL_CHECK_TIMER_THREAD = null;
            INTERNAL_SERVER_TIMER_INSTANCE = null;
        }

        if (OneBot.botThread != null) {
            OneBot.botThread.interrupt();
        }

        if (richImageGenerator != null) {
            richImageGenerator.destroy();
        }

        if (WSClient.botConnection != null) {
            WSClient.botConnection.close();
        }

        WEBSOCKET_INSTANCE = null;
        PLUGIN_INSTANCE = null;

        LOGGER.info("Bot关闭了哦，有缘再见！");
    }

    public void initConfig(){
        saveDefaultConfig();

        serverName = this.getConfig().getString("Server_Name", "").replace("&", "§");

        /* 初始化安全密钥 */
        if(this.getConfig().getString("Announce_Token") == null){
            ACCESS_TOKEN = PasswordGenerator.gen(32);
            this.getConfig().set("Announce_Token", ACCESS_TOKEN);
            saveConfig();
        }
        else {
            ACCESS_TOKEN = this.getConfig().getString("Announce_Token");
        }
    }


    /**
     * 向Disy咨询情感问题
     * @param problem 要咨询的情感问题（可以用任何语言描述）
     * @return 是否需要分手
     */
    public boolean consultLoveIssue(String problem){
        return true;
    }
}
