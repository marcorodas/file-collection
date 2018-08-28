package pe.mrodas.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import pe.mrodas.entity.Config;
import pe.mrodas.entity.Environment;
import pe.mrodas.model.ConfigModel;
import pe.mrodas.model.RestClient;

public class ServiceSaveConfig extends Service<Void> {

    private final boolean isNew;
    private final Config config = new Config().setEnvironment(Environment.get());

    public ServiceSaveConfig(boolean isNew) {
        this.isNew = isNew;
    }

    public void setWorkingDir(String workingDir) {
        config.setWorkingDir(workingDir);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                super.updateMessage("Saving config...");
                return RestClient.execute(ConfigModel.class, model ->
                        isNew ? model.insert(config) : model.update(config)).body();
            }
        };
    }
}
