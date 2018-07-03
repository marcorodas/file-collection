package pe.mrodas.worker;

import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Credential;
import pe.mrodas.entity.Environment;
import pe.mrodas.entity.Session;
import pe.mrodas.entity.User;
import pe.mrodas.model.Login;
import pe.mrodas.model.RestClient;


public class TaskLogin extends Task<Session> {

    private final Credential credential;

    public TaskLogin(String user, String pass) {
        credential = new Credential()
                .setUsername(user)
                .setPassword(pass)
                .setEnvironment(Environment.get());
    }

    @Override
    protected Session call() throws Exception {
        super.updateMessage("Authenticating...");
        return RestClient.execute(Login.class, login -> login.auth(credential));
    }
}
