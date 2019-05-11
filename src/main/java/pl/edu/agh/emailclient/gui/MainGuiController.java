package pl.edu.agh.emailclient.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.connection.EmailReceiver;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.IOException;
import java.time.LocalDateTime;

public class MainGuiController {

    private static final Logger logger = LogManager.getLogger(MainGuiController.class);

    private GuiManager guiManager;

    private EmailReceiver emailReceiver;

    @FXML
    TreeView<Mailbox> folderList;

    @FXML
    TableView<Email> mailList;
    @FXML
    TableColumn<Email, String> subjectColumn;
    @FXML
    TableColumn<Email, String> fromColumn;
    @FXML
    TableColumn<Email, String> dateColumn;

    @FXML
    Label errorLabel;

    public void initMainController(GuiManager guiManager, EmailReceiver emailReceiver) {
        this.guiManager = guiManager;
        this.emailReceiver = emailReceiver;

        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadMailboxes();
        setUpDoubleClick();
    }

    public void newMail() {
        guiManager.showNewMailWindow();
    }

    public void mailBoxClicked() {
        Mailbox selectedFolder = folderList.getFocusModel().getFocusedItem().getValue();
        boolean lastRefreshMoreThanMinuteAgo = selectedFolder.getLastRefreshed() == null || selectedFolder.getLastRefreshed().isBefore(LocalDateTime.now().minusMinutes(1));
        if (lastRefreshMoreThanMinuteAgo) {
            refreshMailbox(selectedFolder);
        }
        showFolderContentInList(selectedFolder);
    }

    public void refreshButtonClicked() {
        Mailbox selectedFolder = folderList.getFocusModel().getFocusedItem().getValue();
        refreshMailbox(selectedFolder);
    }

    private void refreshMailbox(Mailbox folder) {
        try {
            folder.getEmails().clear();
            emailReceiver.loadHeadersForAllMailsInFolder(folder);
        } catch (SmtpServerException | IOException e) {
            errorLabel.setText("Something went wrong");
            logger.error(e);
        }
        showFolderContentInList(folder);
    }

    private void showFolderContentInList(Mailbox mailbox) {
        mailList.getItems().clear();
        mailList.getItems().addAll(mailbox.getEmails());
        mailList.refresh();
    }

    private void loadMailboxes() {
        try {
            emailReceiver.setUpMailBoxes();
        } catch (SmtpServerException | IOException e) {
            errorLabel.setText(e.getMessage());
        }

        Mailbox rootMailbox = emailReceiver.getRootMailbox();
        TreeItem<Mailbox> rootTreeItem = new TreeItem<>(rootMailbox);
        mapMailboxesToTreeList(rootTreeItem, rootMailbox);
        folderList.setRoot(rootTreeItem);
    }

    private void mapMailboxesToTreeList(TreeItem<Mailbox> treeItemParent, Mailbox mailboxParent) {
        for (Mailbox childBox : mailboxParent.getChildBoxes()) {
            TreeItem<Mailbox> childTreeItem = new TreeItem<>(childBox);
            treeItemParent.getChildren().add(childTreeItem);
            if (childBox.hasChildBoxes()) {
                mapMailboxesToTreeList(childTreeItem, childBox);
            }
        }
    }

    private void setUpDoubleClick() {
        mailList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    System.out.println("Double clicked");
                    guiManager.showReadMailWindow(getSelectedFolder(), getSelectedEmail());
                }
            }
        });
    }

    private Mailbox getSelectedFolder() {
        return folderList.getSelectionModel().getSelectedItem().getValue();
    }

    private Email getSelectedEmail() {
        return mailList.getSelectionModel().getSelectedItem();
    }

}
