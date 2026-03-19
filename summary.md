# HealthyBite — Technical Summary

## 1. General Architecture

### Pattern: MVVM (Model-View-ViewModel) with partial Clean Architecture layering

The project follows **MVVM** as its primary UI architecture and introduces a **lightweight domain layer** that separates business rules from the data layer. It does not implement full Clean Architecture (no use-case/interactor classes), but the separation into `data`, `domain`, and `ui` packages is a meaningful step in that direction.

### High-level data flow

```
Room Database / DataStore
        ↓  (Flow<T>)
    DAO / SessionDataStore
        ↓  (Flow<T>)
    ViewModel  ←──  (events)  ──  Composable Screen
        ↓  (StateFlow<UiState>)           ↑
    Composable Screen  ────────────────────┘
```

1. **Room DAOs** expose reactive `Flow`s that re-emit whenever the underlying table changes.
2. **ViewModels** collect these Flows (usually via `flatMapLatest` on the session's `currentUserId`), aggregate or transform the data, and push the result into a `MutableStateFlow<UiState>`.
3. **Compose screens** collect the `StateFlow` via `collectAsState()` and render the UI. User actions (button taps, text input) are forwarded back to the ViewModel as function calls.
4. **No repository layer** exists for most features — ViewModels talk directly to DAOs. The exception is authentication, which goes through `AuthRepository` / `AuthRepositoryImpl`.

---

## 2. Project Structure

```
pl.akp.healthybite/
├── MainActivity.kt                  # Single Activity — Compose entry point
├── HealthyBiteApplication.kt        # Application subclass — manual DI container
│
├── app/
│   ├── HealthyBiteApp.kt            # Root composable (theme + NavGraph)
│   └── MainScaffold.kt              # Post-login shell (top bar, bottom nav, FAB, inner NavHost)
│
├── data/
│   ├── datastore/
│   │   ├── SessionDataStore.kt      # DataStore wrapper for user session (login state, userId)
│   │   └── SessionKeys.kt           # Preference key constants
│   ├── db/
│   │   ├── AppDatabase.kt           # Room database definition (7 entities, 6 DAOs)
│   │   ├── DatabaseSeeder.kt        # DAO-level seeder (runs at startup via SplashViewModel)
│   │   ├── PrepopulateCallback.kt   # Room callback — raw SQL seed on first DB creation
│   │   ├── converters/
│   │   │   └── EnumConverters.kt    # MealType ↔ String Room TypeConverter
│   │   ├── dao/                     # Room DAO interfaces (6 total)
│   │   └── entity/                  # Room entity data classes (7 total)
│   └── repository/
│       └── AuthRepositoryImpl.kt    # AuthRepository implementation (UserDao + SessionDataStore)
│
├── domain/
│   ├── model/
│   │   ├── MealType.kt              # Enum: BREAKFAST, LUNCH, DINNER, SNACK
│   │   └── User.kt                  # Domain model (excludes password)
│   ├── repository/
│   │   └── AuthRepository.kt        # Interface: login, register, logout, getUser, updateCaloriesGoal
│   └── validation/
│       ├── EmailValidator.kt        # Email format validation (android.util.Patterns)
│       ├── PasswordRules.kt         # Enum of password complexity requirements
│       └── PasswordValidator.kt     # Checks password against all PasswordRule entries
│
└── ui/
    ├── auth/                        # Login + Register screens, ViewModels, UiStates
    ├── home/                        # Home tab (daily nutrition summary)
    ├── log/                         # Log tab (today's meal entries list)
    ├── meals/                       # Add Meal screen (template or custom)
    ├── navigation/
    │   ├── NavGraph.kt              # Root-level navigation graph
    │   └── Routes.kt                # Sealed class with all route definitions
    ├── plans/                       # Plans tab (meal plan browser + "apply to today")
    ├── profile/                     # Profile screen (email, calorie goal, logout)
    ├── shopping/                    # Shopping tab (grocery list CRUD)
    ├── splash/                      # Splash screen (session check + DB seeding)
    ├── theme/                       # Material 3 theme (Color, Typography, Theme)
    └── water/                       # Water tab (daily intake tracking)
```

### Key classes

| Class | Responsibility |
|---|---|
| `HealthyBiteApplication` | Manual DI container — creates and holds `AppDatabase`, `SessionDataStore`, `AuthRepository`, `DatabaseSeeder` as lazy singletons |
| `MainActivity` | Only Activity; calls `setContent { HealthyBiteApp() }` |
| `AppDatabase` | Room database (version 7, 7 entities, 6 DAOs, destructive migration fallback) |
| `SessionDataStore` | Wrapper around Jetpack DataStore Preferences; stores `isLoggedIn` flag and `currentUserId` |
| `AuthRepositoryImpl` | Handles login (email+password lookup), register (with duplicate check), logout (clear session), user lookup, and calorie-goal updates |
| `DatabaseSeeder` | DAO-level seeder with double-checked locking; inserts demo user, 16 meal templates, 3 plans, shopping items |
| `PrepopulateCallback` | Room `Callback.onCreate()`; raw SQL inserts of the same seed data on first DB creation |

---

## 3. Data Layer

### Storage: Room (SQLite) + Jetpack DataStore Preferences

All persistent data is stored locally in a Room database (`healthybite.db`). Session state (login flag + user ID) is stored in Jetpack DataStore Preferences (`session.preferences_pb`).

### Database schema (7 entities)

| Entity | Table | Key columns |
|---|---|---|
| `UserEntity` | `users` | `id` (PK, auto), `email` (unique index), `password`, `displayName`, `dailyCaloriesGoal`, `weightKg` |
| `MealTemplateEntity` | `meal_templates` | `id` (PK, auto), `name`, `type` (MealType), `kcal`, `proteinG`, `fatG`, `carbsG` |
| `MealEntryEntity` | `meal_entries` | `id` (PK, auto), `userId`, `templateId`, `name`, `mealType`, `date`, `timestamp`, `kcal`, `proteinG`, `fatG`, `carbsG`, `notes` |
| `ShoppingItemEntity` | `shopping_items` | `id` (PK, auto), `userId`, `name`, `quantity`, `isChecked` |
| `PlanTemplateEntity` | `plan_templates` | `id` (PK, auto), `name` |
| `PlanTemplateItemEntity` | `plan_template_items` | `id` (PK, auto), `planTemplateId` (FK), `mealTemplateName`, `mealType` |
| `WaterEntryEntity` | `water_entries` | `id` (PK, auto), `userId`, `date`, `amountMl`, `timestamp` |

### DAO interfaces (6 total)

| DAO | Key operations |
|---|---|
| `UserDao` | `getByEmail()`, `getById()`, `insert()`, `insertIgnore()`, `updateCaloriesGoal()` |
| `MealTemplateDao` | `observeByType(MealType): Flow`, `getByName()`, `count()`, `insert()`, `insertAll()` |
| `MealEntryDao` | `observeEntriesForUserAndDate(userId, date): Flow`, `insert()`, `insertAll()`, `deleteById()` |
| `ShoppingDao` | `observeItemsByUser(userId): Flow`, `insert()`, `insertAll()`, `updateChecked()`, `deleteById()` |
| `PlanDao` | `observePlansWithItems(): Flow<List<PlanWithItems>>`, `insert()`, `insertItems()` |
| `WaterDao` | `observeTotalForDate(userId, date): Flow<Int?>`, `observeEntriesForDate(): Flow`, `insert()` |

### Repository layer

Only **authentication** has a formal repository abstraction:
- **Interface:** `AuthRepository` (in `domain/repository/`) — defines `login()`, `register()`, `logout()`, `getUser()`, `updateCaloriesGoal()`
- **Implementation:** `AuthRepositoryImpl` (in `data/repository/`) — backed by `UserDao` + `SessionDataStore`

All other features (meals, shopping, water, plans) have **no repository**. ViewModels receive DAO instances directly and call them without an intermediate abstraction.

### Data flow from database to UI

Every feature ViewModel follows the same reactive pattern:

1. `SessionDataStore.currentUserId` emits the logged-in user's ID as a `Flow<Long?>`.
2. `flatMapLatest` maps the userId to the appropriate DAO query (e.g., `mealEntryDao.observeEntriesForUserAndDate(userId, today)`).
3. `collectLatest` receives each new emission, transforms it into a `UiState`, and updates the `MutableStateFlow`.
4. The Compose screen collects the `StateFlow` via `collectAsState()` and recomposes.

---

## 4. Seed / Initial Data

### Two seeding mechanisms exist (redundantly)

#### 1. `PrepopulateCallback` — Room callback (raw SQL, runs once)

- **File:** `data/db/PrepopulateCallback.kt`
- **When:** Triggered by Room's `Callback.onCreate()` — runs exactly once when the SQLite file is first created on disk.
- **How:** Executes raw `INSERT` SQL statements (no DAO access available during callback).
- **What it seeds:**
  - 1 demo user (`demo@healthy.pl` / `zaq1@WSX`)
  - 16 meal templates (4 per MealType)
  - 3 plan templates + 12 plan items
  - 9 shopping items for the demo user

#### 2. `DatabaseSeeder` — DAO-level seeder (runs at every app launch)

- **File:** `data/db/DatabaseSeeder.kt`
- **When:** Called by `SplashViewModel.init{}` on every app launch. Uses double-checked locking (`Mutex` + `@Volatile` flag) to run at most once per process.
- **Guard condition:** `mealTemplateDao.count() > 0` — skips if the table already has data.
- **How:** Uses Room DAOs and runs all inserts inside a single `database.withTransaction {}`.
- **What it seeds:** Same data as `PrepopulateCallback` (demo user, meal templates, plans, shopping items).

#### Initialization order

1. `HealthyBiteApplication` creates `AppDatabase` lazily with `PrepopulateCallback` registered.
2. On first app install, the database file doesn't exist → Room calls `PrepopulateCallback.onCreate()` → seed data is inserted via raw SQL.
3. `SplashViewModel.init{}` calls `databaseSeeder.seedIfNeeded()` → detects `count() > 0` → skips.
4. On subsequent launches (DB already exists), `PrepopulateCallback.onCreate()` is never called. `DatabaseSeeder` checks `count()` and skips again.
5. After a destructive migration (schema version bump), Room drops all tables → `PrepopulateCallback.onCreate()` fires again on the new database.

---

## 5. UI Layer

### Technology: Jetpack Compose (Material 3), single-activity architecture

All UI is built with Jetpack Compose. There are no XML layouts, Fragments, or `RecyclerView`s.

### Main screens

| Screen | File | Purpose |
|---|---|---|
| **Splash** | `ui/splash/SplashScreen.kt` | Loading indicator while DB seeds and session is checked; routes to Login or Home |
| **Login** | `ui/auth/LoginScreen.kt` | Email + password form; authenticates via `AuthViewModel` → `AuthRepository` |
| **Register** | `ui/auth/RegisterScreen.kt` | Account creation form with real-time password rule validation |
| **Home** | `ui/home/HomeScreen.kt` | Dashboard tab — today's aggregate calories, protein, fat, carbs |
| **Log** | `ui/log/LogScreen.kt` | Meal diary tab — lists today's entries with per-entry cards; supports delete |
| **Add Meal** | `ui/meals/AddMealScreen.kt` | Full-screen form to log a meal (from template or custom input) |
| **Shopping** | `ui/shopping/ShoppingScreen.kt` | Grocery list tab — add, check/uncheck, delete items |
| **Water** | `ui/water/WaterScreen.kt` | Water intake tab — quick-add buttons (+250/+500/+750 ml), progress bar |
| **Plans** | `ui/plans/PlansScreen.kt` | Meal plan browser — view plans, "apply to today" bulk-inserts entries |
| **Profile** | `ui/profile/ProfileScreen.kt` | User email, editable calorie goal, logout button |

### ViewModels

| ViewModel | Injected dependencies | Key responsibilities |
|---|---|---|
| `SplashViewModel` | `SessionDataStore`, `UserDao`, `DatabaseSeeder` | Seeds DB, checks session, emits navigation destination |
| `AuthViewModel` | `AuthRepository` | Manages login form state, delegates to `authRepository.login()` |
| `RegisterViewModel` | `AuthRepository` | Manages registration form, delegates to `authRepository.register()` |
| `HomeViewModel` | `SessionDataStore`, `MealEntryDao` | Observes today's entries, computes aggregate nutrition totals |
| `LogViewModel` | `SessionDataStore`, `MealEntryDao` | Observes today's entries, supports entry deletion |
| `AddMealViewModel` | `SessionDataStore`, `MealTemplateDao`, `MealEntryDao` | Template browsing, custom meal form, validation, save |
| `ShoppingViewModel` | `SessionDataStore`, `ShoppingDao` | Shopping list CRUD with two-step delete confirmation |
| `WaterViewModel` | `SessionDataStore`, `WaterDao` | Observes today's water total, inserts new intake entries |
| `PlansViewModel` | `SessionDataStore`, `PlanDao`, `MealTemplateDao`, `MealEntryDao` | Loads plans with items, resolves template kcal, "apply to today" |
| `ProfileViewModel` | `SessionDataStore`, `AuthRepository` | Loads user profile, edits calorie goal, handles logout |

### Navigation structure

**Two-level navigation:**

1. **Root NavHost** (in `NavGraph.kt`, controlled by root `NavHostController`):
   - `splash` → `login` ↔ `register` → `main`
   - `profile` and `add_meal` are pushed on top of `main` (full-screen overlays)

2. **Inner NavHost** (in `MainScaffold.kt`, controlled by `innerNav`):
   - 5 bottom-nav tabs: `home`, `log`, `shopping`, `water`, `plans`
   - Tab switching uses `popUpTo(startDestination)`, `saveState`, and `restoreState` for proper back-stack management

---

## 6. Dependency Flow

### Manual dependency injection via Application subclass

The project uses **no DI framework** (no Hilt, Koin, or Dagger). All dependencies are created as `lazy` properties in `HealthyBiteApplication`:

```
HealthyBiteApplication
  ├── database: AppDatabase (lazy)
  │     ├── .userDao()
  │     ├── .mealEntryDao()
  │     ├── .mealTemplateDao()
  │     ├── .shoppingDao()
  │     ├── .planDao()
  │     └── .waterDao()
  ├── sessionStore: SessionDataStore (lazy)
  ├── authRepository: AuthRepositoryImpl (lazy, depends on database + sessionStore)
  └── databaseSeeder: DatabaseSeeder (lazy, depends on database)
```

### How ViewModels receive dependencies

Each ViewModel defines a nested `Factory` class implementing `ViewModelProvider.Factory`. The factories are instantiated in `NavGraph.kt` and `MainScaffold.kt` by casting `LocalContext.current.applicationContext` to `HealthyBiteApplication` and passing the needed DAOs / stores:

```kotlin
val app = LocalContext.current.applicationContext as HealthyBiteApplication
val vm: HomeViewModel = viewModel(
    factory = HomeViewModel.Factory(app.sessionStore, app.database.mealEntryDao())
)
```

---

## 7. Potential Refactoring Points

### 7.1 Missing repository layer for most features

- `HomeViewModel`, `LogViewModel`, `AddMealViewModel`, `ShoppingViewModel`, `WaterViewModel`, and `PlansViewModel` all depend directly on Room DAOs.
- This tight coupling means replacing Room with any other data source (Firestore, REST API) requires modifying every ViewModel.
- **Recommendation:** Introduce repository interfaces (e.g., `MealRepository`, `ShoppingRepository`, `WaterRepository`, `PlanRepository`) in the `domain` layer, with Room-backed implementations in `data`.

### 7.2 Entity classes leak into the UI layer

- `LogUiState.entries` is typed as `List<MealEntryEntity>` (a Room entity).
- `ShoppingUiState.items` is typed as `List<ShoppingItemEntity>`.
- `AddMealUiState.templates` is typed as `List<MealTemplateEntity>`.
- **Impact:** The UI layer has a compile-time dependency on Room entity classes. Changing the persistence layer would ripple into every screen.
- **Recommendation:** Map entities to domain/UI models in the repository or ViewModel layer (similar to the existing `UserEntity.toDomain()` pattern in `AuthRepositoryImpl`).

### 7.3 Duplicate seeding logic

- `PrepopulateCallback` and `DatabaseSeeder` both insert the same demo data using different mechanisms (raw SQL vs. DAOs).
- **Impact:** Maintenance burden — any change to seed data must be duplicated in two places.
- **Recommendation:** Remove `PrepopulateCallback` and rely solely on `DatabaseSeeder`, or vice versa.

### 7.4 Manual DI boilerplate

- Every ViewModel requires a nested `Factory` class with boilerplate `@Suppress("UNCHECKED_CAST")`.
- Composables must cast `applicationContext` to access dependencies.
- **Recommendation:** Adopt Hilt (with `@HiltViewModel` + `@Inject constructor`) to eliminate factory boilerplate.

### 7.5 Plaintext password storage

- `UserEntity.password` is stored in plaintext. While noted as acceptable for a local-only demo, this becomes a security issue when migrating to a remote backend.

### 7.6 `PlanTemplateItemEntity` references meal templates by name (String)

- `mealTemplateName` is used instead of a foreign key to `meal_templates.id`.
- Renaming a template would silently break all plan items referencing it.
- **Recommendation:** Use `mealTemplateId: Long` (FK) instead, with a join query.

### 7.7 Date captured at ViewModel creation

- `LocalDate.now().toString()` is captured once in `init{}`. If the user keeps the app open past midnight, data continues to be scoped to the previous day until the ViewModel is recreated.

---

## 8. Firebase Readiness

### What would need to change to replace Room with Firebase Firestore

#### 8.1 Classes that must be created

- **Firestore repository implementations** for each feature:
  - `FirestoreMealRepository` (replaces direct `MealEntryDao` / `MealTemplateDao` usage)
  - `FirestoreShoppingRepository` (replaces `ShoppingDao`)
  - `FirestoreWaterRepository` (replaces `WaterDao`)
  - `FirestorePlanRepository` (replaces `PlanDao`)
  - `FirestoreAuthRepository` (replaces `AuthRepositoryImpl` if Firebase Auth is used)
- **Repository interfaces** in `domain/repository/` for features that currently lack them (meals, shopping, water, plans)
- **Domain model classes** for entities that currently only exist as Room entities (`MealEntry`, `MealTemplate`, `ShoppingItem`, `WaterEntry`, `PlanTemplate`, `PlanTemplateItem`)

#### 8.2 Classes that must be modified

| Class/File | Required changes |
|---|---|
| `HealthyBiteApplication` | Replace Room database + DAO lazy properties with Firestore repository instances. If using Firebase Auth, replace `AuthRepositoryImpl` with a Firebase-backed implementation. |
| `AppDatabase` | Can be removed entirely (or retained for offline caching) |
| All 6 DAO interfaces | No longer needed if Firestore is the sole data source; kept if Room is used as offline cache |
| All 7 entity classes | May be repurposed as Firestore document models or replaced with domain models + Firestore DTOs |
| `DatabaseSeeder` | Replaced with Firestore seed scripts or Cloud Functions |
| `PrepopulateCallback` | Removed |
| `SessionDataStore` | Replaced or simplified if using Firebase Auth (auth state is managed by Firebase SDK) |
| `NavGraph.kt` | Update ViewModel factory instantiation to pass Firestore repositories instead of DAOs |
| `MainScaffold.kt` | Same as NavGraph — update factory instantiation |
| `HomeViewModel` | Change constructor to accept a repository interface instead of `MealEntryDao` |
| `LogViewModel` | Same — replace `MealEntryDao` with repository interface |
| `AddMealViewModel` | Replace `MealTemplateDao` + `MealEntryDao` with repository interfaces |
| `ShoppingViewModel` | Replace `ShoppingDao` with repository interface |
| `WaterViewModel` | Replace `WaterDao` with repository interface |
| `PlansViewModel` | Replace `PlanDao` + `MealTemplateDao` + `MealEntryDao` with repository interfaces |
| `ProfileViewModel` | Minimal changes if `AuthRepository` interface is kept; implementation swapped |
| `SplashViewModel` | Replace `UserDao` + `DatabaseSeeder` with Firebase Auth session check |
| `LogUiState`, `ShoppingUiState`, `AddMealUiState` | Change entity types to domain models |

#### 8.3 Current Firebase state

- The `firebase-bom:34.10.0` platform dependency and the `com.google.gms.google-services` plugin are already declared in `build.gradle.kts`.
- No Firebase SDK libraries (Auth, Firestore, etc.) are imported yet — only the BOM is present.
- No `google-services.json` configuration file was found in the project.
- No Firebase code exists in any source file.

#### 8.4 Migration strategy (recommended)

1. **Introduce repository interfaces** for all features (meals, shopping, water, plans).
2. **Create Room-backed implementations** of these interfaces (extracting current DAO logic from ViewModels).
3. **Refactor ViewModels** to depend on interfaces rather than DAOs.
4. **Map all Room entities to domain models** at the repository boundary.
5. **Add Firebase dependencies** (Firestore, Auth) and create Firestore-backed implementations of the same interfaces.
6. **Swap implementations** in `HealthyBiteApplication` (or introduce Hilt for cleaner DI switching).
7. **Remove or repurpose Room** as an offline cache behind the Firestore repositories.
