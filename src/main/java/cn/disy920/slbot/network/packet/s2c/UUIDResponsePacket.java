package cn.disy920.slbot.network.packet.s2c;

import cn.disy920.slbot.utils.GsonFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UUIDResponsePacket extends AbstractS2CPacket {
    private String ID;
    private String UUID;
    private String errorMsg;
    private boolean whitelisted;

    public UUIDResponsePacket(String ID, String UUID, boolean whitelisted, String errorMsg) {
        super("UUID");
        this.ID = ID;
        this.UUID = UUID;
        this.whitelisted = whitelisted;
        this.errorMsg = errorMsg;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    @Override
    @NotNull
    public String genStringPacket() {
        Map<String, Object> packet = new HashMap<>();
        packet.put("type", "UUID");

        Map<String, Object> params = new HashMap<>();
        params.put("errorMsg", errorMsg);
        params.put("whitelisted", whitelisted);
        params.put("ID", ID);
        params.put("UUID", UUID);

        packet.put("args", params);

        return GsonFactory.getGsonInstance().toJson(packet);
    }
}
