package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Compares images using structural similarity (SSIM).
 */
class SsimDiffFunction extends DiffFunction<SsimDiffFunction.SsimDiff> {
    private static final double k1 = 0.01;
    private static final double k2 = 0.03;
    private static final double L = 255;
    private static final double c1 = (k1 * L) * (k1 * L);
    private static final double c2 = (k2 * L) * (k2 * L);

    /**
     * Computes the variance of an array.
     *
     * @param floats  array
     * @param average average of the array
     * @return variance of the array
     */
    private static float getVariance(@Nonnull float[] floats, float average) {
        float variance = 0;
        for (float luma : floats) {
            float d1 = luma - average;
            variance += d1 * d1;
        }
        return variance / floats.length;
    }

    private static double ssim(@Nonnull Image img1, float[] lumas2, float average2, float var2) {
        float[] lumas1 = getLumas(img1);
        float average1 = getAverage(lumas1);
        float var1 = getVariance(lumas1, average1);

        float covariance1 = 0;
        for (int i = 0; i < lumas1.length; ++i) {
            float d1 = lumas1[i] - average1;
            float d2 = lumas2[i] - average2;
            covariance1 += d1 * d2;
        }
        covariance1 /= lumas1.length;
        float covariance = covariance1;

        return (2 * average1 * average2 + c1) * (2 * covariance + c2) / ((average1 * average1 + average2 * average2 + c1) * (var1 + var2 + c2));
    }

    @Nonnull
    private static float[] getLumas(@Nonnull Image img1) {
        int[] rgbArray = img1.rgbArray();
        float[] lumas = new float[rgbArray.length];
        for (int i = 0; i < rgbArray.length; i++) {
            int rgb = rgbArray[i];
            int r = (0x00ff0000 & rgb) >>> 16;
            int g = (0x0000ff00 & rgb) >>> 8;
            int b = (0x000000ff & rgb);
            lumas[i] = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        }
        return lumas;
    }

    private static float getAverage(@Nonnull float[] lumas) {
        float v = 0;
        for (float luma : lumas) {
            v += luma;
        }
        return v / lumas.length;
    }

    @Nonnull
    @Override
    protected SsimDiff diff(@Nonnull Path subImage, @Nonnull Image scaledImage, @Nonnull Image[] subSections) {
        float[] lumas = getLumas(scaledImage);
        float average = getAverage(lumas);
        float variance = getVariance(lumas, average);
        double[] output = new double[subSections.length];
        for (int i = 0; i < subSections.length; i++) {
            output[i] = ssim(subSections[i], lumas, average, variance);
        }
        return new SsimDiff(subImage, output);
    }

    static final class SsimDiff extends SubImagesDiff<SsimDiff> {
        private final double[] ssims;

        SsimDiff(Path path, double[] ssims) {
            super(path);
            this.ssims = ssims;
        }

        @Override
        public boolean isBetter(int i, @Nonnull SsimDiff other) {
            return ssims[i] > other.ssims[i];
        }

        @Override
        public boolean isBetter(int i1, int i2) {
            return ssims[i1] > ssims[i2];
        }
    }
}
