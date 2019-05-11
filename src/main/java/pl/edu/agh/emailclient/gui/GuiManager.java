package pl.edu.agh.emailclient.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.connection.*;
import pl.edu.agh.emailclient.exceptions.EmailException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.IOException;


public class GuiManager {

    private static final Logger logger = LogManager.getLogger(ImapServerConnection.class);

    private Stage primaryStage;

    private Scene scene;

    private SmtpServerConnection smtpServerConnection;

    private ImapServerConnection imapServerConnection;

    private Image icon = new Image("icon.png");

    private EmailReceiver emailReceiver;

    private EmailSender emailSender;

    public GuiManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showLoginWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fx/login.fxml"));
            scene = new Scene(fxmlLoader.load());
            LoginController loginController = fxmlLoader.getController();
            loginController.initLoginController(this);
//        scene.getStylesheets().add("/fx/styles.css");
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Could not open login window", e.getMessage(), e);
        }
    }

    public void showMainWindow() {
        try {
            primaryStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/main.fxml"));
            scene = new Scene(loader.load());
            MainGuiController mainGuiController = loader.getController();
            mainGuiController.initMainController(this, emailReceiver);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Simple Email Client");
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Could not open main window", e.getMessage(), e);
        }
    }

    public void showNewMailWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/newMail.fxml"));
            scene = new Scene(loader.load());
            NewMailController controller = loader.getController();
            Stage secondaryStage = new Stage();
            controller.initNewMailController(secondaryStage, emailSender);
            secondaryStage.getIcons().add(icon);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("New mail");
            secondaryStage.show();
        } catch (IOException e) {
            logger.error("Could not open main window", e.getMessage(), e);
        }
    }

    public void showReadMailWindow(Mailbox selectedMailbox, Email selectedEmail) {
        try {
            emailReceiver.loadEmailContentFromServer(selectedMailbox, selectedEmail, 3);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/readMail.fxml"));
            scene = new Scene(loader.load());
            ReadMailController controller = loader.getController();
            Stage secondaryStage = new Stage();
            controller.initReadMailController(this, secondaryStage, emailReceiver, selectedEmail);
            secondaryStage.getIcons().add(icon);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle(selectedEmail.getSubject());
            secondaryStage.show();
        } catch (IOException e) {
            logger.error("Could not open main window", e.getMessage(), e);
        } catch (EmailException e) {
            logger.error("Could not open email", e.getMessage(), e);
        }
    }

    public void showImageWindow(AttachmentFile file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/showImage.fxml"));
            scene = new Scene(loader.load());
            ShowImageController controller = loader.getController();
            Stage secondaryStage = new Stage();
            controller.initImageViewController(secondaryStage, file);
            secondaryStage.getIcons().add(icon);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle(file.getName());
            secondaryStage.show();
        } catch (IOException e) {
            logger.error("Could not open image", e.getMessage(), e);
        }
    }

    public void setSmtpServerConnection(SmtpServerConnection smtpServerConnection) {
        if (this.smtpServerConnection == null) {
            this.smtpServerConnection = smtpServerConnection;
            this.emailSender = new EmailSender(smtpServerConnection);
        }
    }

    public void setImapServerConnection(ImapServerConnection imapServerConnection) {
        if (this.imapServerConnection == null) {
            this.imapServerConnection = imapServerConnection;
            this.emailReceiver = new EmailReceiver(imapServerConnection);
        }
    }

}
