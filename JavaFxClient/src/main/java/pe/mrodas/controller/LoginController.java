package pe.mrodas.controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import pe.mrodas.MainApp;
import pe.mrodas.worker.TaskLogin;
import pe.wallet.imageprocess.util.JFXValidator;

public class LoginController extends BaseController {

    @FXML
    public JFXTextField txtUsername;
    @FXML
    public JFXPasswordField password;
    @FXML
    public Text txtService, txtInvalid;
    @FXML
    public JFXProgressBar progress;
    @FXML
    public Button btnLogin;

    private Service<Void> loginService;

    public LoginController() {
        super("/fxml/Login.fxml");
        super.setTitle("Login");
    }

    private void showTxtInvalid(boolean show) {
        txtInvalid.setManaged(show);
        txtInvalid.setVisible(show);
    }

    @Override
    public void initialize() {
        txtUsername.getValidators().add(new JFXValidator.Required("Username required!"));
        password.getValidators().add(new JFXValidator.Required("Can't be empty!"));
        this.showTxtInvalid(false);
        this.setServices();
    }

    private void setServices() {
        loginService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new TaskLogin(txtUsername.getText(), password.getText());
            }
        };
        loginService.setOnSucceeded(this::onLoginResponse);
        loginService.setOnFailed(super::onServiceFailed);
        BooleanProperty[] properties = new BooleanProperty[]{
                txtService.visibleProperty(), txtService.managedProperty(),
                txtUsername.disableProperty(), password.disableProperty(),
                btnLogin.disableProperty(), progress.visibleProperty()
        };
        for (BooleanProperty property : properties) {
            property.bind(loginService.runningProperty());
        }
        txtService.textProperty().bind(loginService.messageProperty());
    }

    private void onLoginResponse(WorkerStateEvent e) {
        if (MainApp.session().getPerson() == null) {
            this.showTxtInvalid(true);
            txtUsername.requestFocus();
        } else {
            super.handle(() -> {

                new MenuController().prepareStage(btnLogin).show();
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
            this.showTxtInvalid(false);
            loginService.restart();
        }
    }
}
