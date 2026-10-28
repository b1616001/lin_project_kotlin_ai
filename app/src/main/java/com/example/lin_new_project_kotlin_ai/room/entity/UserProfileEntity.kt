package com.example.lin_new_project_kotlin_ai.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lin_new_project_kotlin_ai.room.Converters
import com.example.lin_new_project_kotlin_ai.room.data.UserData


@Entity(tableName = UserProfileEntity.USER_PROFILE, indices = [Index("uid")]) //indices 索引專用的資料欄
class UserProfileEntity {
    companion object {
        //@Ignore 說明如果用加上這個 代表資料表不會有此欄位
        const val USER_PROFILE: String = "user_profile"
    }
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0 //主鍵

    @JvmField
    @TypeConverters(Converters::class) //借助裡面的JSONToUesrData方法進行UesrData轉換成字串寫入資料庫，UesrDataStringToJSON使用將JSON字串轉成UesrData物件
    var userData: UserData? = null

    @JvmField
    @androidx.room.ColumnInfo(defaultValue = "0")
    var createTime: Long = 0L

    @JvmField
    @androidx.room.ColumnInfo(name = "note")
    var note: String? = null

    @JvmField
    @androidx.room.ColumnInfo(name = "textname")
    var textname: String? = null

}
