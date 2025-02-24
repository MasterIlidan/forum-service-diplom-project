package ru.students.forumservicediplomproject.exeption;

public class HashAlreadyRegisteredExeption extends RuntimeException {
    public HashAlreadyRegisteredExeption(String message, Throwable causedBy) {
        super(message, causedBy);
    }
    public HashAlreadyRegisteredExeption(String message) {
        super(message);
    }
}
