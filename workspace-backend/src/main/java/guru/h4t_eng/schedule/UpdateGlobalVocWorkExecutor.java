package guru.h4t_eng.schedule;

import guru.h4t_eng.global_voc.GlobalVocService;

import java.util.concurrent.TimeUnit;

/**
 * Update global vocabulary each {@link #UPDATE_GLOBAL_VOC_MIN}
 *
 * Created by aalexeenka on 19.01.2017.
 */
public class UpdateGlobalVocWorkExecutor extends WorkExecutor {

    public static final int UPDATE_GLOBAL_VOC_MIN = 5;


    public UpdateGlobalVocWorkExecutor() {
        super("update-global-voc", GlobalVocService.getInstance()::initialize);
    }

    @Override
    public void start() {
        scheduledTask = executorService.scheduleWithFixedDelay(doTaskWork(appWork),
                UPDATE_GLOBAL_VOC_MIN,
                UPDATE_GLOBAL_VOC_MIN,
                TimeUnit.MINUTES
        );
    }
}
