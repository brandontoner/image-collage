package com.brandontoner.jimagecollage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

final class ImageCollageImpl<T extends SubImagesDiff<T>> implements ImageCollage {
    @Nonnull private static final Logger LOGGER = LogManager.getLogger(ImageCollageImpl.class);
    @Nonnull private final Path target;
    @Nonnull private final Set<Path> subImages;
    @Nonnull private final DiffFunction<T> diffFunction;
    private final int subSectionsX;
    private final int subSectionsY;
    @CheckForNull private final Path outputDirectory;

    ImageCollageImpl(@Nonnull ImageCollageBuilder builder) {
        target = Objects.requireNonNull(builder.getTargetImage());
        outputDirectory = builder.getOutputDirectory();
        subImages = Set.copyOf(builder.getSubImages());
        diffFunction = (DiffFunction<T>) Objects.requireNonNull(builder.getDiffFunction());
        subSectionsX = builder.getHorizontalSubSections();
        subSectionsY = builder.getVerticalSubSections();
    }

    @Nonnull
    @Override
    public CompletableFuture<Path> start() {
        CompletableFuture<Path> output = new CompletableFuture<>();
        ForkJoinPool.commonPool().submit(() -> compute(output));
        return output;
    }

    private void compute(@Nonnull CompletableFuture<? super Path> completableFuture) {
        try {
            MasterImage<T> masterImage = new MasterImage<>(target, subSectionsX, subSectionsY);
            subImages.parallelStream()
                     .map(subImage -> diffFunction.diff(masterImage, subImage))
                     .filter(Objects::nonNull)
                     .forEachOrdered(masterImage::add);

            BufferedImage output = masterImage.compile();

            File of;
            if (outputDirectory == null) {
                of = File.createTempFile("ImageCollage", ".jpg");
            } else {
                Files.createDirectories(outputDirectory);
                of = Files.createTempFile(outputDirectory, "ImageCollage", ".jpg").toFile();
            }
            LOGGER.info("Writing image to {}", of);
            ImageIO.write(output, "JPEG", of);

            completableFuture.complete(of.toPath());
        } catch (Throwable t) {
            completableFuture.completeExceptionally(t);
        }
    }
}
