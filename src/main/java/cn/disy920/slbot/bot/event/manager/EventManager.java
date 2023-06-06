package cn.disy920.slbot.bot.event.manager;

import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.bot.event.GroupMemberEvent;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.bot.event.annotations.EventPriority;
import cn.disy920.slbot.bot.event.listener.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public final class EventManager {

    private final ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(1024);
    private final Map<EventPriority, Map<Listener, List<Method>>> listenerCache = new HashMap<>(6){{
        put(EventPriority.MONITOR, new HashMap<>(8));
        put(EventPriority.HIGHEST, new HashMap<>(8));
        put(EventPriority.HIGH, new HashMap<>(16));
        put(EventPriority.NORMAL, new HashMap<>(32));
        put(EventPriority.LOW, new HashMap<>(16));
        put(EventPriority.LOWEST, new HashMap<>(8));
    }};

    private final Thread eventHandlerThread;

    public EventManager() {
        this.eventHandlerThread = new Thread(() -> {
            while (true) {
                try {
                    Event event = eventQueue.take();

                    /* 依照优先级逐次执行事件 */
                    for (Map.Entry<Listener, List<Method>> monitor : listenerCache.get(EventPriority.MONITOR).entrySet()) {
                        callEvent(monitor, event, EventPriority.MONITOR);
                    }

                    for (Map.Entry<Listener, List<Method>> highest : listenerCache.get(EventPriority.HIGHEST).entrySet()) {
                        callEvent(highest, event, EventPriority.HIGHEST);
                    }

                    for (Map.Entry<Listener, List<Method>> high : listenerCache.get(EventPriority.HIGH).entrySet()) {
                        callEvent(high, event, EventPriority.HIGH);
                    }

                    for (Map.Entry<Listener, List<Method>> normal : listenerCache.get(EventPriority.NORMAL).entrySet()) {
                        callEvent(normal, event, EventPriority.NORMAL);
                    }

                    for (Map.Entry<Listener, List<Method>> low : listenerCache.get(EventPriority.LOW).entrySet()) {
                        callEvent(low, event, EventPriority.LOW);
                    }

                    for (Map.Entry<Listener, List<Method>> lowest : listenerCache.get(EventPriority.LOWEST).entrySet()) {
                        callEvent(lowest, event, EventPriority.LOWEST);
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "Event-Handler");

        this.eventHandlerThread.start();
    }

    private void callEvent(Map.Entry<Listener, List<Method>> entry, Event event, EventPriority priority) {
        for (Method method : entry.getValue()) {
            if (entry.getKey().getListenerType() == event.getClass() || method.getParameterTypes()[0] == event.getClass()) {
                try {
                    method.setAccessible(true);
                    method.invoke(entry.getKey(), event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    // listenerCache.get(priority).remove(entry.getKey());  // 不能边遍历边删
                }
            }
        }
    }

    public boolean addEvent(@NotNull Event event) {
        return eventQueue.offer(event);
    }

    public void register(@NotNull Listener listener) {
        for (Method method : listener.getClass().getMethods()) {
            BotEventHandler handler = method.getAnnotation(BotEventHandler.class);
            if (handler != null && method.getParameterTypes().length == 1) {
                EventPriority priority = handler.priority();

                Map<Listener, List<Method>> methodMap = listenerCache.get(priority);

                if (methodMap.containsKey(listener)) {
                    methodMap.get(listener).add(method);
                }
                else {
                    List<Method> methodList = new ArrayList<>();
                    methodList.add(method);

                    methodMap.put(listener, methodList);
                }
            }
        }
    }

    public void close() {
        if (this.eventHandlerThread != null && !this.eventHandlerThread.isInterrupted()) {
            this.eventQueue.clear();
            this.eventHandlerThread.interrupt();
        }
    }
}
