package guru.h4t_eng.common;

import java.util.concurrent.CompletableFuture;

/**
 * WorkWithExecutorServices.
 *
 * Created by aalexeenka on 12.05.2017.
 */
public interface WorkWithExecutorServices {

    CompletableFuture<Void> start();

    void stop();
}
