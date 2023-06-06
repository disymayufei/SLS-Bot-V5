package cn.disy920.slbot.bukkit.listeners;

import cn.disy920.slbot.utils.LuckRating;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;

public class OnChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        if (event.getMessage().equals("#幸运指数")){

            String playerName = event.getPlayer().getName();

            int rating = LuckRating.getRating(playerName);

            new BukkitRunnable(){
                @Override
                public void run() {
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendRawMessage("<占卜师> " + ChatColor.BOLD + ChatColor.GREEN + "%s今日的幸运指数是：%d，看起来他是个%s".formatted(playerName, rating, LuckRating.getString(rating))));
                }
            }.runTaskLater(PLUGIN_INSTANCE, 2);

        }
    }
}
