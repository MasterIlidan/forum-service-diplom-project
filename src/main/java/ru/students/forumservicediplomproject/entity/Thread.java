package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Thread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long threadId;
    @NotNull
    private String threadName;
    @ManyToOne
    private Forum forumId;
    @ManyToOne
    private User createdBy;
    @NotNull
    private Timestamp creationDate;
    @OneToOne
    private Message lastMessage;
}
