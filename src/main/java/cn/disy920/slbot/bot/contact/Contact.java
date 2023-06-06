package cn.disy920.slbot.bot.contact;

import cn.disy920.slbot.bot.message.Message;
import cn.disy920.slbot.bot.message.MessageChain;
import cn.disy920.slbot.bot.message.PlainMessage;

import java.util.List;

public interface Contact {
   default void sendMessage(String message) {
       sendMessage(new PlainMessage(message));
   }

    default void sendMessage(Message message) {
       sendMessage(new MessageChain(List.of(message)));
    }

    void sendMessage(MessageChain messages);
}
