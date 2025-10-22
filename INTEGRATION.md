# Integration Guide

This guide explains how to integrate nhtsa-recall-lookup with your existing projects, particularly with [nhtsa-vin-decoder](https://github.com/Wal33D/nhtsa-vin-decoder).

## Option 1: Standalone Usage

The library can be used completely independently:

```java
// Java
RecallLookupService service = RecallLookupService.getInstance();
service.getRecalls("Honda", "CR-V", "2019", callback);
```

```python
# Python
from nhtsa_recall_lookup import NHTSARecallLookup
lookup = NHTSARecallLookup()
recalls = lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")
```

## Option 2: Integration with nhtsa-vin-decoder

### Java/Android

Add both libraries as dependencies:

```gradle
dependencies {
    implementation 'io.github.vindecoder:nhtsa-vin-decoder:2.1.0'
    implementation 'io.github.recalllookup:nhtsa-recall-lookup:1.0.0'
}
```

Or as Git submodules:

```bash
# In your project root
git submodule add https://github.com/Wal33D/nhtsa-vin-decoder.git modules/nhtsa-vin-decoder
git submodule add https://github.com/Wal33D/nhtsa-recall-lookup.git modules/nhtsa-recall-lookup
```

Then in `settings.gradle`:
```gradle
include ':nhtsa-vin-decoder'
project(':nhtsa-vin-decoder').projectDir = new File('modules/nhtsa-vin-decoder')

include ':nhtsa-recall-lookup'
project(':nhtsa-recall-lookup').projectDir = new File('modules/nhtsa-recall-lookup')
```

### Combined Usage Example (Java)

```java
import io.github.vindecoder.nhtsa.VINDecoderService;
import io.github.vindecoder.nhtsa.VehicleData;
import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;

public class VehicleInspector {

    public void inspectVehicle(String vin) {
        // Decode VIN first
        VINDecoderService vinService = VINDecoderService.getInstance();
        RecallLookupService recallService = RecallLookupService.getInstance();

        vinService.decodeVIN(vin, new VINDecoderService.VINDecoderCallback() {
            @Override
            public void onSuccess(VehicleData vehicle) {
                System.out.println("Vehicle: " + vehicle.getDisplayName());

                // Then look up recalls using decoded info
                recallService.getRecalls(
                    vehicle.getMake(),
                    vehicle.getModel(),
                    vehicle.getModelYear(),
                    new RecallLookupService.RecallCallback() {
                        @Override
                        public void onSuccess(List<RecallRecord> recalls) {
                            System.out.println("Found " + recalls.size() + " recalls");

                            // Check for critical recalls
                            for (RecallRecord recall : recalls) {
                                if (recall.isCriticalSafety()) {
                                    System.out.println("⚠️ CRITICAL RECALL: " +
                                        recall.getNhtsaCampaignNumber());
                                }
                            }
                        }

                        @Override
                        public void onError(String error) {
                            System.err.println("Recall lookup failed: " + error);
                        }
                    }
                );
            }

            @Override
            public void onError(String error) {
                System.err.println("VIN decode failed: " + error);
            }
        });
    }
}
```

### Combined Usage Example (Python)

```python
from nhtsa_vin_decoder import NHTSAVinDecoder
from nhtsa_recall_lookup import NHTSARecallLookup

def inspect_vehicle(vin):
    # Decode VIN
    vin_decoder = NHTSAVinDecoder()
    vehicle = vin_decoder.decode(vin)

    if vehicle.is_valid():
        print(f"Vehicle: {vehicle.get_display_name()}")

        # Look up recalls
        recall_lookup = NHTSARecallLookup()
        recalls = recall_lookup.get_recalls_for_vehicle(
            vehicle.make,
            vehicle.model,
            str(vehicle.year) if vehicle.year else None
        )

        if recalls:
            print(f"Found {len(recalls)} recalls")

            # Check for critical recalls
            critical = recall_lookup.filter_critical_recalls(recalls)
            if critical:
                print(f"⚠️ {len(critical)} CRITICAL RECALLS FOUND")
    else:
        print(f"Invalid VIN: {vehicle.error_text}")

# Example usage
inspect_vehicle("1HGCM82633A004352")
```

## Option 3: Conditional Dependency

You can make the recall functionality optional in your project:

### Java Example

```java
public class OptionalRecallIntegration {
    private static boolean RECALL_LOOKUP_AVAILABLE;

    static {
        try {
            Class.forName("io.github.recalllookup.core.RecallLookupService");
            RECALL_LOOKUP_AVAILABLE = true;
        } catch (ClassNotFoundException e) {
            RECALL_LOOKUP_AVAILABLE = false;
        }
    }

    public void decodeWithOptionalRecalls(String vin) {
        // Always decode VIN
        VehicleData vehicle = decodeVIN(vin);

        // Optionally add recalls if library is available
        if (RECALL_LOOKUP_AVAILABLE) {
            addRecallInformation(vehicle);
        } else {
            System.out.println("Recall lookup not available");
        }
    }
}
```

### Python Example

```python
# Try to import recall lookup
try:
    from nhtsa_recall_lookup import NHTSARecallLookup
    RECALL_LOOKUP_AVAILABLE = True
except ImportError:
    RECALL_LOOKUP_AVAILABLE = False
    print("Recall lookup not available. Install with: pip install nhtsa-recall-lookup")

def decode_with_optional_recalls(vin):
    # Always decode VIN
    from nhtsa_vin_decoder import NHTSAVinDecoder
    decoder = NHTSAVinDecoder()
    vehicle = decoder.decode(vin)

    # Optionally add recalls if available
    if RECALL_LOOKUP_AVAILABLE:
        lookup = NHTSARecallLookup()
        recalls = lookup.get_recalls_for_vehicle(
            vehicle.make,
            vehicle.model,
            str(vehicle.year) if vehicle.year else None
        )
        print(f"Found {len(recalls)} recalls")
    else:
        print("Recall information not available")

    return vehicle
```

## Migration from Embedded Recall Functions

If you're currently using the recall functions embedded in nhtsa-vin-decoder v2.1+, migrating to the standalone library is straightforward:

### Java Migration

**Before (embedded):**
```java
VINDecoderService service = VINDecoderService.getInstance();
service.getRecallsForVehicle("Honda", "CR-V", "2019", callback);
```

**After (standalone):**
```java
RecallLookupService service = RecallLookupService.getInstance();
service.getRecalls("Honda", "CR-V", "2019", callback);
```

### Python Migration

**Before (embedded):**
```python
from nhtsa_vin_decoder import NHTSAVinDecoder
decoder = NHTSAVinDecoder()
recalls = decoder.get_recalls_for_vehicle("Honda", "CR-V", "2019")
```

**After (standalone):**
```python
from nhtsa_recall_lookup import NHTSARecallLookup
lookup = NHTSARecallLookup()
recalls = lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")
```

## Benefits of Separation

1. **Modularity** - Use only what you need
2. **Smaller Dependencies** - Recall lookup doesn't require VIN decoding overhead
3. **Independent Updates** - Update recall functionality without touching VIN decoder
4. **Cleaner Architecture** - Single responsibility principle
5. **Flexibility** - Mix and match with other VIN decoders or recall sources

## Support

For issues or questions:
- nhtsa-recall-lookup: [GitHub Issues](https://github.com/Wal33D/nhtsa-recall-lookup/issues)
- nhtsa-vin-decoder: [GitHub Issues](https://github.com/Wal33D/nhtsa-vin-decoder/issues)