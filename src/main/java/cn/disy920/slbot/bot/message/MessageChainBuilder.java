package cn.disy920.slbot.bot.message;

public class MessageChainBuilder {

    private final MessageChain chain;

    public MessageChainBuilder() {
        this.chain = new MessageChain();
    }

    public MessageChainBuilder append(String text) {
        this.chain.add(new PlainMessage(text));
        return this;
    }

    public MessageChainBuilder append(Message mes) {
        this.chain.add(mes);
        return this;
    }

    public MessageChainBuilder append(Object o) {
        return append(String.valueOf(o));
    }

    public MessageChain build() {
        return this.chain;
    }
}
