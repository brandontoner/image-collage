package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SubImagesDiff<T extends SubImagesDiff<T>> {
    private final AtomicInteger usages = new AtomicInteger(0);
    private final Path path;

    SubImagesDiff(Path path) {
        this.path = path;
    }

    abstract boolean isBetter(int i, @Nonnull T other);

    abstract boolean isBetter(int i1, int i2);

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
