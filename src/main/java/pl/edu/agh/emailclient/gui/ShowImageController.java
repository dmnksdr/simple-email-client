package pl.edu.agh.emailclient.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import pl.edu.agh.emailclient.connection.AttachmentFile;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ShowImageController {

    @FXML private ImageView imageView;

    public void initImageViewController(Stage secondaryStage, AttachmentFile attachmentFile) {
        InputStream is = new ByteArrayInputStream(attachmentFile.getImageContent());
        Image image = new Image(is);
        if (image.getWidth() > 1000 || image.getHeight() > 1000) {
            imageView.setFitHeight(1000);
            imageView.setFitWidth(1000);
        }
        imageView.setImage(image);
    }


}
