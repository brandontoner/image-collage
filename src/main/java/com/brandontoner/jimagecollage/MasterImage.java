package com.brandontoner.jimagecollage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

final class MasterImage<T extends SubImagesDiff<T>> {
    private static final Logger LOGGER = LogManager.getLogger(MasterImage.class);
    @Nonnull private final Image[] subSections;
    @Nonnull private final T[] bestImages;
    private final int subSectionsX;
    private final int subSectionsY;
    private final int subSectionWidth;
    private final int subSectionHeight;
    private final int usagesPerImage;

    MasterImage(@Nonnull Path target, int subSectionsX, int subSectionsY, int usagesPerImage) {
        this.usagesPerImage = usagesPerImage;
        Image image = new Image(Objects.requireNonNull(ImageUtils.read(target)));
        this.subSectionsX = subSectionsX;
        this.subSectionsY = subSectionsY;

        subSectionWidth = image.getWidth() / subSectionsX;
        subSectionHeight = image.getHeight() / subSectionsY;

        int subImagesCount = subSectionsY * subSectionsX;
        subSections = new Image[subImagesCount];
        bestImages = (T[]) new SubImagesDiff[subImagesCount];

        for (int y = 0; y < subSectionsY; ++y) {
            for (int x = 0; x < subSectionsX; ++x) {
                subSections[y * subSectionsX + x] =
                        image.subImage(x * subSectionWidth, y * subSectionHeight, subSectionWidth, subSectionHeight);
            }
        }
    }

    boolean add(@Nonnull T entry) {
        boolean added = false;
        int usages;
        while ((usages = entry.getUsages()) < usagesPerImage) {
            int bestIndex = -1;

            for (int i = 0; i < bestImages.length; i++) {
                T bestImage = bestImages[i];
                if (bestImage == null || entry.isBetter(i, bestImage)) {
                    // better than the existing best image
                    if (bestImage == null || usages <= bestImage.getUsages()) {
                        // only replace images that have been used the same number of times or more
                        if (bestIndex == -1 || entry.isBetter(i, bestIndex)) {
                            // better than the diff for this image
                            bestIndex = i;
                        }
                    }
                }
            }

            if (bestIndex == -1) {
                return added;
            }
            entry.incrementUsages();
            T oldFile = bestImages[bestIndex];
            bestImages[bestIndex] = entry;
            if (oldFile != null) {
                // RE-PROCESS OVERWRITTEN FILE
                oldFile.decrementUsages();
                add(oldFile);
            }
            added = true;
        }
        return added;
    }

    @Nonnull
    BufferedImage compile() {
        LOGGER.info("Compiling images into collage");
        BufferedImage bi = ImageUtils.read(bestImages[0].path());
        int scale = getScale(bi.getWidth(), bi.getHeight());
        int width = bi.getWidth() / scale;
        int height = bi.getHeight() / scale;
        BufferedImage output =
                new BufferedImage(width * subSectionsX, height * subSectionsY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = output.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, output.getWidth(), output.getHeight());

        Collection<Map.Entry<Point, T>> diffs = new ArrayList<>(bestImages.length);

        for (int y = 0; y < subSectionsY; ++y) {
            for (int x = 0; x < subSectionsX; ++x) {
                if (bestImages[y * subSectionsX + x] != null) {
                    diffs.add(new AbstractMap.SimpleImmutableEntry<>(new Point(x, y),
                                                                     bestImages[y * subSectionsX + x]));
                }
            }
        }
        diffs.parallelStream()
             .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), ImageUtils.read(e.getValue().path())))
             .forEach(e -> {
                 int x = e.getKey().x;
                 int y = e.getKey().y;
                 synchronized (g2) {
                     g2.drawImage(e.getValue(), x * width, y * height, width, height, null);
                 }
             });
        g2.dispose();
        return output;
    }

    private int getScale(long width, long height) {
        for (int scale = 1; true; ++scale) {
            if (Math.multiplyExact(width * subSectionsX / scale, height * subSectionsY / scale) < Integer.MAX_VALUE) {
                return scale;
            }
        }
    }

    int subSectionWidth() {
        return subSectionWidth;
    }

    int subSectionHeight() {
        return subSectionHeight;
    }

    @Nonnull
    Image[] subSections() {
        return subSections;
    }
}
