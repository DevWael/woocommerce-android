package co.innoshop.android.ui.orders

import android.content.Context
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_NOTE_ADD_FAILED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_NOTE_ADD_SUCCESS
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_TRACKING_ADD_FAILED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_TRACKING_ADD_SUCCESS
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_TRACKING_DELETE_FAILED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_TRACKING_DELETE_SUCCESS
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.extensions.isVirtualProduct
import co.innoshop.android.model.Refund
import co.innoshop.android.model.toAppModel
import co.innoshop.android.network.ConnectionChangeReceiver
import co.innoshop.android.network.ConnectionChangeReceiver.ConnectionChangeEvent
import co.innoshop.android.push.NotificationHandler
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.WooLog
import co.innoshop.android.util.WooLog.T
import co.innoshop.android.util.WooLog.T.NOTIFICATIONS
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.NotificationAction.MARK_NOTIFICATIONS_READ
import org.wordpress.android.fluxc.action.WCOrderAction
import org.wordpress.android.fluxc.action.WCOrderAction.ADD_ORDER_SHIPMENT_TRACKING
import org.wordpress.android.fluxc.action.WCOrderAction.DELETE_ORDER_SHIPMENT_TRACKING
import org.wordpress.android.fluxc.action.WCOrderAction.POST_ORDER_NOTE
import org.wordpress.android.fluxc.action.WCOrderAction.UPDATE_ORDER_STATUS
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_SINGLE_PRODUCT
import org.wordpress.android.fluxc.generated.NotificationActionBuilder
import org.wordpress.android.fluxc.generated.WCOrderActionBuilder
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderNoteModel
import org.wordpress.android.fluxc.model.WCOrderShipmentTrackingModel
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.fluxc.model.notification.NotificationModel
import org.wordpress.android.fluxc.model.order.OrderIdentifier
import org.wordpress.android.fluxc.model.order.toIdSet
import org.wordpress.android.fluxc.model.refunds.WCRefundModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.CoreOrderStatus
import org.wordpress.android.fluxc.store.NotificationStore
import org.wordpress.android.fluxc.store.NotificationStore.MarkNotificationsReadPayload
import org.wordpress.android.fluxc.store.NotificationStore.OnNotificationChanged
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCOrderStore.DeleteOrderShipmentTrackingPayload
import org.wordpress.android.fluxc.store.WCOrderStore.FetchOrderNotesPayload
import org.wordpress.android.fluxc.store.WCOrderStore.FetchOrderShipmentTrackingsPayload
import org.wordpress.android.fluxc.store.WCOrderStore.FetchOrderStatusOptionsPayload
import org.wordpress.android.fluxc.store.WCOrderStore.OnOrderChanged
import org.wordpress.android.fluxc.store.WCOrderStore.OnOrderStatusOptionsChanged
import org.wordpress.android.fluxc.store.WCOrderStore.UpdateOrderStatusPayload
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.OnProductChanged
import org.wordpress.android.fluxc.store.WCRefundStore
import javax.inject.Inject

