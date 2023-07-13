package com.library.events.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.events.consumer.entity.Book;
import com.library.events.consumer.entity.LibraryEvent;
import com.library.events.consumer.jpa.BookRepository;
import com.library.events.consumer.jpa.LibraryEventsRepository;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventService {

    private final LibraryEventsRepository libraryEventsRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Process library event {}", consumerRecord.value());
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);

        switch (libraryEvent.getLibraryEventType()){
            case NEW -> save(libraryEvent);
            case UPDATE -> validateAndSave(libraryEvent);
            default -> log.info("Invalid LibraryEventType");
        }
    }

    private void validateAndSave(LibraryEvent libraryEvent) {
        if (Objects.isNull(libraryEvent.getLibraryEventId())) {
            throw new IllegalArgumentException("LibraryEventId cannot be null for update");
        }
        LibraryEvent libraryEventById = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).orElseThrow(
                () -> new IllegalArgumentException("Not a valid LibraryEventId")
        );
        log.info("Validation is successful for library event {}", libraryEvent);
        save(libraryEvent);

    }

    private void save(LibraryEvent libraryEvent) {
        log.info("Save library event {}", libraryEvent);
        libraryEvent.setBook(libraryEvent.getBook());
        libraryEventsRepository.save(libraryEvent);
        bookRepository.save(libraryEvent.getBook());
        log.info("Saved value {}", libraryEvent);
    }
}
