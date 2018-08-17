package pe.mrodas.worker;

import javafx.concurrent.Task;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import lombok.Setter;

import pe.mrodas.helper.FileHelper;

public class ServiceMoveFiles extends ServiceFiles<Void> {
    @Setter
    private List<File> sourceFiles;

    public ServiceMoveFiles(Path target) {
        super(target);
    }

    private void move(Path sourceFile) throws NoSuchAlgorithmException, IOException {
        String sourceName = sourceFile.getFileName().toString();
        String targetName = String.format("%s.%s", FileHelper.getMD5(sourceFile), FileHelper.getExtension(sourceName));
        Path targetFile = super.resolve(targetName);
        if (Files.exists(targetFile)) {
            Files.delete(sourceFile);
        } else {
            Files.move(sourceFile, targetFile);
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                super.updateMessage("Importing files...");
                if (!ServiceMoveFiles.super.isValid(sourceFiles)) return null;
                int total = sourceFiles.size();
                for (int i = 0; i < total; i++) {
                    this.updateProgress(i, total);
                    Path src = sourceFiles.get(i).toPath();
                    String format = String.format("Reading file [%d/%d]: %s ...", i, total, src.getFileName());
                    this.updateMessage(format);
                    ServiceMoveFiles.this.move(src);
                    this.updateProgress(i + 1, total);
                }
                return null;
            }
        };
    }
}
