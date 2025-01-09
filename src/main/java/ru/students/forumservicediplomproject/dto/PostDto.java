package ru.students.forumservicediplomproject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto {

    @NotNull
    private String title;
    private String createBy;
    private String messageBody;
}
