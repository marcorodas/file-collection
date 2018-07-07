package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.helper.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TaskMoveFiles extends Task<Void> {
    private final String workingDir, targetDir;
    private final List<File> sourceFiles;

    public TaskMoveFiles(List<File> sourceFiles, String workingDir, String targetDir) {
        this.workingDir = workingDir;
        this.targetDir = targetDir;
        this.sourceFiles = sourceFiles;
    }

    @Override
    protected Void call() throws Exception {
        super.updateMessage("Importing files...");
        if (workingDir == null) {
            throw new IOException("Working directory is not set!");
        }
        if (targetDir == null) {
            throw new IOException("Destiny directory can't be null!");
        }
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return null;
        }
        Path targetDirPath = Paths.get(workingDir, targetDir);
        if (Files.notExists(targetDirPath)) {
            Files.createDirectory(targetDirPath);
        }
        if (!Files.isDirectory(targetDirPath)) {
            throw new IOException("Invalid directory:\n" + targetDirPath.toString());
        }
        this.execute(targetDirPath);
        return null;
    }

    private void execute(Path targetDirPath) throws NoSuchAlgorithmException, IOException {
        int total = sourceFiles.size();
        for (int i = 0; i < total; i++) {
            String srcName = sourceFiles.get(i).getName();
            this.showProgress(i, total, srcName);
            Path src = sourceFiles.get(i).toPath();
            String targetName = FileHelper.getMD5(src) + "." + FileHelper.getExtension(srcName);
            Path target = targetDirPath.resolve(targetName);
            if (Files.exists(target)) {
                Files.delete(src);
            } else {
                Files.move(src, target);
            }
        }
    }

    private void showProgress(int i, int total, String name) {
        this.updateProgress(i, total);
        String format = String.format("Reading file [%d/%d]: %s ...", i, total, name);
        this.updateMessage(format);
    }
}
