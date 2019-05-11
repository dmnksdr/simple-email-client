package pl.edu.agh.emailclient.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class EmailSender {

    private static final Logger logger = LogManager.getLogger(EmailSender.class);
    
    private SmtpServerConnection smtpServerConnection;

    private static final String MIME = "MIME-Version: 1.0\n";
    private static final String MIME_BOUNDARY_PLAIN = "MIME_BOUNDARY";
    private static final String MIME_BOUNDARY = "--" + MIME_BOUNDARY_PLAIN + "\n";
    private static final String CONTENT_TYPE_MULTIPART = "Content-Type: multipart/mixed; boundary=\"" + MIME_BOUNDARY_PLAIN + "\"\n\n";
    private static final String CONTENT_TRANSFER_ENCODING="Content-Transfer-Encoding: 8bit\n";
    private static final String CONTENT_TYPE_TEXT = "Content-Type: text/html;\n\n";

    private static final String ATTACHMENT_CONTENT_TYPE = "Content-Type: image/png; name=\"%s\"\n"; // TODO extension
    private static final String ATTACHMENT_CONTENT_DESCRIPTION = "Content-Description: %s\n";
    private static final String ATTACHMENT_CONTENT_DISPOSITION = "Content-Disposition: attachment; filename=\"%s\"\n";
    private static final String ATTACHMENT_CONTENT_ENCODING = "Content-Transfer-Encoding: BASE64\n\n";

    private static final String END_OF_DATA = "\n.\n";

    public EmailSender(SmtpServerConnection smtpServerConnection) {
        this.smtpServerConnection = smtpServerConnection;
    }

    public void sendEmail(EmailPropertiesDTO properties) throws IOException, SmtpServerException {
        try {
            smtpServerConnection.startConversation();
            smtpServerConnection.sendMessageAndWaitForResponse("MAIL FROM:<" + smtpServerConnection.getEmailAddress() + ">");
            for (String toAddress : properties.getAllRecipients()) {
                smtpServerConnection.sendMessageAndWaitForResponse("RCPT TO:<" + toAddress + ">");
            }
            smtpServerConnection.sendMessageAndWaitForResponse("DATA");
            smtpServerConnection.sendMessageAndWaitForResponse(constructDataPart(properties));
            smtpServerConnection.sendMessageAndWaitForResponse("QUIT");
        } finally {
            smtpServerConnection.closeSocket();
        }
    }

    private String constructDataPart(EmailPropertiesDTO propertiesDTO) throws IOException {
        return constructDataHeader(propertiesDTO) + constructDataBody(propertiesDTO) + END_OF_DATA;
    }

    private String constructDataHeader(EmailPropertiesDTO properties) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(getCurrentDate()).append("\n");
        for (String recipient : properties.getToAddresses()) {
            sb.append("To: ").append(recipient).append("\n");
        }
        for (String recipient : properties.getCcAddresses()) {
            sb.append("Cc: ").append(recipient).append("\n");
        }
        for (String recipient : properties.getBccAddresses()) {
            sb.append("Bcc: ").append(recipient).append("\n");
        }
        sb.append("From: ").append(properties.getName()).append(" <").append(properties.getFromAddress()).append(">\n");
        sb.append("Subject: ").append(properties.getSubject()).append("\n");
//        if (properties.hasAttachment()) {
            sb.append(MIME);
            sb.append(CONTENT_TYPE_MULTIPART);
            sb.append(MIME_BOUNDARY);
//        }
        return sb.toString();
    }

    private String constructDataBody(EmailPropertiesDTO properties) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(MIME_BOUNDARY);
        sb.append(CONTENT_TRANSFER_ENCODING);
        sb.append(CONTENT_TYPE_TEXT);
        sb.append(properties.getTextContent()).append("\n\n");
        if (properties.hasAttachment()) {
            String fileName = properties.getAttachmentFile().getName();
            sb.append(MIME_BOUNDARY);
            sb.append(String.format(ATTACHMENT_CONTENT_TYPE, fileName));
            sb.append(String.format(ATTACHMENT_CONTENT_DESCRIPTION, fileName));
            sb.append(String.format(ATTACHMENT_CONTENT_DISPOSITION, fileName));
            sb.append(ATTACHMENT_CONTENT_ENCODING);
            sb.append(getEncodedFileAsString(properties.getAttachmentFile()));
        }
        sb.append("\n\n").append("--" + MIME_BOUNDARY_PLAIN + "--").append("\n");
        System.out.println("\n=======================\n");
        System.out.println(sb.toString());
        System.out.println("\n=======================\n");
        return sb.toString();
    }


    private String getEncodedFileAsString(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            long length = file.length();
            byte[] bytes = new byte[(int) length];

            int offset = 0;
            int numRead;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    private String getCurrentDate() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    public String getFromName() {
        return smtpServerConnection.getEmailName();
    }

    public String getFromAddress() {
        return smtpServerConnection.getEmailAddress();
    }

}
