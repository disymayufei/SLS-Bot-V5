package cn.disy920.slbot.bukkit.listeners;

import cn.disy920.slbot.Main;
import cn.disy920.slbot.bukkit.Guardian;
import cn.disy920.slbot.bukkit.database.YamlDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Date;

public class OnPlayerJoin implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        String serverName = Main.serverName;

        if(!Guardian.kickPlayerIfNotInWhitelist(joiningPlayer)){  // 检测是否在白名中, 并t出不在白名的玩家
            String playerName = event.getPlayer().getName();

            if(YamlDatabase.INSTANCE.isOnlineMode(playerName)){
                Guardian.noPasswordLogin(playerName);
                event.getPlayer().sendMessage("[" + serverName + "§6小管家§r] §a§l正版验证成功，亲爱的正版玩家§6" + playerName + "§a欢迎回来！");
                return;
            }

            if(event.getPlayer().getAddress() != null) {
                if (YamlDatabase.INSTANCE.getNoPwdStamp(playerName) >= new Date().getTime()) {

                    if(event.getPlayer().getAddress().getHostString().equals("127.0.0.1")){
                        event.getPlayer().sendMessage("[" + serverName + "§6小管家§r] §c§l由于加速服务器属于外置，为保证您的安全，已自动关闭免密登录！");
                        return;
                    }

                    if ("".equals(YamlDatabase.INSTANCE.getPlayerIP(playerName))) {  // 数据库中没有IP信息
                        Guardian.noPasswordLogin(playerName);
                        YamlDatabase.INSTANCE.setPlayerIP(playerName, event.getPlayer().getAddress().getHostName());
                        event.getPlayer().sendMessage("[" + serverName + "§6小管家§r] §a§lIP已成功记录，后续同IP均可在时限内免密登录！");
                    } else {

                        if (event.getPlayer().getAddress().getHostName().equals(YamlDatabase.INSTANCE.getPlayerIP(playerName))){  // 比对IP一致
                            Guardian.noPasswordLogin(playerName);
                            event.getPlayer().sendMessage("[" + serverName + "§6小管家§r] §a§l免密登录成功，欢迎回来！");

                        }
                        else {
                            event.getPlayer().sendMessage("[" + serverName + "§6小管家§r] §c§l您的IP可能发生了变化，免密登录已失效，如有需要请重新在群内登记！");
                        }
                    }
                }
            }
        }
    }
}
