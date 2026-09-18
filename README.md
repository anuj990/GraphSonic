# GraphSonic

GraphSonic is an Android application for turning mathematical expressions into interactive graphs and sound.

It combines a Jetpack Compose Android UI with a native C++ mathematical engine. You can enter equations, graph multiple expressions together, inspect values with a cursor, pan and zoom the graph, and listen to the graph as audio.

> **Project status:** Active development. The repository currently contains the core graphing, mathematical parsing/evaluation, visualization, audio, history, and native JNI layers. Some performance and cleanup work remains on the development roadmap.

## What GraphSonic Does

GraphSonic is designed around the idea that a mathematical function can be experienced in two ways:

1. **Visually** — sample the function and render it as an interactive graph.
2. **Audibly** — map graph values to musical frequencies and play the result.

The app currently supports up to **8 equations** at once. Each equation is represented as an independent graph layer with its own expression, color, visibility state, and audio-enabled state.

## Core Features

### Mathematical expression input

The expression engine supports mathematical notation including:

- Variables such as `x`
- Numeric constants
- Parentheses
- Addition, subtraction, multiplication, division, and powers
- Implicit multiplication such as `2x` and `xsin(x)`
- Function notation such as `sin(x)`
- Function-style input without parentheses where supported, such as `sin 2x`
- Superscript powers such as `x²` and larger superscript exponents
- Mathematical symbols including `π`, `√`, `∛`, `×`, `÷`, and `−`
- Trigonometric functions
- Inverse trigonometric aliases including `arcsin`, `arccos`, and `arctan`
- Hyperbolic functions
- Logarithms
- Two-argument logarithms such as `log(x,2)`
- Absolute value
- Floor and ceiling
- Exponential functions
- Square roots and cube roots
- Domain-aware evaluation
- Negative-base fractional powers where the mathematical result is real, including examples such as `(-8)^(1/3)`

The parser, evaluator, and canonical-expression system live in the native C++ layer.

### Live expression validation

The equation input validates expressions while they are being entered.

Incomplete expressions can remain temporarily valid while typing, while completed invalid expressions can surface the native parser error before graphing.

For example, an expression such as:

`abc(x)`

can be rejected during input rather than only after pressing the graph button.

### Multiple equations

The visualization supports up to 8 equations simultaneously.

Each equation has:

- A stable layer ID
- Its original entered expression
- A canonical expression used for duplicate detection
- Its sampled graph data
- A graph color
- Visibility state
- Audio state

Equations can be:

- Added
- Edited
- Removed
- Shown or hidden
- Muted or unmuted

Editing an equation preserves the layer identity and replaces its native expression and graph data.

### Interactive graph visualization

The graph view supports:

- Panning
- Pinch-to-zoom
- Adaptive graph sampling
- Pixel-aware sampling
- Manual long-press cursor interaction
- Multi-equation cursor inspection
- Discontinuity and undefined-region handling
- Asymptote/gap handling
- Re-sampling after viewport changes

The native sampler adapts its sampling density to the current viewport instead of using one fixed resolution for every zoom level.

Current sampling parameters in the ViewModel include:

- Initial sample count: 3000
- Sampling target: approximately 3 samples per pixel
- Minimum viewport samples: 1000
- Maximum viewport samples: 20000

The native graph sampler also separates undefined/discontinuous portions so that the renderer does not incorrectly connect unrelated graph segments.

### Graph cursor

The visualization provides a manual cursor for inspecting graph values.

With multiple equations, cursor state can be used to inspect the corresponding values of visible graph layers at the selected x-coordinate.

### Audio / sonification

GraphSonic converts graph values into sound.

The audio subsystem contains:

- `AudioEngine`
- `ListenController`
- `FrequencyMapper`
- `NoteMapper`
- `GraphSegmentExtractor`
- `GraphSegment`
- `Waveform`
- `ListenState`

Supported controls include:

- Listen / stop
- Per-equation audio enable/disable
- Frequency mode
- Waveform selection
- Playback speed
- Volume

The audio system is designed around defined graph segments so that undefined mathematical regions do not become arbitrary audio transitions.

### History

