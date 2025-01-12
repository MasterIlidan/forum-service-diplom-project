package ru.students.forumservicediplomproject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MessageDto {
    @NotNull(message = "Сообщение не должно быть пустым")
    private String messageBody;
}
