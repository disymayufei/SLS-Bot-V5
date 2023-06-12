package cn.disy920.slbot.bukkit;

import cn.disy920.slbot.bukkit.database.YamlDatabase;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;

@SuppressWarnings("FieldCanBeLocal")
public class Guardian {

    private static final String DEFAULT_KICK_MSG = "§d§l[StarLight服务器]\n§6§l你好像还没有绑定过你的ID\n请加入我们的QQ群:§b535392227\n§6§l并在审核通过后，在交流群中群里发送§e“#绑定ID:你的ID”\n§l§6即可§a正常§6进入服务器了哦！";

    /* t出不在白名单的玩家的方法，被t出则返回true */
    public static boolean kickPlayerIfNotInWhitelist(Player player){
        String kickMsg = PLUGIN_INSTANCE.getConfig().getString("Kick_Message");
        if("".equals(kickMsg)){
            kickMsg = DEFAULT_KICK_MSG;  // 如果配置文件缺失，则替换为默认踢出信息
        }

        if(!YamlDatabase.INSTANCE.isInWhiteList(player.getName())){
            player.kickPlayer(kickMsg);
            return true;
        }

        return false;
    }

    /* 请注意，该方法仅对AuthMe有效 */
    public static void noPasswordLogin(Player player){
        new BukkitRunnable(){
            @Override
            public void run() {
                if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
                    AuthMeApi.getInstance().forceLogin(player);
                }
            }
        }.runTaskLater(PLUGIN_INSTANCE, 20);
    }
}
