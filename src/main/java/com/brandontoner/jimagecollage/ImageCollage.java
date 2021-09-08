package com.brandontoner.jimagecollage;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface ImageCollage {
    static ImageCollageBuilder builder() {
        return new ImageCollageBuilderImpl();
    }

    CompletableFuture<Path> start();
}
