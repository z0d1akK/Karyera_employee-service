package com.zodiakk.employeeservice.integration.auth.config;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.integration.auth.exception.EmployeeAccountLinkConflictException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> new TopicPartition(
                                record.topic() + KafkaEventConstants.DLT_SUFFIX,
                                record.partition()
                        )
                );

        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                EmployeeAccountLinkConflictException.class,
                JacksonException.class
        );

        return errorHandler;
    }
}
