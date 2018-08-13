package pe.mrodas.worker;

import javafx.concurrent.Task;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Setter;

public class ServiceReadFiles extends ServiceFiles<List<File>> {
    @Setter
    private FileFilter filter;

    public ServiceReadFiles(Path path, FileFilter filter) {
        super(path);
        this.filter = filter;
    }

    @Override
    protected Task<List<File>> createTask() {
        return new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                super.updateMessage("Reading files...");
                File[] files = ServiceReadFiles.super.listFiles(filter);
                return files == null ? new ArrayList<>() : Arrays.asList(files);
            }
        };
    }
}
