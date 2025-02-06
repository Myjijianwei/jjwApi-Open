package com.jjw.project;

import com.jjw.project.client.jjwApiClient;
import com.jjw.project.model.User;

public class Main {
    public static void main(String[] args) {
        jjwApiClient yuApiClient = new jjwApiClient();
        String result1 = yuApiClient.getNameByGet("鱼皮");
        System.out.println();
        String result2 = yuApiClient.getNameByPost("鱼皮");
        System.out.println();

        User user = new User();
        user.setUsername("jjw");
        String result3 = yuApiClient.getUserNameByPost(user);
//        System.out.println(result1);
//        System.out.println(result2);
//        System.out.println(result3);
    }
}
