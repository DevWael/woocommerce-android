package co.innoshop.android.ui.main

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.di.ActivityScope
import co.innoshop.android.ui.base.UIMessageResolver
import dagger.Module
import dagger.Provides

@Module
object MockedMainModule {
    private var userIsLoggedIn = false

    fun setUserIsLoggedInResponse(userLoggedIn: Boolean) {
        userIsLoggedIn = userLoggedIn
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideMainPresenter(): MainContract.Presenter {
        val mockedMainPresenter = mock<MainPresenter>()
        whenever(mockedMainPresenter.userIsLoggedIn()).thenReturn(userIsLoggedIn)
        return mockedMainPresenter
    }

    @JvmStatic
    @ActivityScope
    @Provides
    fun provideUiMessageResolver(mainUIMessageResolver: MainUIMessageResolver): UIMessageResolver {
        return mainUIMessageResolver
    }
}