GraphSonic stores equation history locally.

The history repository:

- Uses Android `SharedPreferences` with JSON storage
- Keeps up to 100 entries
- De-duplicates expressions
- Moves a newly selected/added expression to the top
- Provides history data to the equation and history flows

History is local to the application and does not require a backend service.

## Architecture

GraphSonic uses a layered Android/native architecture.

```text
Android / Jetpack Compose
        |
        v
Feature screens + ViewModels
        |
        v
Kotlin application/domain layer
        |
        +----------------------+
        |                      |
        v                      v
    GraphEngine          ListenController
        |                      |
        v                      v
    NativeBridge          AudioEngine
        |
        v
JNI
        |
        v
C++ mathematical + graph engine
```

### Kotlin layer

The main Kotlin packages are:

```text
com.anuj.graphsonic
├── data
│   └── history
├── domain
│   └── model
├── engine
│   ├── GraphEngine
│   └── NativeBridge
├── feature
│   ├── audio
│   ├── equation
│   ├── history
│   ├── navigation
│   └── visualization
└── ui
```

### Data and history

`data/history`

Contains the local equation history repository.

### Domain models

`domain/model`

Contains graph data models such as:

- `GraphData`
- `GraphPoint`

### Native bridge

`engine/NativeBridge.kt`

Defines the JNI boundary used by Kotlin to communicate with the C++ expression engine.

The native API currently includes operations for:

- Creating an expression
- Validating an expression
- Evaluating an expression
- Checking whether an expression is defined at x
- Generating graph samples
- Destroying native expressions
- Obtaining canonical expressions

### Graph engine

`engine/GraphEngine.kt`

Provides the Kotlin-side graph generation abstraction over the native bridge.

### Equation feature

`feature/equation`

Responsible for expression entry and the initial graphing flow.

The equation screen also performs live validation through the ViewModel/native parser.

### Visualization feature

`feature/visualization`

This is the main graphing feature.

Important components include:

- `VisualizationScreen`
- `VisualizationViewModel`
- `GraphLayer`
- `GraphViewport`
- `GraphViewportController`
- `GraphCursorState`
- `GraphSampler`
- `GraphCanvas`
- `GraphCursor`
- `CursorInfoCard`
- `ListenControls`
- `ListenPanel`
- `ListenInfoCard`
- `GraphCoordinateUtils`

The ViewModel owns the graph-layer state, native expression handles, history integration, viewport-triggered resampling, cursor state, and listening configuration.

### Audio feature

`feature/audio`

Responsible for turning sampled graph data into sound.

The audio path is intentionally separated from graph rendering so graph interaction and sonification can evolve independently.

### Navigation

`feature/navigation`

Contains:

- `AppDestination`
- `AppNavigation`

The navigation layer connects equation input, history, and visualization.

### UI

`ui`

Contains application theme, colors, typography, and shared UI elements.

## Native C++ Engine

The native engine is located under:

```text
app/src/main/cpp
```

It is built as the shared library:

`graphsonic`

using CMake and C++17.

### Native source structure

```text
app/src/main/cpp
├── CMakeLists.txt
├── graphsonic.cpp
├── jni
│   └── NativeBridge.cpp
├── math
│   ├── AST.cpp
│   ├── AST.h
│   ├── Evaluator.cpp
│   ├── Evaluator.h
│   ├── Expression.cpp
│   ├── Expression.h
│   ├── Lexer.cpp
│   ├── Lexer.h
│   ├── Parser.cpp
│   ├── Parser.h
│   ├── Token.cpp
│   └── Token.h
└── graph
    ├── GraphSampler.cpp
    └── GraphSampler.h
```

### Lexer

The lexer converts the expression string into tokens.

The token model includes mathematical operators, parentheses, functions, superscript notation, and comma-separated function arguments.

### Parser

The parser converts tokens into an AST.

The parser is responsible for expression structure, precedence, implicit multiplication, function arguments, superscript powers, and syntax errors.

### AST

The AST represents mathematical expressions using nodes for:

- Numbers
- Variables
- Unary operations
- Binary operations
- Functions

