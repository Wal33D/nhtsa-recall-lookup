package io.github.recalllookup.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;

import java.util.List;

/**
 * Android wrapper for NHTSA Recall Lookup Service
 *
 * Provides easy-to-use Android interface for recall lookups with:
 * - NHTSA API integration (no API key required)
 * - Main thread callbacks for safe UI updates
 * - Built-in caching to reduce API calls
 * - Simple async API
 *
 * Example usage:
 * <pre>
 * RecallLookupAndroid lookup = new RecallLookupAndroid(context);
 * lookup.getRecalls("Honda", "CR-V", "2019", new RecallLookupAndroid.RecallCallback() {
 *     {@literal @}Override
 *     public void onSuccess(List{@literal <}RecallRecord{@literal >} recalls) {
 *         // Handle successful lookup on main thread
 *         if (RecallLookupAndroid.hasCriticalRecalls(recalls)) {
 *             showWarning(recalls.size() + " recalls found!");
 *         }
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
     * Callback interface for recall lookups
     */
    public interface RecallCallback {
        /**
         * Called on success with recall data (runs on main thread)
         * @param recalls List of RecallRecord objects
         */
        void onSuccess(List<RecallRecord> recalls);

        /**
         * Called on error with error message (runs on main thread)
         * @param error Error message
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
     * Get recall campaigns for a vehicle by make, model, and year
     *
     * Callbacks are executed on the main thread, making it safe to update UI directly.
     *
     * @param make Vehicle make (e.g., "Honda")
     * @param model Vehicle model (e.g., "Accord")
     * @param modelYear Model year (e.g., "2019") or null for all years
     * @param callback Callback for results (called on main thread)
     */
    public void getRecalls(String make, String model, String modelYear, RecallCallback callback) {
        recallService.getRecalls(make, model, modelYear, new RecallLookupService.RecallCallback() {
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
     * Get recall campaigns for a vehicle by make and model (all years)
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
     * @param campaignNumber NHTSA campaign number (e.g., "23V123000")
     * @param callback Callback for results (called on main thread)
     */
    public void getRecallByCampaignNumber(String campaignNumber, RecallCallback callback) {
        recallService.getRecallByCampaignNumber(campaignNumber, new RecallLookupService.RecallCallback() {
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
     * Clear the recall cache
     *
     * Use this to force fresh data from the NHTSA API.
     */
    public void clearCache() {
        recallService.clearCache();
    }

    /**
     * Check if a recall lookup is cached
     *
     * @param make Vehicle make
     * @param model Vehicle model
     * @param modelYear Model year or null for all years
     * @return true if the lookup is cached
     */
    public boolean isCached(String make, String model, String modelYear) {
        return recallService.isCached(make, model, modelYear);
    }

    // ====== Static Helper Methods ======

    /**
     * Check if any recalls in the list are critical safety issues
     *
     * Critical recalls include "park it" (do not drive) or "park outside" (fire risk) flags.
     *
     * @param recalls List of recalls to check
     * @return true if any critical recalls are found
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
     * Count how many critical recalls are in the list
     *
     * @param recalls List of recalls to check
     * @return Number of critical recalls
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
     * Filter recalls by model year
     *
     * @param recalls List of recalls
     * @param modelYear Year to filter by
     * @return Filtered list containing only recalls for the specified year
     */
    public static List<RecallRecord> filterByYear(List<RecallRecord> recalls, String modelYear) {
        if (recalls == null || recalls.isEmpty() || modelYear == null) {
            return recalls;
        }
        List<RecallRecord> filtered = new java.util.ArrayList<>();
        for (RecallRecord recall : recalls) {
            if (modelYear.equals(recall.getModelYear())) {
                filtered.add(recall);
            }
        }
        return filtered;
    }

    /**
     * Check if any recalls have OTA (Over-The-Air) updates available
     *
     * OTA updates can be applied remotely without visiting a dealer.
     *
     * @param recalls List of recalls to check
     * @return true if any recalls can be fixed via OTA update
     */
    public static boolean hasOTAUpdates(List<RecallRecord> recalls) {
        if (recalls == null || recalls.isEmpty()) {
            return false;
        }
        for (RecallRecord recall : recalls) {
            if (recall.isOverTheAir()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get summary text for recalls (useful for notifications)
     *
     * @param recalls List of recalls
     * @return Summary string like "3 recalls (1 critical)"
     */
    public static String getRecallSummary(List<RecallRecord> recalls) {
        if (recalls == null || recalls.isEmpty()) {
            return "No recalls found";
        }
        int total = recalls.size();
        int critical = countCriticalRecalls(recalls);
        if (critical > 0) {
            return total + " recall" + (total > 1 ? "s" : "") + " (" + critical + " critical)";
        } else {
            return total + " recall" + (total > 1 ? "s" : "");
        }
    }
}
