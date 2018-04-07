package guru.h4t_eng.email;

import guru.h4t_eng.config.EmailProperties;
import guru.h4t_eng.logs.AppLoggerFactory;
import org.slf4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * EmailService.
 *
 * Send email.
 *
 * Created by aalexeenka on 12.12.2016.
 */
public final class EmailService {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(EmailService.class);
    public static final String FROM_EMAIL = "alexeenka.voc.guru@gmail.com";

    private EmailService() {
        Properties props = new Properties();
        final EmailProperties emailProperties = EmailProperties.getInstance();

        props.put("mail.smtp.host", emailProperties.getHost());
        props.put("mail.smtp.socketFactory.port", emailProperties.getSocketFactoryPort());
        props.put("mail.smtp.socketFactory.class", emailProperties.getSocketFactoryClass());
        props.put("mail.smtp.auth", emailProperties.getAuth());
        props.put("mail.smtp.port", emailProperties.getPort());

        session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailProperties.getUserName(), emailProperties.getPwd());
                    }
                });

        session.setDebug(emailProperties.getDebug());

    }

    public static EmailService instance = new EmailService();

    private final Session session;

    public void sendNewUserEmail(String toEmail) {
        // call using hashing queue
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Welcome to VOC.GURU");
            message.setText("Your login: alexey.alexeenka@gmail.com, your password: 123456. " +
                    "Please click on: to prove it's your email \n"
                    + "Ignore letter, if"
            );
            Transport.send(message);
        } catch (MessagingException e) {
            LOG.error("Error when send email to ");
            throw new RuntimeException(e);
        }
    }

}
