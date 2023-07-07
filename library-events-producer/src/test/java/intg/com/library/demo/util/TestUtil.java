package com.library.demo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.demo.domain.Book;
import com.library.demo.domain.LibraryEvent;
import com.library.demo.domain.LibraryEventType;

public class TestUtil {

    public static Book bookRecord(){
        return new Book(123, "Dilip", "Kafka Using Spring Boot");
    }

    public static Book bookRecordWithInvalidValues(){
        return new Book(null, "", "Kafka Using Spring Boot");
    }

    public static LibraryEvent libraryEvent(){
        return new LibraryEvent(null, LibraryEventType.NEW, bookRecord());
    }

    public static LibraryEvent libraryEventWithId(){
        return new LibraryEvent(123, LibraryEventType.NEW, bookRecord());
    }


    public static LibraryEvent libraryEventUpdate(){
        return new LibraryEvent(null, LibraryEventType.UPDATE, bookRecord());
    }

    public static LibraryEvent libraryEventWithInvalidBook(){
        return new LibraryEvent(null, LibraryEventType.NEW, bookRecordWithInvalidValues());
    }

    public static LibraryEvent parseLibraryEventRecord(ObjectMapper objectMapper, String json){
        try {
            return objectMapper.readValue(json, LibraryEvent.class);
        }catch (JsonProcessingException exception){
            throw new RuntimeException(exception);
        }
    }

}
