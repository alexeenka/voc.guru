package guru.h4t_eng.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * ReflectionUtil4Tst.
 *
 * Created by aalexeenka on 10.05.2017.
 */
public final class ReflectionUtil4Tst {
    private ReflectionUtil4Tst() {}

    public static void injectField(Object target,
                             String fieldName,
                             Object fieldValue
    ) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static <T> void injectStaticFieldInt(Class<T> target,
                             String fieldName,
                             int fieldValue
    ) {
        try {
            Field field = target.getDeclaredField(fieldName);
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
