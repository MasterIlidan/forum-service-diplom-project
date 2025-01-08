package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    @Id
    @NotNull
    private long longId;
    @NotNull
    private String stringId;
}
