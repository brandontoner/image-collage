package com.brandontoner.jimagecollage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

enum ImageUtils {
    ;
    private static final Logger LOGGER = LogManager.getLogger(ImageUtils.class);

    @CheckForNull
    static BufferedImage read(Path p) {
        LOGGER.info("Loading file {}", p);
        BufferedImage input;
        try {
            input = ImageIO.read(p.toFile());
            if (input == null) {
                return null;
            }
            Metadata metadata = ImageMetadataReader.readMetadata(p.toFile());
            Optional<String> orientation =
                    Optional.ofNullable(metadata.getFirstDirectoryOfType(ExifIFD0Directory.class))
                            .map(v -> v.getDescription(ExifDirectoryBase.TAG_ORIENTATION));
            if (orientation.isPresent()) {
                input = rotate(input, orientation.get());
            }
            return input;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static BufferedImage rotate(BufferedImage img, String orientation) {
        return switch (orientation) {
            case "Top, left side (Horizontal / normal)", "Unknown (0)" -> img;
            case "Right side, top (Rotate 90 CW)" -> rotate90cw(img);
            case "Bottom, right side (Rotate 180)" -> rotate180(img);
            case "Left side, bottom (Rotate 270 CW)" -> rotate270cw(img);
            default -> throw new IllegalArgumentException(orientation);
        };
    }

    private static BufferedImage rotate90cw(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] rgbIn = img.getRGB(0, 0, width, height, null, 0, width);
        int[] rgbOut = new int[rgbIn.length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                rgbOut[(x + 1) * height - 1 - y] = rgbIn[y * width + x];
            }
        }
        BufferedImage newImage = new BufferedImage(height, width, img.getType());
        newImage.setRGB(0, 0, height, width, rgbOut, 0, height);
        return newImage;
    }

    private static BufferedImage rotate180(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] rgbIn = img.getRGB(0, 0, width, height, null, 0, width);
        int[] rgbOut = new int[rgbIn.length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                rgbOut[(height - y) * width - 1 - x] = rgbIn[y * width + x];
            }
        }
        BufferedImage newImage = new BufferedImage(width, height, img.getType());
        newImage.setRGB(0, 0, width, height, rgbOut, 0, width);
        return newImage;
    }

    private static BufferedImage rotate270cw(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] rgbIn = img.getRGB(0, 0, width, height, null, 0, width);
        int[] rgbOut = new int[rgbIn.length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                rgbOut[(width - 1 - x) * height + y] = rgbIn[y * width + x];
            }
        }
        BufferedImage newImage = new BufferedImage(height, width, img.getType());
        newImage.setRGB(0, 0, height, width, rgbOut, 0, height);
        return newImage;
    }
}
