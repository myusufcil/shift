# Shift - Coding Standards & Conventions

## 📁 Project Structure

```
Shift/
├── app/                           # Main application module
├── core/                          # Core shared modules
│   ├── common/                    # Common utilities, Result, extensions
│   ├── designsystem/              # Material3 theme, components
│   ├── domain/                    # Base domain interfaces
│   ├── data/                      # Base repository patterns
│   └── database/                  # SQLDelight driver factory
├── feature/                       # Feature modules
│   ├── habits/
│   │   ├── domain/                # Habit entities, use cases, repository interfaces
│   │   ├── data/                  # Repository implementations, SQLDelight
│   │   └── presentation/          # Screens, ViewModels
│   ├── statistics/
│   ├── settings/
│   └── onboarding/
└── composeApp/                    # (Legacy - to be migrated)
```

## 📦 Package Naming Convention

### Pattern
```
com.cil.shift.{module-type}.{module-name}.{layer}
```

### Examples
```kotlin
// Core modules
com.cil.shift.core.common
com.cil.shift.core.designsystem.theme
com.cil.shift.core.database

// Feature modules
com.cil.shift.feature.habits.domain.model
com.cil.shift.feature.habits.domain.usecase
com.cil.shift.feature.habits.domain.repository
com.cil.shift.feature.habits.data.repository
com.cil.shift.feature.habits.data.mapper
com.cil.shift.feature.habits.presentation.screen
com.cil.shift.feature.habits.presentation.viewmodel
```

## 🏗️ Architecture Layers

### 1. Domain Layer (Pure Kotlin)
- **NO** Android/iOS dependencies
- **NO** framework dependencies (except Kotlin stdlib, coroutines)
- Contains: Entities, Use Cases, Repository Interfaces

```kotlin
// ✅ GOOD - Pure Kotlin
data class Habit(
    val id: String,
    val name: String,
    val createdAt: Long
)

interface HabitRepository {
    fun getHabits(): Flow<List<Habit>>
    suspend fun createHabit(habit: Habit)
}

class GetHabitsUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> = repository.getHabits()
}

// ❌ BAD - Android dependency in domain
import android.content.Context // NEVER in domain!
```

### 2. Data Layer
- Implements repository interfaces from domain
- Contains SQLDelight schemas, DAO, API clients
- Maps between data models and domain models

```kotlin
// ✅ GOOD
class HabitRepositoryImpl(
    private val database: HabitsDatabase
) : HabitRepository {
    override fun getHabits(): Flow<List<Habit>> {
        return database.habitQueries.getAll()
            .asFlow()
            .mapToList()
            .map { entities -> entities.map { it.toDomain() } }
    }
}

// Mapper
fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    createdAt = created_at
)
```

### 3. Presentation Layer
- Contains Screens (Voyager), ViewModels, UI State
- Depends on domain layer (NOT data layer)
- Uses Compose Multiplatform

```kotlin
// ✅ GOOD - ViewModel depends on use cases, not repositories
class HabitsViewModel(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModel() {
    // ...
}

// ❌ BAD - ViewModel depends on repository (skip use case)
class HabitsViewModel(
    private val habitRepository: HabitRepository // AVOID!
) : ViewModel()
```

## 📝 Naming Conventions

### Files & Classes

| Type | Convention | Example |
|------|-----------|---------|
| Entity/Model | PascalCase, noun | `Habit`, `HabitCompletion` |
| Use Case | PascalCase, Verb+Noun+UseCase | `GetHabitsUseCase`, `CreateHabitUseCase` |
| Repository Interface | PascalCase, Noun+Repository | `HabitRepository` |
| Repository Impl | PascalCase, Noun+RepositoryImpl | `HabitRepositoryImpl` |
| ViewModel | PascalCase, Feature+ViewModel | `HabitsViewModel`, `CreateHabitViewModel` |
| Screen (Voyager) | PascalCase, Feature+Screen | `HabitsListScreen`, `CreateHabitScreen` |
| Composable Function | PascalCase, descriptive | `HabitCard`, `CircularProgressIndicator` |
| Extension Function | camelCase, verb | `toLocalDate()`, `formatShort()` |
| Constants | SCREAMING_SNAKE_CASE | `DATABASE_NAME`, `DEFAULT_HABIT_COLOR` |

