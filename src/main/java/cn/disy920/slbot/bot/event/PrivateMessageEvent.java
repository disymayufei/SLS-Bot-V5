package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.User;
import cn.disy920.slbot.bot.message.MessageChain;

public class PrivateMessageEvent extends AbstractEvent implements MessageEvent {

    protected final User sender;
    protected final MessageChain messageChain;

    public PrivateMessageEvent(long timeStamp, long botID, User sender, MessageChain messages) {
        super(timeStamp, botID);

        this.sender = sender;
        this.messageChain = messages;
    }

    @Override
    public User getSender() {
        return sender;
    }

    @Override
    public String getSenderName() {
        return sender.getNick();
    }

    @Override
    public MessageChain getMessage() {
        return this.messageChain;
    }
}
