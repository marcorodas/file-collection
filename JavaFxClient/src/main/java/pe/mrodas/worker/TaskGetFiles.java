package pe.mrodas.worker;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskGetFiles extends Task<List<File>> {

    private final String workingDir, relativePath;
    private final FileFilter filter;

    public TaskGetFiles(String workingDir, String relativePath, FileFilter filter) {
        this.workingDir = workingDir;
        this.relativePath = relativePath;
        this.filter = filter;
    }

    @Override
    protected List<File> call() throws Exception {
        super.updateMessage("Getting files...");
        if (workingDir == null) {
            throw new IOException("Working directory is not set!");
        }
        if (relativePath == null) {
            throw new IOException("Relative Path can't be null!");
        }
        File file = Paths.get(workingDir, relativePath).toFile();
        if (!file.exists() && !file.mkdir()) {
            throw new IOException("Unable to create directory:\n" + file.getAbsolutePath());
        }
        if (!file.isDirectory()) {
            throw new IOException("Invalid directory:\n" + file.getAbsolutePath());
        }
        File[] files = file.listFiles(filter);
        return files == null ? new ArrayList<>() : Arrays.asList(files);
    }
}
