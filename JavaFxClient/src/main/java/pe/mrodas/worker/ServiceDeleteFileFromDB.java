package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import lombok.Setter;

import pe.mrodas.model.FileModel;
import pe.mrodas.model.RestClient;

public class ServiceDeleteFileFromDB extends Service<Void> {
    @Setter
    private String md5;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                RestClient.execute(FileModel.class, fileModel -> fileModel.delete(md5));
                return null;
            }
        };
    }
}
