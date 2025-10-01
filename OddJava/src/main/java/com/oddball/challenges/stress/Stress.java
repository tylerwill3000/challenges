package com.oddball.challenges.stress;

import jakarta.persistence.*;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Date date;

    @Column(name="stress", length = 1)
    private int stress;

    Stress(long userId, Date date, int stress) {
        this.userId = userId;
        this.date = date;
        this.stress = stress;
    }
}