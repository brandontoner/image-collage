package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;

interface SubImagesDiff<T extends SubImagesDiff<T>> {
    boolean isBetter(int i, @Nonnull T other);

    boolean isBetter(int i1, int i2);

    Path path();
}
