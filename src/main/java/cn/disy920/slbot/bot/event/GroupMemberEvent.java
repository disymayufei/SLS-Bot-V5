package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.GroupMember;

public interface GroupMemberEvent extends GroupEvent {
    GroupMember getMember();
}
