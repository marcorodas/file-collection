package pe.mrodas.helper;

import javafx.concurrent.Task;

public class TaskLogin extends Task<Void> {

    private final String user, pass;

    public TaskLogin(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    @Override
    protected Void call() throws Exception {
        super.updateMessage("Authenticating...");
        return null;
    }
}
