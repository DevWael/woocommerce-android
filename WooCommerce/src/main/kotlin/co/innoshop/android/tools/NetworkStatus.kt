package co.innoshop.android.tools

import android.content.Context
import co.innoshop.android.annotations.OpenClassOnDebug
import org.wordpress.android.util.NetworkUtils
import javax.inject.Singleton

@Singleton
@OpenClassOnDebug
class NetworkStatus(private var context: Context) {
    fun isConnected() = NetworkUtils.isNetworkAvailable(context)
}
