package cn.disy920.slbot.bot.event.listener;

import cn.disy920.slbot.bot.event.Event;

/**
 * 所有的事件监听器都必须实现本接口
 */
public interface Listener {

    /**
     * 监听器监听的事件类型，若不覆写则会自动读取形参类型
     * Tips: 为应对Java那屎一样的泛型擦除而被迫做出的妥协
     * @return 监听器监听的事件类
     */
    default Class<? extends Event> getListenerType() {
        return null;
    }
}
