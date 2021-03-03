package com.mat.tracker

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

fun chooser(matcher: Matcher<Intent>): Matcher<Intent> = Matchers.allOf(
        IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
        IntentMatchers.hasExtra(Matchers.`is`(Intent.EXTRA_INTENT), matcher)
)

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadForAtLeast(delay)
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "wait for " + delay + "milliseconds"
        }
    }
}

fun prepareFakeFileUris(amount: Int): ArrayList<Uri> {
    val filesList = arrayListOf<Uri>()
    repeat(amount) {
        filesList.add(Uri.parse("file$it"))
    }
    return filesList
}