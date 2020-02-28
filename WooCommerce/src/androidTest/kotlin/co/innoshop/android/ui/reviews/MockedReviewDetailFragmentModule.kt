package co.innoshop.android.ui.reviews

import co.innoshop.android.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MockedReviewDetailFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [MockedReviewDetailModule::class])
    abstract fun reviewDetailFragment(): ReviewDetailFragment
}
