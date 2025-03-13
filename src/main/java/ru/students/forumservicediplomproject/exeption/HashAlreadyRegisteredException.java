package ru.students.forumservicediplomproject.exeption;

public class HashAlreadyRegisteredException extends RuntimeException {
    public HashAlreadyRegisteredException(String message, Throwable causedBy) {
        super(message, causedBy);
    }
    public HashAlreadyRegisteredException(String message) {
        super(message);
    }
}
