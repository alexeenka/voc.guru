package guru.h4t_eng.datasource.voc_order_field;

import com.google.common.collect.ImmutableMap;
import guru.h4t_eng.datasource.training.RepeatWordValue;
import guru.h4t_eng.datasource.work_effort.WorkEffortValuePerDay;
import guru.h4t_eng.model.training.TrainingWordValue;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

/**
 * VocOrderFieldAdapter.
 *
 * Created by aalexeenka on 23.09.2016.
 */
public class OneStringValuesUtil {

    private static final Class[] supported_classes = new Class []{
            WorkEffortValuePerDay.class,
            TrainingWordValue.class,
            RepeatWordValue.class
    };

    private static final ImmutableMap<Class, VocField[]> vocFieldPerClass;

    public static final String SEPARATOR = ";";

    private OneStringValuesUtil() {
    }

    public static void applyDatabaseValue(OneStringValues instance, String databaseValue) {
        if (instance == null) return;
        if (StringUtils.isBlank(databaseValue)) return;

        final VocField[] vocFields = getVocFields(instance);

        final String[] values = databaseValue.split(SEPARATOR);

        for (VocField vocField : vocFields) {
            if (vocField.order >= values.length) {
                continue;
            }


            try {
                if (vocField.type.equals(Integer.class)) {
                    try {
                        vocField.field.set(instance, Integer.valueOf(values[vocField.order]));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    continue;
                }

                if (vocField.type.equals(String.class)) {
                    vocField.field.set(instance, values[vocField.order]);
                    continue;
                }

                throw new RuntimeException("Unsupported type: " + vocField.type);

            } catch (IllegalAccessException e) {
                throw new RuntimeException("OneStringValuesUtil, can't set value", e);
            }
        }
    }

    public static String toDatabaseValue(OneStringValues instance) {
        if (instance == null) return StringUtils.EMPTY;

        final StringBuilder str = new StringBuilder();
        final VocField[] vocFields = getVocFields(instance);

        for (int i=0, n=vocFields.length; i<n; i++) {
            try {
                str.append(vocFields[i].field.get(instance));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("OneStringValuesUtil, can't get value", e);
            }
            if (i + 1 < n) {
                str.append(SEPARATOR);
            }
        }

        return str.toString();
    }


    private static VocField[] getVocFields(OneStringValues instance) {
        VocField[] vocFields = vocFieldPerClass.get(instance.getClass());
        if (vocFields != null) return vocFields;

        Class superclass = instance.getClass().getSuperclass();
        while (!Class.class.equals(superclass))
        {
            vocFields = vocFieldPerClass.get(superclass);
            if (vocFields != null) return vocFields;
            superclass = instance.getClass().getSuperclass();
        }

        throw new RuntimeException("Can't find values for class: " + instance.getClass());
    }


    private static class VocField {
        private java.lang.reflect.Field field;
        private int order;
        private Class type;

        public VocField(Field field, int order, Class type) {
            this.field = field;
            this.order = order;
            this.type = type;
        }
    }


    static {
        ImmutableMap.Builder<Class, VocField[]> vocFieldPerClassBuilder = ImmutableMap.builder();

        for (Class iClass : supported_classes)
        {
            final VocField[] vocFieldWithValidation = getVocFieldWithValidation(iClass);
            vocFieldPerClassBuilder.put(iClass, vocFieldWithValidation);
        }

        vocFieldPerClass = vocFieldPerClassBuilder.build();
    }

    private static VocField[] getVocFieldWithValidation(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();

        ArrayList<VocField> vocFieldsList = new ArrayList<>();

        int checkMax = 0;
        for (Field field : fields) {
            if (!field.isAnnotationPresent(StringValueOrder.class)) {
                continue;
            }
            final StringValueOrder vocFieldNumber = field.getAnnotation(StringValueOrder.class);

            for (VocField vocField : vocFieldsList) {
                if (vocField.order == vocFieldNumber.order()) throw new RuntimeException("Duplicated StringValueOrder annotation for class = " + clazz + " for order = " + vocFieldNumber.order());
            }
            field.setAccessible(true);
            vocFieldsList.add(new VocField(field, vocFieldNumber.order(), vocFieldNumber.type()));
            if (vocFieldNumber.order() > checkMax) {
                checkMax = vocFieldNumber.order();
            }
        }

        for (int i=0; i<checkMax; i++) {
            boolean find = false;
            for (VocField vocField : vocFieldsList) {
                if (vocField.order == i) {
                    find = true;
                    break;
                }
            }

            if (!find) throw new RuntimeException("Missing StringValueOrder annotation for class = " + clazz + " order = " + i);
        }

        Collections.sort(vocFieldsList, (o1, o2) -> Integer.valueOf(o1.order).compareTo(o2.order));
        return vocFieldsList.toArray(new VocField[vocFieldsList.size()]);
    }


}
