package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Setter;

import java.io.File;
import java.util.List;

public class ServiceImportFiles extends Service<Void> {

    @Setter
    private List<File> fileList;

    @Override
    protected Task<Void> createTask() {
        return new TaskImportFiles(fileList);
    }
}
