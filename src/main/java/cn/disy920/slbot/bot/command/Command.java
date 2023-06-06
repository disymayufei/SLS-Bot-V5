package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.event.MessageEvent;

@FunctionalInterface
public interface Command<T extends MessageEvent> extends Operate<T> {
}
