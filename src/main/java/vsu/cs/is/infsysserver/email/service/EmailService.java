package vsu.cs.is.infsysserver.email.service;

public interface EmailService {

    void sendVerificationCode(String email, String code);

}
