package pl.edu.agh.emailclient.connection;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
public class EmailPropertiesDTO {

    private static final String RECIPIENT_SEPARATOR = ";";

    private String fromAddress;

    private List<String> toAddresses;

    private List<String> ccAddresses;

    private List<String> bccAddresses;

    private String subject;

    private String textContent;

    private String name;

    private File attachmentFile;

    private static final Pattern EMAIL_VALIDATION_PATTERN = Pattern.compile("^(.+)@(.+)$");

    private static final Predicate<String> inputIsValidEmailAddress
            = input -> {
        Matcher matcher = EMAIL_VALIDATION_PATTERN.matcher(input);
        return matcher.matches();
    };

    public Set<String> getAllRecipients() {
        return Stream.of(toAddresses, ccAddresses, bccAddresses)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public boolean hasAttachment() {
        return attachmentFile != null;
    }

    public static class EmailPropertiesDTOBuilder {

        private List<String> toAddresses = new ArrayList<>();

        private List<String> ccAddresses = new ArrayList<>();

        private List<String> bccAddresses = new ArrayList<>();

        public EmailPropertiesDTO.EmailPropertiesDTOBuilder toAddresses(String input) {
            Arrays.stream(input.split(RECIPIENT_SEPARATOR))
                    .filter(inputIsValidEmailAddress)
                    .forEach(recipient -> toAddresses.add(recipient.trim()));
            return this;
        }

        public EmailPropertiesDTO.EmailPropertiesDTOBuilder ccAddresses(String input) {
            Arrays.stream(input.split(RECIPIENT_SEPARATOR))
                    .filter(inputIsValidEmailAddress)
                    .forEach(recipient -> ccAddresses.add(recipient.trim()));
            return this;
        }

        public EmailPropertiesDTO.EmailPropertiesDTOBuilder bccAddresses(String input) {
            Arrays.stream(input.split(RECIPIENT_SEPARATOR))
                    .filter(inputIsValidEmailAddress)
                    .forEach(recipient -> bccAddresses.add(recipient.trim()));
            return this;
        }

    }

}
