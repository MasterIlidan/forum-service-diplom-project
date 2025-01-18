package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
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
    @ManyToOne
    private User createdBy;
    @NotNull
    private Date creationDate;
}
