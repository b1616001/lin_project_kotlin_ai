package com.example.lin_new_project_kotlin_ai.room

import androidx.room.TypeConverter
import com.example.lin_new_project_kotlin_ai.room.data.FavoritesData
import com.example.lin_new_project_kotlin_ai.room.data.HistoryData
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.google.gson.Gson
import java.util.Date

object Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }

    @JvmStatic
    @TypeConverter
    fun UesrDataStringToJSON(userData: String?): UserData? {
        if (userData == null) {
            return UserData()
        } else {
            return Gson().fromJson<UserData?>(userData, UserData::class.java)
        }
    }

    @JvmStatic
    @TypeConverter
    fun JSONToUesrData(userData: UserData?): String? {
        if (userData == null) {
            return Gson().toJson(UserData())
        } else {
            return Gson().toJson(userData)
        }
    }

    @JvmStatic
    @TypeConverter
    fun HistoryDataStringToJSON(historyData: String?): HistoryData? {
        if (historyData == null) {
            return HistoryData()
        } else {
            return Gson().fromJson<HistoryData?>(historyData, HistoryData::class.java)
        }
    }

    @JvmStatic
    @TypeConverter
    fun JSONToHistoryData(historyData: HistoryData?): String? {
        if (historyData == null) {
            return Gson().toJson(HistoryData())
        } else {
            return Gson().toJson(historyData)
        }
    }


    @JvmStatic
    @TypeConverter
    fun FavoritesDataStringToJSON(favoritesData: String?): FavoritesData? {
        if (favoritesData == null) {
            return FavoritesData()
        } else {
            return Gson().fromJson<FavoritesData?>(favoritesData, FavoritesData::class.java)
        }
    }

    @JvmStatic
    @TypeConverter
    fun JSONToFavoritesData(favoritesData: FavoritesData?): String? {
        if (favoritesData == null) {
            return Gson().toJson(FavoritesData())
        } else {
            return Gson().toJson(favoritesData)
        }
    }
}