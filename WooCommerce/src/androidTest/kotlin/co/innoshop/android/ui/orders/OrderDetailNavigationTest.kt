package co.innoshop.android.ui.orders

import android.os.Build.VERSION
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import co.innoshop.android.R
import co.innoshop.android.helpers.WCDateTimeTestUtils
import co.innoshop.android.helpers.WCMatchers
import co.innoshop.android.ui.TestBase
import co.innoshop.android.ui.main.MainActivityTestRule
import co.innoshop.android.util.DateUtils
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.wordpress.android.fluxc.model.SiteModel

@RunWith(AndroidJUnit4::class)
@LargeTest
class OrderDetailNavigationTest : TestBase() {
    @Rule
    @JvmField var activityTestRule = MainActivityTestRule()

    @Before
    override fun setup() {
        super.setup()
        // Bypass login screen and display dashboard
        activityTestRule.launchMainActivityLoggedIn(null, SiteModel())

        // Make sure the bottom navigation view is showing
        activityTestRule.activity.showBottomNav()

        // add mock data to order list screen
        activityTestRule.setOrderListWithMockData()

        // Click on Orders tab in the bottom bar
        Espresso.onView(ViewMatchers.withId(R.id.orders)).perform(ViewActions.click())
    }

    @Test
    fun verifyOrderDetailCardViewPopulatedSuccessfully() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail()
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card heading matches this format:
        // #1 Jane Masterson
        onView(withId(R.id.orderStatus_orderNum)).check(matches(
                withText(appContext.getString(
                R.string.orderdetail_orderstatus_heading,
                mockWCOrderModel.number, mockWCOrderModel.billingFirstName, mockWCOrderModel.billingLastName)))
        )

        // check if order card date created matches this format:
        // Created 8/12/2017 at 4:11 PM:
        val todayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, todayDateString))))
    }

    @Test
    fun verifyOrderDetailCardDateCreatedTodayView() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail(
                WCDateTimeTestUtils.formatDate(WCDateTimeTestUtils.getCurrentDateTime())
        )
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card created date is displayed in this format:
        // Created <friendly date category> at <time> message, for Today:
        val todayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, todayDateString))))
    }

    @Test
    fun verifyOrderDetailCardDateCreatedYesterdayView() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail(
                WCDateTimeTestUtils.formatDate(WCDateTimeTestUtils.getCurrentDateTimeMinusDays(1))
        )
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card created date is displayed in this format:
        // Created <friendly date category> at <time> message, for Yesterday:
        val yDayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, yDayDateString))))
    }

    @Test
    fun verifyOrderDetailCardDateCreatedTwoDaysView() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail(
                WCDateTimeTestUtils.formatDate(WCDateTimeTestUtils.getCurrentDateTimeMinusDays(3))
        )
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card created date is displayed in this format:
        // Created <friendly date category> at <time> message, for Older than 2 days:
        val yDayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, yDayDateString))))
    }

    @Test
    fun verifyOrderDetailCardDateCreatedOlderThanAWeekView() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail(
                WCDateTimeTestUtils.formatDate(WCDateTimeTestUtils.getCurrentDateTimeMinusDays(14))
        )
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card created date is displayed in this format:
        // Created <friendly date category> at <time> message, for Older than a week:
        val yDayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, yDayDateString))))
    }

    @Test
    fun verifyOrderDetailCardDateCreatedOlderThanAMonthView() {
        // add mock data to order detail screen
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail(
                WCDateTimeTestUtils.formatDate(WCDateTimeTestUtils.getCurrentDateTimeMinusMonths(2))
        )
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order card created date is displayed in this format:
        // Created <friendly date category> at <time> message, for Older than a month:
        val yDayDateString = DateUtils.getFriendlyShortDateAtTimeString(
                appContext,
                mockWCOrderModel.dateCreated
        )
        onView(withId(R.id.orderStatus_created)).check(
                matches(withText(appContext.getString(R.string.orderdetail_orderstatus_created, yDayDateString))))
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardPendingStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail())

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText("Pending"))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_pending_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_pending_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardProcessingStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("Processing")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_processing_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_processing_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardOnHoldStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("On Hold")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_hold_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_hold_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardCompletedStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("Completed")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_completed_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_completed_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardCancelledStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("Cancelled")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_cancelled_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_cancelled_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardRefundedStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("Refunded")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_refunded_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_refunded_bg))
        )
    }

    /**
     * This test checks if the order status label name, label text color and label background color matches the
     * corresponding order status. In order to check background color of the TagView, we need to get the
     * background color of the GradientDrawable and check if it matches. The getColor() method in GradientDrawable
     * is only available on devices above API 23. So adding a check here that assumes the device testing takes place
     * in devices above API 23.
     */
    @Test
    fun verifyOrderDetailCardFailedStatusLabelView() {
        Assume.assumeTrue(
                "Requires API 24 or higher due to getColor() method in GradientDrawable not available " +
                        "in devices below API 24",
                VERSION.SDK_INT >= 24
        )

        // add mock data to order detail screen
        val wcOrderStatusModel = WcOrderTestUtils.generateOrderStatusDetail("Failed")
        activityTestRule.setOrderDetailWithMockData(WcOrderTestUtils.generateOrderDetail(), wcOrderStatusModel)

        // click on the first order in the list and check if redirected to order detail
        Espresso.onView(ViewMatchers.withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order status label is displayed correctly
        // Check if order status label name, label text color, label background color is displayed correctly
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagText(wcOrderStatusModel.label))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagTextColor(appContext, R.color.orderStatus_failed_text))
        )
        onView(withId(R.id.orderStatus_orderTags)).check(
                matches(WCMatchers.withTagBackgroundColor(appContext, R.color.orderStatus_failed_bg))
        )
    }
}
