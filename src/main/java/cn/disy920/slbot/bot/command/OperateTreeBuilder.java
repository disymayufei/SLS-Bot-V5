package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.event.Event;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.utils.container.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OperateTreeBuilder<T extends Event> extends AbstractOperateBuilder<T> {

    private final LinkedList<OperateNode<T>> nodeList = new LinkedList<>();
    private final LinkedList<Requirement<T>> requirementList = new LinkedList<>();

    @NotNull
    private StatelessOperate<T> failedOperate = (event) -> {};

    private final Class<T> eventClass;

    public OperateTreeBuilder(@NotNull Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    public OperateTreeBuilder<T> addOperate(OperateNode<T> node) {
        nodeList.addLast(node);
        return this;
    }

    public OperateNode<T> createNode() {
        return new OperateNode<>();
    }

    public OperateTreeBuilder<T> require(Requirement<T> requirement) {
        this.requirementList.add(requirement);
        return this;
    }

    public OperateTreeBuilder<T> executesWhenFailed(@NotNull StatelessOperate<T> function) {
        this.failedOperate = function;
        return this;
    }

    @Override
    public void register() {
        Pair<Class<T>, StatelessOperate<T>> listener = this.build();
        Bot.getEventManager().register(listener.getSecond());
    }

    @Override
    public Pair<Class<T>, StatelessOperate<T>> build() {
        StatelessOperate<T> result = new StatelessOperate<>() {
            @Override
            public Class<T> getListenerType() {
                return eventClass;
            }

            @Override
            @BotEventHandler
            public void run(T event) {
                for (Requirement<T> requirement : requirementList) {
                    if (!requirement.judge(event)) {
                        failedOperate.run(event);
                        return;
                    }
                }

                for (OperateNode<T> node : nodeList) {
                    for (Requirement<T> requirement : node.requirementList) {
                        if (!requirement.judge(event)) {
                            node.failedOperate.run(event);
                            return;
                        }
                    }

                    boolean success = node.operate.run(event);
                    if (!success){
                        node.failedOperate.run(event);
                    }
                    return;
                }
            }
        };

        return new Pair<>(eventClass, result);
    }

    public static class OperateNode<T extends Event> {
        private final List<Requirement<T>> requirementList;

        @NotNull
        private Operate<T> operate = (event) -> Operate.SUCCESS;

        @NotNull
        private StatelessOperate<T> failedOperate = (event) -> {};

        public OperateNode() {
            this.requirementList = new LinkedList<>();
        }

        public OperateNode<T> require(@NotNull Requirement<T> requirement) {
            this.requirementList.add(requirement);
            return this;
        }

        public OperateNode<T> executes(@NotNull Operate<T> function) {
            this.operate = function;
            return this;
        }

        public OperateNode<T> executesWhenFailed(@NotNull StatelessOperate<T> function) {
            this.failedOperate = function;
            return this;
        }
    }
}
