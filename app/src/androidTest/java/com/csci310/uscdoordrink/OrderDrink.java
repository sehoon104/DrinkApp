package com.csci310.uscdoordrink;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

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

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OrderDrink {
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
    public void orderDrink() {
        Activity a = mActivityTestRule.getActivity();
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.signInButtonCustomer)));
        materialButton.perform(click());

        UiObject accountSelect = mUiDevice.findObject(new UiSelector().text("Sup Man"));
        try {
            accountSelect.click();
            Thread.sleep(5000);
        } catch (UiObjectNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            MapTestAPI mapTestAPI = new MapTestAPI();
            Symbol symbol = mapTestAPI.findSymbolFromMerchantName("Sup Man");
            if (a == null) {
                CredentialTestingManager.logOut();
                throw new Exception("Failed");
            }
            mapTestAPI.clickOnSymbol(symbol, a, mUiDevice);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ViewInteraction frameLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.mapView), withContentDescription("Showing a Map created with Mapbox. Scroll by dragging two fingers. Zoom by pinching two fingers."),
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0)),
                        4),
                        isDisplayed()));
        frameLayout.perform(click());

        UiObject orderSelect = mUiDevice.findObject(new UiSelector().text("OrderTestDrink"));
        try {
            orderSelect.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        ViewInteraction extendedFloatingActionButton = onView(
                allOf(withId(R.id.finalizeOrderButton), withText("Finalize Order"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        extendedFloatingActionButton.perform(click());

        ViewInteraction extendedFloatingActionButton2 = onView(
                allOf(withId(R.id.checkoutButton), withText("Checkout"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        extendedFloatingActionButton2.perform(click());

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navLogout), withContentDescription("Log Out"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                4),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());
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
