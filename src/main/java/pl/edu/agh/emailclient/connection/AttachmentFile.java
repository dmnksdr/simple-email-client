package pl.edu.agh.emailclient.connection;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Builder
public class AttachmentFile implements Serializable {

    private byte[] imageContent;

    private String format;

    private String name;

    private String encoding;

    FileType fileType;

    enum FileType {
        IMAGE,
        TEXT;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttachmentFile file = (AttachmentFile) o;
        return Objects.equals(name, file.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
