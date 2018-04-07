package guru.h4t_eng.test;

import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * WithLogging.
 *
 * Created by aalexeenka on 12/16/2015.
 */
public class WithLogging {

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher watcher = new TestWatcher()
    {
        @Override
        protected void starting(Description desc)
        {
            System.out.print(desc.getDisplayName());
        }

        @Override
        protected void succeeded(Description description)
        {
            System.out.println(" OK");
        }

        @Override
        protected void failed(Throwable e, Description description)
        {
            System.out.println(" FAILED");
        }

        @Override
        protected void skipped(AssumptionViolatedException e, Description description)
        {
            System.out.println(" SKIPPED");
        }
    };

    public String getTestName() {
        return testName.getMethodName();
    }

}
