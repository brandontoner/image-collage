package com.brandontoner.jimagecollage;

import javax.annotation.CheckForNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

final class MasterImage {
    private static final Logger LOGGER = LogManager.getLogger(MasterImage.class);
    @Nonnull
    private final Image[][] subSections;
    @Nonnull
    private final Diff[][] bestImages;
    @Nonnull
    private final long[][] diffs;
    private final int subSectionsX;
    private final int subSectionsY;
    private final int subSectionWidth;
    private final int subSectionHeight;

    MasterImage(@Nonnull final Path target, final int subSectionsX, final int subSectionsY) throws IOException {
        Image image = new Image(ImageIO.read(target.toFile()));
        this.subSectionsX = subSectionsX;
        this.subSectionsY = subSectionsY;

        this.subSectionWidth = image.getWidth() / subSectionsX;
        this.subSectionHeight = image.getHeight() / subSectionsY;

        this.subSections = new Image[subSectionsY][subSectionsX];
        this.bestImages = new Diff[subSectionsY][subSectionsX];
        this.diffs = new long[subSectionsY][subSectionsX];


        for (int y = 0; y < subSectionsY; ++y) {
            for (int x = 0; x < subSectionsX; ++x) {
                this.diffs[y][x] = Long.MAX_VALUE;
                this.subSections[y][x] = image.subImage(x * this.subSectionWidth,
                                                        y * this.subSectionHeight,
                                                        this.subSectionWidth,
                                                        this.subSectionHeight);
            }
        }
    }

    @Nonnull
    private static BufferedImage scale(final BufferedImage bi, final int w, final int h) {
        final BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = resizedImg.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, w, h);
        g2.drawImage(bi, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    public void add(@Nonnull Diff entry) {
        long[][] diffArray = entry.diffs();
        if (diffArray == null) {
            return;
        }

        int bestX = -1;
        int bestY = -1;
        long bestDiff = Long.MAX_VALUE;

        for (int y = 0; y < this.subSectionsY; ++y) {
            for (int x = 0; x < this.subSectionsX; ++x) {
                final long diff = diffArray[y][x];
                if (diff < this.diffs[y][x] && diff < bestDiff) {
                    // better than the diff for this image, and better this best diff
                    bestX = x;
                    bestY = y;
                    bestDiff = diff;
                }
            }
        }

        if (bestX != -1) {
            this.diffs[bestY][bestX] = bestDiff;
            final Diff oldFile = this.bestImages[bestY][bestX];
            this.bestImages[bestY][bestX] = entry;
            if (oldFile != null) {
                // RE-PROCESS OVERWRITTEN FILE
                this.add(oldFile);
            }
        }
    }

    private boolean checkAspectRatio(@Nonnull BufferedImage bi) {
        double thisAspectRatio = (double) subSectionWidth / subSectionHeight;
        double thatAspectRatio = (double) bi.getWidth() / bi.getHeight();
        double ratio = thatAspectRatio / thisAspectRatio;
        return 0.99 < ratio && ratio < 1.01;
    }

    @Nonnull
    BufferedImage compile() throws IOException {
        LOGGER.info("Compiling images into collage");
        BufferedImage bi = ImageIO.read(bestImages[0][0].path().toFile());
        int scale = getScale(bi.getWidth(), bi.getHeight());
        int width = bi.getWidth() / scale;
        int height = bi.getHeight() / scale;
        final BufferedImage output =
                new BufferedImage(width * this.subSectionsX, height * this.subSectionsY, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = output.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, output.getWidth(), output.getHeight());
        for (int y = 0; y < this.subSectionsY; ++y) {
            for (int x = 0; x < this.subSectionsX; ++x) {
                if (this.bestImages[y][x] != null) {
                    g2.drawImage(ImageIO.read(this.bestImages[y][x].path().toFile()),
                                 x * width,
                                 y * height,
                                 width,
                                 height,
                                 null);
                }
            }
        }
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

    @CheckForNull
    Diff diff(@Nonnull final Path path) {
        try {
            final BufferedImage bi = ImageIO.read(path.toFile());
            if (bi == null) {
                LOGGER.error("Cannot load {}", path);
                return null;
            }
            if (!checkAspectRatio(bi)) {
                LOGGER.warn("File {} has bad aspect ratio", path);
                return null;
            }
            LOGGER.info("Loading file {}", path);

            final BufferedImage scaled = scale(bi, this.subSectionWidth, this.subSectionHeight);
            final Image scaledImage = new Image(scaled);
            long[][] output = new long[subSectionsY][subSectionsX];

            for (int y = 0; y < this.subSectionsY; ++y) {
                for (int x = 0; x < this.subSectionsX; ++x) {
                    output[y][x] = this.subSections[y][x].diff(scaledImage);
                }
            }
            return new Diff(path, output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    record Diff(Path path, long[][] diffs) {
    }
}
