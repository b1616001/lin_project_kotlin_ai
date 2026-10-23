package com.example.lin_new_project_kotlin_ai.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity
import com.example.lin_new_project_kotlin_ai.room.entity.HistoryEntity
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity


@Database(
    entities = [UserProfileEntity::class, FavoritesEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
abstract class DBDatabase : RoomDatabase() {
    abstract fun dbDao(): DBDao?


    companion object {
        //以下 此區塊式資料庫更新範本
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE test ADD COLUMN wattAccumulate REAL NOT NULL DEFAULT 0");
//            database.execSQL("ALTER TABLE test ADD COLUMN wattFrequency INTEGER NOT NULL DEFAULT 0");
//            database.execSQL("ALTER TABLE test ADD COLUMN workoutMonth INTEGER NOT NULL DEFAULT 0");
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //當添加int 類型數據時，需要添加默認值
//            database.execSQL("ALTER TABLE user_profile ADD COLUMN sleepMode INTEGER NOT NULL DEFAULT 0");
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE user_profile ADD COLUMN passCode TEXT");
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE user_profile ADD COLUMN passcodeOn INTEGER NOT NULL DEFAULT 0");
            }
        } //    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        //        @Override
        //        public void migrate(@NonNull SupportSQLiteDatabase database) {
        //            database.execSQL("ALTER TABLE expense ADD COLUMN itemName TEXT");
        //            database.execSQL("ALTER TABLE expense ADD COLUMN itemNo TEXT");
        //            database.execSQL("ALTER TABLE expense ADD COLUMN itemQuantity INTEGER NOT NULL DEFAULT 0");
        //            //當添加int 類型數據時，需要添加默認值
        //            database.execSQL("ALTER TABLE expense ADD COLUMN itemPrice INTEGER NOT NULL DEFAULT 0");
        //        }
        //    };
        //
        //    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        //        @Override
        //        public void migrate(@NonNull SupportSQLiteDatabase database) {
        //            //當添加int 類型數據時，需要添加默認值
        //            database.execSQL("ALTER TABLE expense ADD COLUMN itemTotal INTEGER NOT NULL DEFAULT 0");
        //        }
        //    };
    }
}
