package pl.edu.agh.emailclient.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;

import java.io.IOException;
import java.util.Base64;

public class SmtpServerConnection extends ServerConnection {

    private static final Logger logger = LogManager.getLogger(SmtpServerConnection.class);

    public SmtpServerConnection(String smtpServerAddress, String login, String password) throws IOException {
        this(smtpServerAddress, 25, login, password);
    }

    private SmtpServerConnection(String smtpServerAddress, int smtpServerPort, String login, String password) {
        this.serverAddress = smtpServerAddress;
        this.serverPort = smtpServerPort;
        this.login = login;
        this.password = password;
    }

    @Override
    public void authenticate() throws IOException, SmtpServerException {
        logger.info("Checking authentication for SMTP server");
        sendMessageAndWaitForResponse("AUTH LOGIN");
        sendMessageAndWaitForResponse(Base64.getEncoder().encodeToString(login.getBytes()));
        sendMessageAndWaitForResponse(Base64.getEncoder().encodeToString(password.getBytes()));
    }

    @Override
    String getName() {
        return "SMTP";
    }

    public void sendGreeting() throws IOException, SmtpServerException {
        logger.warn("Greeting SMTP server");
        out.println("HELO emailclient.agh.edu.pl");
        String greetingResponse = in.readLine();
        if (!greetingResponse.startsWith("2")) {
            logger.warn("Problem with connection, got: " + greetingResponse + " as reply to HELO");
        }
    }

    @Override
    void validateResponse(String response) throws SmtpServerException {
        if (response.startsWith("4") || response.startsWith("5")) {
            throw new SmtpServerException("Incorrect response from server: [" + response + "] in reply to: [" + response + "]");
        }
    }



}
