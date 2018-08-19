package pe.mrodas.helper;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileHelper {

    public String getMD5(Path path) throws NoSuchAlgorithmException, IOException {
        byte[] byteArray = Files.readAllBytes(path);
        byte[] digest = MessageDigest.getInstance("MD5").digest(byteArray);
        return DatatypeConverter.printHexBinary(digest);
    }

    public void copy(File source, File target) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel targetChannel = new FileOutputStream(target).getChannel()) {
            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    private int getSeparatorPathIndex(String filename) {
        int lastUnixPos = filename.lastIndexOf(47); //  "/"
        int lastWindowsPos = filename.lastIndexOf(92);//"\"
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public String getExtension(String filename) {
        if (filename != null) {
            int dirSeparatorIndex = FileHelper.getSeparatorPathIndex(filename);
            int extensionPos = filename.lastIndexOf(46);//  "."
            return dirSeparatorIndex > extensionPos
                    ? "" : filename.substring(extensionPos + 1);
        }
        return null;
    }

    public String getName(Path path) {
        return FileHelper.getName(path.getFileName().toString());
    }

    public String getName(String filename) {
        if (filename != null) {
            int dirSeparatorIndex = FileHelper.getSeparatorPathIndex(filename);
            int extensionPos = filename.lastIndexOf(46);//  "."
            return dirSeparatorIndex > extensionPos
                    ? filename.substring(dirSeparatorIndex + 1)
                    : filename.substring(dirSeparatorIndex + 1, extensionPos);
        }
        return null;
    }

    public Properties getProperties(String pathname) {
        ClassLoader loader = FileHelper.class.getClass().getClassLoader();
        Properties prop = new Properties();
        try (InputStream file = loader.getResourceAsStream(pathname)) {
            prop.load(file);
        } catch (IOException ignored) {
        }
        return prop;
    }
}
