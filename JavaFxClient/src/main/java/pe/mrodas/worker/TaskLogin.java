package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Credential;
import pe.mrodas.entity.User;
import pe.mrodas.model.Login;
import pe.mrodas.model.RestClient;


public class TaskLogin extends Task<Void> {

    private final Credential credential;

    public TaskLogin(String user, String pass) {
        credential = new Credential()
                .setUsername(user)
                .setPassword(pass);
    }

    private void onSuccess(User user) {
        RestClient.setToken(user.getToken());
        MainApp.session().setPerson(user.getPerson());
    }

    @Override
    protected Void call() throws Exception {
        super.updateMessage("Authenticating...");
        RestClient.execute(Login.class, login -> login.auth(credential), this::onSuccess, info -> MainApp.onError(info, false));
        return null;
    }
}
