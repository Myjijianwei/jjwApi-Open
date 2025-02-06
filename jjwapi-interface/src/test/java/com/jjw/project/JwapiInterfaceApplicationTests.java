package com.jjw.project;

import com.jjwapi.jjwapiclientsdk.client.JjwApiClient;
import com.jjwapi.jjwapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class JjwapiInterfaceApplicationTests {
	@Resource
	private JjwApiClient jjwApiClient;


	@Test
	void contextLoads() {
		String result = jjwApiClient.getNameByGet("jjw");
		System.out.println(result);
		User user = new User();
		user.setUsername("jjw");
		jjwApiClient.getUserNameByPost(user);
	}

}
