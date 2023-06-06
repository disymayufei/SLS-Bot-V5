package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.event.MessageEvent;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.bot.event.listener.Listener;

@FunctionalInterface
public interface StatelessCommand<T extends MessageEvent> extends Listener {
    void run(T event);
}
