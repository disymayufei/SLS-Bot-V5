package cn.disy920.slbot.bot.event;

/**
 * 所有机器人事件的基类
 * 任何机器人终节点事件必须继承自本类，并实现Event接口
 */
public abstract class AbstractEvent {

    protected final long timeStamp;
    protected final long botID;

    protected AbstractEvent(long timeStamp, long botID) {
        this.timeStamp = timeStamp;
        this.botID = botID;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public long getBotID() {
        return this.botID;
    }
}
