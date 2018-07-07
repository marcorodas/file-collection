package pe.mrodas.worker;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskGetFiles extends Task<List<File>> {

    private final String workingDir, relativePath;
    private List<String> extensions;

    public TaskGetFiles(String workingDir, String relativePath) {
        this.workingDir = workingDir;
        this.relativePath = relativePath;
    }

    public TaskGetFiles(String workingDir, String relativePath, Stream<String> extensions) {
        this(workingDir, relativePath);
        this.extensions = extensions
                .map(s -> s.replace("*", ""))
                .collect(Collectors.toList());
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
        File[] files = file.listFiles(extensions == null ? null : this::filter);
        return files == null ? new ArrayList<>() : Arrays.asList(files);
    }

    private boolean filter(File file) {
        return extensions.stream().anyMatch(s -> file.getName().endsWith(s));
    }

}
