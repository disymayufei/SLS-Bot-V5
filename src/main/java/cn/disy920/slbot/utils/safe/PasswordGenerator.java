package cn.disy920.slbot.utils.safe;

import java.security.SecureRandom;

/**
 * 用于生成难预测的，安全的密钥
 */
public class PasswordGenerator {

    private static final String CONST_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_+*?!#@";

    /**
     * 生成指定长度的安全密码
     * @return 生成的密码
     */
    public static String gen(int length){
        SecureRandom random = new SecureRandom();
        StringBuilder resultBuilder = new StringBuilder();

        for(int i = 0; i < length; i++){
            resultBuilder.append(CONST_ALPHABET.charAt(random.nextInt(CONST_ALPHABET.length())));
        }

        return resultBuilder.toString();
    }
}
