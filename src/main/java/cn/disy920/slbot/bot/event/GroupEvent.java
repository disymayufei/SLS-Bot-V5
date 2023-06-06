package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.Group;

public interface GroupEvent extends Event {
    Group getGroup();
}
