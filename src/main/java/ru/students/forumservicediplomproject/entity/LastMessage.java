package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LastMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    private Forum forum;
    @OneToOne
    private Thread thread;
    @OneToOne
    private Post post;
    @ManyToOne
    private Message lastMessage;
}
