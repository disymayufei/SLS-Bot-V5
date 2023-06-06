package cn.disy920.slbot.error;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum BasicError implements Error {

    NONE(null, 0),
    EMPTY_ID("输入的内容为空", 1),
    OTHER_ERROR("出现其他错误！", 2),

    /* 绑定ID部分 */
    ALREADY_BIND("这个ID已经被人绑定过了哦！", 100),
    SIMILAR_ID("这个ID存在与之相近的ID，请更换一个区别更大的吧！", 101),
    INVALID_ID("绑定的ID不合法，请依照服务器要求绑定ID", 102),
    FULL("绑定的ID数超出限制，无法再绑定ID", 110),
    ALREADY_USED("该槽位已被使用，请尝试换一个", 111),
    BIND_ERROR("绑定ID出错，请联系腐竹查看后台错误信息！", 190),

    /* 删除ID部分 */
    NOT_EXISTED("这个ID并没有被任何人绑定过哦！", 200),
    NOT_OWNER("这个ID不是你绑定的，因此无法删除！", 201),
    NEVER_BIND("你还没绑定过任何ID诶，这让我怎么删除呢？", 202),
    CANCEL_BIND_ERROR("取消绑定ID出错，请联系腐竹查看后台错误信息！", 290),
    CLEAR_ERROR("清除个人信息出错，请联系腐竹查看后台错误信息！", 291),

    /* 审核部分 */
    ALREADY_PASSED("该QQ已通过审核，该操作无法进行！", 300),
    NOT_PASS("该QQ还未通过审核，无法撤销审核！", 301),
    PASS_ERROR("通过审核出错啦！请联系腐竹查看后台错误信息！", 390),
    WITHDRAW_ERROR("取消审核出错啦！请联系腐竹查看后台错误信息！", 391),
    DENY_ERROR("拒绝审核出错啦！请联系腐竹查看后台错误信息", 392),

    /* 管理操作部分 */
    ALREADY_ADMIN("该QQ已是机器人管理，无需再次添加！", 400),
    NOT_ADMIN("该QQ不是机器人管理，无法取消", 401),
    ADD_ADMIN_ERROR("腐竹腐竹！添加管理员出错了，快去查看后台错误信息吧！", 490),
    REMOVE_ADMIN_ERROR("腐竹腐竹！移除管理员出错了，快去查看后台错误信息吧！", 491),
    KICK_MEMBER_ERROR("移出玩家的时候发生异常，快去查看后台错误信息吧！", 492);

    /* 黑白名单部分 */

    private final String info;
    private final int code;

    BasicError(String info, int code){
        this.info = info;
        this.code = code;
    }

    @Nullable
    public BasicError getErrorByCode(int code){
        for(BasicError error : BasicError.values()){
            if(error.code == code){
                return error;
            }
        }

        return null;
    }

    public int getCode(){
        return this.code;
    }

    @Override
    @Nullable
    public String getInfo(){
        return this.info;
    }

    @Override
    @NotNull
    public String toString() {
        String errorType = super.toString();
        return "%s (info:%s, code:%d)".formatted(errorType, this.info, this.code);
    }
}
