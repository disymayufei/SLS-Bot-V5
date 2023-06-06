package cn.disy920.slbot.bot.command;


import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.bot.event.MessageEvent;
import cn.disy920.slbot.utils.container.Pair;

public abstract class AbstractCommandBuilder<T extends MessageEvent> {
    public abstract void register();

    public abstract Pair<Class<T>, ? extends StatelessCommand<T>> build();
}
