package guru.h4t_eng.exception;

/**
 * An exception class for all cassandra related exceptions from the shared
 * library, regardless of driver used.
 *
 * Created by Alexey Alexeenka on 03.07.2015.
 */
public class CassandraException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CassandraException()
    {
        super();
    }

    public CassandraException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CassandraException(String message)
    {
        super(message);
    }

    public CassandraException(Throwable cause)
    {
        super(cause);
    }
}
