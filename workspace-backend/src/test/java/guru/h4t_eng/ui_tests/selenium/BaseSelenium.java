package guru.h4t_eng.ui_tests.selenium;

import guru.h4t_eng.Users4Tst;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlContains;

/**
 * BaseSelenium.
 * <p>
 * Created by aalexeenka on 12/22/2015.
 */
@Ignore
public class BaseSelenium {

    private static final String WEB_DRIVER_VERSION = "2.37";

    private enum OsType {
        WINDOWS,
        MAC_OS
    }

    private static OsType osType = OsType.WINDOWS;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            osType = OsType.MAC_OS;
        }
    }

    protected static WebDriver driver;

    final static int SCRIPT_TIME_OUT_SEC = 3;

    // need to think about configuration
    public static final String APPLICATION_URL = "https://voc-test.guru:9080/";

    private static WebDriver configureAndGetDriver() throws URISyntaxException {
        // About driver: https://sites.google.com/a/chromium.org/chromedriver/capabilities
        ChromeOptions options = new ChromeOptions();
        // Arguments full list: http://peter.sh/experiments/chromium-command-line-switches/
        options.addArguments("--start-maximized", "--disable-notifications", "--disable-plugins");

        File driverPath = Paths.get(BaseSelenium.class.getResource("/").toURI())
                .getParent().getParent()
                .resolve("~selenium/webdriver/" + WEB_DRIVER_VERSION + "/chromedriver" + (OsType.WINDOWS.equals(osType) ? ".exe" : ""))
                .toFile();

        final ChromeDriverService driverService = new ChromeDriverService.Builder()
                .usingDriverExecutable(driverPath)
                .usingAnyFreePort()
                .build();

        return new ChromeDriver(driverService, options);
    }


    public static void setUp() throws Exception {
        driver = configureAndGetDriver();
        driver.get(APPLICATION_URL);
        driver.manage().timeouts().setScriptTimeout(SCRIPT_TIME_OUT_SEC, TimeUnit.SECONDS);
    }

    public static void tearDown() throws Exception {
        if (driver != null) {
            driver.quit();
        }
    }

    abstract class SeleniumTest {
        public void run() {
            try {
                driver.navigate().to(APPLICATION_URL);
                test();
            } catch (Throwable th) {
                final String prefix = Paths.get("").toAbsolutePath() + "/target/selenium-error/"
                        + BaseSelenium.this.getClass().getName();

                if (driver != null) {
                    try {
                        // make and write screenshot
                        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        FileUtils.copyFile(screenshot, new File(prefix + "/screenshot.png"));

                        // write error to file
                        PrintStream ps = new PrintStream(prefix + "/error.log");
                        th.printStackTrace(ps);
                        ps.close();
                        th.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                throw th;
            }
        }

        public abstract void test();
    }

    public static void loginAsSamanta() {
        driver.findElement(By.id("vkLogin")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("install_allow")));
        // Login as Samanta Zharkova
        driver.findElement(By.name("email")).sendKeys(Users4Tst.SAMANTA_EMAIL);
        driver.findElement(By.name("pass")).sendKeys(Users4Tst.SAMANTA_PSW);
        driver.findElement(By.id("install_allow")).click();
        assertThat(driver.getCurrentUrl(), endsWith("#/training"));
    }

    public static void loginAsDavid() {
        loginAsFbUser(Users4Tst.DAVE_EMAIL, Users4Tst.DAVE_PSW);
    }

    public static void loginAsFbUser(String userEmail, String userPsw) {
        driver.findElement(By.id("fbLogin")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("loginbutton")));
        // Login as David
        driver.findElement(By.id("email")).sendKeys(userEmail);
        driver.findElement(By.id("pass")).sendKeys(userPsw);

        driver.findElement(By.id("loginbutton")).click();

        final By confirmPrivilegeButtonSelector = By.name("__CONFIRM__");
        new WebDriverWait(driver, 10).until(ExpectedConditions.or(
                urlContains("https://voc-test.guru:9080"),
                ExpectedConditions.presenceOfElementLocated(confirmPrivilegeButtonSelector))
        );

        final List<WebElement> privilegesConfirmElements = driver.findElements(confirmPrivilegeButtonSelector);
        Boolean isPresent = privilegesConfirmElements.size() > 0;
        if (isPresent) {
            // needed for first login
            privilegesConfirmElements.get(0).click();
            // wait until page will be loaded
            new WebDriverWait(driver, 10).until(ExpectedConditions.or(urlContains("https://voc-test.guru:9080")));
        }

        // check if it's training page
        new WebDriverWait(driver, 10).until(ExpectedConditions.urlToBe("https://voc-test.guru:9080/index.html#/training"));
        assertThat(driver.getCurrentUrl(), endsWith("#/training"));
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
