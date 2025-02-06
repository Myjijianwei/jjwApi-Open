package com.jjw.project.controller;

import com.jjwapi.jjwapiclientsdk.model.User;
import com.jjwapi.jjwapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 名称 API
 *
 * @author yupi
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/")
    public String getNameByGet(String name) {

        return "GET 你的名字是" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String signature = request.getHeader("signature");
        // 获取请求体内容
        String body = request.getHeader("body");
        // todo 实际情况应该是去数据库中查是否已分配给用户
        if (!accessKey.equals("jjw")) {
            System.out.println("============================"+accessKey);
            throw new RuntimeException("无权限");
        }
        if (Long.parseLong(nonce) > 10000) {
            throw new RuntimeException("无权限");
        }
        // todo 时间和当前时间不能超过 5 分钟
        try {
            Instant requestTime = Instant.ofEpochMilli(Long.parseLong(timestamp));
            Instant currentTime = Instant.now();
            if (ChronoUnit.MINUTES.between(requestTime, currentTime) > 5) {
                throw new RuntimeException("请求已过期");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("时间戳格式错误");
        }
        // todo 实际情况中是从数据库中查出 secretKey
        String secretKey = "abcdefg"; // 这里假设从数据库查出的 secretKey 是 "abcdefg"
        String serverSign = SignUtils.genSign(body, secretKey);
        if (!serverSign.equals(signature)) {
            throw new RuntimeException("无权限");
        }
        // todo 调用次数 + 1 invokeCount
        // 假设这里有一个 invokeCountService 来处理调用次数的增加
        // invokeCountService.increaseInvokeCount(accessKey);

        return "POST 用户名字是" + user.getUsername();
    }
}

