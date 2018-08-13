package pe.mrodas.worker;

import javafx.concurrent.Task;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import lombok.Setter;
import okhttp3.ResponseBody;

import pe.mrodas.entity.FileItem;
import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;

public class ServiceGetMissingFiles extends ServiceFiles<Void> {

    private final Integer idRoot;
    @Setter
    private List<String> md5List;

    public ServiceGetMissingFiles(Path path, Integer idRoot) {
        super(path);
        this.idRoot = idRoot;
    }

    private void downloadItem(FileItem item) throws Exception {
        String itemName = String.format("%s.%s", item.getMd5(), item.getExtension());
        File file = super.resolve(itemName).toFile();
        ResponseBody body = RestClient.execute(FileModel.class, fileModel -> fileModel.getFile(item.getIdFile())).body();
        this.saveToDisk(file, body);
    }

    private void saveToDisk(File fileToSave, ResponseBody body) throws IOException {
        try (InputStream inputStream = body.byteStream();
             FileOutputStream outputStream = new FileOutputStream(fileToSave)
        ) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ServiceGetMissingFiles.super.isValidDirPath();
                super.updateMessage("Getting Missing Files...");
                List<FileItem> fileItems = RestClient.execute(FileModel.class, fileModel -> fileModel.getMissingFilesId(idRoot, md5List)).body();
                int fileItemsSize = fileItems == null ? 0 : fileItems.size();
                for (int i = 0; i < fileItemsSize; i++) {
                    String progress = String.format("[%d/%d] Downloading...", i + 1, fileItemsSize);
                    this.updateMessage(progress);
                    ServiceGetMissingFiles.this.downloadItem(fileItems.get(i));
                    this.updateProgress(i, fileItemsSize);
                }
                return null;
            }
        };
    }
}