### Variables

```kotlin
// ✅ GOOD
private val _state = MutableStateFlow(HabitsState())
val state: StateFlow<HabitsState> = _state.asStateFlow()

val habitId: String
val isCompleted: Boolean
val createdAt: Long

// ❌ BAD
val HabitID: String // Wrong case
val is_completed: Boolean // Snake case
val created: Long // Not descriptive
```

## 🎯 Use Case Patterns

### Standard Use Case Template

```kotlin
class SomeActionUseCase(
    private val repository: SomeRepository
) {
    suspend operator fun invoke(param: Type): Result<ReturnType> {
        return try {
            val result = repository.performAction(param)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### Flow-based Use Case

```kotlin
class ObserveSomethingUseCase(
    private val repository: SomeRepository
) {
    operator fun invoke(): Flow<List<Item>> {
        return repository.observeItems()
    }
}
```

## 🔄 State Management

### UI State Pattern

```kotlin
// ✅ GOOD - Immutable data class for UI state
data class HabitsState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
class HabitsViewModel : ViewModel() {
    private val _state = MutableStateFlow(HabitsState())
    val state: StateFlow<HabitsState> = _state.asStateFlow()

    fun updateState(transform: (HabitsState) -> HabitsState) {
        _state.update(transform)
    }
}

// ❌ BAD - Mutable state
class BadViewModel : ViewModel() {
    var habits: List<Habit> = emptyList() // Mutable!
    var isLoading: Boolean = false
}
```

## 🗃️ SQLDelight Conventions

### File Naming
- One `.sq` file per table
- PascalCase matching table name
- Location: `{module}/src/commonMain/sqldelight/com/cil/shift/feature/{name}/`

### Query Naming

```sql
-- Habit.sq

-- Queries: camelCase, verb-based
getAll:
SELECT * FROM Habit WHERE is_archived = 0;

getById:
SELECT * FROM Habit WHERE id = ?;

insert:
INSERT OR REPLACE INTO Habit VALUES (?, ?, ?, ?);

deleteById:
UPDATE Habit SET is_archived = 1 WHERE id = ?;

-- ❌ BAD naming
selectHabits:  -- Use getAll instead
get_by_id:     -- Use camelCase
```

## 🎨 Compose Conventions

### Composable Organization

```kotlin
// ✅ GOOD - Screen composable with ViewModel
@Composable
fun HabitsListScreen(
    viewModel: HabitsViewModel = koinScreenModel()
) {
    val state by viewModel.state.collectAsState()

    HabitsListContent(
        state = state,
        onHabitClick = viewModel::onHabitClick,
        onToggleComplete = viewModel::onToggleComplete
    )
}

// Pure composable - stateless, testable
@Composable
private fun HabitsListContent(
    state: HabitsState,
    onHabitClick: (String) -> Unit,
    onToggleComplete: (String) -> Unit
) {
    // UI implementation
}
```

### Modifier Order

```kotlin
// ✅ GOOD - Consistent modifier order
Modifier
    .fillMaxWidth()          // 1. Size
    .padding(16.dp)          // 2. Padding/spacing
    .background(Color.Blue)  // 3. Background/border
    .clickable { }           // 4. Interaction
    .testTag("habit-card")   // 5. Semantics/testing
