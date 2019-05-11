package pl.edu.agh.emailclient.mailbox;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.emailclient.connection.AttachmentFile;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static pl.edu.agh.emailclient.utils.Decoder.decodeDate;
import static pl.edu.agh.emailclient.utils.Decoder.decodeSubject;
import static pl.edu.agh.emailclient.utils.Decoder.decodeFromField;

@Getter
@Builder
public class Email implements Serializable {

    private long id;

    private String from;

    private String subject;

    private LocalDateTime date;

    private String text;

    private Set<AttachmentFile> files;

    public void setText(String text) {
        this.text = text;
    }

    public void addFile(AttachmentFile file) {
        if (this.files == null) this.files = new HashSet<>();
        this.files.add(file);
    }

    public void addFiles(List<AttachmentFile> files) {
        if (this.files == null) this.files = new HashSet<>();
        this.files.addAll(files);
    }

    public boolean hasAttachments() {
        return files != null && !files.isEmpty();
    }

    public static class EmailBuilder {

        private String from;
        private String subject;
        private LocalDateTime date;

        public EmailBuilder from(String input) {
            from = decodeFromField(input);
            return this;
        }

        public EmailBuilder subject(String input) {
            subject = decodeSubject(input);
            return this;
        }

        public EmailBuilder date(String input) {
            this.date = decodeDate(input);
            return this;
        }

    }

    @Override
    public String toString() {
        return subject + " - " + from + " - " + date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return id == email.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
