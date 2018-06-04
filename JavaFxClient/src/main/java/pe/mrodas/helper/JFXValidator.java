package pe.wallet.imageprocess.util;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;

/**
 *
 * @author skynet
 */
public class JFXValidator {

    private static final Node ICON = new FontAwesomeIconView(FontAwesomeIcon.WARNING);

    public static class Required extends ValidatorBase {

        public Required(String message) {
            super.setMessage(message);
            super.setIcon(ICON);
        }

        @Override
        protected void eval() {
            Node node = srcControl.get();
            if (node instanceof TextInputControl) {
                String text = ((TextInputControl) node).getText();
                hasErrors.set(text == null || text.isEmpty());
            } else if (node instanceof JFXComboBox<?>) {
                JFXComboBox<?> combo = (JFXComboBox<?>) node;
                String text = combo.isEditable() ? combo.getEditor().getText() : null;
                boolean textIsEmptyOrNull = text == null || text.isEmpty();
                hasErrors.set(combo.getValue() == null && textIsEmptyOrNull);
            }
        }

    }

    public static class Email extends ValidatorBase {

        public Email() {
            super.setMessage("Invalid email!");
            super.setIcon(ICON);
        }

        @Override
        protected void eval() {
            String email = ((TextInputControl) srcControl.get()).getText();
            if (email == null || email.trim().isEmpty()) {
                hasErrors.set(false);
            } else {
                String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
                Matcher matcher = Pattern.compile(regex).matcher(email.trim());
                hasErrors.set(!matcher.matches());
            }
        }

    }

    public static class Repassword extends ValidatorBase {

        private final JFXPasswordField password;

        public Repassword(JFXPasswordField password) {
            this.password = password;
            super.setMessage("Don't match!");
            super.setIcon(ICON);
        }

        @Override
        protected void eval() {
            String text = ((JFXPasswordField) srcControl.get()).getText();
            boolean val = text == null || text.trim().isEmpty()
                    || !text.trim().equals(password.getText());
            hasErrors.set(val);
        }

    }
}
