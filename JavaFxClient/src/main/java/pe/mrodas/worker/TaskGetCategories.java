package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.entity.Tag;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

import java.util.List;

public class TaskGetCategories extends Task<List<Tag>> {
    private final int idRoot;

    public TaskGetCategories(int idRoot) {
        this.idRoot = idRoot;
    }

    @Override
    protected List<Tag> call() throws Exception {
        super.updateMessage("Getting categories...");
        return RestClient.execute(TagModel.class, model -> model.getCategories(idRoot));
    }
}
