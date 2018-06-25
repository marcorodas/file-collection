package pe.mrodas.helper;

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

    @Override
    protected Void call() throws Exception {
        User user = RestClient.create(Login.class)
                .auth(credential)
                .execute().body();
        super.updateMessage("Authenticating...");
        if (user == null) {
            super.updateMessage("Error: user == null");
            throw new Exception(super.getMessage());
        } else {
            RestClient.setToken(user.getToken());
            MainApp.session().setPerson(user.getPerson());
        }
        return null;
    }
}
