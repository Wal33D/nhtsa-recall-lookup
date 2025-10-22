package io.github.recalllookup.core;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NHTSA Recall Lookup Service
 *
 * Standalone service for looking up vehicle recalls using the NHTSA API.
 * Features:
 * - No API key required (free government API)
 * - Built-in caching to reduce API calls
 * - Support for VIN-based and make/model/year lookups
 * - Asynchronous and synchronous methods
 *
 * @author Wal33D
 */
public class RecallLookupService {

    private static final String TAG = "RecallLookup";
    private static final String BASE_URL = "https://api.nhtsa.gov/recalls/";

    private static RecallLookupService instance;
    private final NHTSARecallApiService apiService;

    // Cache for recall data
    private final Map<String, List<RecallRecord>> cache = new HashMap<>();

    /**
     * Retrofit API interface for NHTSA Recall endpoints
     */
    public interface NHTSARecallApiService {
        @GET("recallsByVehicle")
        Call<RecallResponse> getRecallsByVehicle(
                @Query("make") String make,
                @Query("model") String model,
                @Query("modelYear") String modelYear
        );

        @GET("recallsByVehicle")
        Call<RecallResponse> getRecallsByVehicle(
                @Query("make") String make,
                @Query("model") String model
        );

        @GET("recalls/campaignNumber")
        Call<RecallResponse> getRecallByCampaignNumber(
                @Query("campaignNumber") String campaignNumber
        );
    }

    /**
     * Callback interface for recall lookups
     */
    public interface RecallCallback {
        void onSuccess(List<RecallRecord> recalls);
        void onError(String error);
    }

    /**
     * Private constructor for singleton pattern
     */
    private RecallLookupService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(NHTSARecallApiService.class);
    }

    /**
     * Get singleton instance of recall lookup service
     * @return RecallLookupService instance
     */
    public static synchronized RecallLookupService getInstance() {
        if (instance == null) {
            instance = new RecallLookupService();
        }
        return instance;
    }

    /**
     * Get recall campaigns for a vehicle by make, model, and optional year
     *
     * @param make Vehicle make (e.g., "Honda")
     * @param model Vehicle model (e.g., "Accord")
     * @param modelYear Optional model year (can be null for all years)
     * @param callback Callback for results
     */
    public void getRecalls(String make, String model, String modelYear, RecallCallback callback) {
        if (make == null || make.trim().isEmpty() || model == null || model.trim().isEmpty()) {
            callback.onError("Make and model are required");
            return;
        }

        final String normalizedMake = make.trim();
        final String normalizedModel = model.trim();
        final String cacheKey = normalizedMake + "_" + normalizedModel + "_" + (modelYear != null ? modelYear : "ALL");

        // Check cache first
        if (cache.containsKey(cacheKey)) {
            List<RecallRecord> cached = cache.get(cacheKey);
            if (cached != null) {
                System.out.println(TAG + ": Returning cached recall data for: " + cacheKey);
                callback.onSuccess(cached);
                return;
            }
        }

        // Make API call
        System.out.println(TAG + ": Fetching recalls for: " + normalizedMake + " " + normalizedModel + " " + modelYear);
        Call<RecallResponse> call;
        if (modelYear != null && !modelYear.trim().isEmpty()) {
            call = apiService.getRecallsByVehicle(normalizedMake, normalizedModel, modelYear);
        } else {
            call = apiService.getRecallsByVehicle(normalizedMake, normalizedModel);
        }

        call.enqueue(new Callback<RecallResponse>() {
            @Override
            public void onResponse(Call<RecallResponse> call, Response<RecallResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecallResponse recallResponse = response.body();
                    List<RecallRecord> recalls = recallResponse.getResults();

                    if (recalls != null) {
                        // Cache the result
                        cache.put(cacheKey, recalls);

                        System.out.println(TAG + ": Found " + recalls.size() + " recalls");
                        callback.onSuccess(recalls);
                    } else {
                        System.out.println(TAG + ": No recalls found");
                        callback.onSuccess(new java.util.ArrayList<>());
                    }
                } else {
                    System.err.println(TAG + ": API call unsuccessful: " + response.code());
                    callback.onError("Failed to fetch recalls. HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RecallResponse> call, Throwable t) {
                System.err.println(TAG + ": API call failed: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get recall campaigns for a vehicle (all years)
     *
     * @param make Vehicle make
     * @param model Vehicle model
     * @param callback Callback for results
     */
    public void getRecalls(String make, String model, RecallCallback callback) {
        getRecalls(make, model, null, callback);
    }

    /**
     * Get a specific recall by campaign number
     *
     * @param campaignNumber NHTSA campaign number
     * @param callback Callback for results
     */
    public void getRecallByCampaignNumber(String campaignNumber, RecallCallback callback) {
        if (campaignNumber == null || campaignNumber.trim().isEmpty()) {
            callback.onError("Campaign number is required");
            return;
        }

        final String normalizedNumber = campaignNumber.trim();
        final String cacheKey = "campaign_" + normalizedNumber;

        // Check cache
        if (cache.containsKey(cacheKey)) {
            List<RecallRecord> cached = cache.get(cacheKey);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
        }

        Call<RecallResponse> call = apiService.getRecallByCampaignNumber(normalizedNumber);

        call.enqueue(new Callback<RecallResponse>() {
            @Override
            public void onResponse(Call<RecallResponse> call, Response<RecallResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecallResponse recallResponse = response.body();
                    List<RecallRecord> recalls = recallResponse.getResults();

                    if (recalls != null) {
                        cache.put(cacheKey, recalls);
                        callback.onSuccess(recalls);
                    } else {
                        callback.onSuccess(new java.util.ArrayList<>());
                    }
                } else {
                    callback.onError("Failed to fetch recall. HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RecallResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Clear the recall cache
     */
    public void clearCache() {
        cache.clear();
        System.out.println(TAG + ": Recall cache cleared");
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
        if (make == null || model == null) return false;
        String cacheKey = make.trim() + "_" + model.trim() + "_" + (modelYear != null ? modelYear : "ALL");
        return cache.containsKey(cacheKey);
    }
}