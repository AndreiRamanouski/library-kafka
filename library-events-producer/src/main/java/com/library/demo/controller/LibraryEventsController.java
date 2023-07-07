package com.library.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.library.demo.domain.LibraryEvent;
import com.library.demo.domain.LibraryEventType;
import com.library.demo.producer.LibraryEventsProducerImpl;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("api/v1")
public class LibraryEventsController {

    private final LibraryEventsProducerImpl libraryEventsProducer;

    @PostMapping("library-event")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {
        log.info("Handle post library endpoint. Event type {}", libraryEvent.libraryEventType());

        //produce message

        libraryEventsProducer.sendLibraryEvent_approach1(libraryEvent);

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("library-event")
    public ResponseEntity<?> updateEvent(@RequestBody @Valid LibraryEvent libraryEvent)
            throws JsonProcessingException {
        log.info("Handle post library endpoint. Event type {}", libraryEvent.libraryEventType());

        ResponseEntity<String> body = performValidation(libraryEvent);
        if (body != null) {
            return body;
        }

        //produce message

        libraryEventsProducer.sendLibraryEvent_approach1(libraryEvent);

        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

    private static ResponseEntity<String> performValidation(LibraryEvent libraryEvent) {
        if (Objects.isNull(libraryEvent.libraryEventId())) {
            return ResponseEntity.badRequest().body("Library Event Id must not be null");
        }
        if (!libraryEvent.libraryEventType().equals(LibraryEventType.UPDATE)) {
            return ResponseEntity.badRequest().body("Only UPDATE event type is supported");
        }
        return null;
    }

}
