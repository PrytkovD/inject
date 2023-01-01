package ru.itis.prytkovd.inject.scopes;

import jakarta.inject.Provider;
import ru.itis.prytkovd.inject.Key;

import java.lang.annotation.Annotation;

public interface Scope {
    <T> Provider<T> scope(Key<T> key, Provider<T> unscoped);

    Class<? extends Annotation> annotationType();
}
