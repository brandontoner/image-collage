import com.brandontoner.jimagecollage.CropFunction;
import com.brandontoner.jimagecollage.DiffFunction;
import com.brandontoner.jimagecollage.ImageCollage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public enum Main {
    ;
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        ImageCollage collage = ImageCollage.builder()
                                           .withDiffFunction(DiffFunction.ssim())
                                           .withDiffFunction(DiffFunction.absRgb())
                                           .withTargetImage("D:\\target.jpg")
                                           .withSubImageDirectory("D:\\Users\\brand\\Pictures\\iCloud Photos\\Photos")
                                           .withOutputDirectory("D:\\output")
                                           .withSubSections(64)
                                           .withUsagePerImage(10)
                                           .withCropFunction(CropFunction.cropFromMiddle())
                                           .build();

        CompletableFuture<Path> future = collage.start();

        Path f = future.join();
        LOGGER.info("Generated picture at {}", f.toAbsolutePath());
    }
}
