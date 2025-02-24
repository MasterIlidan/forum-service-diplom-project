package ru.students.forumservicediplomproject.exeption;

public class TrackerServiceException extends RuntimeException {
    public TrackerServiceException(String message, Throwable causedBy){
        super(message, causedBy);
    }
    public TrackerServiceException(String message) {
        super(message);
    }
}
