package ru.itis.prytkovd.inject.exceptions;

public class InjectionException extends RuntimeException {
    public InjectionException() {
        super();
    }

    public InjectionException(String message) {
        super(message);
    }
}