Binary nodes contain an operator and left/right children. Function nodes contain their function name and argument nodes.

### Evaluator

The evaluator walks the AST for a supplied x value.

It contains domain handling for operations such as:

- Division by zero
- Invalid logarithm arguments
- Invalid square roots
- Trigonometric singularities
- Inverse-trigonometric domain limits
- Invalid powers
- Undefined results
- Non-finite results

The evaluator returns undefined/non-finite results in a form the graph and audio layers can recognize.

### Expression

`Expression` ties parsing and evaluation together and provides canonical expression output.

Canonical expressions are used by the Kotlin layer to detect duplicate equations even when equivalent input forms have different textual representations.

### Graph sampler

`GraphSampler` samples an expression over an x-range.

Its responsibilities include:

- Generating graph samples
- Adapting sample density
- Detecting undefined regions
- Preserving discontinuities
- Avoiding false line connections across gaps
- Supporting viewport-sized sampling

This is an important correctness boundary between mathematical evaluation and visual rendering.

### JNI

`jni/NativeBridge.cpp` exposes the C++ engine to Kotlin.

JNI responsibilities include:

- Converting Kotlin strings into native strings
- Constructing and destroying native expression objects
- Translating native parser exceptions into Java exceptions
- Returning graph arrays to Kotlin
- Returning canonical expressions
- Performing defensive handle checks
- Returning safe undefined values for evaluation failures

## Expression Processing Pipeline

A typical expression follows this path:

```text
User input
   |
   v
EquationScreen
   |
   v
VisualizationViewModel
   |
   v
NativeBridge
   |
   v
JNI
   |
   v
Lexer
   |
   v
Parser
   |
   v
AST
   |
   v
Expression
   |
   +--------> canonical expression
   |
   +--------> evaluator
   |
   v
GraphSampler
   |
   v
GraphData
   |
   +--------> Compose graph rendering
   |
   +--------> Audio segment extraction
                    |
                    v
               Frequency mapping
                    |
                    v
               AudioEngine
```

## Threading and Lifecycle Safety

GraphSonic contains asynchronous graph resampling and audio playback, so native expression lifetime is important.

The current architecture uses:

- A native read/write lock around native expression access
- A sampling generation counter to reject stale resampling results
- ViewModel-scoped coroutines for asynchronous graph work
- Explicit audio lifecycle handling
- Playback generation/state handling
- Cleanup of native expression handles when the ViewModel is cleared

The goal is to prevent background graph/audio work from using native expression objects after they have been destroyed.

## Build Configuration

The repository currently uses:

- Android Gradle Plugin: 9.3.1
- Kotlin: 2.2.10
- Compile SDK: 37
- Target SDK: 37
- Minimum SDK: 26
- Java compatibility: 11
- CMake: 3.22.1
- C++ standard: C++17
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Lifecycle
- AndroidX Room dependency is currently present in the Gradle configuration, although equation history itself is implemented through the history repository rather than a Room database.

Application identifiers:

`com.anuj.graphsonic`

Native library:

`graphsonic`

Current application version:

- versionCode: 1
- versionName: 1.0

## Building the Project

Open the repository in Android Studio and allow Gradle to synchronize.

The project uses the Gradle wrapper.

### Debug build

Linux/macOS:

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

### Unit tests

Linux/macOS:

```bash
./gradlew test
```

Windows:

```bat
gradlew.bat test
```

### Android instrumentation tests

Linux/macOS:

```bash
./gradlew connectedAndroidTest
```

Windows:

```bat
gradlew.bat connectedAndroidTest
```

The native layer is compiled automatically through the configured CMake external native build.

## Testing the Main User Flows

After building, the following flows are useful smoke tests.

### Basic graphing

1. Launch GraphSonic.
2. Enter `sin(x)`.
3. Graph the expression.
4. Confirm the graph renders.

### Invalid expression validation

1. Open equation input.
2. Type `abc(x)`.
3. Confirm an error appears while typing.
4. Replace it with `sin(x)`.
5. Confirm the error disappears.

### Multiple equations

1. Add more than one equation.
2. Confirm each graph is independently visible.
3. Toggle individual graph visibility.
4. Toggle individual audio state.
5. Remove an equation.
6. Edit an equation.

