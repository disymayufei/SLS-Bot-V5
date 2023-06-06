package cn.disy920.slbot.selenium;

import org.openqa.selenium.OutputType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class ImageOutputType {

    public static final OutputType<InputStream> INPUT_STREAM = new OutputType<>() {
        @Override
        public InputStream convertFromBase64Png(String base64Png) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64Png));
        }

        @Override
        public InputStream convertFromPngBytes(byte[] png) {
            return new ByteArrayInputStream(png);
        }

        @Override
        public String toString() {
            return "OutputType.INPUT_STREAM";
        }
    };

    public static final OutputType<BufferedImage> BUFFERED_IMAGE = new OutputType<>() {
        @Override
        public BufferedImage convertFromBase64Png(String base64Png) {
            try {
                return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Png)));
            } catch (IOException e) {
                return null;  // 此错误不可能发生
            }
        }

        @Override
        public BufferedImage convertFromPngBytes(byte[] png) {
            try {
                return ImageIO.read(new ByteArrayInputStream(png));
            } catch (IOException e) {
                return null;  // 此错误不可能发生
            }
        }
    };
}
