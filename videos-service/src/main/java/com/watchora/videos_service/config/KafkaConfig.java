package com.watchora.videos_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Published when video is uploaded  to S3
    // Encoding service consume this
    @Bean
    public NewTopic videoUploadedTopic(){
        return TopicBuilder.name("Video.uploaded")
                .partitions(3).replicas(1).build();

    }

    // Published  when encoding is completed

    @Bean
    public NewTopic videoEncodedTopic (){
        return TopicBuilder.name("Video.uploaded")
                .partitions(3).replicas(1).build();

    }




}
