package guru.h4t_eng;

import guru.h4t_eng.datasource.UserDataSource;

import java.util.UUID;

/**
 * Data that needed to be initialized.
 *
 * Created by aalexeenka on 16.11.2016.
 */
public final class UsersDatabaseData4Tst {

    public static final UUID SAMANTA_UUID;
    public static final UUID DAVE_UUID;

    static {
        SAMANTA_UUID = UserDataSource.getInstance().getUserIdByVkID(Users4Tst.SAMANTA_VK_ID);
        DAVE_UUID = UserDataSource.getInstance().getUserIdByFbID(Users4Tst.DAVE_FB_ID);
    }

}
