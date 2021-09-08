package com.brandontoner.jimagecollage;

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

final class ImageCollageImpl implements ImageCollage {
    private final Path target;
    private final Set<Path> subImages;
    private final int subSectionsX;
    private final int subSectionsY;
    @CheckForNull
    private final Path outputDirectory;

    ImageCollageImpl(@Nonnull final ImageCollageBuilder builder) {
        target = Objects.requireNonNull(builder.getTargetImage());
        outputDirectory = builder.getOutputDirectory();
        subImages = Set.copyOf(builder.getSubImages());
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

    private void compute(@Nonnull CompletableFuture<Path> completableFuture) {
        try {
            final MasterImage masterImage = new MasterImage(target, subSectionsX, subSectionsY);
            subImages.parallelStream().map(masterImage::diff).filter(Objects::nonNull).forEachOrdered(masterImage::add);

            final BufferedImage output = masterImage.compile();

            File of;
            if (outputDirectory == null) {
                of = File.createTempFile("ImageCollage", ".jpg");
            } else {
                Files.createDirectories(outputDirectory);
                of = Files.createTempFile(outputDirectory, "ImageCollage", ".jpg").toFile();
            }
            ImageIO.write(output, "JPEG", of);

            completableFuture.complete(of.toPath());
        } catch (Throwable t) {
            completableFuture.completeExceptionally(t);
        }
    }
}
