package com.library.events.consumer.service;

import com.library.events.consumer.entity.FailureRecord;
import com.library.events.consumer.jpa.FailureRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FailureService {
    private final FailureRecordRepository failureRecordRepository;

    public void saveFailedRecord(ConsumerRecord<Integer,String> consumerRecord, Exception exception, String retry) {
        log.info("saveFailedRecord");
        FailureRecord failureRecord = new FailureRecord(null, consumerRecord.topic(), consumerRecord.key(),
                consumerRecord.value(), consumerRecord.partition(),
                consumerRecord.offset(), exception.getLocalizedMessage(), retry);
        failureRecordRepository.save(failureRecord);
        log.info("Saved failure record {}", failureRecord);

    }
}
