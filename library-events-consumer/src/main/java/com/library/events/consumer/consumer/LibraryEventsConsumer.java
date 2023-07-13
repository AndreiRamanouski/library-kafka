package com.library.events.consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.library.events.consumer.service.LibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LibraryEventsConsumer {

    private final LibraryEventService libraryEventService;

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "library-events-listener")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord: {}", consumerRecord);
        libraryEventService.processLibraryEvent(consumerRecord);
    }
}
