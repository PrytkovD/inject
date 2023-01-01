package ru.itis.prytkovd.inject.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;

public final class ReflectionUtils {
    public static <T> List<Constructor<?>> annotatedConstructorsOf(Class<T> type, Class<? extends Annotation> annotationType) {
        return annotatedMembersIn(type.getConstructors(), annotationType);
    }

    public static <T> List<Method> annotatedMethodsOf(Class<T> type, Class<? extends Annotation> annotationType) {
        return annotatedMembersIn(type.getMethods(), annotationType);
    }

    public static <T> List<Field> annotatedFieldsOf(Class<T> type, Class<? extends Annotation> annotationType) {
        return annotatedMembersIn(type.getDeclaredFields(), annotationType);
    }

    public static <T, M extends AccessibleObject & Member> List<M> annotatedMembersIn(M[] members, Class<? extends Annotation> annotationType) {
        return AnnotatedElementFilter.annotatedIn(members, annotationType);
    }

    public static <T extends AccessibleObject & Member> T makeAccessible(T member) {
        member.setAccessible(true);
        return member;
    }
}
