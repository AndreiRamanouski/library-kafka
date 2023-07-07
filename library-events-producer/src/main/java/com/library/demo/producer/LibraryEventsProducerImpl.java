package com.library.demo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.demo.domain.LibraryEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsProducerImpl {

    @Value("${spring.kafka.topic}")
    private String topicName;

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent_approach1(LibraryEvent libraryEvent)
            throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        log.info("Sending key {} and value {} to the topic {}....", key, value, topicName);
        //async
        // block the first call
        // then return CompletableFuture every time. non-blocking calls
        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(topicName,
                libraryEvent.libraryEventId(), objectMapper.writeValueAsString(libraryEvent));
        return completableFuture.whenComplete((sendResult, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        });
    }


    public SendResult<Integer, String> sendLibraryEvent_approach2(LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        log.info("Sending key {} and value {} to the topic {}....", key, value, topicName);
        //sync
        // block the first call
        // block and wait until the message is sent. blocking calls. wait up until 3 seconds in this particular case
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topicName, libraryEvent.libraryEventId(),
                        objectMapper.writeValueAsString(libraryEvent))
                .get(3, TimeUnit.SECONDS);
        handleSuccess(key, value, sendResult);
        return sendResult;
    }

//
//    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent_approach3(LibraryEvent libraryEvent)
//            throws JsonProcessingException {
//        Integer key = libraryEvent.libraryEventId();
//        String value = objectMapper.writeValueAsString(libraryEvent);
//        log.info("Sending key {} and value {} to the topic {}....", key, value, topicName);
//
//        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);
//
//        //async
//        // block the first call
//        // then return CompletableFuture every time. non-blocking calls
//        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(producerRecord);
//        return completableFuture.whenComplete((sendResult, throwable) -> {
//            if (Objects.nonNull(throwable)) {
//                handleFailure(key, value, throwable);
//            } else {
//                handleSuccess(key, value, sendResult);
//            }
//        });
//    }

//    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
//        List<RecordHeader> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
//        return new ProducerRecord<>(topicName, null, key, value, recordHeaders);
//    }

    private void handleSuccess(Integer key, String value, SendResult sendResult) {
        log.info("Message was sent with key {} and value {}. Partition is {}", key, value,
                sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending message with key {} and value {}. Exception message is {}", key, value,
                throwable.getLocalizedMessage());
    }

}
