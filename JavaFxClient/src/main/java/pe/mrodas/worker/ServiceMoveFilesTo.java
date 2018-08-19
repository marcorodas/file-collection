package pe.mrodas.worker;

import javafx.concurrent.Task;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.mrodas.helper.FileHelper;

public class ServiceMoveFilesTo extends ServiceFiles<List<Path>> {

    public interface FileNameBuilder {
        String build(Path filePath) throws Exception;
    }

    private List<File> sourceFiles;
    private FileNameBuilder fullNameBuilder;

    public ServiceMoveFilesTo(Path targetPath) {
        super(targetPath);
    }

    public void setSourceFiles(List<File> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFiles = Collections.singletonList(sourceFile);
    }

    public void setFileNameBuilder(FileNameBuilder nameBuilder) {
        this.fullNameBuilder = source -> {
            String name = nameBuilder.build(source);
            String extension = FileHelper.getExtension(source.getFileName().toString());
            return String.format("%s.%s", name, extension);
        };
    }

    private Path move(Path source) throws Exception {
        Path targetFile = super.resolve(fullNameBuilder.build(source));
        if (Files.exists(targetFile)) {
            Files.delete(source);
        } else {
            Files.move(source, targetFile);
        }
        return targetFile;
    }

    @Override
    protected Task<List<Path>> createTask() {
        if (fullNameBuilder == null) {
            this.setFileNameBuilder(FileHelper::getName);
        }
        return new Task<List<Path>>() {
            @Override
            protected List<Path> call() throws Exception {
                List<Path> uploaded = new ArrayList<>();
                super.updateMessage("Moving files...");
                if (!ServiceMoveFilesTo.super.isValid(sourceFiles)) return null;
                int total = sourceFiles.size();
                for (int i = 0; i < total; i++) {
                    this.updateProgress(i, total);
                    Path src = sourceFiles.get(i).toPath();
                    String format = String.format("Moving file [%d/%d]: %s ...", i, total, src.getFileName());
                    this.updateMessage(format);
                    Path target = ServiceMoveFilesTo.this.move(src);
                    uploaded.add(target);
                    this.updateProgress(i + 1, total);
                }
                return uploaded;
            }
        };
    }
}
