package com.agrirakshak.drone.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {ScanEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ScanDao scanDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "agrirakshak_database"
                            )
                            .addMigrations(new Migration(1, 2) {
                                @Override
                                public void migrate(
                                        SupportSQLiteDatabase database
                                ) {

                                    database.execSQL(
                                            "ALTER TABLE scans ADD COLUMN latitude REAL NOT NULL DEFAULT 0"
                                    );

                                    database.execSQL(
                                            "ALTER TABLE scans ADD COLUMN longitude REAL NOT NULL DEFAULT 0"
                                    );
                                }
                            })
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}