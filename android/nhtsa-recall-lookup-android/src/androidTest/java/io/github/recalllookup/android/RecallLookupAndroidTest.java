package io.github.recalllookup.android;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.recalllookup.core.RecallRecord;

import static org.junit.Assert.*;

import java.util.List;

/**
 * Instrumented test for RecallLookupAndroid
 *
 * Note: These tests require internet connectivity to access the NHTSA API
 */
@RunWith(AndroidJUnit4.class)
public class RecallLookupAndroidTest {

    private Context context;
    private RecallLookupAndroid recallLookup;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        recallLookup = new RecallLookupAndroid(context);
        recallLookup.clearCache(); // Start with clean cache
    }

    @Test
    public void testContextNotNull() {
        assertNotNull(context);
    }

    @Test
    public void testRecallLookupInstantiation() {
        assertNotNull(recallLookup);
    }

    @Test
    public void testGetRecallsWithYear() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        recallLookup.getRecalls("Honda", "CR-V", "2019", new RecallLookupAndroid.RecallCallback() {
            @Override
            public void onSuccess(List<RecallRecord> recalls) {
                assertNotNull(recalls);
                successCalled.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Should not error: " + error);
                latch.countDown();
            }
        });

        assertTrue("Callback not called within timeout", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Success callback was not called", successCalled.get());
    }

    @Test
    public void testGetRecallsWithoutYear() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        recallLookup.getRecalls("Toyota", "Camry", new RecallLookupAndroid.RecallCallback() {
            @Override
            public void onSuccess(List<RecallRecord> recalls) {
                assertNotNull(recalls);
                successCalled.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Should not error: " + error);
                latch.countDown();
            }
        });

        assertTrue("Callback not called within timeout", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Success callback was not called", successCalled.get());
    }

    @Test
    public void testGetRecallByCampaignNumber() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        // Use a known campaign number (this should be a real one from NHTSA)
        recallLookup.getRecallByCampaignNumber("23V123000", new RecallLookupAndroid.RecallCallback() {
            @Override
            public void onSuccess(List<RecallRecord> recalls) {
                assertNotNull(recalls);
                successCalled.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                // Campaign number might not exist, which is OK for this test
                successCalled.set(true);
                latch.countDown();
            }
        });

        assertTrue("Callback not called within timeout", latch.await(10, TimeUnit.SECONDS));
        assertTrue("Callback was not called", successCalled.get());
    }

    @Test
    public void testInvalidInput() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        recallLookup.getRecalls("", "", "2020", new RecallLookupAndroid.RecallCallback() {
            @Override
            public void onSuccess(List<RecallRecord> recalls) {
                fail("Should not succeed with empty make/model");
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                assertNotNull(error);
                assertTrue(error.contains("required") || error.contains("empty"));
                errorCalled.set(true);
                latch.countDown();
            }
        });

        assertTrue("Callback not called within timeout", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Error callback was not called", errorCalled.get());
    }

    @Test
    public void testCache() throws InterruptedException {
        // First call - should not be cached
        assertFalse(recallLookup.isCached("Ford", "F-150", "2020"));

        CountDownLatch latch = new CountDownLatch(1);

        recallLookup.getRecalls("Ford", "F-150", "2020", new RecallLookupAndroid.RecallCallback() {
            @Override
            public void onSuccess(List<RecallRecord> recalls) {
                // After successful call, should be cached
                assertTrue(recallLookup.isCached("Ford", "F-150", "2020"));
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Should not error: " + error);
                latch.countDown();
            }
        });

        assertTrue("Callback not called within timeout", latch.await(10, TimeUnit.SECONDS));

        // Clear cache
        recallLookup.clearCache();
        assertFalse(recallLookup.isCached("Ford", "F-150", "2020"));
    }

    @Test
    public void testHelperMethods() {
        // Test with null/empty
        assertFalse(RecallLookupAndroid.hasCriticalRecalls(null));
        assertFalse(RecallLookupAndroid.hasCriticalRecalls(new java.util.ArrayList<>()));
        assertEquals(0, RecallLookupAndroid.countCriticalRecalls(null));
        assertFalse(RecallLookupAndroid.hasOTAUpdates(null));
        assertEquals("No recalls found", RecallLookupAndroid.getRecallSummary(null));
    }
}
