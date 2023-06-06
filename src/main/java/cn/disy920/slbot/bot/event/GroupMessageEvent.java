package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;
import cn.disy920.slbot.bot.message.MessageChain;

public class GroupMessageEvent extends AbstractEvent implements GroupEvent, MessageEvent {

    protected final GroupMember sender;
    protected final Group group;
    protected final MessageChain messages;

    public GroupMessageEvent(long timeStamp, long botID, GroupMember sender, MessageChain messages, Group group) {
        super(timeStamp, botID);

        this.sender = sender;
        this.group = group;
        this.messages = messages;
    }

    @Override
    public GroupMember getSender() {
        return this.sender;
    }

    @Override
    public String getSenderName() {
        return this.sender.getNick();
    }

    @Override
    public MessageChain getMessage() {
        return this.messages;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }
}
