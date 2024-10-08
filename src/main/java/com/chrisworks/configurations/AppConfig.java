package com.chrisworks.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(AppConfigProperties.class)
public class AppConfig {

    @Bean("tinkHttpClient")
    public RestClient tinkHttpClient(RestClient.Builder restClientBuilder, AppConfigProperties appConfigProperties) {
        return restClientBuilder.baseUrl(appConfigProperties.getTinkConfig().getBaseUrl()).build();
    }

}
