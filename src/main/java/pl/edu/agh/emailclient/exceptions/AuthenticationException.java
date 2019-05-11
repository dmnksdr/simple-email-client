package pl.edu.agh.emailclient.exceptions;

public class AuthenticationException extends EmailException {

    public AuthenticationException(String errorMessage) {
        super(errorMessage);
    }

}
