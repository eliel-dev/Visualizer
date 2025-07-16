# Technology Stack

## Build System
- **Gradle** with Android Gradle Plugin 8.11.1
- **Kotlin** 1.6.21 as primary language
- Multi-module Android project structure

## Platform & SDK
- **Android SDK**: Compile SDK 28, Min SDK 15, Target SDK 28
- **Support Libraries**: Android Support Library 28.0.0
- **Testing**: JUnit 4.12, Espresso 3.0.2

## Key Dependencies
- **Kotlin Standard Library**: kotlin-stdlib-jdk7
- **Apache Commons Math**: commons-math3 3.6.1 (for mathematical operations)
- **Android Support**: AppCompat, ConstraintLayout

## Common Commands

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build all modules
./gradlew build
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run tests for specific module
./gradlew :app:test
./gradlew :visualizer:test
```

### Development Commands
```bash
# Install debug APK
./gradlew installDebug

# Generate lint report
./gradlew lint
```

## Code Style
- **Language**: Kotlin preferred over Java
- **Extensions**: Uses Kotlin Android Extensions for view binding
- **Namespace**: Explicit namespace declarations in build.gradle files