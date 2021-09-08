package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface ImageCollage {
    @Nonnull
    static ImageCollageBuilder builder() {
        return new ImageCollageBuilderImpl();
    }

    @Nonnull
    CompletableFuture<Path> start();
}
