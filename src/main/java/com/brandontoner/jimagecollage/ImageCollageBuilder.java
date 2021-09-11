package com.brandontoner.jimagecollage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Builder for {@link ImageCollage}.
 */
@NotThreadSafe
public interface ImageCollageBuilder {
    /**
     * Sets the image that the sub images should attempt to create.
     *
     * @param path path of the path containing target image
     * @return builder with target image set
     * @throws NullPointerException if path is null
     * @see ImageCollageBuilder#withTargetImage(Path)
     * @see ImageCollageBuilder#getTargetImage()
     */
    @Nonnull
    default ImageCollageBuilder withTargetImage(@Nonnull String path) {
        return withTargetImage(Path.of(path));
    }

    /**
     * Sets the image that the sub images should attempt to create.
     *
     * @param path path containing target image
     * @return builder with target image set
     * @throws NullPointerException if path is null
     * @see ImageCollageBuilder#withTargetImage(String)
     * @see ImageCollageBuilder#getTargetImage()
     */
    @Nonnull
    ImageCollageBuilder withTargetImage(@Nonnull Path path);

    /**
     * Gets the path containing the image that the sub images should attempt to create
     *
     * @return path containing target image, or null if unset
     */
    @CheckForNull
    Path getTargetImage();

    /**
     * Adds a directory containing the sub images that should be used to comprise the target image.  Multiple
     * invocations will add sub images, not overwrite them.
     *
     * @param subImages path to the directory containing the sub images
     * @return builder with sub images set
     * @throws NullPointerException if path is null
     * @see ImageCollageBuilder#withSubImageDirectory(Path)
     * @see ImageCollageBuilder#withSubImage(Path)
     * @see ImageCollageBuilder#getSubImages()
     */
    @Nonnull
    default ImageCollageBuilder withSubImageDirectory(@Nonnull String subImages) throws IOException {
        return withSubImageDirectory(Path.of(subImages));
    }

    /**
     * Adds a directory containing the sub images that should be used to comprise the target image.  Multiple
     * invocations will add sub images, not overwrite them.
     *
     * @param subImages directory containing the sub images
     * @return builder with sub images set
     * @throws NullPointerException if path is null
     * @throws IOException          if an I/O error is thrown when accessing the starting file.
     * @see ImageCollageBuilder#withSubImageDirectory(String)
     * @see ImageCollageBuilder#withSubImage(Path)
     * @see ImageCollageBuilder#getSubImages()
     */
    @Nonnull
    ImageCollageBuilder withSubImageDirectory(@Nonnull Path subImages) throws IOException;

    /**
     * Adds a sub images that should be used to comprise the target image.  Multiple invocations will add sub images,
     * not overwrite them.
     *
     * @param path path containing the sub image
     * @return builder with sub image set
     * @throws NullPointerException if path is null
     * @see ImageCollageBuilder#withSubImageDirectory(String)
     * @see ImageCollageBuilder#withSubImageDirectory(Path)
     * @see ImageCollageBuilder#getSubImages()
     */
    @Nonnull
    ImageCollageBuilder withSubImage(@Nonnull Path path);

    /**
     * Gets the collection of paths containing the sub images that should be used to comprise the target image.
     *
     * @return immutable collection of paths containing sub images
     */
    @Nonnull
    Collection<Path> getSubImages();

    /**
     * Sets the directory in which to place the output file.
     *
     * @param path output directory
     * @return builder with output path set
     */
    @Nonnull
    default ImageCollageBuilder withOutputDirectory(@Nonnull String path) {
        return withOutputDirectory(Path.of(path));
    }

    /**
     * Sets the directory in which to place the output file.
     *
     * @param path output directory
     * @return builder with output path set
     */
    @Nonnull
    ImageCollageBuilder withOutputDirectory(@Nonnull Path path);

    /**
     * Gets the directory in which to place the output file. If null, it will be placed in a random temp folder.
     *
     * @return directory to place output file or null
     */
    @CheckForNull
    Path getOutputDirectory();

    /**
     * Sets the number of subsections to be used to comprise the target image.
     *
     * @param num the number of horizontal and vertical subsections
     * @return builder with subsections set
     * @see ImageCollageBuilder#withHorizontalSubSections(int)
     * @see ImageCollageBuilder#withVerticalSubSections(int)
     * @see ImageCollageBuilder#getHorizontalSubSections()
     * @see ImageCollageBuilder#getVerticalSubSections()
     */
    @Nonnull
    default ImageCollageBuilder withSubSections(int num) {
        return withHorizontalSubSections(num).withVerticalSubSections(num);
    }

    /**
     * Sets the number of horizontal subsections to be used to comprise the target image.
     *
     * @param num the number of horizontal subsections
     * @return builder with subsections set
     * @see ImageCollageBuilder#withSubSections(int)
     * @see ImageCollageBuilder#withVerticalSubSections(int)
     * @see ImageCollageBuilder#getHorizontalSubSections()
     * @see ImageCollageBuilder#getVerticalSubSections()
     */
    @Nonnull
    ImageCollageBuilder withHorizontalSubSections(int num);

    /**
     * Gets the number of horizontal subsections to be used to comprise the target image.
     *
     * @return number of horizontal sub images
     */
    int getHorizontalSubSections();

    /**
     * Sets the number vertical of subsections to be used to comprise the target image.
     *
     * @param num the number of vertical subsections
     * @return builder with subsections set
     * @see ImageCollageBuilder#withSubSections(int)
     * @see ImageCollageBuilder#withHorizontalSubSections(int)
     * @see ImageCollageBuilder#getHorizontalSubSections()
     * @see ImageCollageBuilder#getVerticalSubSections()
     */
    @Nonnull
    ImageCollageBuilder withVerticalSubSections(int num);

    /**
     * Gets the number of vertical subsections to be used to comprise the target image.
     *
     * @return number of vertical sub images
     */
    int getVerticalSubSections();

    /**
     * Sets the diff function to use to compare images with subsections of the master image.
     *
     * @param diffFunction new diff function
     * @param <U>          new diff type
     * @return a builder <b>COULD BE A NEW INSTANCE</b> with the diff function set.
     */
    @Nonnull
    <U extends SubImagesDiff<U>> ImageCollageBuilder withDiffFunction(@Nonnull DiffFunction<U> diffFunction);

    /**
     * Sets the maximum times each image can be used, defaults to 1.
     *
     * @return builder with max usages set.
     */
    @Nonnull
    ImageCollageBuilder withUsagePerImage(int n);

    /**
     * @return maximum times each image can be used.
     */
    int getUsagesPerImage();

    /**
     * Sets the crop function used to get the images into the correct aspect ratio.
     *
     * @param cropFunction crop function
     * @return builder with crop function set
     */
    @Nonnull
    ImageCollageBuilder withCropFunction(@Nonnull CropFunction cropFunction);

    /**
     * Gets the crop function used to get images into the correct aspect ratio.
     */
    @Nonnull
    CropFunction getCropFunction();

    /**
     * Gets the diff function to use to compare images with subsections of the master image.
     *
     * @return diff function
     */
    @Nonnull
    DiffFunction<?> getDiffFunction();

    /**
     * Builds the {@link ImageCollage}.
     *
     * @return image collage
     */
    @Nonnull
    ImageCollage build();
}
