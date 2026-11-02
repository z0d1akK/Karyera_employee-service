package com.zodiakk.employeeservice.config;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    @Value("${app.kafka.topics.employee-account-events}")
    private String employeeAccountEventsTopic;

    @Value("${app.kafka.topics.employee-events}")
    private String employeeEventsTopic;

    @Bean
    public NewTopic userEventsTopic() {
        return buildTopic(userEventsTopic);
    }

    @Bean
    public NewTopic userEventsDltTopic() {
        return buildTopic(userEventsTopic + KafkaEventConstants.DLT_SUFFIX);
    }

    @Bean
    public NewTopic employeeAccountEventsTopic() {
        return buildTopic(employeeAccountEventsTopic);
    }

    @Bean
    public NewTopic employeeAccountEventsDltTopic() {
        return buildTopic(employeeAccountEventsTopic + KafkaEventConstants.DLT_SUFFIX);
    }

    @Bean
    public NewTopic employeeEventsTopic() {
        return buildTopic(employeeEventsTopic);
    }

    @Bean
    public NewTopic employeeEventsDltTopic() {
        return buildTopic(employeeEventsTopic + KafkaEventConstants.DLT_SUFFIX);
    }

    private NewTopic buildTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
