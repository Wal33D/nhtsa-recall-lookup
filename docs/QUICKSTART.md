# Quick Start Guide

## 🚀 Using the Library NOW

### Python (Works Immediately!)

```bash
# Clone the repo
git clone https://github.com/Wal33D/nhtsa-recall-lookup.git
cd nhtsa-recall-lookup

# Run Python example directly
python3 -c "
from python.nhtsa_recall_lookup import NHTSARecallLookup
lookup = NHTSARecallLookup()
recalls = lookup.get_recalls_for_vehicle('Honda', 'CR-V', '2019')
print(f'Found {len(recalls)} recalls for 2019 Honda CR-V')
for r in recalls[:2]:
    print(f'  - {r.nhtsa_campaign_number}: {r.component}')
"
```

### Java (As Git Submodule)

Add to your existing project:

```bash
# In your project root
git submodule add https://github.com/Wal33D/nhtsa-recall-lookup.git modules/recall-lookup
```

Add to your `build.gradle`:
```gradle
dependencies {
    // Add the recall lookup module
    implementation project(':recall-lookup')

    // Add required dependencies
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

Add to your `settings.gradle`:
```gradle
include ':recall-lookup'
project(':recall-lookup').projectDir = new File('modules/recall-lookup')
```

### Direct JAR Download (Coming Soon)

For now, use as a submodule or copy the source files directly into your project.

## 📦 Installing Dependencies

### For Java Projects

Add these to your `build.gradle` or `pom.xml`:

```xml
<!-- Maven -->
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>retrofit</artifactId>
    <version>2.9.0</version>
</dependency>
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>converter-gson</artifactId>
    <version>2.9.0</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

```gradle
// Gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.google.code.gson:gson:2.10.1'
```

### For Python Projects

No dependencies required! Uses standard library only.

Optional for development:
```bash
pip install pytest  # For running tests
```

## 🔥 Fastest Way to Test

### Python Test (30 seconds)
```bash
curl -s https://raw.githubusercontent.com/Wal33D/nhtsa-recall-lookup/main/examples/python_example.py | python3
```

### Java Test (Copy & Paste)

Create `RecallTest.java`:
```java
// Copy the classes from:
// - java/io/github/recalllookup/core/RecallRecord.java
// - java/io/github/recalllookup/core/RecallResponse.java
// - java/io/github/recalllookup/core/RecallLookupService.java

// Then add this main method:
public class RecallTest {
    public static void main(String[] args) {
        RecallLookupService service = RecallLookupService.getInstance();
        service.getRecalls("Honda", "CR-V", "2019",
            new RecallLookupService.RecallCallback() {
                public void onSuccess(List<RecallRecord> recalls) {
                    System.out.println("Found " + recalls.size() + " recalls");
                    System.exit(0);
                }
                public void onError(String error) {
                    System.err.println("Error: " + error);
                    System.exit(1);
                }
            });

        // Wait for async callback
        try { Thread.sleep(3000); } catch (Exception e) {}
    }
}
```

Compile and run:
```bash
javac -cp "retrofit-2.9.0.jar:gson-2.10.1.jar:converter-gson-2.9.0.jar:." RecallTest.java
java -cp "retrofit-2.9.0.jar:gson-2.10.1.jar:converter-gson-2.9.0.jar:." RecallTest
```

## 🎯 Integration Options

### Option 1: Copy Source Files
Simply copy the Java or Python files you need into your project.

### Option 2: Git Submodule
Add as a submodule and reference in your build.

### Option 3: Build JAR Locally
```bash
# If you have dependencies available:
javac -cp "lib/*:." java/io/github/recalllookup/**/*.java
jar cf recall-lookup.jar -C . io/github/recalllookup
```

## ⚠️ Known Issues

1. **Gradle Build**: Requires Java 11-17 for building. Source code works with any Java 8+.
2. **No Published Artifacts**: Not yet on Maven Central or PyPI.
3. **Dependencies Required**: Java version needs Retrofit and Gson.

## 💡 Tips

- Python version works out-of-the-box with no setup
- Java version needs Retrofit/Gson dependencies
- All API calls are cached to reduce load
- No API key required (free government API)

## 📧 Support

Issues? Open an issue on GitHub or email aquataze@yahoo.com