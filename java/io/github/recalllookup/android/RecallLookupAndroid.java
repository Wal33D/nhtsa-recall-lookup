package io.github.recalllookup.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Android wrapper for NHTSA Recall Lookup
 *
 * Provides Android-friendly async interface for recall lookups with:
 * - Main thread callbacks for UI updates
 * - Context-aware lifecycle management
 * - Simple async API
 * - Built-in caching
 *
 * Example usage:
 * <pre>
 * RecallLookupAndroid recallLookup = new RecallLookupAndroid(context);
 * recallLookup.getRecalls("Honda", "CR-V", "2019", new RecallLookupAndroid.RecallCallback() {
 *     {@literal @}Override
 *     public void onSuccess(List{@literal <}RecallRecord{@literal >} recalls) {
 *         // Handle recalls on main thread
 *         updateRecallsList(recalls);
 *     }
 *
 *     {@literal @}Override
 *     public void onError(String error) {
 *         // Handle error on main thread
 *         Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
 *     }
 * });
 * </pre>
 *
 * @author Wal33D
 */
public class RecallLookupAndroid {

    private final Context context;
    private final RecallLookupService recallService;
    private final Handler mainHandler;

    /**
     * Callback interface for recall lookups (runs on main thread)
     */
    public interface RecallCallback {
        /**
         * Called on success with recall data (runs on main thread)
         */
        void onSuccess(List<RecallRecord> recalls);

        /**
         * Called on error with error message (runs on main thread)
         */
        void onError(String error);
    }

    /**
     * Creates a new RecallLookupAndroid instance
     * @param context Android context
     */
    public RecallLookupAndroid(Context context) {
        this.context = context.getApplicationContext();
        this.recallService = RecallLookupService.getInstance();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Get recall campaigns for a vehicle
     *
     * @param make Vehicle make (e.g., "Honda")
     * @param model Vehicle model (e.g., "CR-V")
     * @param modelYear Optional model year (can be null for all years)
     * @param callback Callback for results (called on main thread)
     */
    public void getRecalls(String make, String model, String modelYear, RecallCallback callback) {
        recallService.getRecalls(make, model, modelYear,
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    mainHandler.post(() -> callback.onSuccess(recalls));
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> callback.onError(error));
                }
            });
    }

    /**
     * Get recall campaigns for a vehicle (all years)
     *
     * @param make Vehicle make
     * @param model Vehicle model
     * @param callback Callback for results (called on main thread)
     */
    public void getRecalls(String make, String model, RecallCallback callback) {
        getRecalls(make, model, null, callback);
    }

    /**
     * Get a specific recall by campaign number
     *
     * @param campaignNumber NHTSA campaign number
     * @param callback Callback for results (called on main thread)
     */
    public void getRecallByCampaignNumber(String campaignNumber, RecallCallback callback) {
        recallService.getRecallByCampaignNumber(campaignNumber,
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    mainHandler.post(() -> callback.onSuccess(recalls));
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> callback.onError(error));
                }
            });
    }

    /**
     * Check if recalls are critical (require immediate attention)
     *
     * @param recalls List of recall records
     * @return true if any recall is critical
     */
    public static boolean hasCriticalRecalls(List<RecallRecord> recalls) {
        if (recalls == null || recalls.isEmpty()) {
            return false;
        }

        for (RecallRecord recall : recalls) {
            if (recall.isCriticalSafety()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count critical recalls
     *
     * @param recalls List of recall records
     * @return number of critical recalls
     */
    public static int countCriticalRecalls(List<RecallRecord> recalls) {
        if (recalls == null || recalls.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (RecallRecord recall : recalls) {
            if (recall.isCriticalSafety()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Filter recalls by year
     *
     * @param recalls List of recall records
     * @param year Model year to filter by
     * @return filtered list of recalls
     */
    public static List<RecallRecord> filterByYear(List<RecallRecord> recalls, String year) {
        if (recalls == null || year == null) {
            return recalls;
        }

        return recalls.stream()
            .filter(recall -> year.equals(recall.getModelYear()))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Clear the recall cache
     */
    public void clearCache() {
        recallService.clearCache();
    }

    /**
     * Check if a recall lookup is cached
     *
     * @param make Vehicle make
     * @param model Vehicle model
     * @param modelYear Optional model year
     * @return true if cached
     */
    public boolean isCached(String make, String model, String modelYear) {
        return recallService.isCached(make, model, modelYear);
    }
}