package pl.akp.healthybite

import android.app.Application
import androidx.room.Room
import pl.akp.healthybite.data.datastore.SessionDataStore
import pl.akp.healthybite.data.datastore.sessionDataStore
import pl.akp.healthybite.data.db.AppDatabase
import pl.akp.healthybite.data.db.PrepopulateCallback
import pl.akp.healthybite.data.repository.AuthRepositoryImpl
import pl.akp.healthybite.domain.repository.AuthRepository

class HealthyBiteApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "healthybite.db")
            .addCallback(PrepopulateCallback())
            .build()
    }

    val sessionStore: SessionDataStore by lazy {
        SessionDataStore(sessionDataStore)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao(), sessionStore)
    }
}
