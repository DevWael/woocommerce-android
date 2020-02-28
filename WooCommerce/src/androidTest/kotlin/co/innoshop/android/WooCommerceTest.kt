package co.innoshop.android

import co.innoshop.android.di.AppComponent
import co.innoshop.android.di.DaggerAppComponentTest

open class WooCommerceTest : co.innoshop.android.WooCommerce() {
    override val component: AppComponent by lazy {
        DaggerAppComponentTest.builder()
                .application(this)
                .build()
    }
}
