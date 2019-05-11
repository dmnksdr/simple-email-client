package pl.edu.agh.emailclient.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class ServerConnection {

    private static final Logger logger = LogManager.getLogger(ServerConnection.class);

    private Socket socket;
    PrintWriter out;
    BufferedReader in;

    String serverAddress;
    int serverPort;
    String login;
    String password;

    public void checkCredentials() throws IOException, SmtpServerException {
        openSocket();
        authenticate();
        closeSocket();
    }

    String sendMessageAndWaitForResponse(String message) throws SmtpServerException, IOException {
        logger.info("Sending: " + message + " to " + getName() + " server");
        out.println(message);
        String response = readResponse();
        validateResponse(response);
        logger.info("Response from " + getName() + " server: " + response);
        return response;
    }

    String sendMessageAndWaitForResponseTillOk(String message) throws SmtpServerException, IOException {
        logger.info("Sending: " + message + " to " + getName() + " server");
        out.println(message);
        String response = readResponseTillOk();
        validateResponse(response);
        logger.info("Response from " + getName() + " server: " + response);
        return response;
    }

    private String readResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(in.readLine()); // wait and read first line
        while(in.ready())
            sb.append("\n").append(in.readLine());
        return sb.toString();
    }

    private String readResponseTillOk() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean isLastPart;
        do {
            String part = readResponse();
            sb.append("\n").append(part);
            isLastPart = part.contains("OK") && part.contains("completed");
        } while (!isLastPart);

        return sb.toString();
    }

    abstract void validateResponse(String response) throws SmtpServerException;

    protected abstract void authenticate() throws SmtpServerException, IOException;

    public void startConversation() throws SmtpServerException, IOException {
        openSocket();
        authenticate();
    }

    public void openSocket() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info(getName() + " server says hello: " + readResponse());
        }
    }

    public void closeSocket() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public String getEmailAddress() {
        return login + "@" + serverAddress.replaceFirst("^\\w*\\.", "");
    }

    public String getEmailName() {
        return getEmailAddress().split("@")[0];
    }

    abstract String getName();

}
