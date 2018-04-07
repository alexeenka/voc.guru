package guru.h4t_eng.rest.json;

/**
 * FastJsonParserException.
 *
 * Created by aalexeenka on 19.05.2016.
 */
public class FastJsonParserException extends RuntimeException {

    public FastJsonParserException() {
    }

    public FastJsonParserException(String message) {
        super(message);
    }

    public FastJsonParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastJsonParserException(Throwable cause) {
        super(cause);
    }

    public FastJsonParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
