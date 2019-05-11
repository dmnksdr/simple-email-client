package pl.edu.agh.emailclient.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;


public class Decoder {

    private static final Logger logger = LogManager.getLogger(Decoder.class);

    private static final String UTF_8_BASE64 = "=?UTF-8?B?";

    private static final String UTF_8_QUOTED_PRINTABLE = "=?UTF-8?Q?";

    private static final String PARTS_SEPARATOR = "?";

    public static String decodeSubject(String input) {
        boolean isEncoded = input.toUpperCase().startsWith(UTF_8_BASE64) || input.toUpperCase().startsWith(UTF_8_QUOTED_PRINTABLE);
        if (!isEncoded) {
            return input;
        }
        boolean isBase64 = input.toUpperCase().startsWith(UTF_8_BASE64);
        String[] parts = StringUtils.substringsBetween(input, PARTS_SEPARATOR, PARTS_SEPARATOR);
        String encodingType = parts[0];
        String payload = parts[1];
        String decodedInput = null;
        try {
            if (isBase64) {
                decodedInput = new String(Base64.getDecoder().decode(payload), encodingType);
            } else {
                decodedInput = new String(QuotedPrintableCodec.decodeQuotedPrintable(payload.getBytes())).replaceAll("_"," ");
            }
        } catch (UnsupportedEncodingException | DecoderException e) {
            logger.error("Problem while parsing e-mail ", e);
        }
        return decodedInput;
    }

    /**
     * example: "=?utf-8?B?Q2FzdG9yYW1hIFBvbHNrYSBTcC4geiBvLm8uIC9XUA==?=" <wp@wp.pl>
     */
    public static String decodeFromField(String input) {
        String[] firstAttempt = StringUtils.substringsBetween(input, "<", ">");
        if (firstAttempt != null && firstAttempt.length > 0) {
            return firstAttempt[0];
        }

        String[] secondAttempt = StringUtils.substringsBetween(input, "\"", "\"");
        if (secondAttempt != null && secondAttempt.length > 0) {
            return decodeSubject(secondAttempt[0]);
        }
        return null;
    }

    public static LocalDateTime decodeDate(String input) {
        // Wed, 17 Apr 2019 20:56:44 +0200
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (DateTimeParseException e) {
            logger.warn("Parsing date: " + input + " with RFC_1123_DATE_TIME format failed");
        }

        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("d MMM yy HH:mm:ss"));
        } catch (DateTimeParseException e) {
            logger.warn("Parsing date: " + input + " with d MMM yy HH:mm:ss format failed");
        }
        return LocalDateTime.now();
    }

}
