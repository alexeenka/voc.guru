package guru.h4t_eng.test.selenium;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BaseSelenium.
 *
 * Created by aalexeenka on 12/22/2015.
 */
public class BaseSelenium {

    protected static WebDriver driver;

    final static int SCRIPT_TIME_OUT_SEC = 3;

    // need to think about configuration
    public static final String APPLICATION_URL = "http://localhost:9080/";

    private static WebDriver configureAndGetDriver()
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        return new ChromeDriver(options);
    }


    public static void setUp() throws Exception
    {
        driver = configureAndGetDriver();
        driver.get(APPLICATION_URL);
        driver.manage().timeouts().setScriptTimeout(SCRIPT_TIME_OUT_SEC, TimeUnit.SECONDS);
    }

    public static void tearDown() throws Exception
    {
        driver.quit();
    }

    abstract class SeleniumTest
    {
        public void run() {
            try
            {
                driver.navigate().to(APPLICATION_URL);
                test();
            }
            catch (Throwable th)
            {
                final String prefix = Paths.get("").toAbsolutePath() + "/target/selenium-error/"
                        + BaseSelenium.this.getClass().getName();

                if (driver != null)
                {
                    try
                    {
                        // make and write screenshot
                        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        FileUtils.copyFile(screenshot, new File(prefix + "/screenshot.png"));

                        // write error to file
                        PrintStream ps = new PrintStream(prefix + "/error.log");
                        th.printStackTrace(ps);
                        ps.close();
                        th.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                throw th;
            }
        }

        public abstract void test();
    }

    public static void sleep100ms()
    {
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep200ms()
    {
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep500ms()
    {
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep1s()
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep2s()
    {
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleep3s()
    {
        try
        {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void loginAsSamanta() {
        driver.findElement(By.id("img_vk_login_id")).click();
        sleep1s();
        // todo: test with your own user from vk

        // Login as Samanta Zharkova
        driver.findElement(By.name("email")).sendKeys("example@email.com");
        driver.findElement(By.name("pass")).sendKeys("ddd");

        driver.findElement(By.id("install_allow")).click();
        sleep2s();
        Assert.assertEquals(driver.getCurrentUrl(),APPLICATION_URL + "index.html#/create-knowledge");
    }

    public List<String> getHtmlTextsById(String id) {
        final List<WebElement> elements = driver.findElements(By.id(id));
        final List<String> result = new ArrayList<>();
        for (WebElement element : elements) {
            result.add(element.getText());
        }

        return result;
    }

}
