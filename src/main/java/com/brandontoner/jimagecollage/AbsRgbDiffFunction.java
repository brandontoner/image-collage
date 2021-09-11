package com.brandontoner.jimagecollage;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Computes the diff between two images as the sum of abs(r1 - r2) + abs(g1 - g2) + abs(b1 - b2) for each pixel.
 */
class AbsRgbDiffFunction extends DiffFunction<AbsRgbDiffFunction.LongDiff> {
    private static long diff(@Nonnull Image a, @Nonnull Image b) {
        assert a.getHeight() == b.getHeight();
        assert a.getWidth() == b.getWidth();

        long sum = 0L;
        int[] rgbA = a.rgbArray();
        int[] rgbB = b.rgbArray();
        for (int i = 0; i < rgbA.length; i++) {
            int thisRGB = rgbA[i];
            int otherRGB = rgbB[i];
            int ar = (thisRGB >> 16) & 0xFF;
            int br = (otherRGB >> 16) & 0xFF;

            int ag = (thisRGB >> 8) & 0xFF;
            int bg = (otherRGB >> 8) & 0xFF;

            int ab = (thisRGB) & 0xFF;
            int bb = (otherRGB) & 0xFF;

            sum += Math.abs(ar - br) + Math.abs(ag - bg) + Math.abs(ab - bb);
        }
        return sum;
    }

    @Override
    @Nonnull
    protected LongDiff diff(@Nonnull Path subImage, @Nonnull Image scaledImage, @Nonnull Image[] subSections) {
        long[] output = new long[subSections.length];
        for (int i = 0; i < subSections.length; i++) {
            output[i] = diff(subSections[i], scaledImage);
        }
        return new LongDiff(subImage, output);
    }

    static final class LongDiff extends SubImagesDiff<LongDiff> {
        @Nonnull private final long[] diffs;

        private LongDiff(@Nonnull Path path, @Nonnull long[] diffs) {
            super(path);
            this.diffs = diffs;
        }

        @Override
        public boolean isBetter(int i, @Nonnull LongDiff other) {
            return diffs[i] < other.diffs[i];
        }

        @Override
        public boolean isBetter(int i1, int i2) {
            return diffs[i1] < diffs[i2];
        }
    }
}
