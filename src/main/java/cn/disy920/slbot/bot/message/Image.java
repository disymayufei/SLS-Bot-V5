package cn.disy920.slbot.bot.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

public class Image implements Message {

    @Nullable
    protected final String imageBase64;

    public Image(InputStream stream) {
        String base64;
        try {
            base64 = Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (IOException e) {
            base64 = null;
        }
        this.imageBase64 = base64;
    }

    public Image(byte[] bytes) {
        this.imageBase64 = Base64.getEncoder().encodeToString(bytes);
    }

    public Image(File file) {
        String base64;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            base64 = null;
        }
        this.imageBase64 = base64;
    }

    @Nullable
    public String convertToUrl() {
        return this.imageBase64 != null ? "base64://" + this.imageBase64 : null;
    }

    @Nullable
    public String getImageBase64() {
        return this.imageBase64;
    }

    @Override
    @NotNull
    public String contentToString() {
        return "[图片]";
    }

}
