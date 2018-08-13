package pe.mrodas.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import pe.wallet.imageprocess.util.JFXValidator;

import pe.mrodas.MainApp;
import pe.mrodas.entity.Credential;
import pe.mrodas.entity.Environment;
import pe.mrodas.entity.Session;
import pe.mrodas.model.LoginModel;
import pe.mrodas.model.RestClient;

public class LoginController extends BaseController {

    class TaskLogin extends Task<Session> {

        private final Credential credential;

        public TaskLogin(String user, String pass) {
            credential = new Credential()
                    .setUsername(user).setPassword(pass)
                    .setEnvironment(Environment.get());
        }

        @Override
        protected Session call() throws Exception {
            super.updateMessage("Authenticating...");
            return RestClient.execute(LoginModel.class, model -> model.auth(credential)).body();
        }
    }

    @FXML
    private VBox content;
    @FXML
    private JFXTextField txtUsername;
    @FXML
    private JFXPasswordField password;
    @FXML
    private Button btnLogin;
    @FXML
    private ProgressController progressController;

    private Service<Session> serviceLogin;

    public LoginController() {
        super("/fxml/Login.fxml");
        super.setTitle("Login");
    }

    @Override
    public void initialize() {
        txtUsername.getValidators().add(new JFXValidator.Required("Username required!"));
        password.getValidators().add(new JFXValidator.Required("Can't be empty!"));
        serviceLogin = new Service<Session>() {
            @Override
            protected Task<Session> createTask() {
                return new TaskLogin(txtUsername.getText(), password.getText());
            }
        };
        serviceLogin.setOnSucceeded(this::onLoginResponse);
        serviceLogin.setOnFailed(event -> {
            super.onServiceFailed(event);
            txtUsername.requestFocus();
        });
        content.disableProperty().bind(serviceLogin.runningProperty());
        progressController.bindService(serviceLogin);
    }

    private void onLoginResponse(WorkerStateEvent e) {
        Session session = (Session) e.getSource().getValue();
        String errorMessage = this.checkError(session);
        if (errorMessage == null) {
            MainApp.setSession(session);
            RestClient.setToken(session.getUser().getToken());
            super.handle(() -> new MenuController()
                    .setDebugMode(MainApp.debugMode())
                    .prepareStage(btnLogin).show());
        } else {
            super.dialogInfo(errorMessage);
        }
    }

    private String checkError(Session session) {
        if (session.getUser() == null) {
            return "Bad Server Response: User Null";
        }
        if (session.getUser().getToken() == null) {
            return "Bad Server Response: Token Null";
        }
        return null;
    }

    @FXML
    public void txtUsernameOnKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            password.requestFocus();
        }
    }

    @FXML
    public void txtPasswordOnKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            btnLogin.requestFocus();
            btnLogin.fire();
        }
    }

    @FXML
    public void btnLoginOnAction(ActionEvent e) {
        if (txtUsername.validate() && password.validate()) {
            serviceLogin.restart();
        }
    }
}
