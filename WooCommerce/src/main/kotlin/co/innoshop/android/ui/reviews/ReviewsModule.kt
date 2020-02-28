package co.innoshop.android.ui.reviews

import co.innoshop.android.di.FragmentScope
import co.innoshop.android.ui.reviews.ReviewsModule.ReviewDetailFragmentModule
import co.innoshop.android.ui.reviews.ReviewsModule.ReviewListFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    ReviewDetailFragmentModule::class,
    ReviewListFragmentModule::class
])
object ReviewsModule {
    @Module
    internal abstract class ReviewDetailFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ReviewDetailModule::class])
        abstract fun reviewDetailFragment(): ReviewDetailFragment
    }

    @Module
    internal abstract class ReviewListFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ReviewListModule::class])
        abstract fun reviewListFragment(): ReviewListFragment
    }
}
