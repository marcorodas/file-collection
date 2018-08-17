package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Setter;

import pe.mrodas.entity.FileItem;
import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;

public class ServiceGetFileNames extends Service<List<String>> {
    @Setter
    private List<Integer> tagsId;
    private int categoryId;

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        tagsId = null;
    }

    @Override
    protected Task<List<String>> createTask() {
        return new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                super.updateMessage("Getting Files...");
                List<FileItem> fileItems = tagsId == null
                        ? RestClient.execute(FileModel.class, fileModel ->
                        fileModel.getFilesUntagged(categoryId)).body()
                        : RestClient.execute(FileModel.class, fileModel ->
                        fileModel.getFiles(tagsId)).body();
                return fileItems == null ? new ArrayList<>() : fileItems.stream()
                        .map(item -> String.format("%s.%s", item.getMd5(), item.getExtension()))
                        .collect(Collectors.toList());
            }
        };
    }
}
