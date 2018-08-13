package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.util.List;

import pe.mrodas.entity.Tag;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

public class ServiceGetCategories extends Service<List<Tag>> {
    private final int idRoot;

    public ServiceGetCategories(int idRoot) {
        this.idRoot = idRoot;
    }

    @Override
    protected Task<List<Tag>> createTask() {
        return new Task<List<Tag>>() {
            @Override
            protected List<Tag> call() throws Exception {
                super.updateMessage("Getting categories...");
                return RestClient.execute(TagModel.class, model ->
                        model.getCategories(idRoot)).body();
            }
        };
    }
}
