package pl.edu.agh.emailclient.mailbox;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmailTest {

    @Test
    public void createEmailWithUtfTest() {
        Email email = Email.builder()
        .subject("=?utf-8?B?MjIgdXJvZHppbnkgQ2FzdG9yYW1hISBTcHJhd2TFuiBtb2MgbmFncsOzZA==?=")
        .from("=?utf-8?B?Q2FzdG9yYW1hIFBvbHNrYSBTcC4geiBvLm8uIC9XUA==?=")
        .date("Wed, 17 Apr 2019 20:56:44 +0200").build();

        assertEquals("2019-04-17T20:56:44", email.getDate().toString());
        assertEquals("Castorama Polska Sp. z o.o. /WP", email.getFrom());
        assertEquals("22 urodziny Castorama! Sprawdź moc nagród", email.getSubject());
    }
}