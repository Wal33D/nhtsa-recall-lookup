import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;
import java.util.List;
import java.util.Scanner;

/**
 * Example Java application demonstrating NHTSA Recall Lookup
 */
public class JavaExample {

    public static void main(String[] args) {
        RecallLookupService service = RecallLookupService.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("NHTSA Recall Lookup Example");
        System.out.println("============================\n");

        // Example 1: Interactive lookup
        System.out.print("Enter vehicle make (e.g., Honda): ");
        String make = scanner.nextLine();

        System.out.print("Enter vehicle model (e.g., Accord): ");
        String model = scanner.nextLine();

        System.out.print("Enter model year (optional, press Enter to skip): ");
        String year = scanner.nextLine();

        System.out.println("\nSearching for recalls...\n");

        service.getRecalls(make, model, year.isEmpty() ? null : year,
            new RecallLookupService.RecallCallback() {
                @Override
                public void onSuccess(List<RecallRecord> recalls) {
                    if (recalls.isEmpty()) {
                        System.out.println("No recalls found for this vehicle.");
                    } else {
                        System.out.println("Found " + recalls.size() + " recalls:\n");

                        // Count critical recalls
                        int criticalCount = 0;
                        int otaCount = 0;

                        for (RecallRecord recall : recalls) {
                            System.out.println("Campaign #: " + recall.getNhtsaCampaignNumber());
                            System.out.println("Component: " + recall.getComponent());

                            if (recall.getSummary() != null) {
                                String summary = recall.getSummary();
                                if (summary.length() > 150) {
                                    summary = summary.substring(0, 147) + "...";
                                }
                                System.out.println("Summary: " + summary);
                            }

                            // Check safety flags
                            if (recall.isCriticalSafety()) {
                                System.out.println("⚠️  CRITICAL SAFETY RECALL");
                                if (Boolean.TRUE.equals(recall.getParkIt())) {
                                    System.out.println("   DO NOT DRIVE THIS VEHICLE");
                                }
                                if (Boolean.TRUE.equals(recall.getParkOutside())) {
                                    System.out.println("   PARK OUTSIDE - FIRE RISK");
                                }
                                criticalCount++;
                            }

                            if (recall.isOverTheAir()) {
                                System.out.println("✓ Can be fixed via over-the-air update");
                                otaCount++;
                            }

                            System.out.println("---");
                        }

                        System.out.println("\nSummary:");
                        System.out.println("Total recalls: " + recalls.size());
                        if (criticalCount > 0) {
                            System.out.println("⚠️  Critical safety recalls: " + criticalCount);
                        }
                        if (otaCount > 0) {
                            System.out.println("✓ OTA fixable recalls: " + otaCount);
                        }
                    }

                    // Example 2: Batch lookup
                    System.out.println("\n\nExample 2: Batch Lookup Demo");
                    System.out.println("==============================");
                    batchLookupExample();
                }

                @Override
                public void onError(String error) {
                    System.err.println("Error: " + error);
                }
            });

        // Keep program running until async callback completes
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void batchLookupExample() {
        RecallLookupService service = RecallLookupService.getInstance();

        String[][] vehicles = {
            {"Honda", "CR-V", "2019"},
            {"Toyota", "Camry", "2020"},
            {"Ford", "F-150", "2021"}
        };

        for (String[] vehicle : vehicles) {
            final String vehicleInfo = vehicle[2] + " " + vehicle[0] + " " + vehicle[1];

            service.getRecalls(vehicle[0], vehicle[1], vehicle[2],
                new RecallLookupService.RecallCallback() {
                    @Override
                    public void onSuccess(List<RecallRecord> recalls) {
                        if (!recalls.isEmpty()) {
                            int critical = 0;
                            for (RecallRecord r : recalls) {
                                if (r.isCriticalSafety()) critical++;
                            }

                            System.out.print(vehicleInfo + ": " + recalls.size() + " recalls");
                            if (critical > 0) {
                                System.out.print(" (⚠️  " + critical + " critical)");
                            }
                            System.out.println();
                        } else {
                            System.out.println(vehicleInfo + ": ✓ No recalls");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.err.println(vehicleInfo + ": Error - " + error);
                    }
                });
        }
    }
}