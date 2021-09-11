package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.awt.image.BufferedImage;

@Immutable
final class Image {
    @Nonnull private final int[] rgbArray;
    private final int width;
    private final int height;

    Image(@Nonnull BufferedImage read) {
        width = read.getWidth();
        height = read.getHeight();

        rgbArray = read.getRGB(0, 0, width, height, null, 0, width);
    }

    private Image(@Nonnull int[] rgbArray, int width, int height) {
        this.rgbArray = rgbArray;
        this.width = width;
        this.height = height;
    }

    @Nonnull
    Image subImage(int xOffset, int yOffset, int width, int height) {
        int[] array = new int[width * height];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                array[y * width + x] = rgbArray[(y + yOffset) * this.width + x + xOffset];
            }
        }

        return new Image(array, width, height);
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    @Nonnull
    int[] rgbArray() {
        return rgbArray;
    }
}
