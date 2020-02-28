package co.innoshop.android.ui.orders

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import co.innoshop.android.R
import co.innoshop.android.R.id
import co.innoshop.android.helpers.WCMatchers
import co.innoshop.android.ui.TestBase
import co.innoshop.android.ui.main.MainActivityTestRule
import co.innoshop.android.util.DateUtils
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.wordpress.android.fluxc.model.SiteModel

@RunWith(AndroidJUnit4::class)
@LargeTest
class OrderDetailNotesCardTest : TestBase() {
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
        onView(withId(R.id.orders)).perform(click())
    }

    @Test
    fun verifyOrderNotesCardViewPopulatedSuccessfully() {
        val mockWCOrderModel = WcOrderTestUtils.generateOrderDetail()
        activityTestRule.setOrderDetailWithMockData(mockWCOrderModel)

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // check if order notes list count is 6 (3 notes, 3 headers)
        val recyclerView = activityTestRule.activity.findViewById(R.id.notesList_notes) as RecyclerView
        assertSame(6, recyclerView.adapter?.itemCount)
    }

    @Test
    fun verifyOrderNotesCardItemDateFormatPopulatedSuccessfully() {
        val mockOrderNotesList = WcOrderTestUtils.generateSampleNotes()
        activityTestRule.setOrderDetailWithMockData(
                WcOrderTestUtils.generateOrderDetail(),
                orderNotes = mockOrderNotesList
        )

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that first note date displayed matches this format: April 5, 2019 at 10:42 PM
        onView(withId(R.id.orderDetail_noteList)).perform(scrollTo())
        onView(WCMatchers.withRecyclerView(R.id.notesList_notes).atPositionOnView(0, R.id.orderNote_header))
                .check(matches(withText(DateUtils
                        .getFriendlyLongDateAtTimeString(appContext, mockOrderNotesList[0].dateCreated).capitalize()
                )))

        // verify that second note displayed matches this format: November 5, 2018 at 7:45 PM
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(1, R.id.orderNote_header))
                .check(matches(withText(DateUtils
                        .getFriendlyLongDateAtTimeString(appContext, mockOrderNotesList[1].dateCreated).capitalize()
                )))

        // verify that third note displayed matches this format: December 4, 2016 at 5:45 PM
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(2, R.id.orderNote_header))
                .check(matches(withText(DateUtils
                        .getFriendlyLongDateAtTimeString(appContext, mockOrderNotesList[2].dateCreated).capitalize()
                )))
    }

    @Test
    fun verifyOrderNotesCardItemNotePopulatedSuccessfully() {
        val mockOrderNotesList = WcOrderTestUtils.generateSampleNotes()
        activityTestRule.setOrderDetailWithMockData(
                WcOrderTestUtils.generateOrderDetail(),
                orderNotes = mockOrderNotesList
        )

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that date displayed matches this format: "This should be displayed first"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(0, R.id.orderNote_note))
                .check(matches(withText(mockOrderNotesList[0].note)))

        // verify that date displayed matches this format: "This should be displayed second"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(1, R.id.orderNote_note))
                .check(matches(withText(mockOrderNotesList[1].note)))

        // verify that date displayed matches this format: "This should be displayed third"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(2, R.id.orderNote_note))
                .check(matches(withText(mockOrderNotesList[2].note)))
    }

    @Test
    fun verifyOrderNotesCardItemCustomerNoteDisplayedSuccessfully() {
        val mockOrderNotesList = WcOrderTestUtils.generateSampleNotes()
        activityTestRule.setOrderDetailWithMockData(
                WcOrderTestUtils.generateOrderDetail(),
                orderNotes = mockOrderNotesList
        )

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that the first note item is set to "Note from customer"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(0, R.id.orderNote_header))
                .check(matches(withText("Nov 5, 2018 (System)")))

        // verify that the first note item icon is set to R.drawable.ic_note_public
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(0, R.id.orderNote_icon))
                .check(matches(WCMatchers.withDrawable(R.drawable.ic_note_public)))
    }

    @Test
    fun verifyOrderNotesCardItemSystemNoteDisplayedSuccessfully() {
        val mockOrderNotesList = WcOrderTestUtils.generateSampleNotes()
        activityTestRule.setOrderDetailWithMockData(
                WcOrderTestUtils.generateOrderDetail(),
                orderNotes = mockOrderNotesList
        )

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that the second note item is set to "System Status"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(1, R.id.orderNote_header))
                .check(matches(withText(appContext.getString(R.string.orderdetail_note_system))))

        // verify that the second note item icon is set to R.drawable.ic_note_system
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(1, R.id.orderNote_icon))
                .check(matches(WCMatchers.withDrawable(R.drawable.ic_note_system)))
    }

    @Test
    fun verifyOrderNotesCardItemPrivateNoteDisplayedSuccessfully() {
        val mockOrderNotesList = WcOrderTestUtils.generateSampleNotes()
        activityTestRule.setOrderDetailWithMockData(
                WcOrderTestUtils.generateOrderDetail(),
                orderNotes = mockOrderNotesList
        )

        // click on the first order in the list and check if redirected to order detail
        onView(withId(R.id.ordersList))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that the third note item is set to "Private Note"
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(2, R.id.orderNote_header))
                .check(matches(withText(appContext.getString(R.string.orderdetail_note_private))))

        // verify that the third note item icon is set to R.drawable.ic_note_private
        onView(WCMatchers.withRecyclerView(id.notesList_notes).atPositionOnView(2, R.id.orderNote_header))
                .check(matches(WCMatchers.withDrawable(R.drawable.ic_note_private)))
    }
}
