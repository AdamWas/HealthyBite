package pl.akp.healthybite.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class PrepopulateCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL(
            """
            INSERT INTO users (email, password, displayName)
            VALUES ('demo@healthy.pl', 'zaq1@WSX', 'Demo User')
            """.trimIndent()
        )
    }
}
