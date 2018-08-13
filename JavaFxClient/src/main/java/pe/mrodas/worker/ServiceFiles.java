package pe.mrodas.worker;

import javafx.concurrent.Service;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import lombok.Getter;

abstract class ServiceFiles<T> extends Service<T> {
    @Getter
    private final Path dirPath;

    ServiceFiles(Path dirPath) {
        this.dirPath = dirPath;
    }

    /**
     * Checks if {@link ServiceFiles#dirPath} is a valid directory.
     * Creates the directory {@link ServiceFiles#dirPath} and all nonexistent parent directories
     * if not exists.
     *
     * @throws IOException if {@link ServiceFiles#dirPath} is invalid
     */
    void isValidDirPath() throws IOException {
        if (dirPath == null) {
            throw new IOException("Directory can't be null!");
        }
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        } else if (!Files.isDirectory(dirPath)) {
            throw new IOException("Invalid directory:\n" + dirPath.toString());
        }
    }

    /**
     * Checks if the fileList is not null or empty.
     * Also checks if {@link ServiceFiles#dirPath} is a valid directory.
     * Creates the directory {@link ServiceFiles#dirPath} and all nonexistent parent directories
     * if not exists.
     *
     * @param fileList The list to check
     * @return true if fileList is not null and not empty
     * @throws IOException if {@link ServiceFiles#dirPath} is invalid
     */
    boolean isValid(List<File> fileList) throws IOException {
        if (fileList == null || fileList.isEmpty()) {
            return false;
        }
        this.isValidDirPath();
        return true;
    }

    /**
     * A shortcut of {@link ServiceFiles#getDirPath()}.toFile().listFiles(filter).
     * Also checks if {@link ServiceFiles#dirPath} is a valid directory.
     * Creates the directory {@link ServiceFiles#dirPath} and all nonexistent parent directories
     * if not exists.
     *
     * @param filter A file filter
     * @return An array of abstract pathnames denoting the files and directories in the directory {@link ServiceFiles#dirPath}
     * @throws IOException - If can't read the directory {@link ServiceFiles#dirPath}
     * @see java.io.File#listFiles(java.io.FileFilter)
     */
    File[] listFiles(FileFilter filter) throws IOException {
        this.isValidDirPath();
        return dirPath.toFile().listFiles(filter);
    }

    /**
     * A shortcut of {@link ServiceFiles#getDirPath()}.resolve(other)
     *
     * @param other the path string to resolve against this pat
     * @return the resulting path
     * @see java.nio.file.Path#resolve(String)
     */
    Path resolve(String other) {
        return dirPath.resolve(other);
    }
}
