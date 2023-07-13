package com.library.events.consumer.config;

import com.library.events.consumer.service.FailureService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaTemplate kafkaTemplate;
    @Value("${topics.retry}")
    String retryTopic;
    @Value("${topics.dlt}")
    String dltTopic;

    private final FailureService failureService;


    public DeadLetterPublishingRecoverer publishingRecoverer() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (r, e) -> {
                    if (e.getCause() instanceof Exception) {
                        log.error("Publishing to retry topic {}", retryTopic);
                        return new TopicPartition(retryTopic, r.partition());
                    } else {
                        log.error("Publishing to dead letter topic {}", dltTopic);
                        return new TopicPartition(dltTopic, r.partition());
                    }
                });
        return recoverer;
    }

    public ConsumerRecordRecoverer consumerRecordRecoverer() {
        ConsumerRecordRecoverer consumerRecordRecoverer = ((consumerRecord, exception) -> {
            log.error("Exception in publishingRecoverer: {} {}", exception.getLocalizedMessage(), exception);
            ConsumerRecord<Integer, String> record = (ConsumerRecord<Integer, String>) consumerRecord;
            if (exception.getCause() instanceof RecoverableDataAccessException) {
                //recovery logic
                log.info("Inside recovery");
                failureService.saveFailedRecord(record, exception, "RETRY");
            } else {
                //non-recovery logic
                log.info("Inside Dead");
                failureService.saveFailedRecord(record, exception, "DEAD");
            }
        });
        return consumerRecordRecoverer;
    }


    public DefaultErrorHandler defaultErrorHandler() {

        //        List<Class<IllegalArgumentException>> exceptionsToIgnore = List.of(
        //                IllegalArgumentException.class
        //        );

        List<Class<IllegalArgumentException>> exceptionsToIgnore = new ArrayList<>();

        FixedBackOff fixedBackOff = new FixedBackOff(1000l, 2);

        ExponentialBackOffWithMaxRetries exponentialBackOffWithMaxRetries = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOffWithMaxRetries.setInitialInterval(1_000l);
        exponentialBackOffWithMaxRetries.setMultiplier(2.0);
        exponentialBackOffWithMaxRetries.setMaxInterval(2_000l);

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
                consumerRecordRecoverer(),
                //                publishingRecoverer(),
                //                                fixedBackOff
                exponentialBackOffWithMaxRetries
        );
        exceptionsToIgnore.forEach(defaultErrorHandler::addNotRetryableExceptions);
        defaultErrorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            log.error("Failed record in retry Listener, exception: {}, delivery attempt: {}", ex.getLocalizedMessage(),
                    deliveryAttempt);
        }));
        return defaultErrorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        //                concurrent consumer
        factory.setConcurrency(3);
        // to acknowledge messages manually
        //                factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.setCommonErrorHandler(defaultErrorHandler());

        return factory;
    }
}
