package pe.mrodas.worker;

import javafx.concurrent.Task;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;

public class ServiceUploadFiles extends ServiceFiles<Void> {
    private List<File> filesToUpload;
    private Integer idTag;
    @Getter
    private List<String> uploadedFileNames = new ArrayList<>();

    public ServiceUploadFiles(Path target) {
        super(target);
    }

    public void config(List<File> uploadItems, Integer idTag) {
        this.filesToUpload = uploadItems;
        this.idTag = idTag;
    }

    private boolean isValid() throws IOException {
        if (idTag == null || idTag <= 0) {
            throw new IOException("idTag can't be null, zero or negative!");
        }
        return super.isValid(filesToUpload);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                super.updateMessage("Uploading files...");
                if (!ServiceUploadFiles.this.isValid()) return null;
                HashMap<String, String> map = new HashMap<>();
                map.put("idTag", String.valueOf(idTag));
                Map<String, RequestBody> bodyMap = RestClient.createBodyPart(map);
                int total = filesToUpload.size();
                for (int i = 0; i < total; i++) {
                    this.updateProgress(i, total);
                    File file = filesToUpload.get(i);
                    Path targetFile = ServiceUploadFiles.this.resolve(file.getName());
                    String progress = String.format("[%d/%d] ", i + 1, total);
                    this.updateMessage(progress + "Preparing...");
                    MultipartBody.Part bodyFile = RestClient.createBodyPart("file", file);
                    this.updateMessage(progress + "Uploading...");
                    Response<ResponseBody> response = RestClient.execute(FileModel.class, fileModel -> fileModel.uploadFile(bodyMap, bodyFile));
                    if (response.raw().code() != HttpURLConnection.HTTP_CREATED) {
                        progress += "File Already Uploaded! ";
                    }
                    if (Files.exists(targetFile)) {
                        this.updateMessage(progress + "And Exists in FileSystem! Deleting...");
                        Files.delete(file.toPath());
                    } else {
                        this.updateMessage(progress + "Moving...");
                        Files.move(file.toPath(), targetFile);
                    }
                    uploadedFileNames.add(file.getName());
                    this.updateProgress(i + 1, total);
                }
                return null;
            }
        };
    }
}