### Graph interaction

1. Pan the graph.
2. Pinch to zoom.
3. Long-press to move the cursor.
4. Test multiple visible equations at the same cursor position.
5. Test functions with discontinuities such as `tan(x)`.

### Mathematical edge cases

Useful expressions include:

```text
x²
x³
2x
xsin(x)
sin 2x
log(x,2)
floor(x)
ceil(x)
(-8)^(1/3)
tan(x)
1/x
sqrt(x)
```

These exercise different parser, evaluator, and graph-sampling paths.

### Audio

1. Graph a continuous expression.
2. Start listening.
3. Stop listening.
4. Start again.
5. Toggle equation audio on/off.
6. Change waveform, frequency mode, speed, and volume.
7. Test an expression containing undefined regions.

## Current Development Roadmap

The current engineering backlog contains several known areas that are intentionally separate from the completed mathematical correctness fixes.

### Graph coverage during panning

The current resampling flow samples the visible viewport. A planned improvement is to sample a small buffer beyond the viewport so normal panning does not expose empty graph edges.

### Resampling state protection

Further work is planned around ensuring that concurrent graph operations cannot lose newer state when older asynchronous work finishes.

### Listen-state performance

The audio UI currently exposes frequently changing playback state. Further work is planned to reduce unnecessary Compose recomposition caused by high-frequency state updates.

### Graph allocation performance

Graph data currently crosses the native/Kotlin boundary as arrays and is represented by Kotlin graph data structures. Further optimization can reduce copying and object allocation during high-density sampling.

### Native/release optimization

Release optimization is currently disabled in the Android build configuration. Native and release build optimization should be evaluated before production distribution.

### Architecture cleanup

The repository still contains some legacy or potentially unused visualization/support classes. Cleanup should happen only after confirming their actual usage and after the core behavior is stable.

## Important Design Principles

### Native engine owns mathematical correctness

Parsing and numerical evaluation are implemented in C++. Kotlin should not duplicate mathematical parsing rules.

### Kotlin owns application state

Compose and ViewModel code manage:

- User input
- Graph layers
- Navigation
- History
- Viewport state
- Audio controls
- Cursor state

### Graph and audio share mathematical data

The graph sampler produces data that can be used both visually and sonically.

### Undefined is meaningful

A function being undefined at a particular x is not treated as an ordinary zero or an arbitrary point. Graph sampling and audio segmentation use defined regions to preserve mathematical discontinuities.

### Stable equation identity

Graph layers have stable IDs so that editing, visibility, audio state, and native handles can be managed independently.

## Repository Layout

At a high level:

```text
GraphSonic/
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   │   ├── cpp/
│   │   │   ├── java/
│   │   │   ├── keepRules/
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

## Contributing

Before changing the project:

1. Understand whether the behavior belongs in Kotlin or the native engine.
2. Preserve the native expression lifecycle rules.
3. Avoid duplicating parser/evaluator logic in Kotlin.
4. Test mathematical edge cases when modifying the parser/evaluator.
5. Test discontinuities when modifying graph sampling.
6. Test rapid start/stop and lifecycle transitions when modifying audio.
7. Build after each focused change.
8. Keep unrelated UI changes separate from correctness or performance fixes.

For larger changes, prefer small, focused commits that make regressions easy to identify.

## Known Scope Boundaries

GraphSonic is currently focused on explicit functions of x.

Implicit equations and more advanced mathematical systems are not described as supported features unless implemented by the current parser/evaluator.

The application currently uses local history storage and does not require a network backend for its core graphing or sonification workflow.

## License

No license file is currently present in the repository. Until a license is added, the repository should not be assumed to grant broad rights to reuse, modify, or redistribute the source.

## Project Goal

The long-term goal of GraphSonic is to make mathematical functions explorable through both **visual structure and sound**, while keeping the mathematical engine accurate, the graph interactive, and the audio representation responsive.

The project deliberately separates the mathematical engine, graph sampling, Android state management, visualization, and audio pipeline so each part can evolve without making the entire application dependent on one implementation layer.
