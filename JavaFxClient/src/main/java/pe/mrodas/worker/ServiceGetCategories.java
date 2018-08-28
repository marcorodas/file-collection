package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Setter;
import pe.mrodas.entity.Tag;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

import java.util.List;

public class ServiceGetCategories extends Service<List<Tag>> {
    @Setter
    private int idRoot;

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
