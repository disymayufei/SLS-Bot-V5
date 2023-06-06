package cn.disy920.slbot.bot.message;

import org.jetbrains.annotations.NotNull;

public class QuoteReply implements Message{

    protected final Integer messageID;

    public QuoteReply(int messageID) {
        this.messageID = messageID;
    }

    public QuoteReply(MessageChain chain) {
        this.messageID = chain.getMessageID();
    }

    @Override
    @NotNull
    public String contentToString() {
        return "";
    }

    public Integer getMessageID() {
        return messageID;
    }
}
