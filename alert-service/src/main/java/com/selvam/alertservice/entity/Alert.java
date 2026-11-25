package com.selvam.alertservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "alert")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Alert {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long userId;
    private LocalDateTime createdAt;
    private boolean sent;
}
