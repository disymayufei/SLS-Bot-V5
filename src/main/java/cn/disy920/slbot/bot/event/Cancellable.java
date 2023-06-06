package cn.disy920.slbot.bot.event;

public interface Cancellable {
    void cancel();

    boolean isCancelled();
}
