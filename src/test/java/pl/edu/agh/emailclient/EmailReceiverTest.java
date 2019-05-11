package pl.edu.agh.emailclient;

import org.junit.Before;
import org.junit.Test;

import pl.edu.agh.emailclient.connection.EmailReceiver;
import pl.edu.agh.emailclient.connection.ImapServerConnection;
import pl.edu.agh.emailclient.exceptions.EmailException;
import pl.edu.agh.emailclient.exceptions.SmtpServerException;
import pl.edu.agh.emailclient.mailbox.Email;
import pl.edu.agh.emailclient.mailbox.Mailbox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class EmailReceiverTest {

    private static final int RETRIES = 2;

    private ImapServerConnection connectionMock = mock(ImapServerConnection.class);

    private EmailReceiver emailReceiver = new EmailReceiver(connectionMock);


    @Before
    public void setUp() {
    }

    @Test
    public void setUpMailBoxTest() throws SmtpServerException, IOException, URISyntaxException {
        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(ClassLoader.getSystemResource("list.txt")
                .toURI())).forEach(line -> sb.append(line).append("\n"));
        when(connectionMock.listMailboxes(anyString(), anyString())).thenReturn(sb.toString());
        emailReceiver.setUpMailBoxes();
        // add assertions
    }

    @Test
    public void loadHeadersTest() throws SmtpServerException, IOException, URISyntaxException {
        // given
        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(ClassLoader.getSystemResource("fetch.txt")
                .toURI())).forEach(line -> sb.append(line).append("\n"));
        Mailbox mailbox = new Mailbox("INBOX", "INBOX");

        // when
        when(connectionMock.getHeadersForAllMailsFromMailbox(anyString(), anyLong())).thenReturn(sb.toString());
        emailReceiver.loadHeadersForAllMailsInFolder(mailbox);

        // then
        assertEquals(4, mailbox.getEmails().size());
