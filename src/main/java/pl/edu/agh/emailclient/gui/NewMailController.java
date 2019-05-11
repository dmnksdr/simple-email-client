package pl.edu.agh.emailclient.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.agh.emailclient.connection.EmailPropertiesDTO;
import pl.edu.agh.emailclient.connection.EmailSender;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;

import java.io.File;
import java.io.IOException;

public class NewMailController {

    @FXML private TextField fromNameField;
    @FXML private TextField fromAddressField;
    @FXML private TextField toField;
    @FXML private TextField ccField;
    @FXML private TextField bccField;
    @FXML private TextField subject;
//    @FXML private TextArea text;
    @FXML private HTMLEditor text;
    @FXML private Label infoLabel;
    @FXML private HBox fromHBox;

    private EmailSender emailSender;

    private Stage stage;

    private File attachmentFile;

    public void initNewMailController(Stage stage, EmailSender emailSender) {
        this.emailSender = emailSender;
        this.stage = stage;

        fromNameField.setText(emailSender.getFromName());
        fromAddressField.setText(emailSender.getFromAddress());
    }

    public void send() {
        EmailPropertiesDTO emailProp = EmailPropertiesDTO.builder()
                .toAddresses(toField.getText().trim())
                .ccAddresses(ccField.getText().trim())
                .bccAddresses(bccField.getText().trim())
                .subject(subject.getText())
                .textContent(text.getHtmlText())
                .name(fromNameField.getText().trim())
                .fromAddress(fromAddressField.getText().trim())
                .attachmentFile(attachmentFile)
                .build();
        try {
            emailSender.sendEmail(emailProp);
        } catch (IOException | SmtpServerException e) {
            infoLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        attachmentFile = fileChooser.showOpenDialog(stage);
        infoLabel.setText("Selected attachment: " + attachmentFile.getName()); //TODO NP
    }

    @FXML
    private void enableFromFieldsOverride() {
        fromHBox.setVisible(true);
        fromNameField.setEditable(true);
        fromAddressField.setEditable(true);
    }
}
