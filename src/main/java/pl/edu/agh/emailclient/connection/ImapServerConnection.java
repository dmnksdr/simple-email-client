package pl.edu.agh.emailclient.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.IOException;


public class ImapServerConnection extends ServerConnection {

    private static final Logger logger = LogManager.getLogger(ImapServerConnection.class);

    private static final String SELECT_MAILBOX_TEMPLATE = "1 SELECT %s";

    private static final String FETCH_HEADERS_TEMPLATE = "1 FETCH %d:* (FLAGS BODY[HEADER.FIELDS (DATE FROM SUBJECT)])";

    private static final String FETCH_EMAIL_TEMPLATE = "1 FETCH %d RFC822";

    public ImapServerConnection(String imapServerAddress, String login, String password) {
        this(imapServerAddress, 143, login, password);
    }

    private ImapServerConnection(String smtpServerAddress, int smtpServerPort, String login, String password) {
        this.serverAddress = smtpServerAddress;
        this.serverPort = smtpServerPort;
        this.login = login;
        this.password = password;
    }

    @Override
    void validateResponse(String response) {
        // TODO
    }

    @Override
    public void authenticate() throws SmtpServerException, IOException {
        sendMessageAndWaitForResponse("1 LOGIN " + login + " " + password);
    }

    @Override
    String getName() {
        return "IMAP";
    }

    public String listMailboxes(String refName, String wildcardedMailbox) throws SmtpServerException, IOException {
        startConversation();
        String response = sendMessageAndWaitForResponseTillOk("1 LIST " + refName + " " + wildcardedMailbox);
        closeSocket();
        return response;
    }

    public String getHeadersForAllMailsFromMailbox(String mailboxName, long startingId) throws SmtpServerException, IOException {
        startConversation();
        sendMessageAndWaitForResponse(String.format(SELECT_MAILBOX_TEMPLATE, mailboxName));
        String response = sendMessageAndWaitForResponseTillOk(String.format(FETCH_HEADERS_TEMPLATE, startingId));
        closeSocket();
        return response;
    }

    public String getEmailContent(String mailboxName, long emailId) throws SmtpServerException, IOException {
        startConversation();
        sendMessageAndWaitForResponse(String.format(SELECT_MAILBOX_TEMPLATE, mailboxName));
        String response = sendMessageAndWaitForResponse(String.format(FETCH_EMAIL_TEMPLATE, emailId));
        closeSocket();
        return response;
    }

}
