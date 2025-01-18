package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
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
    @OneToMany
    private List<Resource> content;
    @NotNull
    private Date creationDate;
}
