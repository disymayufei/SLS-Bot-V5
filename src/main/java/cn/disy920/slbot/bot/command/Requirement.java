package cn.disy920.slbot.bot.command;

@FunctionalInterface
public interface Requirement<T> {
    boolean judge(T event);
}
