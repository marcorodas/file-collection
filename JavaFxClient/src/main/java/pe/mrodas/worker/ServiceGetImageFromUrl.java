package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import pe.mrodas.helper.FileHelper;

public class ServiceGetImageFromUrl extends Service<Path> {

    private URL imageUrl;
    private final Path target;

    public ServiceGetImageFromUrl(Path target) {
        this.target = target;
    }

    public void setImageUrl(String url) throws Exception {
        if (url.isEmpty()) {
            throw new Exception("URL can't be empty!");
        }
        imageUrl = new URL(url);
        imageUrl.toURI();
    }

    private String getExtension(String contentType) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(contentType);
        while (readers.hasNext()) {
            ImageReaderSpi provider = readers.next().getOriginatingProvider();
            if (provider != null) {
                String[] suffixes = provider.getFileSuffixes();
                if (suffixes != null) {
                    return suffixes[0];
                }
            }
        }
        return null;
    }

    @Override
    protected Task<Path> createTask() {
        return new Task<Path>() {
            @Override
            protected Path call() throws Exception {
                Path tmp = Files.createTempFile("imgFile", ".tmp");
                super.updateMessage("Downloading...");
                URLConnection connection = imageUrl.openConnection();
                String contentType = connection.getContentType();
                try (InputStream stream = connection.getInputStream()) {
                    Files.copy(stream, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
                super.updateMessage("Hashing...");
                String fileName = FileHelper.getMD5(tmp) + "." + getExtension(contentType);
                return Files.copy(tmp, target.resolve(fileName));
            }
        };
    }
}
