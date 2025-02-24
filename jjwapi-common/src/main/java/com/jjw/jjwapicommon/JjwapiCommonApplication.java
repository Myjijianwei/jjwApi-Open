package com.jjw.jjwapicommon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class JjwapiCommonApplication {

	public static void main(String[] args) {
		SpringApplication.run(JjwapiCommonApplication.class, args);
	}

}
