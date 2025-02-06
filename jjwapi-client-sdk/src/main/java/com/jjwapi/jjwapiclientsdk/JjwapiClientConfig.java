package com.jjwapi.jjwapiclientsdk;

import com.jjwapi.jjwapiclientsdk.client.JjwApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("jjwapi.client")
@Data
@ComponentScan
public class JjwapiClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public JjwApiClient jjwApiClient() {
        return new JjwApiClient(accessKey, secretKey);
    }
}
