import io.github.recalllookup.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for RecallLookupService
 * Note: These are integration tests that hit the real API
 * For unit tests, mock the Retrofit service
 */
public class RecallLookupTest {

    private RecallLookupService service;

    @BeforeEach
    public void setUp() {
        service = RecallLookupService.getInstance();
        service.clearCache();
    }

    @Test
    public void testGetRecallsForVehicle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        service.getRecalls("Honda", "CR-V", "2019",
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    assertNotNull(recalls);
                    // May or may not have recalls
                    success[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    fail("API call failed: " + error);
                    latch.countDown();
                }
            });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(success[0]);
    }

    @Test
    public void testRecallRecordSafetyFlags() {
        RecallRecord recall = new RecallRecord();

        // Test normal recall
        recall.setParkIt(false);
        recall.setParkOutside(false);
        assertFalse(recall.isCriticalSafety());

        // Test park it
        recall.setParkIt(true);
        recall.setParkOutside(false);
        assertTrue(recall.isCriticalSafety());

        // Test park outside
        recall.setParkIt(false);
        recall.setParkOutside(true);
        assertTrue(recall.isCriticalSafety());

        // Test both
        recall.setParkIt(true);
        recall.setParkOutside(true);
        assertTrue(recall.isCriticalSafety());
    }

    @Test
    public void testRecallRecordOTA() {
        RecallRecord recall = new RecallRecord();

        // Test non-OTA
        recall.setOverTheAirUpdate(false);
        assertFalse(recall.isOverTheAir());

        // Test OTA
        recall.setOverTheAirUpdate(true);
        assertTrue(recall.isOverTheAir());

        // Test null
        recall.setOverTheAirUpdate(null);
        assertFalse(recall.isOverTheAir());
    }

    @Test
    public void testCaching() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        // First call - should hit API
        long start1 = System.currentTimeMillis();
        service.getRecalls("Toyota", "Camry", "2020",
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    long time1 = System.currentTimeMillis() - start1;
                    System.out.println("First call took: " + time1 + "ms");
                    latch1.countDown();
                }

                @Override
                public void onError(String error) {
                    latch1.countDown();
                }
            });

        assertTrue(latch1.await(10, TimeUnit.SECONDS));

        // Second call - should hit cache
        long start2 = System.currentTimeMillis();
        service.getRecalls("Toyota", "Camry", "2020",
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    long time2 = System.currentTimeMillis() - start2;
                    System.out.println("Second call took: " + time2 + "ms");
                    // Cache should be much faster (< 10ms)
                    assertTrue(time2 < 50);
                    latch2.countDown();
                }

                @Override
                public void onError(String error) {
                    latch2.countDown();
                }
            });

        assertTrue(latch2.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testInvalidInput() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        service.getRecalls(null, null, null,
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    fail("Should have failed with null input");
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    assertNotNull(error);
                    assertTrue(error.contains("required"));
                    latch.countDown();
                }
            });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }
}