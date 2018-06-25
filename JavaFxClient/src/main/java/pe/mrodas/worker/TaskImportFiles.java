package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.helper.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskImportFiles extends Task<Void> {

    private final List<File> fileList;
    private final Path workingDir = MainApp.session().getWorkingDir();

    public TaskImportFiles(List<File> fileList) {
        this.fileList = fileList;
    }

    @Override
    protected Void call() throws Exception {
        this.updateProgress(-1, -1);
        this.updateMessage("Reading working directory...");
        if (workingDir == null) {
            this.updateMessage("Error: working directory can't be null");
            return null;
        }
        if (Files.exists(workingDir)) {
            if (!Files.isDirectory(workingDir)) {
                this.updateMessage("Error: working directory must be a directory");
                return null;
            }
        } else {
            Files.createDirectory(workingDir);
        }
        if (fileList == null) {
            this.updateMessage("Error: fileList must be set");
            return null;
        }
        this.importFiles();
        this.updateProgress(1, 1);
        this.updateMessage("Ready");
        return null;
    }

    private File getFile(int i) {
        File file = fileList.get(i);
        this.updateProgress(i, fileList.size());
        String format = String.format("Reading file [%d/%d]: %s ...", i, fileList.size(), file.getName());
        this.updateMessage(format);
        return file;
    }

    private void importFiles() throws NoSuchAlgorithmException, IOException {
        File[] files = workingDir.toFile().listFiles();
        List<String> checksumList = Stream
                .of(files == null ? new File[]{} : files)
                .map(File::getName)
                .collect(Collectors.toList());
        for (int i = 0; i < fileList.size(); i++) {
            File file = this.getFile(i);
            String name = FileHelper.getMD5(file.toPath());
            name += "." + FileHelper.getExtension(file.getName());
            if (!checksumList.contains(name)) {
                FileHelper.copy(file, workingDir.resolve(name).toFile());
            }
            Files.delete(file.toPath());
        }
    }
}
