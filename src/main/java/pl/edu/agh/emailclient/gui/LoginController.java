package pl.edu.agh.emailclient.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.connection.ImapServerConnection;
import pl.edu.agh.emailclient.connection.ServerConnection;
import pl.edu.agh.emailclient.connection.SmtpServerConnection;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;

import java.io.IOException;


public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML private TextField smtpServerAddress;

    @FXML private TextField imapServerAddress;

    @FXML private TextField login;

    @FXML private PasswordField password;

    @FXML private Label errorLabel;

    private GuiManager guiManager;

    public void initialize() {

    }

    public void initLoginController(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    public void login() throws IOException {
        logger.info("User " + login.getText() + " is trying to log in to: " + smtpServerAddress.getText() + " and " + imapServerAddress.getText());
        SmtpServerConnection smtpServerConnection = new SmtpServerConnection(smtpServerAddress.getText(), login.getText(), password.getText());
        boolean smtpAuthenticated = checkAuthentication(smtpServerConnection);

        ImapServerConnection imapServerConnection =  new ImapServerConnection(imapServerAddress.getText(), login.getText(), password.getText());
        boolean imapAuthenticated = checkAuthentication(imapServerConnection);

//        boolean smtpAuthenticated = true;
//        boolean imapAuthenticated = true;
        if (smtpAuthenticated && imapAuthenticated) {
            logger.info("User " + login.getText() + " connected");
            guiManager.setSmtpServerConnection(smtpServerConnection);
            guiManager.setImapServerConnection(imapServerConnection);
            guiManager.showMainWindow();
        }
    }


    public boolean checkAuthentication(ServerConnection serverConnection) {
        try {
            serverConnection.checkCredentials();
        } catch (IOException e) {
            String errorMessage = "That didn't work, check SMTP address and internet connection";
            errorLabel.setText(errorMessage);
            logger.error(errorMessage, e);
            return false;
        } catch (SmtpServerException e) {
            String errorMessage = "That didn't work, check your login and password";
            errorLabel.setText(errorMessage);
            logger.error(errorMessage, e);
            return false;
        }
        return true;
    }

}
