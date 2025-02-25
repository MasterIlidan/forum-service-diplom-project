package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
    private List<Resource> content = new ArrayList<>();
    @NotNull
    private Timestamp creationDate;
}
