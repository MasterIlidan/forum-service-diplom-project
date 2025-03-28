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
    private Status postStatus;
    @NotNull
    private Timestamp creationDate;
    @OneToOne(orphanRemoval = true, mappedBy = "post", cascade = CascadeType.PERSIST)
    private Peers peers;
    @NotNull
    private long countOfDownloads;

    @Transient
    private long totalMessagesInPost;
    @Transient
    private Message lastMessageInPost;

    @PrePersist
    public void prePersist() {
        if (peers == null) {
            this.peers = new Peers();
            peers.setPost(this);
        }
    }

    public enum Status {
        NEW, APPROVED, ACTIVE, INACTIVE, ARCHIVE
    }
}


