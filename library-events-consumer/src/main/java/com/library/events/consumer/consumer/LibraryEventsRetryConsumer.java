package com.library.events.consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.events.consumer.entity.LibraryEvent;
import com.library.events.consumer.service.LibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsRetryConsumer {

    private final LibraryEventService libraryEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${topics.retry}", autoStartup = "${retryListener.startup:true}", groupId = "library-events-retry-listener")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

        log.info("Performing retry fo ConsumerRecord: {}",
                objectMapper.readValue(consumerRecord.value(), LibraryEvent.class));
        consumerRecord.headers().forEach((header) -> {
            log.error("Key: {}, Value: {}", header.key(), new String(header.value()));
        });
        try {
            libraryEventService.processLibraryEvent(consumerRecord);
        } catch (Exception e) {
            log.error("Error retrying library event {}", e.getLocalizedMessage());
        }
    }
}
