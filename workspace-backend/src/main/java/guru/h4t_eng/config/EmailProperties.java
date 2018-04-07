package guru.h4t_eng.config;

import java.util.Properties;

/**
 * Settings for work with emails
 *
 * Created by aalexeenka on 12.12.2016.
 */
public class EmailProperties extends AppProperties {

    private static final EmailProperties instance = new EmailProperties("email-settings.properties");
    public static EmailProperties getInstance() {return instance;}
    private EmailProperties(String fileName) {
        super(fileName);
    }

    @Override
    protected void reading(Properties prop) {
        host = prop.getProperty("mail.smtp.host");
        socketFactoryPort = prop.getProperty("mail.smtp.socketFactory.port");
        socketFactoryClass = prop.getProperty("mail.smtp.socketFactory.class");
        auth = prop.getProperty("mail.smtp.auth");
        port = prop.getProperty("mail.smtp.port");

        userName = prop.getProperty("mail.userName");
        pwd = prop.getProperty("mail.pwd");
        debug = Boolean.valueOf(prop.getProperty("mail.debug"));
    }

    private String host;
    private String socketFactoryPort;
    private String socketFactoryClass;
    private String auth;
    private String port;
    private String userName;
    private String pwd;
    private Boolean debug;


    public String getHost() {
        return host;
    }

    public String getSocketFactoryPort() {
        return socketFactoryPort;
    }

    public String getSocketFactoryClass() {
        return socketFactoryClass;
    }

    public String getAuth() {
        return auth;
    }

    public String getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPwd() {
        return pwd;
    }

    public Boolean getDebug() {
        return debug;
    }
}
