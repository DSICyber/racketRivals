package com.example.racketrivals

import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.core.Animation
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.FileProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import java.io.File


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val firstActivityRule = ActivityScenarioRule(LogInScreen::class.java)
    private fun createGalleryResultStub(imageUri: Uri): Instrumentation.ActivityResult {
        val resultData = Intent()
        resultData.data = imageUri
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    private fun createImageUri(): Uri {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resources = context.resources

        // Get the URI of the test image using its resource ID
        val imageUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    resources.getResourcePackageName(R.drawable.test_image) + '/' +
                    resources.getResourceTypeName(R.drawable.test_image) + '/' +
                    resources.getResourceEntryName(R.drawable.test_image)
        )
        return imageUri
    }

    @Test
    fun createUserAccount() {
        //watches the intents send when clicked on certain parts
      Intents.init()

        Espresso.onIdle()
        onView(withId(R.id.buttonCreateAccount)).perform(click())
        intended(hasComponent(AccountCreationActivity::class.java.name))
        val idlingResource = CountingIdlingResource("ui_update")
        val secondActivityRule = ActivityScenarioRule(AccountCreationActivity::class.java)
        ViewActions.closeSoftKeyboard()
        onView(withId(R.id.editTextEmail)).perform(typeText("jonhamm@gmail.com"))
        ViewActions.closeSoftKeyboard()
        onView(withId(R.id.editTextPassword)).perform(typeText("7zFG92gSz&W"))
        ViewActions.closeSoftKeyboard()
        onView(withId(R.id.editTextFullName)).perform(scrollTo(), click())
        onView(withId(R.id.editTextFullName)).perform(typeText("Jon Hamm"))
        //when image picker is clicked, the action should be intercepted and the test image will be put in its place as selected image
        val expectedIntent = allOf(
            hasAction(Intent.ACTION_GET_CONTENT), // Or Intent.ACTION_OPEN_DOCUMENT if needed
            hasType("image/*")

        )
        intending(expectedIntent).respondWith(createGalleryResultStub(createImageUri()))
        onView(withId(R.id.compose_view)).perform(click())

        onView(withId(R.id.textViewGoToQuestionnaire)).perform(scrollTo(), click())
        intended(hasComponent(QuestionnaireActivity::class.java.name))
        val thirdActivityRule = ActivityScenarioRule(QuestionnaireActivity::class.java)
        ViewActions.closeSoftKeyboard();
        onView(withId(R.id.experienceLevelRadioGroupOne)).perform(scrollTo(),click())
        onView(withId(R.id.durationRadioGroupOne)).perform(scrollTo(),click())
        onView(withId(R.id.playFrequencyRadioGroupOne)).perform(scrollTo(),click())
        onView(withId(R.id.tennisLessonsRadioGroupZero)).perform(scrollTo(),click())
        onView(withId(R.id.tennisCompetitionsRadioGroupOne)).perform(scrollTo(),click())
        onView(withId(R.id.createAccountButton)).perform(scrollTo(),click())

        Intents.release()
    }
}