package ru.itis.prytkovd.inject.scopes;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import ru.itis.prytkovd.inject.Key;

import java.lang.annotation.Annotation;

final class SingletonScope implements Scope {
    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new SingletonProvider<>(unscoped);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Singleton.class;
    }

    private static final class SingletonProvider<T> implements Provider<T> {
        private final Provider<T> unscoped;
        private volatile T instance;

        public SingletonProvider(Provider<T> unscoped) {
            this.unscoped = unscoped;
        }

        @Override
        public T get() {
            if (instance == null) {
                synchronized (SingletonProvider.class) {
                    if (instance == null) {
                        instance = unscoped.get();
                    }
                }
            }
            return instance;
        }
    }
}
