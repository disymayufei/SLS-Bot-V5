package cn.disy920.slbot.bot.message;

import org.jetbrains.annotations.NotNull;

public interface Message {

    /**
     * 将任何消息转化为字符串的形式
     * 无法被直接转化为字符串的消息（如图片，动画表情等）将依照腾讯的格式进行显示（如：[图片]， [动画表情])
     * @return 被转化为字符串的消息
     */
    @NotNull
    String contentToString();
}
