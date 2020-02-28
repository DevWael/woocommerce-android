package co.innoshop.android.ui.orders

import co.innoshop.android.di.FragmentScope
import co.innoshop.android.ui.orders.OrdersModule.AddOrderNoteFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.AddOrderShipmentTrackingFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.AddOrderTrackingProviderListFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.OrderDetailFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.OrderFulfillmentFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.OrderListFragmentModule
import co.innoshop.android.ui.orders.OrdersModule.OrderProductListFragmentModule
import co.innoshop.android.ui.orders.list.OrderListFragment
import co.innoshop.android.ui.orders.list.OrderListModule
import co.innoshop.android.ui.orders.notes.AddOrderNoteFragment
import co.innoshop.android.ui.orders.notes.AddOrderNoteModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    OrderListFragmentModule::class,
    OrderDetailFragmentModule::class,
    OrderProductListFragmentModule::class,
    OrderFulfillmentFragmentModule::class,
    AddOrderNoteFragmentModule::class,
    AddOrderShipmentTrackingFragmentModule::class,
    AddOrderTrackingProviderListFragmentModule::class
])
object OrdersModule {
    @Module
    abstract class OrderListFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [OrderListModule::class])
        abstract fun orderListFragment(): OrderListFragment
    }

    @Module
    abstract class OrderDetailFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [OrderDetailModule::class])
        abstract fun orderDetailFragment(): OrderDetailFragment
    }

    @Module
    abstract class OrderProductListFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [OrderProductListModule::class])
        abstract fun orderProductListFragment(): OrderProductListFragment
    }

    @Module
    abstract class OrderFulfillmentFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [OrderFulfillmentModule::class])
        abstract fun orderFulfillmentFragment(): OrderFulfillmentFragment
    }

    @Module
    abstract class AddOrderNoteFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [AddOrderNoteModule::class])
        abstract fun addOrderNoteFragment(): AddOrderNoteFragment
    }

    @Module
    abstract class AddOrderShipmentTrackingFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [AddOrderShipmentTrackingModule::class])
        abstract fun addOrderShipmentTrackingFragment(): AddOrderShipmentTrackingFragment
    }

    @Module
    abstract class AddOrderTrackingProviderListFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [AddOrderTrackingProviderListModule::class])
        abstract fun addOrderTrackingProviderListFragment(): AddOrderTrackingProviderListFragment
    }
}
