package cn.disy920.slbot.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface RichImageGenerator {

    void init();

    void destroy();

    void reload();

    /**
     * 通过HTML文件，渲染生成查服图片
     * @param currentNum 服务器当前人数
     * @param maxNum 服务器最大人数
     * @param imageHeight 生成图片的高度
     * @param text 仪表盘下方要显示的文字
     * @param theme 主题编号
     * @return 包含PNG格式图片的输入流，如果图片生成失败，则为null
     */
    @Nullable
    InputStream genQueryServerImage(int currentNum, int maxNum, int imageHeight, @NotNull String text, int theme);
}
