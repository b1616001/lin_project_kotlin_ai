package com.example.lin_new_project_kotlin_ai.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.lin_new_project_kotlin_ai.room.Converters
import com.example.lin_new_project_kotlin_ai.room.data.FavoritesData


@Entity(
    tableName = FavoritesEntity.Companion.FAVORITES,
    primaryKeys = ["favoriteParentUid", "favoriteType"],
    indices = [Index("favoriteParentUid")],
    foreignKeys = [ForeignKey(
        entity = UserProfileEntity::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("favoriteParentUid"),
        onDelete = ForeignKey.CASCADE
    )]
) //CASCADE表示當上面資料表被刪除時，對應的關聯資料表全部刪除
class FavoritesEntity {
    @JvmField
    var favoriteName: String? = "" //名稱
    @JvmField
    var favoriteParentUid: Int = 0 //對應使用者的uid
    @JvmField
    var favoriteType: Int = 0 //Programs的code欄位
    @JvmField
    var isDefault: Boolean = false //用於首頁判斷是否需要我的最愛的符號

    @JvmField
    @TypeConverters(Converters::class) //借助裡面的JSONToUesrData方法進行UesrData轉換成字串寫入資料庫，UesrDataStringToJSON使用將JSON字串轉成UesrData物件
    var favoritesData: FavoritesData? = null

    fun getFavoritesData(): FavoritesData {
        return favoritesData!!
    }

    fun setFavoritesData(favoritesData: FavoritesData) {
        this.favoritesData = favoritesData
    }

    companion object {
        const val FAVORITES: String = "favorites"
    }
}
