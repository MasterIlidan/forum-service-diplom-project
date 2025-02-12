package ru.students.forumservicediplomproject.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostDto {

    @NotEmpty(message = "Название темы не должно быть пустым")
    private String title;
    @NotEmpty(message = "Описание темы не должно быть пустым")
    private String messageBody;
    @NotNull(message = "Нужно загрузить торрент файл")
    private MultipartFile torrentFile;
    private MultipartFile[] images;
}
