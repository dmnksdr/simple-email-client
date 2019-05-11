package pl.edu.agh.emailclient.connection;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.emailclient.exceptions.EmailContentParsingException;
import pl.edu.agh.emailclient.exceptions.EmailException;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class EmailReceiver {

    private static final Logger logger = LogManager.getLogger(EmailReceiver.class);

    private ImapServerConnection imapServerConnection;

    private Mailbox rootMailbox;

    public EmailReceiver(ImapServerConnection imapServerConnection) {
        this.imapServerConnection = imapServerConnection;
    }

    public Mailbox getRootMailbox() {
        return rootMailbox;
    }

    public void setUpMailBoxes() throws SmtpServerException, IOException {
        rootMailbox = new Mailbox(imapServerConnection.getEmailAddress(), "");
        String response = imapServerConnection.listMailboxes("\"\"", "*");
        for (String line : response.split("\n")) {
            String[] inQuotes = StringUtils.substringsBetween(line, "\"", "\"");
            if (inQuotes != null && inQuotes.length > 1) {
                String separator = inQuotes[0];
                String mailboxPath = inQuotes[1];
                String[] splittedPath = mailboxPath.split(separator);
                createMailboxesFromPath(splittedPath);
            }
        }
    }

    private String decodeFolderName(String encoded) {
        return encoded.replaceAll("&AUI-", "ł")
                .replaceAll("&AVs-", "ś");
    }

    public void loadHeadersForAllMailsInFolder(Mailbox mailbox) throws SmtpServerException, IOException {
        tryToReadEmailHeadersFromFile(mailbox);
        loadEmailHeadersFromServer(mailbox);
        saveEmailsToFile(mailbox);
    }

    private void loadEmailHeadersFromServer(Mailbox mailbox) throws SmtpServerException, IOException {
        String headersResponse = imapServerConnection.getHeadersForAllMailsFromMailbox(mailbox.getPath(), mailbox.getIdOfLastEmail());
        String[] allMessages = headersResponse.split("\n\\)");
        for (int i = 0; i < allMessages.length - 1; i++) {
            Email email = loadMessageHeader(allMessages[i].trim());
            mailbox.addEmail(email);
        }
        mailbox.setRefreshedNow();
    }

    public void loadEmailContentFromServer(Mailbox mailbox, Email email, int retries) throws EmailException, IOException {
        if (retries < 1) {
            throw new EmailContentParsingException("Could not parse email");
        }
        try {
            String content = imapServerConnection.getEmailContent(mailbox.getPath(), email.getId());

            System.out.println("\n===============================================================\n");
            for (String line : content.split("\n")) {
                System.out.println(line);
            }
            System.out.println("\n===============================================================\n");

            parsePartsOfDifferentTypes(content, email);
        } catch (EmailContentParsingException e) {
            logger.warn("Unsuccessful attempt to parse email");
            loadEmailContentFromServer(mailbox, email, retries - 1);
        }
    }

    private void tryToReadEmailHeadersFromFile(Mailbox mailbox) {
        File file = new File("textContent/" + mailbox.getName() + ".email");
        if (file.exists()) {
            try (FileInputStream fileIn = new FileInputStream(file);
                 ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
                Set genericSetOfEmails = (Set) objectIn.readObject();
                for (Object emailObject : genericSetOfEmails) {
                    mailbox.addEmail((Email) emailObject);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warn("Could not read stored emails", e);
            }
        }
    }

    public void saveEmailsToFile(Mailbox mailbox) {
        File file = new File("data/" + mailbox.getName() + ".email");
        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(mailbox.getEmails());
        } catch (IOException e) {
            logger.warn("Could not store emails in file", e);
        }
    }

    private Email loadMessageHeader(String message) {
        String[] lines = message.split("\n");
        if (lines.length != 4) {
            logger.error("Wrong format of header");
            return null;
        }
        long id = Long.parseLong(StringUtils.substringBetween(lines[0], "* ", " FETCH"));

        Map<String, String> titleValueMap = createTitleValueMap(lines);

        Email email = Email.builder()
                .id(id)
                .date(titleValueMap.get("Date"))
                .from(titleValueMap.get("From"))
                .subject(titleValueMap.get("Subject"))
                .build();
        logger.info("Email with id: " + email.getId() + " created");
        return email;
    }

    private Map<String, String> createTitleValueMap(String[] lines) {
        Map<String, String> titleValueMap = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] titleAndValue = line.split(": ");
            titleValueMap.put(titleAndValue[0], titleAndValue[1]);
        }
        return titleValueMap;
    }

    private void createMailboxesFromPath(String[] path) {
        Mailbox commonParent = rootMailbox;
        for (String mailboxName : path) {
            commonParent = findOrCreateMailbox(mailboxName, commonParent);
        }
    }

    private Mailbox findOrCreateMailbox(String name, Mailbox parent) {
        for (Mailbox child : parent.getChildBoxes()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        Mailbox newMailBox = new Mailbox(name, parent.getPath() + Mailbox.PATH_SEPARATOR + name);
        parent.addChildBox(newMailBox);
        return newMailBox;
    }

    private void listMailboxes(Mailbox parentMailbox, String prefix) {
        for (Mailbox child : parentMailbox.getChildBoxes()) {
            String path = prefix + Mailbox.PATH_SEPARATOR + child.getName();
            System.out.println(path);
            listMailboxes(child, path);
        }
    }

    private String extractBoundaryFromEmailContent(String content) throws EmailContentParsingException {
        for (String line : content.split("\n")) {
            if (line.contains("boundary=")) {
                return StringUtils.substringsBetween(line, "boundary=\"", "\"")[0];
            }
        }
        throw new EmailContentParsingException("Could not find boundary sign");
    }

    private void parsePartsOfDifferentTypes(String content, Email email) throws EmailContentParsingException {
        String boundary = extractBoundaryFromEmailContent(content);

        List<String> contentParts = new ArrayList<>(Arrays.asList(content.split("--" + boundary)));
        String header = contentParts.get(0);
        contentParts.remove(0);

        for (String part : contentParts) {
            for (String line : part.split("\n")) {
                if (line.contains("Content-Type:")) {
                    String contentType = StringUtils.substringsBetween(line, "Content-Type:", ";")[0];
                    handlePartWithGivenType(part, contentType, email);
                }
            }
        }
    }

    private void handlePartWithGivenType(String part, String contentType, Email email) throws EmailContentParsingException {
        if (contentType.contains("text")) {
            String text = extractTextFromPart(part);
            email.setText(text);
        } else if (contentType.contains("image")) {
            email.addFile(extractImagesFromEmailContent(part));
        }

    }

    private String extractTextFromPart(String part) throws EmailContentParsingException {
        String[] lines = part.split("\n");
        String charset = null;
        String encoding = null;
        boolean headerEncountered = false;
        boolean textStarted = false;
        List<String> textLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("charset")) {
                charset = StringUtils.substringAfterLast(line, "charset=");
            }
            if (line.contains("Content-Transfer-Encoding:")) {
                encoding = StringUtils.substringAfterLast(line, "Content-Transfer-Encoding: ");
                headerEncountered = true;
            }
            boolean isHeaderOver = headerEncountered && line.isEmpty();
            if (textStarted) {
                textLines.add(line);
            }
            if (isHeaderOver) {
                textStarted = true;
            }

        }
        return decodeMailText(textLines, encoding, charset);
    }

    private String decodeMailText(List<String> lines, String encoding, String charsetName) throws EmailContentParsingException {
        if (lines != null && encoding != null) {
            if (encoding.contains("8bit")) {
                return String.join("\n", lines);
            }
            if (encoding.contains("quoted-printable")) {
                try {
                    return new String(QuotedPrintableCodec.decodeQuotedPrintable(String.join("", lines).getBytes()));
                } catch (DecoderException e) {
                    throw new EmailContentParsingException("Could not decode: " + encoding);
                }
            }
            if (encoding.contains("base64")) {
                byte[] bytes = String.join("", lines).getBytes();
                byte[] decodedBytes = Base64.getDecoder().decode(bytes);
                try {
                    Charset charset = Charset.forName(charsetName);
                    return new String(decodedBytes, charset);
                } catch (Exception e) {
                    logger.info("Didn't manage to match charset");
                    return new String(decodedBytes);
                }
            }
        }
        throw new EmailContentParsingException("Unknown text encoding: " + encoding);
    }

    private AttachmentFile extractImagesFromEmailContent(String part) {
        String[] splittedPart = part.split("\n\n");
        String header = splittedPart[0];
        String content = splittedPart[1];

        AttachmentFile.AttachmentFileBuilder fileBuilder = AttachmentFile.builder().fileType(AttachmentFile.FileType.IMAGE);

        for (String line : header.split("\n")) {
            // Content-Type: image/png;
            String format = StringUtils.substringBetween(part, "image/", ";");
            if (format != null && !format.isEmpty()) fileBuilder.format(format);

            // name="android.png"
            String name = StringUtils.substringBetween(line, "name=\"", "\"");
            if (name != null && !name.isEmpty()) fileBuilder.name(name);

            // Content-Transfer-Encoding: base64
            String encoding = StringUtils.substringBetween(line, "Content-Transfer-Encoding: ", "\n");
            if (encoding != null && !name.isEmpty()) fileBuilder.encoding(encoding);
        }

        byte[] attachmentContent = Base64.getMimeDecoder().decode(content.replaceAll("\n", ""));
        fileBuilder.imageContent(attachmentContent);

        return fileBuilder.build();
    }

    public void saveImagesToFiles(Email email) {
        for (AttachmentFile attachmentFile : email.getFiles()) {
            File file = new File("attachments/" + attachmentFile.getName());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(attachmentFile.getImageContent());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
