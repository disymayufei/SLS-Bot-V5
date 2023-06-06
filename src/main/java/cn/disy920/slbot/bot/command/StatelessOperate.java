package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.bot.event.listener.Listener;

@FunctionalInterface
public interface StatelessOperate<T extends Event> extends Listener {
    void run(T event);
}
