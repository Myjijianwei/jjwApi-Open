package com.jjwapi.jjwapiclientsdk.utils;


import org.springframework.util.DigestUtils;

/**
 * 签名认证
 */
public class SignUtils {


    private static final String SALT = "jjw";

    /**
     *
     * @param body
     * @param secretKey
     * @return
     */
    public static String genSign(String body, String secretKey) {
        if (body == null) {
            body = "";
        }
        // 拼接盐值、请求体和 secretKey
        String content = SALT + body + secretKey;
        // 使用 DigestUtils.md5DigestAsHex 方法进行 MD5 加密
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}
