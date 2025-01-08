package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private long postId;
    @NotNull
    private String title;
    @ManyToOne
    @NotNull
    private Thread thread;
    @NotNull
    private String hashInfo; //хеш торрента

}
