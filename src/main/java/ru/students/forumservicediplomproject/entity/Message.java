package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private long messageId;
    @NotNull
    private String messageBody;
    @ManyToOne
    @NotNull
    private User messageBy;
    @ManyToOne
    @NotNull
    private Post postId;
    @OneToMany
    @NotNull
    private List<Resource> content;

}
