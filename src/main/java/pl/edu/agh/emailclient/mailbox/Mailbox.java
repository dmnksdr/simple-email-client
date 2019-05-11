package pl.edu.agh.emailclient.mailbox;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class Mailbox {

    public static final String PATH_SEPARATOR = "/";

    private String name;

    private String path;

    private List<Mailbox> childBoxes = new ArrayList<>();

    private Set<Email> emails = new HashSet<>();

    private LocalDateTime lastRefreshed;

    public Mailbox(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public void addChildBox(Mailbox childBox) {
        childBoxes.add(childBox);
    }

    public void addEmail(Email email) {
        if (email != null) {
            emails.add(email);
        }
    }

    public boolean hasChildBoxes() {
        return childBoxes != null;
    }

    public void setRefreshedNow() {
        lastRefreshed = LocalDateTime.now();
    }

    public long getIdOfLastEmail() {
        long maxId = 1;
        if (!emails.isEmpty()) {
            maxId = emails.stream().mapToLong(Email::getId).max().orElse(1);
        }
        return maxId;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
