package ru.itis.prytkovd.inject;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import ru.itis.prytkovd.inject.exceptions.InjectionException;
import ru.itis.prytkovd.inject.providers.Providers;
import ru.itis.prytkovd.inject.scopes.Scope;
import ru.itis.prytkovd.inject.scopes.Scopes;
import ru.itis.prytkovd.inject.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static ru.itis.prytkovd.inject.util.InjectionUtils.injectableFieldsOf;
import static ru.itis.prytkovd.inject.util.InjectionUtils.qualifierIn;
import static ru.itis.prytkovd.inject.util.ModuleUtils.extractProviderMethods;

public final class Injector {
    private final Map<Key<?>, Provider<?>> providerMap = new HashMap<>();
    private final Map<Class<? extends Annotation>, Scope> scopeMap = new HashMap<>();

    private Injector(List<Object> modules, Map<Key<?>, Provider<?>> providerMap, Map<Class<? extends Annotation>,
        Scope> scopeMap) {
        this.scopeMap.putAll(scopeMap);
        this.providerMap.putAll(providerMap);

        Deque<Method> processingDeque = new ArrayDeque<>();
        Deque<Method> rejectedDeque = new ArrayDeque<>();
        int lastRejectedDequeSize = 0;
        boolean haltProcessing = false;

        for (Object module : modules) {
            processingDeque.addAll(
                extractProviderMethods(module).stream()
                    .sorted(Comparator.comparingInt(Method::getParameterCount))
                    .toList()
            );

            while (true) {
                if (processingDeque.isEmpty()) {
                    int rejectedDequeSize = rejectedDeque.size();

                    if (rejectedDequeSize == 0) {
                        break;
                    }

                    if (rejectedDequeSize == lastRejectedDequeSize) {
                        haltProcessing = true;
                    }

                    processingDeque.addAll(rejectedDeque);
                    rejectedDeque.clear();

                    lastRejectedDequeSize = rejectedDequeSize;
                }

                Method method = processingDeque.removeFirst();

                Class<?> returnType = method.getReturnType();
                Annotation qualifierAnnotation = qualifierIn(method.getAnnotations());
                Key<?> key;

                if (qualifierAnnotation == null) {
                    key = Key.of(returnType);
                } else {
                    key = Key.of(returnType, qualifierAnnotation);
                }

                try {
                    System.out.println("METHOD: " + method);
                    Provider<?> provider = Providers.fromMethod(key, module, method, this);
                    this.providerMap.put(key, provider);
                } catch (InjectionException e) {
                    if (haltProcessing) {
                        throw e;
                    }
                    rejectedDeque.addLast(method);
                }

                System.out.println("PROVIDER MAP: " + this.providerMap);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T instance(Class<T> type) {
        return instance(Key.of(type));
    }

    public <T> T instance(Key<T> key) {
        Provider<T> provider = provider(key);
        return provider == null ? null : provider.get();
    }

    public <T> Provider<T> provider(Class<T> type) {
        return provider(Key.of(type));
    }

    @SuppressWarnings("unchecked")
    public <T> Provider<T> provider(Key<T> key) {
        if (!providerMap.containsKey(key)) {
            Provider<T> provider = Providers.ofType(key, this);
            providerMap.put(key, provider);
        }

        return (Provider<T>)providerMap.get(key);
    }

    public <T> void injectFields(T instance) {
        injectableFieldsOf(instance.getClass()).stream()
            .map(ReflectionUtils::makeAccessible)
            .forEach(field -> {
                Key<?> key = Key.of(field.getType(), qualifierIn(field.getAnnotations()));

                Provider<?> provider = provider(key);

                if (provider == null) {
                    throw new InjectionException("Unknown key: " + key);
                }

                try {
                    field.set(instance, provider.get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public Scope scope(Class<? extends Annotation> annotationType) {
        return scopeMap.get(annotationType);
    }

    public static final class Builder {
        private final List<Object> modules = new ArrayList<>();
        private Map<Key<?>, Provider<?>> providerMap = new HashMap<>();
        private final Map<Class<? extends Annotation>, Scope> scopeMap = new HashMap<>();

        public Builder() {
            scopeMap.put(Singleton.class, Scopes.SINGLETON);
        }

        public Builder parent(Injector parent) {
            this.providerMap = parent.providerMap;
            return this;
        }

        public Builder module(Object module) {
            modules.add(Objects.requireNonNull(module, "module must not be null"));
            return this;
        }

        public Builder scope(Scope scope) {
            Objects.requireNonNull(scope, "scope must not be null");
            scopeMap.put(scope.annotationType(), scope);
            return this;
        }

        public Injector build() {
            return new Injector(modules, providerMap, scopeMap);
        }
    }
}
