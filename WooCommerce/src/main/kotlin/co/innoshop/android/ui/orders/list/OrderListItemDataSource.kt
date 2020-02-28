package co.innoshop.android.ui.orders.list

import co.innoshop.android.model.TimeGroup
import co.innoshop.android.model.TimeGroup.GROUP_FUTURE
import co.innoshop.android.model.TimeGroup.GROUP_OLDER_MONTH
import co.innoshop.android.model.TimeGroup.GROUP_OLDER_TWO_DAYS
import co.innoshop.android.model.TimeGroup.GROUP_OLDER_WEEK
import co.innoshop.android.model.TimeGroup.GROUP_TODAY
import co.innoshop.android.model.TimeGroup.GROUP_YESTERDAY
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.ui.orders.list.OrderListItemIdentifier.OrderIdentifier
import co.innoshop.android.ui.orders.list.OrderListItemIdentifier.SectionHeaderIdentifier
import co.innoshop.android.ui.orders.list.OrderListItemUIType.LoadingItem
import co.innoshop.android.ui.orders.list.OrderListItemUIType.OrderListItemUI
import co.innoshop.android.ui.orders.list.OrderListItemUIType.SectionHeader
import co.innoshop.android.util.DateUtils
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.WCOrderActionBuilder
import org.wordpress.android.fluxc.model.LocalOrRemoteId.RemoteId
import org.wordpress.android.fluxc.model.WCOrderListDescriptor
import org.wordpress.android.fluxc.model.WCOrderSummaryModel
import org.wordpress.android.fluxc.model.list.datasource.ListItemDataSourceInterface
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCOrderStore.FetchOrderListPayload
import org.wordpress.android.util.DateTimeUtils
import java.util.Date

/**
 * Works with a [androidx.paging.PagedList] by providing the logic needed to fetch the data used to populate
 * the order list view.
 *
 * @see [ListItemDataSourceInterface] and [org.wordpress.android.fluxc.model.list.datasource.InternalPagedListDataSource]
 * in FluxC to get a better understanding of how this works with the underlying internal list management code.
 */
class OrderListItemDataSource(
    private val dispatcher: Dispatcher,
    private val orderStore: WCOrderStore,
    private val networkStatus: NetworkStatus,
    private val fetcher: OrderFetcher
) : ListItemDataSourceInterface<WCOrderListDescriptor, OrderListItemIdentifier, OrderListItemUIType> {
    override fun getItemsAndFetchIfNecessary(
        listDescriptor: WCOrderListDescriptor,
        itemIdentifiers: List<OrderListItemIdentifier>
    ): List<OrderListItemUIType> {
        val remoteItemIds = itemIdentifiers.mapNotNull { (it as? OrderIdentifier)?.remoteId }
        val ordersMap = orderStore.getOrdersForDescriptor(listDescriptor, remoteItemIds)

        // Fetch missing items
        fetcher.fetchOrders(
                site = listDescriptor.site,
                remoteItemIds = remoteItemIds.filter { !ordersMap.containsKey(it) }
        )

        val mapSummary = { remoteOrderId: RemoteId ->
            ordersMap[remoteOrderId].let { order ->
                if (order == null) {
                    LoadingItem(remoteOrderId)
                } else {
                    OrderListItemUI(
                            remoteOrderId = RemoteId(order.remoteOrderId),
                            orderNumber = order.number,
                            orderName = "${order.billingFirstName} ${order.billingLastName}",
                            orderTotal = order.total,
                            status = order.status,
                            dateCreated = order.dateCreated,
                            currencyCode = order.currency
                    )
                }
            }
        }

        return itemIdentifiers.map { identifier ->
            when (identifier) {
                is OrderIdentifier -> mapSummary(identifier.remoteId)
                is SectionHeaderIdentifier -> SectionHeader(title = identifier.title)
            }
        }
    }

    override fun getItemIdentifiers(
        listDescriptor: WCOrderListDescriptor,
        remoteItemIds: List<RemoteId>,
        isListFullyFetched: Boolean
    ): List<OrderListItemIdentifier> {
        val orderSummaries = orderStore.getOrderSummariesByRemoteOrderIds(listDescriptor.site, remoteItemIds)
                .let { summariesByRemoteId ->
                    val summaries = remoteItemIds.mapNotNull { summariesByRemoteId[it] }

                    if (!networkStatus.isConnected()) {
                        // The network is not connected so remove any order summaries from the list where
                        // a matching order has not yet been downloaded. This prevents the user from seeing
                        // a "loading" view for that item indefinitely.
                        val cachedOrders = orderStore.getOrdersForDescriptor(listDescriptor, remoteItemIds)
                        summaries.filter { cachedOrders.containsKey(RemoteId(it.remoteOrderId)) }
                    } else summaries
                }

        val listFuture = mutableListOf<OrderIdentifier>()
        val listToday = mutableListOf<OrderIdentifier>()
        val listYesterday = mutableListOf<OrderIdentifier>()
        val listTwoDays = mutableListOf<OrderIdentifier>()
        val listWeek = mutableListOf<OrderIdentifier>()
        val listMonth = mutableListOf<OrderIdentifier>()
        val mapToRemoteOrderIdentifier = { summary: WCOrderSummaryModel ->
            OrderIdentifier(RemoteId(summary.remoteOrderId))
        }
        orderSummaries.forEach {
            // Default to today if the date cannot be parsed. This date is in UTC.
            val date: Date = DateTimeUtils.dateUTCFromIso8601(it.dateCreated) ?: DateTimeUtils.nowUTC()

            // Check if future-dated orders should be excluded from the results list.
            if (listDescriptor.excludeFutureOrders) {
                val currentUtcDate = DateTimeUtils.nowUTC()
                if (DateUtils.isAfterDate(currentUtcDate, date)) {
                    // This order is dated for the future so skip adding it to the list
                    return@forEach
                }
            }

            when (TimeGroup.getTimeGroupForDate(date)) {
                GROUP_FUTURE -> listFuture.add(mapToRemoteOrderIdentifier(it))
                GROUP_TODAY -> listToday.add(mapToRemoteOrderIdentifier(it))
                GROUP_YESTERDAY -> listYesterday.add(mapToRemoteOrderIdentifier(it))
                GROUP_OLDER_TWO_DAYS -> listTwoDays.add(mapToRemoteOrderIdentifier(it))
                GROUP_OLDER_WEEK -> listWeek.add(mapToRemoteOrderIdentifier(it))
                GROUP_OLDER_MONTH -> listMonth.add(mapToRemoteOrderIdentifier(it))
            }
        }

        val allItems = mutableListOf<OrderListItemIdentifier>()
        if (listFuture.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_FUTURE)) + listFuture
        }

        if (listToday.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_TODAY)) + listToday
        }
        if (listYesterday.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_YESTERDAY)) + listYesterday
        }
        if (listTwoDays.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_OLDER_TWO_DAYS)) + listTwoDays
        }
        if (listWeek.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_OLDER_WEEK)) + listWeek
        }
        if (listMonth.isNotEmpty()) {
            allItems += listOf(SectionHeaderIdentifier(GROUP_OLDER_MONTH)) + listMonth
        }
        return allItems
    }

    override fun fetchList(listDescriptor: WCOrderListDescriptor, offset: Long) {
        val fetchOrderListPayload = FetchOrderListPayload(listDescriptor, offset)
        dispatcher.dispatch(WCOrderActionBuilder.newFetchOrderListAction(fetchOrderListPayload))
    }
}
