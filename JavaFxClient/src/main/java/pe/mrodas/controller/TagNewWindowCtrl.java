package pe.mrodas.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import java.util.function.Predicate;

import pe.mrodas.entity.Tag;
import pe.mrodas.model.RestClient;
import pe.mrodas.model.TagModel;

class TagNewWindowCtrl {
    private final VBox vBoxNewTag;
    private final TextField txtNewTag;
    private Consumer<Service<?>> bindService;
    private int idRoot;
    private Tag tagToSave;
    private final Service<Tag> serviceSaveTag = new Service<Tag>() {
        @Override
        protected Task<Tag> createTask() {
            return new Task<Tag>() {
                @Override
                protected Tag call() throws Exception {
                    super.updateMessage(String.format("Saving tag \"%s\" ...", tagToSave.getName()));
                    return RestClient.execute(TagModel.class, tagModel -> tagModel.save(idRoot, tagToSave)).body();
                }
            };
        }
    };


    TagNewWindowCtrl(Integer idRoot, VBox vBoxNewTag, TextField txtNewTag) {
        this.idRoot = idRoot;
        this.vBoxNewTag = vBoxNewTag;
        this.vBoxNewTag.setVisible(false);
        this.txtNewTag = txtNewTag;
        this.txtNewTag.setOnAction(event -> this.save());
    }

    void setConfig(EventHandler<WorkerStateEvent> onServiceFailed, Consumer<Service<?>> bindService, Consumer<String> showWarning, Predicate<Tag> savedTagIsCategory) {
        serviceSaveTag.setOnFailed(onServiceFailed);
        serviceSaveTag.setOnSucceeded(event -> {
            Tag savedTag = serviceSaveTag.getValue();
            boolean isCategory = savedTagIsCategory.test(savedTag);
            if (isCategory) {
                showWarning.accept(String.format("Unable to Save!\n\"%s\" is a Category!", tagToSave.getName()));
                txtNewTag.requestFocus();
            } else {
                vBoxNewTag.setVisible(false);
            }
        });
        this.bindService = bindService;
    }

    void save() {
        tagToSave = new Tag().setName(txtNewTag.getText());
        bindService.accept(serviceSaveTag);
        serviceSaveTag.restart();
    }

}
