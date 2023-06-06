package cn.disy920.slbot.bot.event;

import cn.disy920.slbot.bot.contact.User;
import cn.disy920.slbot.bot.message.MessageChain;

public interface MessageEvent extends Event {
    User getSender();

    String getSenderName();

    MessageChain getMessage();
}
