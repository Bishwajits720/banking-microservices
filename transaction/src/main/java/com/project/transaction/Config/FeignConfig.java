package com.project.transaction.Config;


import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.project.transaction.Config.ApiKeyFilter.API_KEY;
import static com.project.transaction.Config.ApiKeyFilter.KEY;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(KEY, API_KEY);
        };
    }
}