@OpenClassOnDebug
class OrderDetailPresenter @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val dispatcher: Dispatcher,
    private val orderStore: WCOrderStore,
    private val refundStore: WCRefundStore,
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite,
    private val uiMessageResolver: UIMessageResolver,
    private val networkStatus: NetworkStatus,
    private val notificationStore: NotificationStore
) : OrderDetailContract.Presenter {
    companion object {
        private val TAG: String = OrderDetailPresenter::class.java.simpleName
    }

    override var orderModel: WCOrderModel? = null
    override var orderIdentifier: OrderIdentifier? = null
    override var isUsingCachedNotes = false
    override var isUsingCachedShipmentTrackings = false
    override var deletedOrderShipmentTrackingModel: WCOrderShipmentTrackingModel? = null

    /**
     * Adding another flag here to check if shipment trackings have been fetched from api.
     * This is used to passed to [OrderFulfillmentPresenter] and if true, shipment trackings
     * are fetched from db
     */
    override var isShipmentTrackingsFetched: Boolean = false
    private var pendingRemoteOrderId: Long? = null
    private var pendingMarkReadNotification: NotificationModel? = null

    private var orderView: OrderDetailContract.View? = null
    private var isNotesInit = false
    private var isRefreshingOrderStatusOptions = false

    private var deferredRefunds: Deferred<WooResult<List<WCRefundModel>>>? = null

    override fun takeView(view: OrderDetailContract.View) {
        orderView = view
        dispatcher.register(this)
        ConnectionChangeReceiver.getEventBus().register(this)
    }

    override fun dropView() {
        orderView = null
        isNotesInit = false
        dispatcher.unregister(this)
        ConnectionChangeReceiver.getEventBus().unregister(this)
    }

    /**
     * Loading order detail from local database
     */
    override fun loadOrderDetailFromDb(orderIdentifier: OrderIdentifier): WCOrderModel? =
            orderStore.getOrderByIdentifier(orderIdentifier)

    /**
     * displaying the loaded order detail data in UI
     */
    override fun loadOrderDetail(orderIdentifier: OrderIdentifier, markComplete: Boolean) {
        OrderDetailFragment@this.orderIdentifier = orderIdentifier
        if (orderIdentifier.isNotEmpty()) {
            orderModel = loadOrderDetailFromDb(orderIdentifier)
            orderModel?.let { order ->
                orderView?.showOrderDetail(order, isFreshData = false)
                if (markComplete) orderView?.showChangeOrderStatusSnackbar(CoreOrderStatus.COMPLETED.value)
                loadRefunds()
                loadOrderNotes()
                loadOrderShipmentTrackings()
            } ?: fetchOrder(orderIdentifier.toIdSet().remoteOrderId, true)
        }
    }

    private fun loadRefunds() {
        orderModel?.let {
            val refunds = loadRefundsFromDb(it)
            orderView?.showRefunds(it, refunds)

            GlobalScope.launch(dispatchers.main) {
                fetchRefunds(it.remoteOrderId)
                val freshRefunds = awaitRefunds()
                orderView?.showRefunds(it, freshRefunds)
            }
        }
    }

    private fun loadRefundsFromDb(order: WCOrderModel): List<Refund> {
        return refundStore.getAllRefunds(selectedSite.get(), order.remoteOrderId)
                .map { it.toAppModel() }
                .reversed()
    }

    override fun refreshOrderAfterDelay(refreshDelay: Long) {
        GlobalScope.launch(dispatchers.computation) {
            delay(refreshDelay)
            refreshOrderDetail(false)
        }
    }

    override fun loadOrderNotes() {
        orderView?.showOrderNotesSkeleton(true)
        orderModel?.let { order ->
            // Preload order notes from database if available
            fetchAndLoadOrderNotesFromDb()

            if (networkStatus.isConnected()) {
                // Attempt to refresh notes from api in the background
                requestOrderNotesFromApi(order)
            } else {
                // Track so when the device is connected notes can be refreshed
                orderView?.showOrderNotesSkeleton(false)
                isUsingCachedNotes = true
            }
        }
    }

    /**
     * Fetch the order notes from the device database
     * Segregating the fetching from db and displaying to UI into two separate methods
     * for better ui testing
     */
    override fun fetchOrderNotesFromDb(order: WCOrderModel): List<WCOrderNoteModel> {
        return orderStore.getOrderNotesForOrder(order)
    }

    /**
     * Fetch and display the order notes from the device database
     */
    override fun fetchAndLoadOrderNotesFromDb() {
        orderModel?.let { order ->
            val notes = fetchOrderNotesFromDb(order)
            if (isNotesInit) {
                orderView?.updateOrderNotes(notes)
            } else {
                isNotesInit = true
                orderView?.showOrderNotes(notes)
            }
        }
    }

    override fun loadOrderShipmentTrackings() {
        orderModel?.let { order ->
            // Preload trackings from the db is available
            loadShipmentTrackingsFromDb()

            if (networkStatus.isConnected()) {
                // Attempt to refresh trackings from api in the background
                requestShipmentTrackingsFromApi(order)
            } else {
                // Track so when the device is connected shipment trackings can be refreshed
                isUsingCachedShipmentTrackings = true
            }
        }
    }

    /**
     * Fetch the order shipment trackings from the device database
     * Segregating the fetching from db and displaying to UI into two separate methods
     * for better ui testing
     */
    override fun getOrderShipmentTrackingsFromDb(order: WCOrderModel): List<WCOrderShipmentTrackingModel> {
        return orderStore.getShipmentTrackingsForOrder(order)
    }

    /**
     * Fetch and display the order shipment trackings from the device database
     */
    override fun loadShipmentTrackingsFromDb() {
        orderModel?.let { order ->
            val trackings = getOrderShipmentTrackingsFromDb(order)
            orderView?.showOrderShipmentTrackings(trackings)
        }
    }

    override fun refreshOrderDetail(displaySkeleton: Boolean) {
        orderModel?.let {
            fetchOrder(it.remoteOrderId, displaySkeleton)
        }
    }

    override fun fetchOrder(remoteOrderId: Long, displaySkeleton: Boolean) {
        fetchRefunds(remoteOrderId)

        orderView?.showSkeleton(displaySkeleton)
        val payload = WCOrderStore.FetchSingleOrderPayload(selectedSite.get(), remoteOrderId)
        dispatcher.dispatch(WCOrderActionBuilder.newFetchSingleOrderAction(payload))
    }

    private fun fetchRefunds(remoteOrderId: Long) {
        deferredRefunds = GlobalScope.async {
            refundStore.fetchAllRefunds(selectedSite.get(), remoteOrderId)
        }
    }

    /**
     * Returns true if all the products specified in the [WCOrderModel.LineItem] is a virtual product
     * and if product exists in the local cache.
     */
    override fun isVirtualProduct(order: WCOrderModel) = isVirtualProduct(
            selectedSite.get(), order.getLineItemList(), productStore
    )

    override fun doChangeOrderStatus(newStatus: String) {
        if (!networkStatus.isConnected()) {
            // Device is not connected. Display generic message and exit. Technically we shouldn't get this far, but
            // just in case...
            uiMessageResolver.showOfflineSnack()
            return
        }

        orderModel?.let { order ->
            val payload = UpdateOrderStatusPayload(order, selectedSite.get(), newStatus)
            dispatcher.dispatch(WCOrderActionBuilder.newUpdateOrderStatusAction(payload))
        }
    }

    /**
     * Removes the notification from the system bar if present, fetch the new order notification from the database,
     * and fire the event to mark it as read.
     */
    override fun markOrderNotificationRead(context: Context, remoteNotificationId: Long) {
        NotificationHandler.removeNotificationWithNoteIdFromSystemBar(context, remoteNotificationId.toString())
        notificationStore.getNotificationByRemoteId(remoteNotificationId)?.let {
            // Send event that an order with a matching notification was opened
            AnalyticsTracker.track(Stat.NOTIFICATION_OPEN, mapOf(
                    AnalyticsTracker.KEY_TYPE to AnalyticsTracker.VALUE_ORDER,
                    AnalyticsTracker.KEY_ALREADY_READ to it.read))

            if (!it.read) {
                it.read = true
                pendingMarkReadNotification = it
                val payload = MarkNotificationsReadPayload(listOf(it))
                dispatcher.dispatch(NotificationActionBuilder.newMarkNotificationsReadAction(payload))
            }
        }
    }

    override fun getOrderStatusForStatusKey(key: String): WCOrderStatusModel {
        return orderStore.getOrderStatusForSiteAndKey(selectedSite.get(), key) ?: WCOrderStatusModel().apply {
            statusKey = key
            label = key
        }
    }

    override fun getOrderStatusOptions(): Map<String, WCOrderStatusModel> {
        val options = orderStore.getOrderStatusOptionsForSite(selectedSite.get())
        return if (options.isEmpty()) {
            refreshOrderStatusOptions()
            emptyMap()
        } else {
            options.map { it.statusKey to it }.toMap()
        }
    }

    override fun refreshOrderStatusOptions() {
        // Refresh the order status options from the API
        if (!isRefreshingOrderStatusOptions) {
            isRefreshingOrderStatusOptions = true
            dispatcher.dispatch(
                    WCOrderActionBuilder
                            .newFetchOrderStatusOptionsAction(FetchOrderStatusOptionsPayload(selectedSite.get()))
            )
        }
    }

    override fun deleteOrderShipmentTracking(wcOrderShipmentTrackingModel: WCOrderShipmentTrackingModel) {
        this.deletedOrderShipmentTrackingModel = wcOrderShipmentTrackingModel
        if (!networkStatus.isConnected()) {
            // Device is not connected. Display generic message and exit. Technically we shouldn't get this far, but
            // just in case...
            uiMessageResolver.showOfflineSnack()
            // re-add the deleted tracking item back to the shipment tracking list
            orderView?.undoDeletedTrackingOnError(deletedOrderShipmentTrackingModel)
            deletedOrderShipmentTrackingModel = null
            return
        }

        orderModel?.let { order ->
            AnalyticsTracker.track(Stat.ORDER_TRACKING_DELETE, mapOf(
                    AnalyticsTracker.KEY_SOURCE to AnalyticsTracker.VALUE_ORDER_DETAIL
            ))
            val payload = DeleteOrderShipmentTrackingPayload(selectedSite.get(), order, wcOrderShipmentTrackingModel)
            dispatcher.dispatch(WCOrderActionBuilder.newDeleteOrderShipmentTrackingAction(payload))
        }
    }

    private suspend fun awaitRefunds(): List<Refund> {
        return deferredRefunds?.await()?.let { requestResult ->
            if (!requestResult.isError) {
                requestResult.model?.map { it.toAppModel() } ?: emptyList()
            } else {
                emptyList()
            }
        } ?: emptyList()
    }

    @Suppress("unused")
    @Subscribe(threadMode = MAIN)
    fun onOrderChanged(event: OnOrderChanged) {
        if (event.causeOfChange == WCOrderAction.FETCH_SINGLE_ORDER) {
            if (event.isError || (orderIdentifier.isNullOrBlank() && pendingRemoteOrderId == null)) {
                val message = event.error?.message ?: "empty orderIdentifier"
                WooLog.e(T.ORDERS, "$TAG - Error fetching order : $message")
                orderView?.showLoadOrderError()
            } else {
                orderModel = loadOrderDetailFromDb(orderIdentifier!!)
                GlobalScope.launch(dispatchers.main) {
                    orderModel?.let { order ->
                        fetchRefunds(order.remoteOrderId)
                        val refunds = awaitRefunds()
                        orderView?.showOrderDetail(order, isFreshData = true)
                        orderView?.showRefunds(order, refunds)
                        orderView?.showSkeleton(false)
                        loadOrderNotes()
                        loadOrderShipmentTrackings()
                    } ?: orderView?.showLoadOrderError()
                }
            }
        } else if (event.causeOfChange == WCOrderAction.FETCH_ORDER_NOTES) {
            orderView?.showOrderNotesSkeleton(false)
            if (event.isError) {
                WooLog.e(T.ORDERS, "$TAG - Error fetching order notes : ${event.error.message}")
                orderView?.showNotesErrorSnack()
            } else {
                orderModel?.let { order ->
                    AnalyticsTracker.track(
                            Stat.ORDER_NOTES_LOADED,
                            mapOf(AnalyticsTracker.KEY_ID to order.remoteOrderId))

                    isUsingCachedNotes = false
                    val notes = orderStore.getOrderNotesForOrder(order)
                    orderView?.updateOrderNotes(notes)
                }
            }
        } else if (event.causeOfChange == WCOrderAction.FETCH_ORDER_SHIPMENT_TRACKINGS) {
            if (event.isError) {
                WooLog.e(T.ORDERS, "$TAG - Error fetching order shipment tracking info: ${event.error.message}")
            } else {
                orderModel?.let { order ->
                    AnalyticsTracker.track(
                            Stat.ORDER_TRACKING_LOADED,
                            mapOf(AnalyticsTracker.KEY_ID to order.remoteOrderId))

                    isUsingCachedShipmentTrackings = false
                    isShipmentTrackingsFetched = true
                    loadShipmentTrackingsFromDb()
                }
            }
        } else if (event.causeOfChange == UPDATE_ORDER_STATUS) {
            if (event.isError) {
                WooLog.e(T.ORDERS, "$TAG - Error updating order status : ${event.error.message}")

                AnalyticsTracker.track(
                        Stat.ORDER_STATUS_CHANGE_FAILED, mapOf(
                        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
                        AnalyticsTracker.KEY_ERROR_TYPE to event.error.type.toString(),
                        AnalyticsTracker.KEY_ERROR_DESC to event.error.message))

                orderView?.let {
                    it.showOrderStatusChangedError()
                    it.markOrderStatusChangedFailed()
                }
            } else {
                AnalyticsTracker.track(Stat.ORDER_STATUS_CHANGE_SUCCESS)

                // Successfully marked order status changed
                orderModel?.let {
                    orderModel = loadOrderDetailFromDb(it.getIdentifier())
                }
                orderView?.markOrderStatusChangedSuccess()
            }

            // if order detail refresh is pending, call refresh order detail
            orderView?.refreshOrderDetail()
        } else if (event.causeOfChange == POST_ORDER_NOTE) {
            if (event.isError) {
                AnalyticsTracker.track(
                        ORDER_NOTE_ADD_FAILED, mapOf(
                        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
                        AnalyticsTracker.KEY_ERROR_TYPE to event.error.type.toString(),
                        AnalyticsTracker.KEY_ERROR_DESC to event.error.message))

                WooLog.e(T.ORDERS, "$TAG - Error posting order note : ${event.error.message}")
                orderView?.showAddOrderNoteErrorSnack()
            } else {
                AnalyticsTracker.track(ORDER_NOTE_ADD_SUCCESS)
            }

            // note that we refresh even on error to make sure the transient note is removed
            // from the note list
            fetchAndLoadOrderNotesFromDb()
        } else if (event.causeOfChange == DELETE_ORDER_SHIPMENT_TRACKING) {
            if (event.isError) {
                AnalyticsTracker.track(ORDER_TRACKING_DELETE_FAILED)
                WooLog.e(T.ORDERS, "$TAG - Error deleting order shipment tracking : ${event.error.message}")
                orderView?.showDeleteTrackingErrorSnack()
                orderView?.undoDeletedTrackingOnError(deletedOrderShipmentTrackingModel)
                deletedOrderShipmentTrackingModel = null
            } else {
                AnalyticsTracker.track(ORDER_TRACKING_DELETE_SUCCESS)
                orderView?.markTrackingDeletedOnSuccess()
            }

            // if order detail refresh is pending, call refresh order detail
            orderView?.refreshOrderDetail()
        } else if (event.causeOfChange == ADD_ORDER_SHIPMENT_TRACKING) {
            if (event.isError) {
                AnalyticsTracker.track(
                        ORDER_TRACKING_ADD_FAILED, mapOf(
                        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
                        AnalyticsTracker.KEY_ERROR_TYPE to event.error.type.toString(),
                        AnalyticsTracker.KEY_ERROR_DESC to event.error.message))

                WooLog.e(T.ORDERS, "$TAG - Error posting order note : ${event.error.message}")
                orderView?.showAddAddShipmentTrackingErrorSnack()
            } else {
                AnalyticsTracker.track(ORDER_TRACKING_ADD_SUCCESS)
            }

            // note that we refresh even on error to make sure the transient tracking provider is removed
            // from the tracking list
            loadShipmentTrackingsFromDb()
        }
    }

    /**
     * Request a fresh copy of order notes from the api.
     */
    fun requestOrderNotesFromApi(order: WCOrderModel) {
        val payload = FetchOrderNotesPayload(order, selectedSite.get())
        dispatcher.dispatch(WCOrderActionBuilder.newFetchOrderNotesAction(payload))
    }

    /**
     * Request a fresh copy of order shipment tracking records from the api.
     */
    fun requestShipmentTrackingsFromApi(order: WCOrderModel) {
        val payload = FetchOrderShipmentTrackingsPayload(selectedSite.get(), order)
        dispatcher.dispatch(WCOrderActionBuilder.newFetchOrderShipmentTrackingsAction(payload))
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ConnectionChangeEvent) {
        if (event.isConnected) {
            // Refresh order notes now that a connection is active is needed
            orderModel?.let { order ->
                if (isUsingCachedNotes) {
                    requestOrderNotesFromApi(order)
                }

                if (isUsingCachedShipmentTrackings) {
                    requestShipmentTrackingsFromApi(order)
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onNotificationChanged(event: OnNotificationChanged) {
        when (event.causeOfChange) {
            MARK_NOTIFICATIONS_READ -> onNotificationMarkedRead(event)
        }
    }

    @Suppress
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOrderStatusOptionsChanged(event: OnOrderStatusOptionsChanged) {
        isRefreshingOrderStatusOptions = false

        if (event.isError) {
            WooLog.e(T.ORDERS, "${OrderDetailPresenter.TAG} " +
                    "- Error fetching order status options from the api : ${event.error.message}")
            return
        }

        orderView?.refreshOrderStatus()
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductChanged(event: OnProductChanged) {
        // product was just fetched, show its image
        if (event.causeOfChange == FETCH_SINGLE_PRODUCT && !event.isError) {
            orderView?.refreshProductImages()
            // Refresh the customer info section, once the product information becomes available
            orderModel?.let {
                orderView?.refreshCustomerInfoCard(it)
            }
        }
    }

    private fun onNotificationMarkedRead(event: OnNotificationChanged) {
        pendingMarkReadNotification?.let {
            // We only care about logging an error
            if (event.changedNotificationLocalIds.contains(it.noteId)) {
                if (event.isError) {
                    WooLog.e(NOTIFICATIONS, "$TAG - Error marking new order notification as read!")
                    pendingMarkReadNotification = null
                }
            }
        }
    }
}
