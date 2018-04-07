package guru.h4t_eng.test.selenium;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

/**
 * WordFlowTest.
 *
 * todo: Need to be update
 *
 * Created by aalexeenka on 12/22/2015.
 */
@Ignore
public class WordFlowTest extends BaseSelenium {

    public String getImagePath() {
        try {
            return Paths.get(WordFlowTest.class.getResource("/test-0" + ThreadLocalRandom.current().nextInt(1, 8) + ".jpg").toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        BaseSelenium.setUp();
        loginAsSamanta();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        BaseSelenium.tearDown();
    }

    // @Test todo: draftCreate and Change
    // @Test todo: readyToDraft and delete

    @Test
    public void mainScenario_Ready_ChangeEngValue_Delete() {
        new BaseSelenium.SeleniumTest() {
            @Override
            public void test() {
                int readyCount = Integer.valueOf(driver.findElement(By.id("badgeReadyWordCount")).getText());
                int draftCount = Integer.valueOf(driver.findElement(By.id("badgeDraftWordCount")).getText());
                // 1. Click on button 'New Word'
                driver.findElement(By.id("createWordButton")).click();
                sleep500ms();
                // 2. Fill all fields for Word
                String engVal = getAWord();
                setEngVal(engVal);
                fillFieldsForWordAndSave(engVal);
                readyCount++;
                // 3. Check counts, check list values
                checkReadyAndDraftCounts(readyCount, draftCount);
                checkWordOnReadyTab(engVal);
                editWord(engVal);
                // 4a. Change to word, fav def
                String newDef = engVal + " def_0 " + "edit";
                final WebElement favDef = driver.findElements(By.cssSelector("input[ng-model='engDef.val']")).get(0);
                favDef.clear();
                sleep200ms();
                favDef.sendKeys(newDef);
                sleep200ms();
                saveWord();
                // check counts
                checkReadyAndDraftCounts(readyCount, draftCount);
                checkWordOnReadyTab(engVal, newDef, engVal + " rusval_0");

                // 4b. Change eng val and save word
                editWord(engVal);
                String editEngVal = engVal + " _edit";
                setEngVal(editEngVal);
                saveWord();

                // check counts
                checkReadyAndDraftCounts(readyCount, draftCount);
                checkWordOnReadyTab(editEngVal, newDef, engVal + " rusval_0");
                checkNoWordReadyTab(engVal);

                // 5. global refresh and check word counts
                globalRefreshAndCheckWordCounts(readyCount, draftCount);
                checkWordOnReadyTab(editEngVal, newDef, engVal + " rusval_0");
                checkNoWordReadyTab(engVal);
            }
        }.run();
    }

    private void editWord(String engVal) {
        WebElement neededRow = getRowReadyTab(engVal);
        Assert.assertNotNull(neededRow);
        neededRow.findElement(By.id("editReadyWordButton")).click();
        sleep500ms();
    }

    @Test
    public void mainScenario_Draft_Ready_Delete() {
        new BaseSelenium.SeleniumTest() {
            @Override
            public void test() {
                int readyCount = Integer.valueOf(driver.findElement(By.id("badgeReadyWordCount")).getText());
                int draftCount = Integer.valueOf(driver.findElement(By.id("badgeDraftWordCount")).getText());
                // 1. Click on button 'New Word'
                driver.findElement(By.id("createWordButton")).click();
                sleep500ms();
                // 2. Create word start with 'A'
                String engVal = getAWord();
                setEngVal(engVal);
                // 3. Save draft word
                driver.findElement(By.id("saveDraftWordButton")).click(); // todo NO MORE DRAFT WORDS
                sleep500ms();
                // 4. Check that Draft Count was changed
                assertEquals(
                        Integer.valueOf(driver.findElement(By.id("badgeDraftWordCount")).getText()).intValue(),
                        draftCount + 1
                );
                draftCount++;
                // 5. Click on Draft Word Tab
                driver.findElement(By.id("draftWordTab")).click();
                // 6. Click on Draft Edit Button
                {
                    WebElement neededRow = getRow(engVal, "draftRowListDiv", "draftEngValList");
                    Assert.assertNotNull(neededRow);
                    neededRow.findElement(By.id("editDraftWordButton")).click();
                }
                sleep500ms();
                // 7. Fill all fields and save
                fillFieldsForWordAndSave(engVal);
                // 8. Check ready words count and draft word counts
                draftCount--;
                readyCount++;
                checkReadyAndDraftCounts(readyCount, draftCount);
                // 9. Check word on ready words tab
                sleep500ms();
                driver.findElement(By.id("badgeReadyWordCount")).click();
                sleep500ms();
                checkWordOnReadyTab(engVal);
                // 10. remove word and check that word was removed
                sleep2s();
                // Remove word
                // a. get needed row
                {
                    WebElement neededRow = getRowReadyTab(engVal);
                    Assert.assertNotNull(neededRow);
                    neededRow.findElement(By.id("removeReadyWordButton")).click();
                }
                // b. confirm delete
                driver.findElement(By.id("modalCancelButton")).click();
                sleep2s();
                readyCount--;
                // check
                checkNoWordReadyTab(engVal);
                // 11. Global refresh and check words count!
                globalRefreshAndCheckWordCounts(readyCount, draftCount);
            }
        }.run();
    }

    private void checkNoWordReadyTab(String engVal) {
        final WebElement rowReadyTab = getRowReadyTab(engVal);
        Assert.assertNull(rowReadyTab);
    }

    private void setEngVal(String engVal) {
        final WebElement element = driver.findElement(By.cssSelector("input[ng-model='word.engVal']"));
        element.clear();
        sleep100ms();
        element.sendKeys(engVal);
        sleep100ms();
    }


    private WebElement getRowReadyTab(String engVal) {
        return getRow(engVal, "rowListDiv", "engValList");
    }

    private void globalRefreshAndCheckWordCounts(int readyCount, int draftCount) {
        driver.navigate().refresh();
        sleep3s();
        checkReadyAndDraftCounts(readyCount, draftCount);
    }

    private void checkWordOnReadyTab(String engVal) {
        checkWordOnReadyTab(engVal, engVal + " def_0", engVal + " rusval_0");

    }

    private void checkWordOnReadyTab(String engVal, String engDef, String rusVal) {
        final WebElement row = getRowReadyTab(engVal);
        Assert.assertEquals(engVal, row.findElement(By.id("engValList")).getText());
        Assert.assertEquals(engDef, row.findElement(By.id("engDevFavList")).getText());
        Assert.assertEquals(rusVal, row.findElement(By.id("rusValueFavList")).getText());
    }

    private void checkReadyAndDraftCounts(int readyCount, int draftCount) {
        assertEquals(
                Integer.valueOf(driver.findElement(By.id("badgeReadyWordCount")).getText()).intValue(),
                readyCount
        );
        assertEquals(
                Integer.valueOf(driver.findElement(By.id("badgeDraftWordCount")).getText()).intValue(),
                draftCount
        );
    }

    private String getAWord() {
        return "A" + "_word " + new SimpleDateFormat("dd/MM hh:mm:ss").format(new Date());
    }

    private void fillFieldsForWordAndSave(String engVal) {
        // definitions
        for (int i=0, N=3; i<N; i++) {
            driver.findElements(By.cssSelector("input[ng-model='engDef.val']")).get(i).sendKeys(engVal + " def_" + i);
            sleep500ms();
            if (i + 1 < N) driver.findElement(By.id("addDefButton")).click();
            sleep200ms();
        }
        // sentences
        for (int i=0, N=3; i<N; i++) {
            driver.findElements(By.cssSelector("textarea[ng-model='engSentence.val']")).get(i).sendKeys(engVal + " sentence_" + i);
            sleep500ms();
            if (i + 1 < N) driver.findElement(By.id("addSentenceButton")).click();
            sleep200ms();
        }
        // rus values
        for (int i=0, N=4; i<N; i++) {
            driver.findElements(By.cssSelector("input[ng-model='rusVal.val']")).get(i).sendKeys(engVal + " rusVal_" + i);
            sleep500ms();
            if (i + 1 < N) driver.findElement(By.id("addRusButton")).click();
            sleep200ms();
        }
        // image
        driver.findElement(By.cssSelector("input[type='file']")).sendKeys(getImagePath());
        // set part of speach
        new Select(driver.findElement(By.cssSelector("select[ng-model='word.partOfSpeech']"))).selectByValue("Verb");
        // add synonyms
        for (int i=0, N=3; i<N; i++) {
            sleep500ms();
            driver.findElement(By.id("addSynButton")).click();
            sleep200ms();
            driver.findElements(By.cssSelector("input[ng-model='engSyn.val']")).get(i).sendKeys(engVal + " engSyn_" + i);
        }
        // add antonyms
        for (int i=0, N=3; i<N; i++) {
            sleep500ms();
            driver.findElement(By.id("addAntonymButton")).click();
            sleep200ms();
            driver.findElements(By.cssSelector("input[ng-model='engAntonym.val']")).get(i).sendKeys(engVal + " engAntonym_" + i);
        }
        sleep500ms();
        saveWord();
    }

    private void saveWord() {
        driver.findElement(By.id("saveWordButton")).click();
        sleep3s();
    }

    private WebElement getRow(String engVal, String rowListDivId, String engValListId) {
        for (WebElement webElement : driver.findElements(By.id(rowListDivId))) {
            if (engVal.equals(webElement.findElement(By.id(engValListId)).getText())) {
                return webElement;
            }
        }
        return null;
    }
}

