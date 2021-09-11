package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface ImageCollage {
    @Nonnull
    static ImageCollageBuilder builder() {
        return builder(DiffFunction.absRgb());
    }

    @Nonnull
    static <T extends SubImagesDiff<T>> ImageCollageBuilder builder(@Nonnull DiffFunction<T> diffFunction) {
        return new ImageCollageBuilderImpl<>(diffFunction);
    }

    @Nonnull
    CompletableFuture<Path> start();
}
