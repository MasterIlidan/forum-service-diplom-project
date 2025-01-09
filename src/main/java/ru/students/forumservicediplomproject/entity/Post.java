package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    //@NotNull TODO: обязательно вернуть. прикрепление торрента обязательно для создания поста, пока так
    private String hashInfo; //хеш торрента
    @ManyToOne
    private User createdBy;

}
