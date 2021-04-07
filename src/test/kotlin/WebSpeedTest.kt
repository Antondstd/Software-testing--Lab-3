import org.junit.Assert.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.Color
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration


@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class WebSpeedTest {


   fun setUp(browser: String): RemoteWebDriver {
       val driver: WebDriver
       when (browser) {
           "chrome" -> driver = RemoteWebDriver(URL("http://192.168.56.1:4444/"), ChromeOptions())
           else -> driver = RemoteWebDriver(URL("http://192.168.56.1:4444/"), FirefoxOptions())
       }
       driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
       return driver
   }


    fun setUp(browser: String): WebDriver {
        val driver: WebDriver
        when (browser) {
            "chrome" -> driver = ChromeDriver()
            else -> driver = FirefoxDriver()
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
        driver.get("https://www.speedtest.net/")
        return driver
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Test speed`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//div[@class='start-button']//a[@role='button']")).click()
            val results = WebDriverWait(driver, Duration.ofMinutes(2))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='result-data']/a[contains(@href,'result')]")));
            assertTrue(driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value ping-speed']")).text.toInt() >= 0)
            assertTrue(driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value download-speed']")).text.toDouble() > 0)
            assertTrue(driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value upload-speed']")).text.toDouble() > 0)
        }
        finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `5G Stats`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            val builder = Actions(driver)
            builder.moveToElement(driver.findElement(By.xpath("//ul[@class='nav-menu']//a[@href='/insights']"))).build()
                .perform()
            driver.findElement(By.xpath("//ul[@class='sub-menu']//a[@href='/ookla-5g-map']")).click()
            assert(driver.findElement(By.xpath("//div[@class='ookla-5g-map-stats well']//descendant::td[1]")).text.toInt() > 0)
            assert(driver.findElement(By.xpath("//div[@class='ookla-5g-map-stats well']//descendant::td[2]")).text.toInt() > 0)
        }
        finally {
            driver.quit()
        }
    }


    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Global index test`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            val builder = Actions(driver)
            builder.moveToElement(driver.findElement(By.xpath("//ul[@class='nav-menu']//a[@href='/insights']"))).build()
                .perform()
            driver.findElement(By.xpath("//ul[@class='sub-menu']//a[@href='/global-index']")).click()

            assertNotNull(driver.findElement(By.xpath("//table[@class='country-results']//tr[@class='data-result results']//td[@class='country']//a[contains(text(),'Russia')]")))

            driver.findElement(By.xpath("//div[@class='search-button']//a")).click()
            driver.findElement(By.xpath("//div[@role='combobox']//input")).sendKeys("Russia")
            driver.findElement(By.xpath("//li[@role='option']")).click()
            assertEquals("Russia", driver.findElement(By.xpath("//div[@class='page-header-title']")).text)
        }
        finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Setting - change server`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {

            driver.findElement(By.xpath("//div[contains(@class,'result-area')]//a[contains(@href,'/settings')]"))
                .click()
            assertFalse(isElementPresent(driver, By.xpath("//div[contains(text(),'Your settings were updated.')]")))

            driver.findElement(By.xpath("//span[contains(@class,'change-host')]")).click()

            driver.findElement(By.xpath("//input[contains(@name,'location')]")).sendKeys("New York")
            driver.findElement(By.xpath("//span[contains(text(),'New York Software Consultancy')]")).click()

            assertEquals("New York Software Consultancy", driver.findElement(By.xpath("//span[@class='sponsor']")).text)
            assertNotNull(driver.findElement(By.xpath("//div[contains(text(),'Your settings were updated.')]")))
        } finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Settings - changing distance type`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//div[@class='result-area result-area-nav result-area-nav-center']//a[@href='/settings']"))
                .click()
            assertFalse(isElementPresent(driver, By.xpath("//div[contains(text(),'Your settings were updated.')]")))
            val element = driver.findElement(By.xpath("//label[@for='kilometers']"))
            if (Color.fromString(element.getCssValue("color")).asHex() == "#9193a8")
                element.click()
            else
                driver.findElement(By.xpath("//label[@for='miles']")).click()
            assertNotNull(driver.findElement(By.xpath("//div[contains(text(),'Your settings were updated.')]")))
        } finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Settings - changing Date type`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//div[@class='result-area result-area-nav result-area-nav-center']//a[@href='/settings']"))
                .click()
            assertFalse(isElementPresent(driver, By.xpath("//div[contains(text(),'Your settings were updated.')]")))

            val select = Select(driver.findElement(By.xpath("//select[@name='dateFormat']")))
            if (select.getFirstSelectedOption().text == "MM/DD/YYYY")
                select.selectByIndex(1)
            else
                select.selectByIndex(0)

            assertNotNull(driver.findElement(By.xpath("//div[contains(text(),'Your settings were updated.')]")))
        } finally {
            driver.quit()
        }
    }


    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Settings - changing speed type`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//div[@class='result-area result-area-nav result-area-nav-center']//a[@href='/settings']"))
                .click()
            assertFalse(isElementPresent(driver, By.xpath("//div[contains(text(),'Your settings were updated.')]")))
            val element = driver.findElement(By.xpath("//label[@for='kilobits']"))
            if (Color.fromString(element.getCssValue("color")).asHex() == "#9193a8")
                element.click()
            else
                driver.findElement(By.xpath("//label[@for='megabits']")).click()
            assertNotNull(driver.findElement(By.xpath("//div[contains(text(),'Your settings were updated.')]")))
        } finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Settings - Current connection`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//div[@class='result-area result-area-nav result-area-nav-center']//a[@href='/settings']"))
                .click()
            assertTrue(isElementPresent(driver, By.xpath("//legend[text()='Current Connection']")))

            assertEquals(
                3,
                driver.findElement(By.xpath("//legend[text()='Current Connection']/..//descendant::div[1]")).text.filter { ch -> ch == '.' }
                    .count()
            )
            assertEquals(
                "St Petersburg",
                driver.findElement(By.xpath("//legend[text()='Current Connection']/..//descendant::div[2]")).text
            )
            assertTrue(driver.findElement(By.xpath("//legend[text()='Current Connection']/..//descendant::dd[1]")).text.toDouble() >= 0)
            assertTrue(driver.findElement(By.xpath("//legend[text()='Current Connection']/..//descendant::dd[1]")).text.toDouble() >= 0)
        } finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Network page`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {
            driver.findElement(By.xpath("//ul[@class='nav-menu']//a[@href='/speedtest-servers']")).click()
            assertTrue(
                driver.findElement(By.xpath("//div[@class='stats-block']//descendant::div[@class='gauge-number'] [2]")).text.replace(
                    ",",
                    ""
                ).toInt() > 0
            )
            assertTrue(
                driver.findElement(By.xpath("//div[@class='stats-block']//descendant::div[@class='gauge-number'] [1]")).text.replace(
                    ",",
                    ""
                ).toInt() > 0
            )
        } finally {
            driver.quit()
        }
    }


    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Blog page`(browser: String){
        val driver: WebDriver = setUp(browser)
        try {
            val builder = Actions(driver)
            builder.moveToElement(driver.findElement(By.xpath("//ul[@class='nav-menu']//a[@href='/insights']"))).build()
                .perform()
            driver.findElement(By.xpath("//ul[@class='nav-menu']//a[@href='/insights/blog']")).click()
            println(driver.title)
            assertEquals("Speedtest Stories & Analysis: Data-driven articles on internet speeds",driver.title)
        }
        finally {
            driver.quit()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["chrome", "firefox"])
    fun `Test previous results`(browser: String) {
        val driver: WebDriver = setUp(browser)
        try {

            driver.findElement(By.xpath("//div[@class='start-button']//a[@role='button']")).click()
            val results = WebDriverWait(driver, Duration.ofMinutes(2))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='result-data']/a[contains(@href,'result')]")));
            val ping =
                driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value ping-speed']")).text.toInt()
            val downloadSpeed =
                driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value download-speed']")).text.toDouble()
            val uploadSpeed =
                driver.findElement(By.xpath("//span[@class='result-data-large number result-data-value upload-speed']")).text.toDouble()

            driver.findElement(By.xpath("//div[@class='result-area result-area-nav result-area-nav-right']//a[@href='/results']"))
                .click()

            assertEquals(
                ping,
                driver.findElement(By.xpath("//table//descendant::tr[1]//td[@class='table-number ping-speed']")).text.toInt()
            )
            assertEquals(
                downloadSpeed,
                driver.findElement(By.xpath("//table//descendant::tr[1]//td[@class='table-number download-speed']")).text.toDouble(),
                0.01
            )
            assertEquals(
                uploadSpeed,
                driver.findElement(By.xpath("//table//descendant::tr[1]//td[@class='table-number upload-speed']")).text.toDouble(),
                0.01
            )
        }
        finally {
            driver.quit()
        }
    }


    fun isElementPresent(driver: WebDriver, locatorKey: By?): Boolean {
        return try {
            driver.findElement(locatorKey)
            true
        } catch (e: NoSuchElementException) {
            false
        }
    }
}
