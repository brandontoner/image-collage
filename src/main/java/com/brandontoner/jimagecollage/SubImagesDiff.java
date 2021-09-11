package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SubImagesDiff<T extends SubImagesDiff<T>> {
    @Nonnull private final AtomicInteger usages = new AtomicInteger(0);
    @Nonnull private final Path path;

    SubImagesDiff(@Nonnull Path path) {
        this.path = Objects.requireNonNull(path);
    }

    abstract boolean isBetter(int i, @Nonnull T other);

    abstract boolean isBetter(int i1, int i2);

    @Nonnull
    Path path() {
        return path;
    }

    int getUsages() {
        return usages.get();
    }

    void incrementUsages() {
        usages.incrementAndGet();
    }

    void decrementUsages() {
        usages.decrementAndGet();
    }
}
