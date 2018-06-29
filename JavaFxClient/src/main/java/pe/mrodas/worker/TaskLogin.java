package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Credential;
import pe.mrodas.entity.User;
import pe.mrodas.model.Login;
import pe.mrodas.model.RestClient;


public class TaskLogin extends Task<User> {

    private final Credential credential;

    public TaskLogin(String user, String pass) {
        credential = new Credential()
                .setUsername(user)
                .setPassword(pass);
    }

    @Override
    protected User call() throws Exception {
        super.updateMessage("Authenticating...");
        return RestClient.execute(Login.class, login -> login.auth(credential));
    }
}
