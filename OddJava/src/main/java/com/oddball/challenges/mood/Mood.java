package com.oddball.challenges.mood;

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
    name = "moods",
    indexes = {
        @Index(name = "idx_mood_date", columnList = "date"),
        @Index(name = "idx_mood_user", columnList = "userId"),
    }
)
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name="userId")
    private long userId;

    @Column(name = "date", columnDefinition = "date")
    private LocalDate date;

    @Column(name = "mood", length = 1)
    private int mood;

    @Column(name = "description")
    private String description;
}