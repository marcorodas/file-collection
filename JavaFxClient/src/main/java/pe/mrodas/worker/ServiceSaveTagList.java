package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;

import pe.mrodas.entity.TagListsToSave;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

public class ServiceSaveTagList extends Service<Void> {
    private String md5;
    private TagListsToSave tagListsToSave;

    public void config(File file, TagListsToSave tagListsToSave) {
        this.md5 = FileHelper.getName(file.getName());
        this.tagListsToSave = tagListsToSave;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                super.updateMessage("Saving Tags...");
                RestClient.execute(TagModel.class, tagModel -> tagModel.saveTagLists(md5, tagListsToSave));
                return null;
            }
        };
    }
}
