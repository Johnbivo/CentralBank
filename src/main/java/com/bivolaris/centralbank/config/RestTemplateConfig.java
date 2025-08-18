package com.bivolaris.centralbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;



@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "CentralBank-API/1.0");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
