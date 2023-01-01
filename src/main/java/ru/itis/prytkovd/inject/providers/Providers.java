package ru.itis.prytkovd.inject.providers;

import jakarta.inject.Provider;
import ru.itis.prytkovd.inject.Injector;
import ru.itis.prytkovd.inject.Key;
import ru.itis.prytkovd.inject.exceptions.InjectionException;
import ru.itis.prytkovd.inject.scopes.Scope;
import ru.itis.prytkovd.inject.util.InjectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static ru.itis.prytkovd.inject.util.InjectionUtils.qualifierIn;
import static ru.itis.prytkovd.inject.util.InjectionUtils.scopeIn;
import static ru.itis.prytkovd.inject.util.ReflectionUtils.makeAccessible;

public final class Providers {
    public static <T> Provider<T> ofType(Key<T> key, Injector injector) {
        Constructor<T> constructor = InjectionUtils.injectableConstructorOf(key.type());

        if (constructor == null) {
            throw new InjectionException(key + " does not have injectable constructor");
        }

        makeAccessible(constructor);

        List<? extends Provider<?>> paramProviders = paramProviders(constructor, injector);

        Provider<T> provider = () -> {
            try {
                return constructor.newInstance(params(paramProviders));
            } catch (InstantiationException |
                     IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        Annotation scopeAnnotation = scopeIn(key.type().getAnnotations());
        provider = maybeScope(provider, scopeAnnotation, key, injector);

        return provider;
    }

    @SuppressWarnings("unchecked")
    public static <T> Provider<T> fromMethod(Key<T> key, Object instance, Method method, Injector injector) {
        makeAccessible(method);

        List<? extends Provider<?>> paramProviders = paramProviders(method, injector);

        Provider<T> provider = () -> {
            try {
                return (T)method.invoke(
                    instance,
                    params(paramProviders)
                );
            } catch (IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        Annotation scopeAnnotation = scopeIn(method.getAnnotations());
        provider = maybeScope(provider, scopeAnnotation, key, injector);

        return provider;
    }

    @SuppressWarnings("unchecked")
    public static <T> Provider<T> fromMethod(Class<T> type, Method method, Injector injector) {
        Class<?> declaringClass = method.getDeclaringClass();
        Key<?> key = Key.of(declaringClass, qualifierIn(declaringClass.getAnnotations()));
        Object instance = injector.instance(key);
        return (Provider<T>)fromMethod(key, instance, method, injector);
    }

    private static List<? extends Provider<?>> paramProviders(Executable executable, Injector injector) {
        return Arrays.stream(executable.getParameters())
            .map(param -> Key.of(param.getType(), qualifierIn(param.getAnnotations())))
            .map(injector::provider)
            .toList();
    }

    private static Object[] params(List<? extends Provider<?>> paramProviders) {
        return paramProviders.stream()
            .map(Provider::get)
            .toArray();
    }

    private static <T> Provider<T> maybeScope(Provider<T> provider, Annotation scopeAnnotation, Key<T> key, Injector injector) {
        Scope scope = null;

        if (scopeAnnotation != null) {
            scope = injector.scope(scopeAnnotation.annotationType());
        }

        if (scope != null) {
            provider = scope.scope(key, provider);
        }

        return provider;
    }
}
