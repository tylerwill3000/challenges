package com.oddball.challenges.stress;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "stress",
    indexes = {
        @Index(name = "idx_stress_date", columnList = "date"),
        @Index(name = "idx_stress_user", columnList = "userId")
    }
)
public class Stress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "userId")
    private long userId;

    @Column(name="date", columnDefinition = "date")
    private LocalDate date;

    @Column(name="stress", length = 1)
    private int stress;
}