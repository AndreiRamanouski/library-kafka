package com.library.events.consumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
@Builder
@Data
public class FailureRecord {

    @Id
    @GeneratedValue
    private Integer id;
    private String topic;
    private Integer topicKey;
    private String topicValue;
    private Integer partition;
    private Long topicOffset;
    @Column(columnDefinition="TEXT")
    private String exceptionMessage;
    private String status;


}
