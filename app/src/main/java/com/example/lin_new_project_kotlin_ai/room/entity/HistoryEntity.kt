package com.example.lin_new_project_kotlin_ai.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lin_new_project_kotlin_ai.room.Converters
import com.example.lin_new_project_kotlin_ai.room.data.HistoryData
import java.io.Serializable

@Entity(
    tableName = HistoryEntity.Companion.HISTORY,
    indices = [Index("historyParentUid")],
    foreignKeys = [ForeignKey(
        entity = UserProfileEntity::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("historyParentUid"),
        onDelete = ForeignKey.CASCADE
    )]
) //CASCADE表示當上面資料表被刪除時，對應的關聯資料表全部刪除
class HistoryEntity : Serializable {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    @JvmField
    var historyParentUid: Int = 0 //儲存user_profile資料表的uid

    @JvmField
    @TypeConverters(Converters::class) //借助裡面的JSONToHistoryData方法進行UesrData轉換成字串寫入資料庫，TemplateDataStringToJSON使用將JSON字串轉成UesrData物件
    var historyData: HistoryData? = null

    companion object {
        const val HISTORY: String = "history"
    }
}