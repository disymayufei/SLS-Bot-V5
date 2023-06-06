package cn.disy920.slbot.error;

/**
 * Bot错误类型的接口，抛出的任何错误信息均基于此
 */
public interface Error {

    String getInfo();

    int getCode();

}
