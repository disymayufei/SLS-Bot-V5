package cn.disy920.slbot.error;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorPacket {
    private final Error error;
    private final String extraMsg;

    public static final ErrorPacket NONE = ErrorPacket.create(BasicError.NONE, null);
    public static final ErrorPacket ALREADY_USED = ErrorPacket.create(BasicError.ALREADY_USED, null);

    private ErrorPacket(Error errorType, String extraMsg){
        this.error = errorType;
        this.extraMsg = extraMsg;
    }

    public static ErrorPacket create(Error errorType, @Nullable String extraMsg){
        if (errorType == null){
            throw new NullPointerException("Error type cannot be null!");
        }

        return new ErrorPacket(errorType, extraMsg);
    }

    @NotNull
    public Error getError(){
        return this.error;
    }

    @Nullable
    public String getExtraMsg(){
        return this.extraMsg;
    }

    @Override
    public String toString(){
        return error.getInfo() + (extraMsg == null ? "" : "，" + extraMsg);
    }
}
