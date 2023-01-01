package ru.itis.prytkovd.inject.util;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static ru.itis.prytkovd.inject.util.ReflectionUtils.annotatedConstructorsOf;
import static ru.itis.prytkovd.inject.util.ReflectionUtils.annotatedFieldsOf;

public final class InjectionUtils {
    public static Annotation qualifierIn(Annotation[] annotations) {
        List<Annotation> qualifiers = Arrays.stream(annotations)
            .filter(annotation -> annotation.annotationType().isAnnotationPresent(Qualifier.class))
            .toList();

        if (qualifiers.size() > 1) {
            throw new RuntimeException("multiple qualifier annotations found");
        }

        if (qualifiers.size() == 1) {
            return qualifiers.get(0);
        }

        return null;
    }

    public static Annotation scopeIn(Annotation[] annotations) {
        List<Annotation> scopes = Arrays.stream(annotations)
            .filter(annotation -> annotation.annotationType().isAnnotationPresent(Scope.class))
            .toList();

        if (scopes.size() > 1) {
            throw new RuntimeException("multiple scope annotations found");
        }

        if (scopes.size() == 1) {
            return scopes.get(0);
        }

        return null;
    }

    public static <T> List<Field> injectableFieldsOf(Class<T> type) {
        return annotatedFieldsOf(type, Inject.class);
    }

    public static <T> Constructor<T> injectableConstructorOf(Class<T> type) {
        Constructor<T> explicitInjectableConstructor = explicitInjectableConstructorOf(type);

        if (explicitInjectableConstructor != null) {
            return explicitInjectableConstructor;
        }

        return implicitInjectableConstructorOf(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> explicitInjectableConstructorOf(Class<T> type) {
        List<Constructor<?>> explicitInjectableConstructors = annotatedConstructorsOf(type,
            Inject.class);

        if (explicitInjectableConstructors.size() > 1) {
            throw new RuntimeException(type + " has more than one explicit injectable constructors");
        }

        if (explicitInjectableConstructors.size() == 1) {
            return (Constructor<T>)explicitInjectableConstructors.get(0);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> implicitInjectableConstructorOf(Class<T> type) {
        Constructor<?>[] constructors = type.getConstructors();

        if (constructors.length == 1) {
            return (Constructor<T>)constructors[0];
        }

        return null;
    }
}
