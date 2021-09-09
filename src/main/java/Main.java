import com.brandontoner.jimagecollage.ImageCollage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class Main {
    public static final Logger LOGGER = LogManager.getLogger(Main.class);

    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        final ImageCollage collage = ImageCollage.builder()
                                                 .withTargetImage("D:\\target.jpg")
                                                 .withSubImageDirectory(
                                                         "D:\\Users\\brand\\Pictures\\iCloud Photos\\Photos")
                                                 .withOutputDirectory("D:\\output")
                                                 .withSubSections(32)
                                                 .build();

        final CompletableFuture<Path> future = collage.start();

        final Path f = future.join();
        LOGGER.info("Generated picture at {}", f.toAbsolutePath());
    }
}
