package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.event.Event;

@FunctionalInterface
public interface Operate<T extends Event> {

    boolean SUCCESS = true;
    boolean FAILED = false;

    boolean run(T event);
}
