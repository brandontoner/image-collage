package com.brandontoner.jimagecollage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Computes differences between images.
 *
 * @param <T> diff result type
 */
public abstract class DiffFunction<T extends SubImagesDiff<T>> {
    private static final Logger LOGGER = LogManager.getLogger(DiffFunction.class);

    /**
     * Gets a diff function which computes the diff between two images as the sum of abs(r1 - r2) + abs(g1 - g2) +
     * abs(b1 - b2) for each pixel.
     */
    @Nonnull
    public static DiffFunction<?> absRgb() {
        return new AbsRgbDiffFunction();
    }

    /**
     * Gets a diff function which computes the diff between two images with structural similarity (SSIM).
     */
    @Nonnull
    public static DiffFunction<?> ssim() {
        return new SsimDiffFunction();
    }

    @Nonnull
    private static BufferedImage scale(BufferedImage bi, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, w, h);
        g2.drawImage(bi, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    private static boolean checkAspectRatio(@Nonnull BufferedImage bi, int subSectionWidth, int subSectionHeight) {
        double thisAspectRatio = (double) subSectionWidth / subSectionHeight;
        double thatAspectRatio = (double) bi.getWidth() / bi.getHeight();
        double ratio = thatAspectRatio / thisAspectRatio;
        return 0.99 < ratio && ratio < 1.01;
    }

    @CheckForNull
    final T diff(@Nonnull MasterImage<T> masterImage, @Nonnull Path subImage) {
        BufferedImage bi = ImageUtils.read(subImage);
        if (bi == null) {
            LOGGER.error("Cannot load {}", subImage);
            return null;
        }
        int subSectionWidth = masterImage.subSectionWidth();
        int subSectionHeight = masterImage.subSectionHeight();
        if (!checkAspectRatio(bi, subSectionWidth, subSectionHeight)) {
            LOGGER.warn("File {} has bad aspect ratio", subImage);
            return null;
        }

        BufferedImage scaled = scale(bi, subSectionWidth, subSectionHeight);
        Image scaledImage = new Image(scaled);
        Image[] subSections = masterImage.subSections();
        return diff(subImage, scaledImage, subSections);
    }

    @Nonnull
    abstract T diff(@Nonnull Path subImage, Image scaledImage, Image[] subSections);
}
