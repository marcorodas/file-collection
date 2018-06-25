package pe.mrodas.worker;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import pe.mrodas.MainApp;
import pe.mrodas.entity.Credential;
import pe.mrodas.entity.User;
import pe.mrodas.model.Login;
import pe.mrodas.model.RestClient;
import pe.mrodas.rest.ApiError;


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

    private String onError(String errorBody) {
        ApiError apiError = new Gson().fromJson(errorBody, ApiError.class);
        String trace = apiError.getTrace();
        if (trace != null) {
            System.out.println(trace);
        }
        return apiError.getMessage();
    }

    @Override
    protected Void call() throws Exception {
        super.updateMessage("Authenticating...");
        RestClient.execute(Login.class, login -> login.auth(credential), this::onSuccess, this::onError);
        return null;
    }
}
