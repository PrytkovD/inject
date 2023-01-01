package ru.itis.prytkovd.inject;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class Key<T> {
    private final Class<T> type;
    private final Annotation qualifier;
    private final Class<? extends Annotation> qualifierType;

    private Key(Class<T> type, Annotation qualifier, Class<? extends Annotation> qualifierType) {
        this.type = type;
        this.qualifier = qualifier;
        this.qualifierType = qualifierType;
    }

    public static <T> Key<T> of(Class<T> type) {
        return new Key<>(
            Objects.requireNonNull(type, "type must not be null"),
            null,
            null
        );
    }

    public static <T> Key<T> of(Class<T> type, Class<? extends Annotation> qualifierType) {
        return new Key<>(
            Objects.requireNonNull(type, "type must not be null"),
            null,
            Objects.requireNonNull(qualifierType, "qualifierType must not be null")
        );
    }

    public static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        Objects.requireNonNull(type, "type must not be null");
        if (qualifier == null) {
            return new Key<>(type, null, null);
        } else {
            return new Key<>(
                type,
                qualifier,
                qualifier.annotationType()
            );
        }
    }

    public Class<T> type() {
        return type;
    }

    public Annotation qualifier() {
        return qualifier;
    }

    public Class<? extends Annotation> qualifierType() {
        return qualifierType;
    }

    @Override
    public String toString() {
        return "Key{" +
               "type=" + type +
               ", qualifier=" + qualifier +
               ", qualifierType=" + qualifierType +
               ", hashCode=" + hashCode() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Key<?> key = (Key<?>)o;
        return type.equals(key.type) && Objects.equals(qualifier, key.qualifier) && Objects.equals(qualifierType,
            key.qualifierType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, qualifier, qualifierType);
    }
}
