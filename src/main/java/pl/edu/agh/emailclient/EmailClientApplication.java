package pl.edu.agh.emailclient;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.connection.ImapServerConnection;
import pl.edu.agh.emailclient.gui.GuiManager;


public class EmailClientApplication extends Application {

    private static final Logger logger = LogManager.getLogger(ImapServerConnection.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Application start");
        GuiManager guiManager = new GuiManager(primaryStage);
        guiManager.showLoginWindow();
    }

}
