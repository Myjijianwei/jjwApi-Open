package com.jjw.project.service;

import com.jjw.jjwapicommon.model.entity.InterfaceInfo;
import com.jjw.jjwapicommon.service.InnerInterfaceInfoService;
import com.jjw.project.service.impl.InnerInterfaceInfoServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;

@SpringBootTest
class UserInterfaceInfoServiceTest {


    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @Test
    void getInterfaceInfo() {
        InterfaceInfo post = innerInterfaceInfoService.getInterfaceInfo("http://localhost:8123/api/name/user", "POST");
        System.out.println(post);

    }

    @Test
    public void invokeCount() {
        // 调用了userInterfaceInfoService的invokeCount方法，并传入两个参数(1L, 1L)
        boolean b = userInterfaceInfoService.invokeCount(1L, 1L);
        // 表示断言b的值为true，即测试用例期望invokeCount方法返回true
        Assertions.assertTrue(b);
    }
}