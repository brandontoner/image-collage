package com.brandontoner.jimagecollage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of {@link ImageCollageBuilder}.
 */
@NotThreadSafe
final class ImageCollageBuilderImpl implements ImageCollageBuilder {
    /** Collection of sub image files. */
    private final Collection<Path> subImages = new HashSet<>();
    private Path targetImage;
    private int horizontalSubSections;
    private int verticalSubSections;
    private Path outputDirectory;

    @Nonnull
    @Override
    public ImageCollageBuilder withTargetImage(@Nonnull final Path path) {
        this.targetImage = Objects.requireNonNull(path);
        return this;
    }

    @Override
    public Path getTargetImage() {
        return this.targetImage;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withSubImageDirectory(@Nonnull final Path subImages) throws IOException {
        Files.walk(subImages).filter(Files::isRegularFile).forEach(this::withSubImage);
        return this;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withSubImage(@Nonnull final Path path) {
        this.subImages.add(path);
        return this;
    }

    @Nonnull
    @Override
    public Collection<Path> getSubImages() {
        return Set.copyOf(this.subImages);
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withOutputDirectory(Path path) {
        this.outputDirectory = Objects.requireNonNull(path);
        return this;
    }

    @CheckForNull
    @Override
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withHorizontalSubSections(final int num) {
        this.horizontalSubSections = num;
        return this;
    }

    @Override
    public int getHorizontalSubSections() {
        return this.horizontalSubSections;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withVerticalSubSections(final int num) {
        this.verticalSubSections = num;
        return this;
    }

    @Override
    public int getVerticalSubSections() {
        return this.verticalSubSections;
    }

    @Nonnull
    @Override
    public ImageCollage build() {
        return new ImageCollageImpl(this);
    }
}
