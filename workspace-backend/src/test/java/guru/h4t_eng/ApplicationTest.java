package guru.h4t_eng;

import guru.h4t_eng.social_networks.LoginUsersTst;
import guru.h4t_eng.test.CookieCacheTst;
import guru.h4t_eng.test.datasource.TrainingDataSourceTst;
import guru.h4t_eng.test.datasource.VocabularyDatasourceTst;
import guru.h4t_eng.test.rest.*;
import guru.h4t_eng.test.rest.friends.FbFriendsRestTst;
import guru.h4t_eng.test.rest.friends.FriendsRestTst;
import guru.h4t_eng.test.rest.friends.VkFriendsRestTst;
import guru.h4t_eng.test.service.CrudWordDatasourceTst;
import guru.h4t_eng.test.util.IrregularVerbUtilsTst;
import guru.h4t_eng.test.util.OneStringValuesUtilsTst;
import guru.h4t_eng.test.util.SentenceTrainingUtilsTst;
import guru.h4t_eng.test.util.WordFormUtilsTst;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Needed order of tests
 *
 * Created by aalexeenka on 16.11.2016.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // autentication
        LoginUsersTst.class,
        CookieCacheTst.class,

        // datasources
        TrainingDataSourceTst.class,
        VocabularyDatasourceTst.class,

        // Rest. Begin
        CrudWordRestTst.class,
        CounterRestTst.class,
        GlobalVocRestTst.class,

        // Friends
        VkFriendsRestTst.class,
        FbFriendsRestTst.class,
        FriendsRestTst.class,

        VocabularyRestTst.class,
        WordValidationRestTst.class,
        // Rest. End

        // service
        CrudWordDatasourceTst.class,

        // application utility
        IrregularVerbUtilsTst.class,
        OneStringValuesUtilsTst.class,
        SentenceTrainingUtilsTst.class,
        WordFormUtilsTst.class,

        // social_networks


})
public class ApplicationTest {
}
