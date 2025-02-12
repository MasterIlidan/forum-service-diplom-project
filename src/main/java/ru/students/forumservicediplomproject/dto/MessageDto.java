package ru.students.forumservicediplomproject.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    @NotEmpty(message = "Сообщение не должно быть пустым")
    private String messageBody;
}
