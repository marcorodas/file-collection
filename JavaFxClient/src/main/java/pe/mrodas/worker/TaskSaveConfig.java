package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.entity.Config;
import pe.mrodas.model.ConfigModel;
import pe.mrodas.model.RestClient;

public class TaskSaveConfig extends Task<Void> {
    private final boolean isNew;
    private final Config config;

    public TaskSaveConfig(boolean isNew, Config config) {
        this.isNew = isNew;
        this.config = config;
    }


    @Override
    protected Void call() throws Exception {
        super.updateMessage("Saving config...");
        return RestClient.execute(ConfigModel.class, model -> isNew ? model.insert(config) : model.update(config));
    }
}
