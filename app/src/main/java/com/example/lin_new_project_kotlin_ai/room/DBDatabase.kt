package com.example.lin_new_project_kotlin_ai.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity
import com.example.lin_new_project_kotlin_ai.room.entity.HistoryEntity
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity

/**
 * Room AutoMigration 資料庫配置
 *
 * 【AutoMigration 自動遷移機制與使用說明】：
 *
 * 1. 【新增欄位 (Add Column)】：
 *    - 當 Entity 新增欄位時，若是 Nullable（例如 var note: String? = null）或有設定預設值
 *      （例如 @ColumnInfo(defaultValue = "0") var createTime: Long = 0L），
 *    - Room 可自動辨識並產生 ALTER TABLE ADD COLUMN 語法，不需要額外撰寫 Spec。
 *    - 範例：從 Version 1 遷移至 Version 2：AutoMigration(from = 1, to = 2)
 *
 * 2. 【重新命名欄位 (Rename Column)】：
 *    - 因為 SQLite / Room 無法自動推斷是「刪除舊欄位並建立新欄位」還是「更名」，
 *    - 因此需透過 @RenameColumn 指定原欄位名與新欄位名，搭配 AutoMigrationSpec。
 *    - 範例：從 Version 2 遷移至 Version 3：將 user_profile 的 memo 欄位更名為 note 欄位。
 *
 * 3. 【刪除欄位 (Delete Column)】：
 *    - 在 Spec 加上 @DeleteColumn(tableName = "...", columnName = "...")，Room 自動處理。
 *
 * 4. 【重新命名資料表 (Rename Table)】：
 *    - 在 Spec 加上 @RenameTable(fromTableName = "...", toTableName = "...")，Room 自動處理。
 */
@Database(
    entities = [UserProfileEntity::class, FavoritesEntity::class, HistoryEntity::class],
    version = 4,
    autoMigrations = [
        // v1 -> v2: 自動新增 createTime 與 memo 欄位 (自動處理，無需 Spec)
        AutoMigration(from = 1, to = 2),
        // v2 -> v3: 自動將 user_profile 表的 memo 欄位更名為 note 欄位 (透過 @RenameColumn Spec)
        AutoMigration(from = 2, to = 3, spec = DBDatabase.AutoMigration2To3Spec::class),
        // v3 -> v4: 自動新增 textname 欄位 (自動處理，無需 Spec)
        AutoMigration(from = 3, to = 4)
    ],
    exportSchema = true
)
@TypeConverters(
    Converters::class
)
abstract class DBDatabase : RoomDatabase() {
    abstract fun dbDao(): DBDao?

    /**
     * v2 -> v3 的欄位更名 Spec
     * 讓 Room 自動執行欄位更名 (memo -> note)，保留舊有所有資料
     */
    @RenameColumn(
        tableName = UserProfileEntity.USER_PROFILE,
        fromColumnName = "memo",
        toColumnName = "note"
    )
    class AutoMigration2To3Spec : AutoMigrationSpec
}
