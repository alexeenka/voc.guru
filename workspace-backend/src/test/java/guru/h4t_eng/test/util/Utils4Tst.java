package guru.h4t_eng.test.util;

import com.google.gson.Gson;
import guru.h4t_eng.rest.words.model.FormData;
import guru.h4t_eng.security.auth_servlet.AbstractAuthenticationServlet;
import guru.h4t_eng.security.model.H4TUserInfo;
import guru.h4t_eng.security.model.UserAuthType;
import guru.h4t_eng.util.FormDataUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static guru.h4t_eng.UsersDatabaseData4Tst.SAMANTA_UUID;

/**
 * Utils4Tst.
 *
 * Created by aalexeenka on 12/17/2015.
 */
public final class Utils4Tst {

    private static final H4TUserInfo userInfo;

    public static final Gson GSON = new Gson();

    public static String readResource(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(Utils4Tst.class.getResource(path).toURI())));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static File getFileResource(String resourcePath) {
        try {
            return Paths.get(Utils4Tst.class.getResource(resourcePath).toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static {
        userInfo = new H4TUserInfo(UserAuthType.VK,
                15447858,
                "Леша",
                "Алексеенко",
                "http://cs623825.vk.me/v623825858/26a29/ubOqnpfWaMY.jpg",
                AbstractAuthenticationServlet.NO_EMAIL,
                "xx",
                new Date());
        userInfo.setUserId(UUID.fromString("220761a0-6b46-11e5-ba60-a1efb4cdc0bf"));
    }

    public static H4TUserInfo getUserInfo() {
        return userInfo;
    }

    public static FormData newFormDataFromJson__1() {
        return resourceFormData("/rest_resource/word-entity-data-from-gui-01.json");
    }

    public static FormData newDataForSimpleForm() {
        return resourceFormData("/rest_resource/simple-form.json");
    }

    public static FormData newFormDataFromJson__2() {
        return resourceFormData("/rest_resource/word-entity-data-from-gui-02.json");
    }

    public static FormData resourceFormData(String resource) {
        final FormData formData = FormDataUtil.parseJson(readResource(resource));
        formData.setUser(SAMANTA_UUID);

        return formData;
    }

    public static char randomLetter() {
        return (char) ThreadLocalRandom.current().nextInt('A', 'Z' + 1);
    }
}
