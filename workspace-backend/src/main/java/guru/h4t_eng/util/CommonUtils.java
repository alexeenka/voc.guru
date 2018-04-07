package guru.h4t_eng.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonUtils.
 *
 * Created by aalexeenka on 16.08.2016.
 */
public final class CommonUtils {
    private CommonUtils() {
    }

    public static <T> ArrayList<T> makeList(T... args) {
        ArrayList<T> objects = new ArrayList<>();

        for (T arg : args)
        {
            if (arg == null)
            {
                continue;
            }

            objects.add(arg);
        }

        return objects;
    }

    public static String joinWithIndex(List<String> args) {
        final StringBuilder str = new StringBuilder();
        int i=0;
        final int N=args.size();

        for (String arg : args) {
            if (N == 1) {
                return arg;
            }

            str.append(i + 1).append(". ").append(arg);
            if (i + 1 != N) {
                str.append(" ");
            }

            i++;
        }

        return str.toString();

    }
}
