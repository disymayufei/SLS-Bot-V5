package cn.disy920.slbot.utils;

import cn.disy920.slbot.bukkit.database.YamlDatabase;
import cn.disy920.slbot.utils.container.Pair;
import com.google.common.base.Charsets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UUIDTool {

    /**
     * 通过玩家ID计算其离线UUID的方法
     * @param playerID 玩家ID
     * @return 离线玩家的UUID
     */
    @NotNull
    public static UUID getOfflineUUID(String playerID){
        return UUID.nameUUIDFromBytes(( "OfflinePlayer:" + playerID ).getBytes(Charsets.UTF_8 ));
    }


    /**
     * 获得某玩家的UUID，同时判断该玩家ID是否安全（若该ID处于白名单中，则是安全的）
     * @param playerID 玩家ID
     * @return 一个Pair，第一个值为UUID，第二个值为是否安全
     */
    @NotNull
    public static Pair<UUID, Boolean> safeGetOfflineUUID(String playerID){
        boolean isSafe = YamlDatabase.INSTANCE.isInWhiteList(playerID);
        return new Pair<>(getOfflineUUID(playerID), isSafe);
    }

    /**
     * 获得某玩家的StarLight UUID，需要遵循以下规范：
     * 高64bit位为玩家的唯一识别码，如果从QQ绑定则必须为QQ号，否则需要生成一个8bit的唯一特征码
     * 而后32bit位被称为备用位，当高64为发生碰撞时，可以通过该位解除碰撞，否则以全0填充
     * 最后的低32bit位被称为编码位，用于区分同玩家名下的不同账号
     * @param highBits 高64bit位
     * @param backupBits 备用位，长度为32bit
     * @param code 编码位，长度为32bit
     * @return 专属于StarLight玩家的唯一UUID
     */
    @NotNull
    public static UUID getStarLightUUID(long highBits, int backupBits, int code) {
        long lowBits = ((long) backupBits << 4) + code;
        return new UUID(highBits, lowBits);
    }

    /**
     * 依照QQ号生成玩家的唯一StarLight UUID
     * @param QQNum 玩家的QQ号
     * @param slot 玩家的槽位号，通常大号为1，小号为2
     * @return 专属于StarLight玩家的唯一UUID
     */
    @NotNull
    public static UUID getStarLightUUID(long QQNum, int slot) {
        return getStarLightUUID(QQNum, 0, slot);
    }
}
