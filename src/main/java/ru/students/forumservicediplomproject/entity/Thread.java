package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Thread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int threadId;
    @NotNull
    private String threadName;
    @OneToOne
    private Forum forumId;
    @ManyToOne
    private User createdBy;
}
