package guru.h4t_eng.datasource.voc_order_field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * StringValueOrder.
 *
 * Created by aalexeenka on 23.09.2016.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringValueOrder {
    int order();
    Class type() default Integer.class;
}
