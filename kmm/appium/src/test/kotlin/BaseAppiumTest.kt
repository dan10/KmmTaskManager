import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import org.junit.AfterClass
import org.junit.BeforeClass


open class BaseAppiumTest {

    companion object {
        protected const val PORT: Int = 4723
        @JvmStatic
        private var service: AppiumDriverLocalService? = null
        @JvmStatic
        protected var driver: AndroidDriver? = null

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            service = AppiumServiceBuilder()
                .withIPAddress("127.0.0.1")
                .usingPort(PORT)
                .build()
            service?.start()
            val options = UiAutomator2Options()
                .setDeviceName("Android Emulator")
                .enableBiDi()
                .setAppPackage("")
                //.setApp(TestResources.API_DEMOS_APK.toString())
                .eventTimings()

            driver = AndroidDriver(service?.url, options)
        }

        @AfterClass
        @JvmStatic
         fun afterClass() {
             driver?.quit()
             service?.stop()
        }
    }
}