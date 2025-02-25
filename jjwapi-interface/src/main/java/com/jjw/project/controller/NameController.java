package com.jjw.project.controller;

import com.jjwapi.jjwapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;


/**
 * 名称 API
 *
 * @author yupi
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/")
    public String getNameByGet() {
        System.out.println(1);
        return "GET 你的名字是dog";
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user) {
        return "POST 用户名字是" + user.getUserName();
    }
}

