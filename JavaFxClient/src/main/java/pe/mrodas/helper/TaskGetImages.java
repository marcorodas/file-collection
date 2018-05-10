package pe.mrodas.helper;

import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.entity.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaskGetImages extends Task<List<FileItem>> {
    @Override
    protected List<FileItem> call() throws Exception {
        this.updateProgress(-1, -1);
        this.updateMessage("Getting fileItems...");
        List<FileItem> fileItems = new ArrayList<>();
        File workingDir = new File(MainApp.getWorkingDir());
        File[] imageFiles = FileHelper.getImageFiles(workingDir);
        int i = 0;
        for (File imageFile : imageFiles) {
            this.updateMessage(String.format("Checksum '%s'...", imageFile.getName()));
            String md5 = FileHelper.getMD5(imageFile.toPath());
            FileItem fileItem = new FileItem()
                    .setName(imageFile.getName())
                    .setMd5(md5);
            fileItems.add(fileItem);
            this.updateProgress(i++, imageFiles.length);
        }
        return fileItems;
    }
}
