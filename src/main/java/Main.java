import com.brandontoner.jimagecollage.ImageCollage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        final ImageCollage collage = ImageCollage.builder()
                                                 .withTargetImage("D:\\target.jpg")
                                                 .withSubImageDirectory("D:\\New folder")
                                                 .withOutputDirectory("D:\\output")
                                                 .withSubSections(16)
                                                 .build();

        final CompletableFuture<Path> future = collage.start();

        final Path f = future.join();
        System.err.println(f.toAbsolutePath());
    }
}
