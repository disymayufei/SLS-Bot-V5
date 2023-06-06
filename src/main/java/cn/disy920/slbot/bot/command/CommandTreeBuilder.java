package cn.disy920.slbot.bot.command;

import cn.disy920.slbot.bot.Bot;
import cn.disy920.slbot.bot.event.GroupMessageEvent;
import cn.disy920.slbot.bot.event.MessageEvent;
import cn.disy920.slbot.bot.event.annotations.BotEventHandler;
import cn.disy920.slbot.utils.container.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class CommandTreeBuilder<T extends MessageEvent> extends AbstractCommandBuilder<T> {

    private final LinkedList<CommandNode<T>> nodeList = new LinkedList<>();
    private final LinkedList<Requirement<T>> requirementList = new LinkedList<>();

    @NotNull
    private StatelessCommand<T> failedCommand = (event) -> {};

    private final Class<T> eventClass;

    public CommandTreeBuilder(@NotNull Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    public CommandTreeBuilder<T> addCommand(CommandNode<T> node) {
        nodeList.addLast(node);
        return this;
    }

    public CommandNode<T> createNode(@NotNull String command) {
        return new CommandNode<>(command);
    }

    public CommandTreeBuilder<T> require(Requirement<T> requirement) {
        this.requirementList.add(requirement);
        return this;
    }

    public CommandTreeBuilder<T> executeWhenFailed(@NotNull StatelessCommand<T> function) {
        this.failedCommand = function;
        return this;
    }

    @Override
    public void register() {
        Pair<Class<T>, StatelessCommand<T>> listener = this.build();
        if (listener.getSecond().getListenerType() == GroupMessageEvent.class) {
            Bot.getEventManager().register(listener.getSecond());
        }
        else {
            Bot.getEventManager().register(listener.getSecond());
        }
    }

    @Override
    public Pair<Class<T>, StatelessCommand<T>> build() {

        StatelessCommand<T> result = new StatelessCommand<>() {
            @Override
            public Class<T> getListenerType() {
                return eventClass;
            }

            @Override
            @BotEventHandler
            public void run(T event) {
                String plainMes = event.getMessage().contentToString();

                if (plainMes.startsWith("#") || plainMes.startsWith("＃")) {

                    boolean meetCondition = true;

                    for (Requirement<T> requirement : requirementList) {
                        if (!requirement.judge(event)) {
                            meetCondition = false;
                            break;
                        }
                    }

                    for (CommandNode<T> node : nodeList) {
                        if (!node.needArgs) {

                            String literal = plainMes.substring(1);

                            if (node.caseInsensitive) {
                                if (literal.equalsIgnoreCase(node.literal) || node.aliasList.stream().anyMatch(str -> str.equalsIgnoreCase(literal))) {
                                    if (!meetCondition) {
                                        failedCommand.run(event);
                                        return;
                                    }

                                    for (Requirement<T> requirement : node.requirementList) {
                                        if (!requirement.judge(event)) {
                                            node.failedCommand.run(event);
                                            return;
                                        }
                                    }

                                    boolean success = node.command.run(event);
                                    if (!success) {
                                        node.failedCommand.run(event);
                                    }
                                    return;
                                }
                            } else {
                                if (literal.equals(node.literal) || node.aliasList.contains(literal)) {
                                    if (!meetCondition) {
                                        failedCommand.run(event);
                                        return;
                                    }

                                    for (Requirement<T> requirement : node.requirementList) {
                                        if (!requirement.judge(event)) {
                                            node.failedCommand.run(event);
                                            return;
                                        }
                                    }

                                    boolean success = node.command.run(event);
                                    if (!success) {
                                        node.failedCommand.run(event);
                                    }
                                    return;
                                }
                            }
                        } else {

                            int endPosition = Math.min(node.literal.length() + 2, plainMes.length());

                            String literal = plainMes.substring(1, endPosition - 1);
                            String fullCommand = plainMes.substring(1, endPosition);

                            if (fullCommand.endsWith(":") || fullCommand.endsWith("：")) {
                                if (node.caseInsensitive) {
                                    if (literal.equalsIgnoreCase(node.literal) || node.aliasList.stream().anyMatch(str -> str.equalsIgnoreCase(literal))) {
                                        if (!meetCondition) {
                                            failedCommand.run(event);
                                            return;
                                        }

                                        for (Requirement<T> requirement : node.requirementList) {
                                            if (!requirement.judge(event)) {
                                                node.failedCommand.run(event);
                                                return;
                                            }
                                        }

                                        boolean success = node.command.run(event);
                                        if (!success) {
                                            node.failedCommand.run(event);
                                        }
                                        return;
                                    }
                                } else {
                                    if (literal.equals(node.literal) || node.aliasList.contains(literal)) {
                                        if (!meetCondition) {
                                            failedCommand.run(event);
                                            return;
                                        }

                                        for (Requirement<T> requirement : node.requirementList) {
                                            if (!requirement.judge(event)) {
                                                node.failedCommand.run(event);
                                                return;
                                            }
                                        }

                                        boolean success = node.command.run(event);
                                        if (!success) {
                                            node.failedCommand.run(event);
                                        }
                                        return;
                                    }
                                }
                            }


                        }
                    }
                }
            }
        };

        return new Pair<>(eventClass, result);
    }


    public static class CommandNode<T extends MessageEvent> {
        private final String literal;
        private final List<String> aliasList;
        private final List<Requirement<T>> requirementList;

        private boolean needArgs = false;
        private boolean caseInsensitive = false;

        @NotNull
        private Command<T> command = (event) -> Command.SUCCESS;

        @NotNull
        private StatelessCommand<T> failedCommand = (event) -> {};

        public CommandNode(@NotNull String literal) {
            this.literal = literal;
            this.aliasList = new LinkedList<>();
            this.requirementList = new LinkedList<>();
        }

        public CommandNode<T> alias(String alias) {
            aliasList.add(alias);
            return this;
        }

        public CommandNode<T> alias(List<String> aliasList) {
            this.aliasList.addAll(aliasList);
            return this;
        }

        public CommandNode<T> needArgs(boolean val) {
            this.needArgs = val;
            return this;
        }

        public CommandNode<T> setCaseInsensitive(boolean val) {
            this.caseInsensitive = val;
            return this;
        }

        public CommandNode<T> require(@NotNull Requirement<T> requirement) {
            this.requirementList.add(requirement);
            return this;
        }

        public CommandNode<T> executes(@NotNull Command<T> function) {
            this.command = function;
            return this;
        }

        public CommandNode<T> executesWhenFailed(@NotNull StatelessCommand<T> function) {
            this.failedCommand = function;
            return this;
        }
    }
}
