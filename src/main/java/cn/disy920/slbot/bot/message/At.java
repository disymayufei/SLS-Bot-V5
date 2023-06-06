package cn.disy920.slbot.bot.message;

import cn.disy920.slbot.bot.contact.Member;
import org.jetbrains.annotations.NotNull;

public class At implements Message {
    private final long target;  // 被At的目标的QQ号，该值为-1时为At了全体成员

    public static final long ALL = -1;

    public At(long target) {
        this.target = target;
    }

    public long getTarget() {
        return this.target;
    }

    @Override
    @NotNull
    public String contentToString() {
        return "@" + target;
    }

    public String getDisplay() {
        return "@" + Member.getNameCardOrNick(target);
    }
}
