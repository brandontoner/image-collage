package com.brandontoner.jimagecollage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

@FunctionalInterface
public interface CropFunction {
    /**
     * Gets a CropFunction which doesn't do any cropping, just rejects images which do not match the desired aspect
     * ratio.
     */
    @Nonnull
    static CropFunction rejectBadAspectRatio() {
        return (input, width, height) -> {
            double thisAspectRatio = (double) width / height;
            double thatAspectRatio = (double) input.getWidth() / input.getHeight();
            double ratio = thatAspectRatio / thisAspectRatio;
            return 0.95 < ratio && ratio < 1.05 ? input : null;
        };
    }

    /**
     * Gets a CropFunction which takes the biggest subsection of the middle of the image which meets the desired aspect
     * ratio.
     */
    @Nonnull
    static CropFunction cropFromMiddle() {
        return (input, width, height) -> {
            int inWidth = input.getWidth();
            int inHeight = input.getHeight();
            if (width * inHeight <= inWidth * height) {
                int desiredWidth = width * inHeight / height;
                return input.getSubimage((inWidth - desiredWidth) / 2, 0, desiredWidth, inHeight);
            } else {
                int desiredHeight = height * inWidth / width;
                return input.getSubimage(0, (inHeight - desiredHeight) / 2, inWidth, desiredHeight);
            }
        };
    }

    /**
     * Crops an image to the desired aspect ratio. <strong>The output width and height do not need to match!</strong>.
     * The width and height parameters are just for aspect ratio.
     *
     * @param input  input image
     * @param width  desired with
     * @param height desired height
     * @return cropped image, or null if the image cannot be cropped
     */
    @CheckForNull
    BufferedImage crop(@Nonnull BufferedImage input, int width, int height);
}
