package com.library.demo.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class AutoCreateConfig {

    @Value("${spring.kafka.topic}")
    private String topicName;

    @Bean
    @Profile(value = "local")
    public NewTopic libraryEvents() {
        log.info("servers: {}", servers);
        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .replicas(3)
                .build();
    }

    private List<String> servers;

}
