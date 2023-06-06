package cn.disy920.slbot.bot.message;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlainMessage implements Message {
    private final StringBuffer buffer;

    public PlainMessage(String text) {
        this.buffer = new StringBuffer(text);
    }

    public PlainMessage(CharSequence text) {
        this.buffer = new StringBuffer();
    }

    public PlainMessage append(Object text) {
        this.buffer.append(text);
        return this;
    }

    public PlainMessage append(char c) {
        this.buffer.append(c);
        return this;
    }

    public PlainMessage append(int i) {
        this.buffer.append(i);
        return this;
    }

    public String getPlainText() {
        return contentToString();
    }


    @Override
    @NotNull
    public String contentToString() {
        return this.buffer.toString();
    }

    @Override
    public String toString(){
        return String.format("[PlainMessage: %s]", this.buffer);
    }

    @Override
    public boolean equals(Object anotherObj) {
        if (anotherObj == this) {
            return true;
        }
        return (anotherObj instanceof PlainMessage) && ((PlainMessage) anotherObj).buffer.toString().contentEquals(this.buffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buffer);
    }
}
