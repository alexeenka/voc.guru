package guru.h4t_eng.rest;

import guru.h4t_eng.security.model.Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * MainRestService.
 * 
 * Created by aalexeenka on 8/4/2015.
 */
public class Main4Rest {

    public static UUID getUserId(HttpServletRequest request) {
        return (UUID) request.getAttribute(Constants.USER_ID);
    }

}
