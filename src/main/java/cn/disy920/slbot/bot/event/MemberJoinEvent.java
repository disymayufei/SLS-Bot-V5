package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.Group;
import cn.disy920.slbot.bot.contact.GroupMember;

public class MemberJoinEvent extends AbstractEvent implements GroupMemberEvent {
    protected final Group group;
    protected final GroupMember member;

    public MemberJoinEvent(long timeStamp, long botID, Group group, GroupMember member) {
        super(timeStamp, botID);

        this.group = group;
        this.member = member;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public GroupMember getMember() {
        return this.member;
    }
}
