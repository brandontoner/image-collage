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
import java.util.stream.Stream;

/**
 * Implementation of {@link ImageCollageBuilder}.
 */
@NotThreadSafe
final class ImageCollageBuilderImpl<T extends SubImagesDiff<T>> implements ImageCollageBuilder {
    /** Collection of sub image files. */
    @Nonnull private final Collection<Path> subImages = new HashSet<>();
    private Path targetImage;
    private int horizontalSubSections;
    private int verticalSubSections;
    private Path outputDirectory;
    private DiffFunction<T> diffFunction;
    private int usagesPerImage = 1;

    ImageCollageBuilderImpl(@Nonnull DiffFunction<T> diffFunction) {
        this.diffFunction = Objects.requireNonNull(diffFunction);
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withTargetImage(@Nonnull Path path) {
        targetImage = Objects.requireNonNull(path);
        return this;
    }

    @Override
    public Path getTargetImage() {
        return targetImage;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withSubImageDirectory(@Nonnull Path subImages) throws IOException {
        try (Stream<Path> stream = Files.walk(subImages)) {
            stream.filter(Files::isRegularFile).forEach(this::withSubImage);
        }
        return this;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withSubImage(@Nonnull Path path) {
        subImages.add(path);
        return this;
    }

    @Nonnull
    @Override
    public Collection<Path> getSubImages() {
        return Set.copyOf(subImages);
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withOutputDirectory(@Nonnull Path path) {
        outputDirectory = Objects.requireNonNull(path);
        return this;
    }

    @CheckForNull
    @Override
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withHorizontalSubSections(int num) {
        horizontalSubSections = num;
        return this;
    }

    @Override
    public int getHorizontalSubSections() {
        return horizontalSubSections;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withVerticalSubSections(int num) {
        verticalSubSections = num;
        return this;
    }

    @Override
    public int getVerticalSubSections() {
        return verticalSubSections;
    }

    @Nonnull
    @Override
    public <U extends SubImagesDiff<U>> ImageCollageBuilder withDiffFunction(@Nonnull DiffFunction<U> diffFunction) {
        // TODO this probably works fine, since the type erasure is removed at runtime, but it's kinda bad.
        // Could be be replaced with creating a new instance of the builder with all of the same values, and the new
        // diff function
        this.diffFunction = (DiffFunction<T>) Objects.requireNonNull(diffFunction);
        return this;
    }

    @Nonnull
    @Override
    public ImageCollageBuilder withUsagePerImage(int n) {
        usagesPerImage = n;
        return this;
    }

    @Override
    public int getUsagesPerImage() {
        return usagesPerImage;
    }

    @Nonnull
    @Override
    public DiffFunction<T> getDiffFunction() {
        return diffFunction;
    }

    @Nonnull
    @Override
    public ImageCollage build() {
        return new ImageCollageImpl<>(this);
    }
}