//        assertEquals(1, email1.getId());
//        assertEquals("Witaj w WP Poczcie!", email1.getSubject());
//        assertEquals("22 urodziny Castorama! Sprawdź moc nagród", email2.getSubject());
//        assertEquals("wp@wp.pl", email3.getFrom());
//        assertEquals("2019-04-18T19:03:28", email4.getDate().toString());
    }


    @Test
    public void handleMultipartEmail() throws EmailException, IOException, URISyntaxException {

        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(ClassLoader.getSystemResource("mailContent.txt")
                .toURI())).forEach(line -> sb.append(line).append("\n"));

        when(connectionMock.getEmailContent(anyString(), anyLong())).thenReturn(sb.toString());
        emailReceiver.loadEmailContentFromServer(mock(Mailbox.class), Email.builder().build(), RETRIES);
    }

    @Test
    public void handleTextEmail() throws EmailException, IOException, URISyntaxException {

        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(ClassLoader.getSystemResource("mailContent2.txt")
                .toURI())).forEach(line -> sb.append(line).append("\n"));

        when(connectionMock.getEmailContent(anyString(), anyLong())).thenReturn(sb.toString());
        emailReceiver.loadEmailContentFromServer(mock(Mailbox.class), Email.builder().build(), RETRIES);
    }



    private static final String EMAIL_MOCK = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>WP Poczta</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "</head>\n" +
            "<body style=\"background: #ffffff; font-size: 16px; line-height: 1.5; margin: 0; padding: 0;\">\n" +
            "<table cellpadding=\"0\" cellspacing=\"0\" align=\"center\" style=\"max-width: 40em; width: 100%;\">\n" +
            "    <tr>\n" +
            "        <td style=\"padding: 16px;\">\n" +
            "            <a style=\"border: 0; text-decoration: none;\" href=\"https://poczta.wp.pl\" target=\"_blank\"><img src=\"cid:part1.8933EFF5.F4707539@wp.pl\" height=\"60\" width=\"79\" style=\"border: 0; height: 60px; width: 79px; vertical-align: middle;\" alt=\"WP Poczta\"></a>\n" +
            "        </td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td>\n" +
            "            <table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background-color: #ED1C24; min-height: 96px; padding-top: 24px; padding-bottom: 24px; padding-left: 20px; padding-right: 24px;\">\n" +
            "                <tr>\n" +
            "                    <td valin=\"middle\">\n" +
            "                        <h2 style=\"color: #fff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 28px; font-weight: 400; line-height: 1.1; margin: 0; padding: 0; width: 100%;\">\n" +
            "                            Witaj Agh!\n" +
            "                        </h2>\n" +
            "                    </td>\n" +
            "                </tr>\n" +
            "            </table>\n" +
            "        </td>\n" +
            "    </tr>\n" +
            "    <tr>\n" +
            "        <td style=\"padding: 16px 16px 24px;\">\n" +
            "            <p style=\"color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 19px; line-height: 1.5; margin: 0; margin-top: 5px; margin-bottom: 5px;\"><strong>Twoje konto w WP Poczcie zostało utworzone!</strong></p>\n" +
            "\n" +
            "            <p style=\"color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,\n" +
            "          Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal; line-height: 1.5; margin: 0;margin-bottom: 1em;\">Poniżej kilka przydatnych uwag.</p>\n" +
            "\n" +
            "            <h3 style=\"color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 18px; line-height: 1.4; margin-bottom: 0; padding: 0;\">Twój adres e-mail</h3>\n" +
            "\n" +
            "            <ul style=\"margin-top: 8px; margin-bottom: 24px;\">\n" +
            "                <li style=\"color: #222222; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, Helvetica, Arial, sans-serif; font-size: 16px; line-height: 1.4;\">aghedu@wp.pl</li>\n" +
            "            </ul>\n" +
            "\n" +
            "            <h3 style=\"color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 18px; line-height: 1.4; margin-bottom: 0; padding: 0;\">Dostęp do skrzynki</h3>\n" +
            "\n" +
            "            <ul style=\"margin-top: 8px; margin-bottom: 24px;\">\n" +
            "                <li style=\"color: #222222; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, Helvetica, Arial, sans-serif; font-size: 16px; line-height: 1.4; margin-bottom: 0.75em;\">logując się na stronie <a style=\"border: 0; color: #007aff; text-decoration: underline;\" target=\"_blank\" href=\"https://poczta.wp.pl\">https://poczta.wp.pl</a> (zarówno na komputerze stacjonarnym jak i telefonie komórkowym);</li>\n" +
            "\n" +
            "                <li style=\"color: #222222; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, Helvetica, Arial, sans-serif; font-size: 16px; line-height: 1.4; margin-bottom: 0.75em;\">poprzez aplikację mobilną WP&nbsp;Poczty<br>\n" +
            "\n" +
            "                    <div style=\"margin-top: 8px;\">\n" +
            "                        <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
            "                            <tr>\n" +
            "                                <td style=\"padding-right: 8px;\"><a style=\"border: 0; text-decoration: none\" target=\"_blank\" href=\"https://play.google.com/store/apps/details?id=pl.wp.wppoczta\"><img style=\"border: 0; display: block; height: 40px; width: 135px;\" src=\"cid:part8.D287A52C.BE67D23F@wp.pl\" alt=\"Pobierz aplikację WP Poczty na system Android\" height=\"40\" width=\"135\" border=\"0\"></a></td>\n" +
            "                                <td><a style=\"border: 0; text-decoration: none\" target=\"_blank\" href=\"https://itunes.apple.com/pl/app/wp-poczta/id1164870575?l=pl&mt=8\"><img style=\"border: 0; display: block; height: 40px; width: 136px;\" src=\"cid:part10.18F4BD22.2C2C2178@wp.pl\" alt=\"Pobierz aplikację WP Poczty na system iOS\" height=\"40\" width=\"136\" border=\"0\"></a></td>\n" +
            "                            </tr>\n" +
            "                        </table>\n" +
            "                    </div>\n" +
            "                </li>\n" +
            "\n" +
            "                <li style=\"color: #222222; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, Helvetica, Arial, sans-serif; font-size: 16px; line-height: 1.4; margin-bottom: 0.75em;\">poprzez dowolny program pocztowy (MS Outlook, The Bat, Mozilla Thunderbird itp.).\n" +
            "                    <p style=\"color: #757575; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 14px; font-weight: normal; line-height: 1.33; margin-top: 4px; margin-bottom: 1em;\">Informacje o konfiguracji różnych programów pocztowych znajdziesz <a style=\"border: 0; color: #757575; text-decoration: underline;\" target=\"_blank\"  href=\"https://pomoc.wp.pl/jak-skonfigurowac-program-pocztowy\">tutaj</a>.</p>\n" +
            "                </li>\n" +
            "            </ul>\n" +
            "\n" +
            "            <p style=\"color: #757575; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,\n" +
            "          Helvetica, Arial, sans-serif; font-size: 14px; font-weight: normal; line-height: 1.4; margin: 0;margin-bottom: 1em;\">\n" +
            "                Pozdrawiamy,<br>\n" +
            "                Zespół WP Poczty\n" +
            "            </p>\n" +
            "\n" +
            "            <div style=\"background-color: #F5F6F7; border-radius: 4px; margin-top: 32px; margin-bottom: 16px; padding-top: 24px; padding-bottom: 24px; padding-left: 32px; padding-right: 32px;\">\n" +
            "                <h3 style=\"color: #212121; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 20px; font-weight: 600; line-height: 1.2; margin: 0; margin-bottom: 16px;text-align: center;\">Potrzebujesz dodatkowej pomocy?</h3>\n" +
            "                <p style=\"color: #646566; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.5; margin-top: 8px; padding: 0; text-align: center;\">Nasz zespół służy wsparciem i jest po to, aby&nbsp;Ci&nbsp;pomóc.\n" +
            "                    <br/>Skontaktuj się z nami.</p>\n" +
            "                <p style=\"margin-top: 32px; margin-bottom: 16px; text-align: center;\">\n" +
            "                    <a style=\"background-color: #fff; border: 1px solid #E0E0E0; border-radius: 100px; color: #007aff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 16px; font-weight: 400; padding-left: 24px; padding-top: 8px; padding-bottom: 8px; padding-right: 24px; text-decoration: none;\" href=\"https://pomoc.wp.pl\" target=\"_blank\">Skontaktuj się</a>\n" +
            "                </p>\n" +
            "            </div>\n" +
            "        </td>\n" +
            "    </tr>\n" +
            "</table>\n" +
            "</body>\n" +
            "</html>";
}
