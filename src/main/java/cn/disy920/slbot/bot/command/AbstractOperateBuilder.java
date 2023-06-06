package cn.disy920.slbot.bot.command;


import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.utils.container.Pair;

public abstract class AbstractOperateBuilder<T extends Event> {
    public abstract void register();

    public abstract Pair<Class<T>, ? extends StatelessOperate<T>> build();
}
