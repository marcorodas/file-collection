package pe.mrodas.helper;
import lombok.experimental.UtilityClass;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

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

    public String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastUnixPos = filename.lastIndexOf(47); //  "/"
        int lastWindowsPos = filename.lastIndexOf(92);//"\"
        int extensionPos = filename.lastIndexOf(46);//  "."
        int index = Math.max(lastUnixPos, lastWindowsPos) > extensionPos ? -1 : extensionPos;
        return index == -1 ? "" : filename.substring(index + 1);
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
