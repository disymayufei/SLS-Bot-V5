import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.bot.event.listener.Listener;
import cn.disy920.slbot.bot.websocket.WSClient;

import java.net.URI;
import java.net.URISyntaxException;

public class Main implements Listener {
    public static void main(String[] args) throws URISyntaxException {
        WSClient.botConnection = new WSClient(new URI("ws://127.0.0.1:16124"));
        WSClient.botConnection.connect();

        Bot.getEventManager().register(new Main());

        while (true){}
    }

    @BotEventHandler
    public void onGroupMessageSend(GroupMessageEvent event) {
        String content = event.getMessage().contentToString();

        System.out.println(content);

        if (content.equals("你好")) {
            event.getGroup().sendMessage("你好啊！");
        }
    }
}
