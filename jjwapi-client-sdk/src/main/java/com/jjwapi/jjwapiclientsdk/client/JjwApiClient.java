package com.jjwapi.jjwapiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jjwapi.jjwapiclientsdk.model.User;
import com.jjwapi.jjwapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用第三方接口的客户端
 */
public class JjwApiClient {
    private static final String GATEWAY_URL = "http://localhost:8090";
    private String accessKey;
    private String secretKey;

    public JjwApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    // 通用的请求方法，处理请求头
    private HttpResponse sendRequest(String method, String url, String body) {
        Map<String, String> headers = getHeaderMap(body);
        HttpRequest request;
        switch (method.toUpperCase()) {
            case "GET":
                request = HttpRequest.get(url);
                break;
            case "POST":
                request = HttpRequest.post(url);
                break;
            default:
                throw new IllegalArgumentException("不支持的请求方法: " + method);
        }
        request.addHeaders(headers);
        if (body != null && !body.isEmpty()) {
            request.body(body);
        }
        return request.execute();
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

    // 使用GET方法从服务器获取名称信息
    public String getNameByGet() {
        try {
            String url = GATEWAY_URL + "/api/name/";
            HttpResponse response = sendRequest("GET", url, null);
            String result = response.body();
            System.out.println(result);
            return result;
        } catch (Exception e) {
            System.err.println("发起GET请求时出现异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 使用POST方法从服务器获取名称信息
    public String getNameByPost(@RequestParam String name) {
        try {
            String url = GATEWAY_URL + "/api/name/";
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", name);
            String body = HttpUtil.toParams(paramMap);
            HttpResponse response = sendRequest("POST", url, body);
            String result = response.body();
            System.out.println(result);
            return result;
        } catch (Exception e) {
            System.err.println("发起POST请求时出现异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 使用 POST 方法向服务器发送 User 对象，并获取服务器返回的结果
    public String getUserNameByPost(User user) {
        try {
            // 将 User 对象转换为 JSON 字符串
            String json = JSONUtil.toJsonStr(user);
            System.out.println("请求体 JSON: " + json);

            String url = GATEWAY_URL + "/api/name/user/";
            HttpResponse response = sendRequest("POST", url, json);

            // 打印响应状态码和响应体
            System.out.println("响应状态码: " + response.getStatus());
            String result = response.body();
            System.out.println("响应体: " + result);

            // 返回响应体
            return result;
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
            return null;
        }
    }
}