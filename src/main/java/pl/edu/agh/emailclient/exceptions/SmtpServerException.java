package pl.edu.agh.emailclient.exceptions;

public class SmtpServerException extends EmailException {

    public SmtpServerException(String errorMessage) {
        super(errorMessage);
    }

}
