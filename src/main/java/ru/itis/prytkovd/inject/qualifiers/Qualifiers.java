package ru.itis.prytkovd.inject.qualifiers;

import jakarta.inject.Named;

import java.lang.annotation.Annotation;

public final class Qualifiers {
    public static Named named(String value) {
        return new NamedImpl(value);
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private record NamedImpl(String value) implements Named {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}
