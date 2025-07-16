# Project Structure

## Multi-Module Architecture
This is a multi-module Android project with two main modules:

### Root Level
- **build.gradle**: Top-level build configuration
- **settings.gradle**: Module inclusion (`:app`, `:visualizer`)
- **gradle/**: Gradle wrapper files
- **YourCustomPainter.kt**: Example custom painter implementation

### App Module (`app/`)
- **Purpose**: Demo application showcasing the visualizer library
- **Package**: `io.github.jeffshee.visualizerdemo`
- **Type**: Android application module
- **Dependencies**: Depends on `:visualizer` module

### Visualizer Module (`visualizer/`)
- **Purpose**: Reusable audio visualization library
- **Package**: `io.github.jeffshee.visualizer`
- **Type**: Android library module
- **Dependencies**: Apache Commons Math for mathematical operations

## Standard Android Structure
Both modules follow standard Android project structure:
```
src/
├── main/
│   ├── java/           # Kotlin/Java source code
│   ├── res/            # Android resources
│   └── AndroidManifest.xml
├── test/               # Unit tests
└── androidTest/        # Instrumented tests
```

## Key Conventions
- **Modules**: Use `:module` syntax for inter-module dependencies
- **Namespaces**: Explicitly declared in build.gradle files
- **Source**: Kotlin files in `src/main/java/` directory (legacy structure)
- **Resources**: Standard Android resource organization in `res/`
- **Custom Painters**: Implement custom visualization logic by extending painter interfaces