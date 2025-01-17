package ru.students.forumservicediplomproject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.students.forumservicediplomproject.entity.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDto {
    @NotNull(message = "Имя ветки не должно быть пустым")
    private String threadName;
    private long forumId;
    private User createdBy;
}
