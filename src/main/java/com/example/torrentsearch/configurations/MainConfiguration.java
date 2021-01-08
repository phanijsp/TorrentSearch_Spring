package com.example.torrentsearch.configurations;

import com.example.torrentsearch.torrents.TorrentDataHolder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Configuration
public class MainConfiguration {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    @Bean
    public ArrayList<Class<?>> getSources() {
        return new ArrayList<>();
    }
}
