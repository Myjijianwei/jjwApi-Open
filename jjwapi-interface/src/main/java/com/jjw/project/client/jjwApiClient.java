package com.jjw.project.client;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jjw.project.model.User;
import com.jjw.project.utils.SignUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用第三方接口的客户端
 */
public class jjwApiClient {
//    private String accessKey;
//    private String secretKey;
private static final String accessKey = UUID.randomUUID().toString();
    private static final String secretKey = UUID.randomUUID().toString();

//    public jjwApiClient(String accessKey, String secretKey) {
//        this.accessKey = accessKey;
//        this.secretKey = secretKey;
//    }

    // 使用GET方法从服务器获取名称信息
    public String getNameByGet(String name) {
        // 可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        // 将"name"参数添加到映射中
        paramMap.put("name", name);
        // 使用HttpUtil工具发起GET请求，并获取服务器返回的结果
        String result = HttpUtil.get("http://localhost:8123/api/name/", paramMap);
        // 打印服务器返回的结果
        System.out.println(result);
        // 返回服务器返回的结果
        return result;
    }

    // 使用POST方法从服务器获取名称信息
    public String getNameByPost(@RequestParam String name) {
        // 可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        // 使用HttpUtil工具发起POST请求，并获取服务器返回的结果
        String result = HttpUtil.post("http://localhost:8123/api/name/", paramMap);
        System.out.println(result);
        return result;
    }




    private Map<String, String> getHeaderMap(String body) {
        Map<String, String> map = new HashMap<>();
        map.put("accessKey", accessKey);
        map.put("nonce", String.valueOf(new Random().nextInt(10000)));
        map.put("body", body);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("signature", SignUtils.genSign(body, secretKey));
        return map;
    }


    // 使用 POST 方法向服务器发送 User 对象，并获取服务器返回的结果
    public String getUserNameByPost(User user) {
        // 将 User 对象转换为 JSON 字符串
        String json = JSONUtil.toJsonStr(user);
        // 使用 HttpRequest 工具发起 POST 请求，并获取服务器的响应
        HttpResponse httpResponse = HttpRequest.post("http://localhost:8123/api/name/user/")
                .addHeaders(getHeaderMap(json))
                .body(json) // 将 JSON 字符串设置为请求体
                .execute(); // 执行请求
        // 打印服务器返回的状态码
        System.out.println(httpResponse.getStatus());
        // 获取服务器返回的结果
        String result = httpResponse.body();
        // 打印服务器返回的结果
        System.out.println(result);
        // 返回服务器返回的结果
        return result;
    }



    // 使用POST方法向服务器发送User对象，并获取服务器返回的结果
//    public String getUserNameByPost(@RequestBody User user) {
//        // 将User对象转换为JSON字符串
//        String json = JSONUtil.toJsonStr(user);
//        // 使用HttpRequest工具发起POST请求，并获取服务器的响应
//        HttpResponse httpResponse = HttpRequest.post("http://localhost:8123/api/name/user/")
//                .addHeaders(getHeaderMap(json))
//                .body(json) // 将JSON字符串设置为请求体
//                .execute(); // 执行请求
//        // 打印服务器返回的状态码
//        System.out.println(httpResponse.getStatus());
//        // 获取服务器返回的结果
//        String result = httpResponse.body();
//        // 打印服务器返回的结果
//        System.out.println(result);
//        // 返回服务器返回的结果
//        return result;
//    }
}
