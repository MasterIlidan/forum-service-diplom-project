package ru.students.forumservicediplomproject.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super();
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(Exception causedBy) {
        super(causedBy);
    }
    public ResourceNotFoundException(String message, Exception causedBy) {
        super(message, causedBy);
    }
}
