package ru.itis.prytkovd.inject.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

public final class AnnotatedElementFilter {
    public static <T extends AnnotatedElement> List<T> annotatedIn(T[] elements, Class<? extends Annotation> annotationType) {
        return Arrays.stream(elements)
            .filter(element -> element.isAnnotationPresent(annotationType))
            .toList();
    }
}
