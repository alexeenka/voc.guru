package guru.h4t_eng.config;

import java.util.Properties;

/**
 * SocialNetworkProperties.
 *
 * Created by aalexeenka on 2/19/2016.
 */
public class SocialNetworkProperties extends AppProperties {

    private static final SocialNetworkProperties instance = new SocialNetworkProperties("social-network.properties");
    public static SocialNetworkProperties getInstance() {return instance;}
    private SocialNetworkProperties(String fileName) {
        super(fileName);
    }

    @Override
    protected void reading(Properties prop) {
        vkRedirectURL = prop.getProperty("vk-redirect-url");
        vkClientId = prop.getProperty("vk-client-id");
        vkClientSecret = prop.getProperty("vk-client-secret");


        fbRedirectURL = prop.getProperty("fb-redirect-url");
        fbClientId = prop.getProperty("fb-client-id");
        fbClientSecret = prop.getProperty("fb-client-secret");
    }

    private String vkRedirectURL;
    private String vkClientId;
    private String vkClientSecret;
    private String fbRedirectURL;
    private String fbClientId;
    private String fbClientSecret;

    public String getVkRedirectURL() {
        return vkRedirectURL;
    }

    public String getFbRedirectURL() {
        return fbRedirectURL;
    }

    public String getFbClientId() {
        return fbClientId;
    }

    public String getFbClientSecret() {
        return fbClientSecret;
    }

    public String getVkClientId() {
        return vkClientId;
    }

    public String getVkClientSecret() {
        return vkClientSecret;
    }
}
