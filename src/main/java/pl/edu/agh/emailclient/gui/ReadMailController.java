package pl.edu.agh.emailclient.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import pl.edu.agh.emailclient.connection.AttachmentFile;
import pl.edu.agh.emailclient.connection.EmailReceiver;
import pl.edu.agh.emailclient.mailbox.Email;

import java.io.File;

public class ReadMailController {

    @FXML private HTMLEditor htmlField;

    @FXML private ListView<AttachmentFile> attachmentsList;

    private EmailReceiver emailReceiver;

    private GuiManager guiManager;

    private Stage stage;

    private File attachmentFile;

    private Email selectedEmail;

    public void initReadMailController(GuiManager guiManager, Stage stage, EmailReceiver emailReceiver, Email selectedEmail) {
        this.guiManager = guiManager;
        this.emailReceiver = emailReceiver;
        this.stage = stage;
        this.selectedEmail = selectedEmail;

        htmlField.setHtmlText(selectedEmail.getText());
        showAttachments(selectedEmail);
        setUpDoubleClick();
    }

    private void showAttachments(Email selectedEmail) {
        if (selectedEmail.hasAttachments()) {
//            emailReceiver.saveImagesToFiles(selectedEmail);
            ObservableList<AttachmentFile> observableList = FXCollections.observableArrayList();
            observableList.addAll(selectedEmail.getFiles());
            attachmentsList.setItems(observableList);
        }
    }

    private void setUpDoubleClick() {
        attachmentsList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    guiManager.showImageWindow(getSelectedAttachment());
                }
            }
        });
    }

    private AttachmentFile getSelectedAttachment() {
        return attachmentsList.getSelectionModel().getSelectedItems().get(0);
    }



}
