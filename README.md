# NHTSA Recall Lookup

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![Android](https://img.shields.io/badge/Android-API%2021+-green.svg)](https://developer.android.com/)
[![Python](https://img.shields.io/badge/Python-3.6+-blue.svg)](https://www.python.org/)

Standalone multi-platform library for looking up vehicle safety recalls from the NHTSA (National Highway Traffic Safety Administration) database.

**Sister repository to [nhtsa-vin-decoder](https://github.com/Wal33D/nhtsa-vin-decoder)**

**Author**: Wal33D
**Email**: aquataze@yahoo.com

## 🎯 Purpose

This library provides a dedicated, lightweight solution for accessing NHTSA recall data without the overhead of full VIN decoding. Perfect for applications that need to:
- Check vehicles for safety recalls
- Monitor recall campaigns
- Display recall warnings
- Build recall notification systems

## ⚡ Quick Start

### Java
```java
import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;

RecallLookupService service = RecallLookupService.getInstance();

service.getRecalls("Honda", "CR-V", "2019", new RecallCallback() {
    @Override
    public void onSuccess(List<RecallRecord> recalls) {
        System.out.println("Found " + recalls.size() + " recalls");
        for (RecallRecord recall : recalls) {
            if (recall.isCriticalSafety()) {
                System.out.println("⚠️ CRITICAL: " + recall.getSummary());
            }
        }
    }

    @Override
    public void onError(String error) {
        System.err.println("Error: " + error);
    }
});
```

### Android
```java
import io.github.recalllookup.android.RecallLookupAndroid;

RecallLookupAndroid lookup = new RecallLookupAndroid(context);

// Callbacks run on main thread - safe for UI updates
lookup.getRecalls("Honda", "CR-V", "2019", new RecallCallback() {
    @Override
    public void onSuccess(List<RecallRecord> recalls) {
        // Update UI safely
        if (RecallLookupAndroid.hasCriticalRecalls(recalls)) {
            warningIcon.setVisibility(View.VISIBLE);
            warningText.setText(recalls.size() + " recalls found!");
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }
});
```

### Python
```python
from nhtsa_recall_lookup import NHTSARecallLookup

lookup = NHTSARecallLookup()

# Get recalls for specific vehicle
recalls = lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")

for recall in recalls:
    if recall.is_critical_safety():
        print(f"⚠️ CRITICAL: {recall.summary}")
    print(f"Campaign: {recall.nhtsa_campaign_number}")
    print(f"Component: {recall.component}")

# Filter critical recalls
critical = lookup.filter_critical_recalls(recalls)
print(f"Found {len(critical)} critical safety recalls")
```

## ✨ Features

- **Free Government API** - No API key required
- **Multi-Platform** - Java, Android, and Python implementations
- **Safety Indicators** - Critical recall detection (park outside, do not drive)
- **OTA Update Detection** - Identifies recalls fixable via over-the-air updates
- **Built-in Caching** - Reduces API calls and improves performance
- **Android Optimized** - Main thread callbacks for safe UI updates
- **Filtering & Grouping** - Helper methods for organizing recall data
- **Lightweight** - Focused solely on recall lookups, no VIN decoding overhead

## 📁 Project Structure

```
nhtsa-recall-lookup/
├── java/io/github/recalllookup/
│   ├── core/
│   │   ├── RecallLookupService.java  # Main service
│   │   ├── RecallRecord.java         # Data model
│   │   └── RecallResponse.java       # API response
│   └── android/
│       └── RecallLookupAndroid.java  # Android wrapper
├── python/
│   ├── __init__.py
│   └── nhtsa_recall_lookup.py        # Python implementation
├── tests/                            # Test suites
├── examples/                         # Usage examples
└── docs/                            # Documentation
```

## 🚀 Installation

### Quick Start (No Build Required!)

See [QUICKSTART.md](docs/QUICKSTART.md) for the fastest way to use this library.

### 📚 Full Documentation

Browse all documentation in the [docs](docs/) directory:
- [Quick Start Guide](docs/QUICKSTART.md)
- [Android Integration](docs/ANDROID_INTEGRATION.md)
- [VIN Decoder Integration](docs/INTEGRATION.md)
- [Immediate Usage](docs/USE_NOW.md)

### Java/Android (Git Submodule - Available Now)

```bash
# In your project root
git submodule add https://github.com/Wal33D/nhtsa-recall-lookup.git modules/recall-lookup
```

Then in `settings.gradle`:
```gradle
include ':nhtsa-recall-lookup'
project(':nhtsa-recall-lookup').projectDir = new File('path/to/nhtsa-recall-lookup')
```

### Python

Install from PyPI:
```bash
pip install nhtsa-recall-lookup
```

Or from source:
```bash
git clone https://github.com/Wal33D/nhtsa-recall-lookup.git
cd nhtsa-recall-lookup
pip install -e .
```

## 📊 API Methods

### Core Methods

| Method | Description | Parameters |
|--------|-------------|------------|
| `getRecalls()` | Get recalls for vehicle | make, model, year (optional) |
| `getRecallByCampaignNumber()` | Get specific recall | campaign number |
| `clearCache()` | Clear recall cache | none |
| `isCached()` | Check if lookup is cached | make, model, year |

### RecallRecord Fields

| Field | Type | Description |
|-------|------|-------------|
| `nhtsaCampaignNumber` | String | Unique recall identifier |
| `component` | String | Affected component |
| `summary` | String | Recall description |
| `consequence` | String | Safety consequence |
| `remedy` | String | Fix description |
| `parkIt` | Boolean | Do not drive flag |
| `parkOutside` | Boolean | Park outside flag |
| `overTheAirUpdate` | Boolean | OTA fix available |
| `modelYear` | String | Affected model year |
| `make` | String | Vehicle make |
| `model` | String | Vehicle model |

### Helper Methods

**Java/Android:**
- `isCriticalSafety()` - Check if recall is critical
- `isOverTheAir()` - Check if OTA update available
- `hasCriticalRecalls()` - Check list for critical recalls (Android)
- `countCriticalRecalls()` - Count critical recalls (Android)
- `filterByYear()` - Filter recalls by year (Android)

**Python:**
- `filter_critical_recalls()` - Get only critical recalls
- `filter_by_component()` - Filter by component keyword
- `group_by_year()` - Group recalls by model year

## 🧪 Examples

### Check for Critical Recalls (Java)
```java
service.getRecalls("Toyota", "Camry", "2020", new RecallCallback() {
    @Override
    public void onSuccess(List<RecallRecord> recalls) {
        for (RecallRecord recall : recalls) {
            if (recall.getParkIt() == Boolean.TRUE) {
                // DO NOT DRIVE - Critical safety issue
                sendUrgentNotification(recall);
            } else if (recall.getParkOutside() == Boolean.TRUE) {
                // Fire risk - must park outside
                sendSafetyWarning(recall);
            } else if (recall.isOverTheAir()) {
                // Can be fixed remotely
                scheduleOTAUpdate(recall);
            }
        }
    }
});
```

### Batch Processing (Python)
```python
vehicles = [
    ("Honda", "Accord", "2018"),
    ("Toyota", "Camry", "2019"),
    ("Ford", "F-150", "2020")
]

critical_count = 0
for make, model, year in vehicles:
    recalls = lookup.get_recalls_for_vehicle(make, model, year)
    critical = lookup.filter_critical_recalls(recalls)
    if critical:
        print(f"⚠️ {year} {make} {model}: {len(critical)} critical recalls")
        critical_count += len(critical)

print(f"\nTotal critical recalls: {critical_count}")
```

### Android RecyclerView Adapter
```java
public class RecallAdapter extends RecyclerView.Adapter<RecallViewHolder> {
    private List<RecallRecord> recalls;

    @Override
    public void onBindViewHolder(RecallViewHolder holder, int position) {
        RecallRecord recall = recalls.get(position);

        holder.campaignText.setText(recall.getNhtsaCampaignNumber());
        holder.componentText.setText(recall.getComponent());
        holder.summaryText.setText(recall.getSummary());

        if (recall.isCriticalSafety()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.warningIcon.setVisibility(View.VISIBLE);
        }

        if (recall.isOverTheAir()) {
            holder.otaIcon.setVisibility(View.VISIBLE);
        }
    }
}
```

## ⚡ Performance

- **Response Time**: 200-500ms (API call)
- **Cached Response**: <1ms
- **Cache Size**: ~100KB for 100 recall lookups
- **Memory Usage**: Minimal (~1MB)

## 🔒 Safety Flags

The library identifies three critical safety conditions:

1. **Park It** (`parkIt: true`)
   - Vehicle should NOT be driven
   - Immediate safety risk
   - Typically brake, steering, or critical system failures

2. **Park Outside** (`parkOutside: true`)
   - Fire risk present
   - Vehicle should not be parked in garage
   - Usually battery or electrical system issues

3. **Over-The-Air Update** (`overTheAirUpdate: true`)
   - Fix can be applied remotely
   - No dealer visit required
   - Common for software-related recalls

## 🤝 Integration with nhtsa-vin-decoder

This library can be used standalone or together with [nhtsa-vin-decoder](https://github.com/Wal33D/nhtsa-vin-decoder):

```java
// Use VIN decoder to get vehicle info
VINDecoderService vinDecoder = VINDecoderService.getInstance();
vinDecoder.decodeVIN(vin, new VINDecoderCallback() {
    public void onSuccess(VehicleData vehicle) {
        // Then look up recalls
        RecallLookupService.getInstance().getRecalls(
            vehicle.getMake(),
            vehicle.getModel(),
            vehicle.getModelYear(),
            recallCallback
        );
    }
});
```

## 🛠️ Building from Source

### Java/Android
```bash
./gradlew build
./gradlew test
```

### Python
```bash
pip install -e .[dev]
pytest tests/
```

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 📧 Contact

**Author**: Wal33D
**Email**: aquataze@yahoo.com
**GitHub**: [@Wal33D](https://github.com/Wal33D)

## 🙏 Acknowledgments

- NHTSA for providing free public access to recall data
- Contributors and users of the nhtsa-vin-decoder project
- The open source community

## 🔗 Related Projects

- [nhtsa-vin-decoder](https://github.com/Wal33D/nhtsa-vin-decoder) - Full VIN decoder with recall support
- [NHTSA API Documentation](https://vpic.nhtsa.dot.gov/api/) - Official API docs

## 📊 API Status

NHTSA Recall API Status: ![API Status](https://img.shields.io/website?url=https%3A%2F%2Fapi.nhtsa.gov%2Frecalls)