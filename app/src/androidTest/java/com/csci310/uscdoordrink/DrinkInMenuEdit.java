package com.csci310.uscdoordrink;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DrinkInMenuEdit {
    private UiDevice mUiDevice;

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Before
    public void before() throws Exception {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void drinkInMenuEdit() {
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.signInButtonMerchant)));
        materialButton.perform(click());

        UiObject accountSelect = mUiDevice.findObject(new UiSelector().text("Sup Man"));
        try {
            accountSelect.click();
            Thread.sleep(1000);
        } catch (UiObjectNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navSettingsPage), withContentDescription("Settings"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                3),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.accessMenuButton), withText("Edit Menu"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        5),
                                1),
                        isDisplayed()));
        materialButton2.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.drinkTitle), withText("demo"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView.check(matches(withText("demo")));

        UiObject demoDrinkSelect = mUiDevice.findObject(new UiSelector().text("demo"));
        try {
            demoDrinkSelect.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.particularDrinkTitle)));
        appCompatEditText.perform(replaceText("dem"));

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.particularDrinkTitle)));
        appCompatEditText2.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.particularDrinkDescription)));
        appCompatEditText3.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.particularDrinkDescription)));
        appCompatEditText4.perform(replaceText("tes"));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.particularDrinkDescription)));
        appCompatEditText5.perform(closeSoftKeyboard());


        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.particularDrinkCaffeineContent)));
        appCompatEditText6.perform(click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.particularDrinkCaffeineContent)));
        appCompatEditText7.perform(replaceText("22"));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.particularDrinkCaffeineContent)));
        appCompatEditText8.perform(closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.particularDrinkType)));
        appCompatSpinner.perform(click());

        DataInteraction materialTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        materialTextView.perform(click());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.particularDrinkPrice)));
        appCompatEditText9.perform(replaceText("$0.29"));

        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.particularDrinkPrice)));
        appCompatEditText10.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText11 = onView(
                allOf(withId(R.id.particularDrinkDiscount)));
        appCompatEditText11.perform(replaceText("$0.10"));

        ViewInteraction appCompatEditText12 = onView(
                allOf(withId(R.id.particularDrinkDiscount)));
        appCompatEditText12.perform(closeSoftKeyboard());


        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.particularDrinkSaveButton)));
        materialButton3.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.drinkTitle), withText("dem"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView2.check(matches(withText("dem")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.drinkDescription), withText("tes"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView3.check(matches(withText("tes")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.drinkCaffeine), withText("Caffeine: 22 mg"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView4.check(matches(withText("Caffeine: 22 mg")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.drinkPrice), withText("$0.29"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView5.check(matches(withText("$0.29")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.drinkDiscount), withText("Discount: $0.10"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        textView6.check(matches(withText("Discount: $0.10")));

        pressBack();

        CredentialTestingManager.logOut();
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
