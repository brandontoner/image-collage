package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.awt.image.BufferedImage;

@Immutable
final class Image {
    private final int[] rgbArray;
    private final int width;
    private final int height;

    Image(@Nonnull final BufferedImage read) {
        this.width = read.getWidth();
        this.height = read.getHeight();

        this.rgbArray = read.getRGB(0, 0, width, height, null, 0, width);
    }

    private Image(final int[] rgbArray, final int width, final int height) {
        this.rgbArray = rgbArray;
        this.width = width;
        this.height = height;
    }

    private static int diff(final int a, final int b) {
        final int ar = (a >> 16) & 0xFF;
        final int br = (b >> 16) & 0xFF;

        final int ag = (a >> 8) & 0xFF;
        final int bg = (b >> 8) & 0xFF;

        final int ab = (a) & 0xFF;
        final int bb = (b) & 0xFF;

        return Math.abs(ar - br) + Math.abs(ag - bg) + Math.abs(ab - bb);
    }

    int getRGB(final int x, final int y) {
        return this.rgbArray[y * this.width + x];
    }

    @Nonnull
    Image subImage(final int xOffset, final int yOffset, final int width, final int height) {
        int[] array = new int[width * height];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                array[y * width + x] = this.getRGB(x + xOffset, y + yOffset);
            }
        }

        return new Image(array, width, height);
    }

    int getWidth() {
        return this.width;
    }

    int getHeight() {
        return this.height;
    }

    long diff(@Nonnull final Image scaledImage) {
        if (scaledImage.getHeight() != this.getHeight() || scaledImage.getWidth() != this.getWidth()) {
            throw new IllegalArgumentException();
        }
        long sum = 0;
        for (int i = 0; i < rgbArray.length; i++) {
            final int thisRGB = this.rgbArray[i];
            final int otherRGB = scaledImage.rgbArray[i];
            sum += diff(thisRGB, otherRGB);
        }
        return sum;
    }
}
