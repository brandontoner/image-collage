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
import java.util.HashMap;
import java.util.List;
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
    private final CropFunction cropFunction;

    MasterImage(@Nonnull Path target,
                int subSectionsX,
                int subSectionsY,
                int usagesPerImage,
                CropFunction cropFunction) {
        this.usagesPerImage = usagesPerImage;
        this.cropFunction = cropFunction;
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

    void add(@Nonnull T entry) {
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
                return;
            }
            entry.incrementUsages();
            T oldFile = bestImages[bestIndex];
            bestImages[bestIndex] = entry;
            if (oldFile != null) {
                // RE-PROCESS OVERWRITTEN FILE
                oldFile.decrementUsages();
                add(oldFile);
            }
        }
    }

    @Nonnull
    BufferedImage compile() {
        LOGGER.info("Compiling images into collage");
        BufferedImage bi = cropFunction.crop(ImageUtils.read(bestImages[0].path()), subSectionWidth, subSectionHeight);
        int scale = getScale(bi.getWidth(), bi.getHeight());
        int width = bi.getWidth() / scale;
        int height = bi.getHeight() / scale;
        BufferedImage output =
                new BufferedImage(width * subSectionsX, height * subSectionsY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = output.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, output.getWidth(), output.getHeight());


        Map<Path, List<Point>> imageToUsages = new HashMap<>();

        for (int y = 0; y < subSectionsY; ++y) {
            for (int x = 0; x < subSectionsX; ++x) {
                T image = bestImages[y * subSectionsX + x];
                if (image != null) {
                    imageToUsages.computeIfAbsent(image.path(), ignored -> new ArrayList<>()).add(new Point(x, y));
                }
            }
        }
        imageToUsages.entrySet()
                     .parallelStream()
                     .map(e -> new AbstractMap.SimpleImmutableEntry<>(cropFunction.crop(ImageUtils.read(e.getKey()),
                                                                                        subSectionWidth,
                                                                                        subSectionHeight),
                                                                      e.getValue()))
                     .forEach(e -> {
                         synchronized (g2) {
                             for (Point point : e.getValue()) {
                                 g2.drawImage(e.getKey(), point.x * width, point.y * height, width, height, null);
                             }
                         }
                     });
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
