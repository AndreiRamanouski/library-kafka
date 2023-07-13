package com.library.events.consumer.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.library.events.consumer.entity.FailureRecord;
import com.library.events.consumer.jpa.FailureRecordRepository;
import com.library.events.consumer.service.LibraryEventService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final FailureRecordRepository failureRecordRepository;
    private final LibraryEventService libraryEventService;

    @Scheduled(timeUnit = TimeUnit.SECONDS, fixedRate = 10, initialDelay = 1)
    public void retryFailedRecords() throws JsonProcessingException {
        log.info("retryFailedRecords");

        List<FailureRecord> failureRecords = failureRecordRepository.findAllByStatus("DEAD");
        failureRecords.forEach(
                failureRecord -> {
                    log.info("Perform retry for {} entries", failureRecords.size());
                    try {
                        libraryEventService.processLibraryEvent(
                                new ConsumerRecord<>(failureRecord.getTopic(), failureRecord.getPartition(), failureRecord.getTopicOffset(),
                                        failureRecord.getTopicKey(), failureRecord.getTopicValue()));
                    } catch (Exception e) {
                       log.error("Exception {}", e.getLocalizedMessage());
                    }
                    failureRecord.setStatus("SUCCESS");
                    failureRecordRepository.save(failureRecord);
                }
        );
        log.info("Retry Completed");
    }


}
