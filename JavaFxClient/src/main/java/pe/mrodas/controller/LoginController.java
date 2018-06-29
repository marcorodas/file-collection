package pe.mrodas.controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import pe.mrodas.MainApp;
import pe.mrodas.entity.User;
import pe.mrodas.model.RestClient;
import pe.mrodas.worker.TaskLogin;
import pe.wallet.imageprocess.util.JFXValidator;

public class LoginController extends BaseController {

    @FXML
    public VBox content;
    @FXML
    private JFXTextField txtUsername;
    @FXML
    private JFXPasswordField password;
    @FXML
    private Button btnLogin;
    @FXML
    private ProgressController progressController;

    private Service<User> loginService;

    public LoginController() {
        super("/fxml/Login.fxml");
        super.setTitle("Login");
    }

    @Override
    public void initialize() {
        txtUsername.getValidators().add(new JFXValidator.Required("Username required!"));
        password.getValidators().add(new JFXValidator.Required("Can't be empty!"));
        this.setServices();
    }

    private void setServices() {
        loginService = new Service<User>() {
            @Override
            protected Task<User> createTask() {
                return new TaskLogin(txtUsername.getText(), password.getText());
            }
        };
        loginService.setOnSucceeded(this::onLoginResponse);
        loginService.setOnFailed(super::onServiceFailed);
        content.disableProperty().bind(loginService.runningProperty());
        progressController.setService(loginService);
    }

    private void onLoginResponse(WorkerStateEvent e) {
        User user = (User) e.getSource().getValue();
        if (user == null) {
            super.infoAlert("Invalid user or password");
            txtUsername.requestFocus();
        } else {
            RestClient.setToken(user.getToken());
            MainApp.session().setUser(user);
            super.infoAlert("Login Success! User:" + user.getPerson().getFullName());
            super.handle(() -> {
                //new MenuController().prepareStage(btnLogin).show();
            });
        }
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
            loginService.restart();
        }
    }
}
