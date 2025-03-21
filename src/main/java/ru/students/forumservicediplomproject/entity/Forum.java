package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Forum{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long forumId;
    @NotNull
    private String forumName;
    private String description;
    @ManyToOne
    @NotNull
    private User createdBy;
    @NotNull
    private Timestamp creationDate;
    @Transient
    private long totalThreadsInForum;
    @Transient
    private long totalPostsInForum;
    @Transient
    private long totalMessagesInForum;
    @Transient
    private Message lastMessageInForum;
}
