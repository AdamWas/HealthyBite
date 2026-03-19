package pl.akp.healthybite

import android.app.Application
import androidx.room.Room
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.datastore.sessionDataStore
import pl.akp.healthybite.data.db.AppDatabase
import pl.akp.healthybite.data.db.DatabaseSeeder
import pl.akp.healthybite.data.db.PrepopulateCallback
import pl.akp.healthybite.data.repository.AuthRepositoryImpl
import pl.akp.healthybite.domain.repository.AuthRepository

/**
 * Application-level singleton that acts as the manual dependency injection (DI) container.
 *
 * Android creates exactly one instance of this class when the process starts (before any
 * Activity). All shared dependencies (database, session store, repositories) are created
 * here as lazy properties so they are instantiated only once and reused everywhere.
 *
 * Other parts of the app (ViewModelFactories, composables) access these dependencies by
 * casting the applicationContext:
 *     val app = context.applicationContext as HealthyBiteApplication
 *     val dao = app.database.mealEntryDao()
 *
 * This avoids the need for a third-party DI framework (Hilt / Koin) while still keeping
 * a single source of truth for heavyweight objects like the database.
 */
class HealthyBiteApplication : Application() {

    /**
     * Room database – the single SQLite database for the entire app.
     *
     * `by lazy` ensures the database is built only once, on first access, rather than at
     * Application.onCreate() time. This keeps app startup fast because Room's builder does
     * I/O work (opening the SQLite file, running migrations, etc.).
     *
     * - addCallback(PrepopulateCallback()) runs on the very first database creation to insert
     *   seed data (e.g. default meal templates).
     * - fallbackToDestructiveMigration() tells Room that if the schema version changes and no
     *   migration path is defined, it should DROP all tables and recreate the database instead
     *   of crashing. This is acceptable during development but would lose user data in
     *   production.
     */
    val database: AppDatabase by lazy {
        @Suppress("DEPRECATION")
        Room.databaseBuilder(this, AppDatabase::class.java, "healthybite.db")
            .addCallback(PrepopulateCallback())  // seeds initial data on first install
            .fallbackToDestructiveMigration()     // wipe DB if schema changes with no migration
            .build()
    }

    /**
     * DataStore-backed session holding login state and current user ID.
     *
     * `by lazy` delays creation until first use. SessionDataStore wraps Jetpack DataStore
     * (Preferences) so the logged-in user ID and auth state survive app restarts without
     * hitting the database on every launch.
     */
    val sessionStore: SessionDataStore by lazy {
        SessionDataStore(sessionDataStore) // `sessionDataStore` is a Context extension property
    }

    /**
     * Auth operations (login, register, logout) backed by UserDao + SessionDataStore.
     *
     * `by lazy` is used here because AuthRepositoryImpl depends on `database` (also lazy).
     * If this were an eager `val`, the database would be forced to initialise immediately at
     * Application creation – defeating the purpose of lazy loading.
     */
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao(), sessionStore)
    }

    /**
     * DAO-level seeder that inserts demo/template data when the meal_templates table is empty.
     *
     * `by lazy` keeps this lightweight until the first screen actually needs seeded data.
     * The seeder itself checks whether data already exists before writing, so it is safe to
     * call multiple times.
     */
    val databaseSeeder: DatabaseSeeder by lazy {
        DatabaseSeeder(database)
    }
}
