package com.library.events.consumer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CascadeType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Book {

    @Id
    private Integer bookId;
    private String bookName;
    private String bookAuthor;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "library_event_id")
    @JsonIgnore
    private LibraryEvent libraryEvent;
}
