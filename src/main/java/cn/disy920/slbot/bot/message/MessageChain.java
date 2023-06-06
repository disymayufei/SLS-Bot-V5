package cn.disy920.slbot.bot.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MessageChain extends ArrayList<Message> {
    @Nullable
    private final Integer messageID;

    public MessageChain() {
        super();
        this.messageID = null;
    }

    public MessageChain(List<Message> list) {
        super(list);
        this.messageID = null;
    }

    public MessageChain(Integer id) {
        this.messageID = id;
    }

    public MessageChain(int id, List<Message> list) {
        super(list);
        this.messageID = id;
    }

    @Nullable
    public Integer getMessageID() {
        return this.messageID;
    }

    @NotNull
    public static MessageChain deserializeFromJson(JsonArray data, int id) {
        MessageChain chain = new MessageChain(id);
        return deserializeFronJsonArray(data, chain);
    }

    @NotNull
    public static MessageChain deserializeFromJson(JsonArray data) {
        MessageChain chain = new MessageChain();
        return deserializeFronJsonArray(data, chain);
    }

    @NotNull
    private static MessageChain deserializeFronJsonArray(JsonArray data, MessageChain chain) {
        if (data == null || data.isJsonNull() || data.size() == 0) {
            return chain;
        }

        for(JsonElement metaMessage : data) {
            if (metaMessage instanceof JsonObject messageObj) {
                String type = messageObj.get("type").getAsString();
                JsonObject body = messageObj.get("data").getAsJsonObject();

                switch (type) {
                    case "text" -> {
                        String text = body.get("text").getAsString();
                        if (text != null) {
                            chain.add(new PlainMessage(text));
                        }
                    }

                    case "at" -> {
                        long id = Long.parseLong(body.get("qq").getAsString());
                        chain.add(new At(id));
                    }

                    case "face" -> {
                        int id = Integer.parseInt(body.get("id").getAsString());
                        chain.add(new Face(id));
                    }

                    case "image" -> {
                        try {
                            URL imageUrl = new URL(body.get("url").getAsString());

                            URLConnection connection = imageUrl.openConnection();
                            connection.setConnectTimeout(5 * 1000);

                            InputStream connectionStream = connection.getInputStream();

                            if (connectionStream != null) {
                                Image image = new Image(connectionStream);

                                if (image.getImageBase64() != null) {
                                    chain.add(image);
                                }
                            }

                        } catch (IOException ignored) {}
                    }

                }
            }
        }

        return chain;
    }

    @NotNull
    public JsonArray serializeToJson() {
        JsonArray result = new JsonArray();

        for (Message message : this) {
            System.out.println(message);
            if (message instanceof PlainMessage plainText) {
                JsonObject textMap = new JsonObject();
                JsonObject node = new JsonObject();

                textMap.addProperty("text", plainText.getPlainText());

                node.addProperty("type", "text");
                node.add("data", textMap);

                result.add(node);
            } else if (message instanceof At at) {
                JsonObject atMap = new JsonObject();
                JsonObject node = new JsonObject();

                if (at.getTarget() == At.ALL) {
                    atMap.addProperty("qq", "all");
                } else {
                    atMap.addProperty("qq", Long.toString(at.getTarget()));
                }

                node.addProperty("type", "at");
                node.add("data", atMap);

                result.add(node);
            } else if (message instanceof Image image) {
                String imageUrl = image.convertToUrl();

                if (imageUrl != null) {
                    JsonObject imageMap = new JsonObject();
                    JsonObject node = new JsonObject();

                    imageMap.addProperty("file", imageUrl);

                    node.addProperty("type", "image");
                    node.add("data", imageMap);

                    result.add(node);
                }

            } else if (message instanceof Face face) {
                JsonObject faceMap = new JsonObject();
                JsonObject node = new JsonObject();

                faceMap.addProperty("id", face.getFaceID());

                node.addProperty("type", "face");
                node.add("data", faceMap);

                result.add(node);
            } else if (message instanceof QuoteReply reply) {
                JsonObject replyMap = new JsonObject();
                JsonObject node = new JsonObject();

                replyMap.addProperty("id", reply.getMessageID());

                node.addProperty("type", "reply");
                node.add("data", replyMap);

                result.add(node);
            }
        }

        return result;
    }

    public String contentToString() {
        StringBuilder builder = new StringBuilder(size());

        for (Message message : this) {
            builder.append(message.contentToString());
        }

        return builder.toString();
    }
}
