package co.innoshop.android.ui

import androidx.test.platform.app.InstrumentationRegistry
import co.innoshop.android.WooCommerce
import co.innoshop.android.di.AppComponentTest
import co.innoshop.android.di.DaggerAppComponentTest
import org.junit.Before

open class TestBase {
    protected lateinit var appContext: WooCommerce
    protected lateinit var mockedAppComponent: co.innoshop.android.di.AppComponentTest

    @Before
    open fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as WooCommerce

        mockedAppComponent = DaggerAppComponentTest.builder()
                .application(appContext)
                .build()
    }
}
