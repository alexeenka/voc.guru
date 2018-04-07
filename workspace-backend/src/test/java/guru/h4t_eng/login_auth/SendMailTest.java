package guru.h4t_eng.login_auth;

import guru.h4t_eng.config.EmailProperties;
import guru.h4t_eng.email.EmailService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.mail.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by aalexeenka on 02.12.2016.
 */
@Ignore // todo rethink logic about send emails
public class SendMailTest {

//    @Test
//    public void d() {
//        Properties props = System.getProperties();
//        props.put("mail.smtp.starttls.enable", true); // added this line
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.user", "username");
//        props.put("mail.smtp.password", "password");
//        props.put("mail.smtp.port", "587");
//        props.put("mail.smtp.auth", true);
//
//
//
//        Session session = Session.getInstance(props,null);
//        session.setDebug(true);
//        MimeMessage message = new MimeMessage(session);
//
//        System.out.println("Port: "+session.getProperty("mail.smtp.port"));
//
//        // Create the email addresses involved
//        try {
//            InternetAddress from = new InternetAddress("alexeenka.voc.guru@gmail.com");
//            message.setSubject("Yes we can");
//            message.setFrom(from);
//            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse("alexey.alexeenka@gmail.com"));
//
//            // Create a multi-part to combine the parts
//            Multipart multipart = new MimeMultipart("alternative");
//
//            // Create your text message part
//            BodyPart messageBodyPart = new MimeBodyPart();
//            messageBodyPart.setText("some text to send");
//
//            // Add the text part to the multipart
//            multipart.addBodyPart(messageBodyPart);
//
//            // Create the html part
//            messageBodyPart = new MimeBodyPart();
//            String htmlMessage = "Our html text";
//            messageBodyPart.setContent(htmlMessage, "text/html");
//
//
//            // Add html part to multi part
//            multipart.addBodyPart(messageBodyPart);
//
//            // Associate multi-part with message
//            message.setContent(multipart);
//
//            // Send message
//            Transport transport = session.getTransport("smtp");
//            transport.connect("smtp.gmail.com", "alexeenka.voc.guru@gmail.com", "g6U3wf9a");
//            System.out.println("Transport: "+transport.toString());
//            transport.sendMessage(message, message.getAllRecipients());
//
//
//        } catch (AddressException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }

    private static Process mailServer;

    public static final String testUserDomen = "domen.com";
    private static final String greenMailUsers = EmailProperties.getInstance().getUserName() + ":" + EmailProperties.getInstance().getPwd() + "@" + testUserDomen;
    private static final String testUserEmail = EmailProperties.getInstance().getUserName() + "@" + testUserDomen;

    @BeforeClass
    public static void before() throws IOException, URISyntaxException {
        // Run Mail Server: http://www.icegreen.com/greenmail/
        String java8 = Paths.get(System.getenv("JAVA_HOME_8")).toString();
        ProcessBuilder pb = new ProcessBuilder(java8 + "\\bin\\java",
                "-Dgreenmail.setup.test.smtp", "-Dgreenmail.setup.test.imap", "-Dgreenmail.auth.disabled",
                "-Dgreenmail.users=" + greenMailUsers,"-Dlog4j.configuration=log4j.xml","-jar","greenmail-standalone-1.5.2.jar");

        final URL resource = ClassLoader.getSystemClassLoader().getResource(".");
        //noinspection ConstantConditions
        pb.directory(Paths.get(resource.toURI()).getParent().getParent().getParent().resolve("standalone-apps").toFile());
        pb.inheritIO();
        mailServer = pb.start();
    }

    @AfterClass
    public static void after() {
        if (mailServer != null) {
            mailServer.destroy();
        }
    }

    @Test
    public void justSendAndGetEmail() throws MessagingException {
        EmailService.instance.sendNewUserEmail(testUserEmail);
        final Message message = readEmailLocally();
        assertNotNull(message);
        final Address[] froms = message.getFrom();
        assertEquals(froms.length, 1);
        assertThat(Collections.singletonList(froms[0].toString()), hasItem(EmailService.FROM_EMAIL));
    }

    // http://stackoverflow.com/questions/25341198/javax-mail-authenticationfailedexception-is-thrown-while-sending-email-in-java
    public static void main(String args[]) throws MessagingException {
    }

    public static Message readEmailLocally() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");

        Session emailSession = Session.getInstance(props);
        emailSession.setDebug(true);

        Store store = emailSession.getStore();
        store.connect("127.0.0.1", 3143, "foo", "pwd");

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox.getMessage(1);
    }


//    private static void google_example() {
//        Properties props = new Properties();
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "465");
//
//        Session session = Session.getDefaultInstance(props,
//                new Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication("alexeenka.voc.guru@gmail.com", "g6U3wf9a");
//                    }
//                });
//        session.setDebug(true);
//
//        try {
//
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress("alexeenka.voc.guru@gmail.com"));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("4alexey.alexeenka@gmail.com"));
//            message.setSubject("Welcome to VOC.GURU");
//            message.setText("Your login: alexey.alexeenka@gmail.com, your password: 123456. " +
//                    "Please click on: to prove it's your email \n"
//                    + "Ignore letter, if"
//            );
//
//
//            Transport.send(message);
//
//            System.out.println("Done");
//
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
