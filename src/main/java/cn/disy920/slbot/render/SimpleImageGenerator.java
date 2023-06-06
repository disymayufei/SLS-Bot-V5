package cn.disy920.slbot.render;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 用于将字符串转化为显示字符串内容的图片
 */
public class SimpleImageGenerator {

    /**
     * 生成一张图片，其为白底黑子，写有字符串的内容
     * @param text 待展示的字符串
     * @param fontSize 图片中文字的尺寸（单位：像素）
     * @return 一个包含PNG格式图片的输入流
     * @throws IOException 向内存输出流中写入发生错误时会抛出本异常
     */
    public static InputStream gen(String text, int fontSize) throws IOException {
        Font defaultFont = new Font("宋体", Font.PLAIN, fontSize);

        String[] splitTextArray = text.split("\n");
        for(int i = 0; i < splitTextArray.length; i++){
            if(splitTextArray[i].contains("\r")){
                splitTextArray[i] = splitTextArray[i].replace("\r", "");
            }
        }

        String longestText = findLongestString(splitTextArray);

        Rectangle2D r2d = defaultFont.getStringBounds(longestText, new FontRenderContext(AffineTransform.getScaleInstance(1, 1), false, false));

        int unitHeight = (int) Math.floor(r2d.getHeight());
        int width = (int) Math.round(r2d.getWidth()) + 20;
        int height = (unitHeight + 5) * splitTextArray.length + 80;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = image.createGraphics();
        graph.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        graph.fillRect(0, 0, width, height); // 画出矩形区域，以便于在矩形区域内写入文字
        graph.setColor(Color.BLACK);// 再换成黑色，以便于写入文字

        graph.setFont(defaultFont);

        int initialY = fontSize + 20;

        for(String str : splitTextArray){
            graph.drawString(str, 5, initialY);
            initialY += (unitHeight + 5);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", bos);

        return new ByteArrayInputStream(bos.toByteArray());
    }

    @NotNull
    private static String findLongestString(String[] strArray){
        if(strArray.length == 0){
            return "";
        }

        String result = strArray[0];

        for (String str : strArray){
            if(str != null){
                if(str.length() > result.length()){
                    result = str;
                }
            }
        }

        return result == null ? "" : result;
    }
}
