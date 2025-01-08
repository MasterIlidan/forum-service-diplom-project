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
public class ForumDto {
    @NotNull(message = "Название форума не должно быть пустым")
    private String forumName;
    private String description;
    private User createdBy;


}