```

## 🔧 Gradle Build Configuration

### Standard build.gradle.kts Template

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // Add other plugins as needed
}

kotlin {
    // Use compilerOptions instead of kotlinOptions (Kotlin 2.0+)
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ModuleName"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Common dependencies
        }

        androidMain.dependencies {
            // Android-specific dependencies
        }

        iosMain.dependencies {
            // iOS-specific dependencies
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.cil.shift.module.name"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### ❌ Common Gradle Mistakes

```kotlin
// ❌ DON'T - Old kotlinOptions API
androidTarget {
    compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

// ✅ DO - New compilerOptions API
androidTarget {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
```

## 📐 Dependency Rules

### Module Dependencies (Allowed)

```
presentation → domain ← data
     ↓                     ↓
designsystem          database
     ↓                     ↓
   common ← ───────────────┘
```

### Rules
1. **Domain** depends on NOTHING (pure Kotlin)
2. **Data** depends on **Domain** + **Database/Network**
3. **Presentation** depends on **Domain** + **DesignSystem**
4. **NO** circular dependencies
5. **NO** presentation → data (always go through domain)

## 🧪 Testing Conventions

### File Naming
- Test file: `{ClassName}Test.kt`
- Location: `{module}/src/commonTest/kotlin/`

### Test Structure

```kotlin
class GetHabitsUseCaseTest {
    private lateinit var repository: FakeHabitRepository
    private lateinit var useCase: GetHabitsUseCase

    @Before
    fun setup() {
        repository = FakeHabitRepository()
        useCase = GetHabitsUseCase(repository)
    }

    @Test
    fun `getHabits returns habits from repository`() = runTest {
        // Given
        val expectedHabits = listOf(/* test data */)
        repository.setHabits(expectedHabits)

        // When
        val result = useCase().first()

        // Then
        assertEquals(expectedHabits, result)
    }
}
```

## 🌐 Localization (Future)

### String Resources
- Keep all UI strings in resources (not hardcoded)
- Use descriptive keys

```kotlin
// ✅ GOOD
Text(stringResource(Res.strings.habits_empty_state_title))

// ❌ BAD
Text("No habits yet") // Hardcoded!
```

## 🔐 Error Handling

### Result Pattern

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Usage in ViewModel
viewModelScope.launch {
    createHabitUseCase(habit)
        .onSuccess { _state.update { it.copy(isLoading = false) } }
        .onError { error -> _state.update { it.copy(error = error.message) } }
}
```

## 📊 Code Style

### Kotlin Conventions
- Use 4 spaces for indentation
- Max line length: 120 characters
- Use trailing commas in multi-line declarations
- Prefer `val` over `var`
- Use explicit return types for public functions

```kotlin
// ✅ GOOD
fun calculateStreak(
    completions: List<HabitCompletion>,
    startDate: LocalDate,
): Int {
    return completions
        .filter { it.date >= startDate }
        .size
}

// ❌ BAD
fun calculateStreak(completions: List<HabitCompletion>, startDate: LocalDate) =
    completions.filter {it.date>=startDate}.size // Hard to read!
```

## 🚀 Performance

### Avoid in Composables
- Heavy computations (use `remember` or ViewModel)
- Database queries (always in ViewModel/Repository)
- Creating new objects on every recomposition

```kotlin
// ✅ GOOD
@Composable
fun HabitCard(habit: Habit) {
    val backgroundColor = remember(habit.color) {
        Color(habit.color.toLong(16))
    }
}

// ❌ BAD
@Composable
fun HabitCard(habit: Habit) {
    val backgroundColor = Color(habit.color.toLong(16)) // Recreated every recomposition!
}
```

## 📚 Documentation

### KDoc for Public APIs

```kotlin
/**
 * Creates a new habit and saves it to the database.
 *
 * @param habit The habit to create
 * @return Result.Success if created successfully, Result.Error otherwise
 */
suspend fun createHabit(habit: Habit): Result<Unit>
```

### Inline Comments
- Explain **WHY**, not **WHAT**
- Keep comments up-to-date

```kotlin
// ✅ GOOD
// Reset streak to 0 if last completion was more than 2 days ago
if (daysSinceLastCompletion > 2) {
    currentStreak = 0
}

// ❌ BAD
// Set current streak to 0
currentStreak = 0
```

---

## ✅ Checklist Before Committing

- [ ] Code follows naming conventions
- [ ] No hardcoded strings (use resources)
- [ ] Domain layer has no platform dependencies
- [ ] Tests are written and passing
- [ ] No compiler warnings
- [ ] Code is formatted (use IntelliJ's format)
- [ ] Dependencies follow the architecture rules
