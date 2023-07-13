package com.library.events.consumer.jpa;

import com.library.events.consumer.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {

}
