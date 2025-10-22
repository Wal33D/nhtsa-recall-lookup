# 🚀 Use This Library RIGHT NOW

## Python - Works Immediately (30 seconds)

```bash
# Clone and test
git clone https://github.com/Wal33D/nhtsa-recall-lookup.git
cd nhtsa-recall-lookup
python3 validate.py  # Run validation tests
```

### Use in Your Python Project

```python
# Copy this into your project
import sys
sys.path.append('path/to/nhtsa-recall-lookup')

from python.nhtsa_recall_lookup import NHTSARecallLookup

lookup = NHTSARecallLookup()
recalls = lookup.get_recalls_for_vehicle("Honda", "CR-V", "2019")

for recall in recalls:
    if recall.is_critical_safety():
        print(f"⚠️ CRITICAL: {recall.summary}")
```

## Java/Android - Use as Submodule

### Step 1: Add to Your Project
```bash
cd your-project
git submodule add https://github.com/Wal33D/nhtsa-recall-lookup.git libs/recall
```

### Step 2: Add Dependencies to build.gradle
```gradle
dependencies {
    // Required dependencies
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### Step 3: Copy Source Files
```bash
# Copy Java sources into your project
cp -r libs/recall/java/io/github/recalllookup src/main/java/io/github/
```

### Step 4: Use in Your Code
```java
import io.github.recalllookup.core.RecallLookupService;
import io.github.recalllookup.core.RecallRecord;

RecallLookupService service = RecallLookupService.getInstance();
service.getRecalls("Honda", "CR-V", "2019", new RecallCallback() {
    @Override
    public void onSuccess(List<RecallRecord> recalls) {
        System.out.println("Found " + recalls.size() + " recalls");
    }
});
```

## Quick Test URLs

### View on GitHub
https://github.com/Wal33D/nhtsa-recall-lookup

### Test Python Online
```bash
# Run example directly from GitHub
curl -s https://raw.githubusercontent.com/Wal33D/nhtsa-recall-lookup/main/validate.py | python3
```

## Status: ✅ READY TO USE

- Python: **100% Working** - No build required
- Java: **Source Ready** - Add dependencies and copy files
- Android: **Source Ready** - Same as Java + main thread callbacks

## Support

**Email**: aquataze@yahoo.com
**GitHub Issues**: https://github.com/Wal33D/nhtsa-recall-lookup/issues