package cn.disy920.slbot.utils.logger;

import cn.disy920.slbot.Main;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleLogger implements Logger {
    protected final java.util.logging.Logger logger;
    protected final String pluginName;

    public SimpleLogger(JavaPlugin plugin){
        this.logger = plugin.getLogger();
        this.pluginName = plugin.getName();
    }

    @Override
    public void info(Object msg) {
        this.logger.info("[" + this.pluginName + " - INFO] " + msg);
    }

    @Override
    public void warn(Object msg) {
        this.logger.warning("[" + this.pluginName + " - WARN] " + msg);
    }

    @Override
    public void error(Object msg) {
        this.logger.warning("[" + this.pluginName + " - ERR] " + msg);
    }

    @Override
    public void debug(Object msg) {
        if(Main.PLUGIN_INSTANCE.getConfig().getBoolean("Debug_Mode")){
            this.logger.warning("[" + this.pluginName + " - DEBUG] " + msg);
        }
    }
}
