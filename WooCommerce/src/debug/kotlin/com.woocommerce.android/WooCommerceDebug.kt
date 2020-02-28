package co.innoshop.android

import com.facebook.stetho.Stetho
import co.innoshop.android.di.AppComponent
import co.innoshop.android.di.DaggerAppComponentDebug

open class WooCommerceDebug : WooCommerce() {
    override val component: AppComponent by lazy {
        DaggerAppComponentDebug.builder()
                .application(this)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
