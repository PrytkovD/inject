package ru.itis.prytkovd.inject.util;

import ru.itis.prytkovd.inject.Provide;

import java.lang.reflect.Method;
import java.util.List;

import static ru.itis.prytkovd.inject.util.ReflectionUtils.annotatedMethodsOf;

public final class ModuleUtils {
    public static List<Method> extractProviderMethods(Object module) {
        return annotatedMethodsOf(module.getClass(), Provide.class).stream()
            .map(ReflectionUtils::makeAccessible)
            .toList();
    }
}
