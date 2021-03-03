package com.mat.tracker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class TrackerActivityTest {

    private val mockVm: LocationsViewModel = mockk(relaxed = true)
    private lateinit var scenario: ActivityScenario<TrackerActivity>
    private lateinit var koinModule: Module
    private lateinit var files: MutableLiveData<List<Uri>>
    private lateinit var passedTime: MutableLiveData<String>
    private lateinit var state: MutableLiveData<TrackerActivity.State>
    private lateinit var isAnyFileSelected: MutableLiveData<Boolean>
    private lateinit var selectedPositions: MutableSet<Uri>
    private lateinit var newFileEvent: MutableLiveData<Event<String?>>

    @Before
    fun setup() {
        koinModule = module(true, true) {
            single { mockVm }
        }
        loadKoinModules(koinModule)
        files = MutableLiveData()
        passedTime = MutableLiveData()
        state = MutableLiveData(TrackerActivity.State.NOT_TRACING)
        isAnyFileSelected = MutableLiveData(false)
        selectedPositions = mutableSetOf()
        newFileEvent = MutableLiveData()
        every { mockVm.files } returns files
        every { mockVm.passedTimeString } returns passedTime
        every { mockVm.state } returns state
        every { mockVm.isAnyFileSelected } returns isAnyFileSelected
        every { mockVm.selectedPositions } returns selectedPositions
        every { mockVm.newFileEvent } returns newFileEvent

        scenario = launch(TrackerActivity::class.java)
    }

    @After
    fun cleanUp() {
        unloadKoinModules(koinModule)
    }

    @Test
    fun test_appropriate_view_show_according_to_files() {

        onView(withId(R.id.tv_no_files)).check(matches(isDisplayed()))
        onView(withId(R.id.rv_records)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tv_tracking_time)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_tracking)).check(matches(isDisplayed()))

        val notEmptyUriList = listOf(Uri.parse(""))
        files.postValue(notEmptyUriList)

        onView(withId(R.id.tv_no_files)).check(matches(not(isDisplayed())))
        onView(withId(R.id.rv_records)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_tracking)).check(matches(isDisplayed()))
    }

    @Test
    fun test_timer_tv_shows_when_recording() {

        val sampleTime = 123L.toHMMSS()
        passedTime.postValue(sampleTime)
        state.postValue(TrackerActivity.State.TRACING)
        onView(withId(R.id.tv_tracking_time))
            .check(matches((isDisplayed())))
            .check(matches(withText(containsString(sampleTime))))
    }

    @Test
    fun test_fab() {
        coEvery { mockVm.isAnyLocationRecorded() } returns false

        onView(withId(R.id.tv_tracking_time))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_tracking))
            .perform(click())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val allowGpsBtn = device.findObject(
            By.res("android:id/button1")
        )
        allowGpsBtn?.click()
        onView(isRoot()).perform(waitFor(10))

        verify { mockVm.startTracking() }
    }

    @Test
    fun test_deletion_and_share_buttons_show_when_file_selected() {

        onView(withId(R.id.share))
            .check(doesNotExist())
        onView(withId(R.id.delete))
            .check(doesNotExist())
        onView(withId(R.id.rv_records))
            .check(matches(not(isDisplayed())))

        val notEmptyUriList = listOf(Uri.parse(""))
        files.postValue(notEmptyUriList)

        onView(withId(R.id.rv_records))
            .check(matches((isDisplayed())))
        onView(withId(R.id.rv_item_layout))
            .check(matches(isDisplayed()))
            .perform(click())
        isAnyFileSelected.postValue(true)

        onView(withId(R.id.share))
            .check(matches(isDisplayed()))
        onView(withId(R.id.delete))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_share_files_intent() {
        val filesList = prepareFakeFileUris(3)
        files.postValue(filesList)
        selectedPositions.addAll(filesList)
        isAnyFileSelected.postValue(true)

        Intents.init()
        onView(withId(R.id.share))
            .perform(click())
        Intents.intended(
            chooser(
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_SEND_MULTIPLE),
                    IntentMatchers.hasType("text/xml"),
                    hasExtras(BundleMatchers.hasValue(filesList))
                )
            )
        )
        Intents.release()

        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    @Test
    fun test_options_dialog_pops() {

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText(R.string.options))
            .perform(click())
        onView(withId(R.id.options_dialog_layout))
            .check(matches(isDisplayed()))

        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    @Test
    fun test_files_recycler_view() {

        val filesList = prepareFakeFileUris(25)
        files.postValue(filesList)

        onView(withText(filesList.last().toString()))
            .perform(
                actionWithAssertions(ScrollToAction())
            )
            .check(matches(isDisplayed()))
    }
}
