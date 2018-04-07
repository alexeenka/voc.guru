package guru.h4t_eng.exception;

/**
 * An exception class for all cassandra related exceptions from the shared
 * library, regardless of driver used.
 *
 * Created by Alexey Alexeenka on 03.07.2015.
 */
public class H4TApplicationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public H4TApplicationException()
    {
        super();
    }

    public H4TApplicationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public H4TApplicationException(String message)
    {
        super(message);
    }

    public H4TApplicationException(Throwable cause)
    {
        super(cause);
    }
}
