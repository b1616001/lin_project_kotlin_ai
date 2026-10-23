package com.example.lin_new_project_kotlin_ai.room;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;


import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity;
import com.example.lin_new_project_kotlin_ai.room.entity.HistoryEntity;
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface DBDao {
    @Query("select * from " + UserProfileEntity.USER_PROFILE)
    Maybe<List<UserProfileEntity>> getUserProfilesALL();//搜尋所有使用者

    @Query("select * from " + HistoryEntity.HISTORY)
    Maybe<List<HistoryEntity>> getHistoryEntityALL();//搜尋所有歷史資料(HistoryEntity)
    //Date改成存String型別
    @Query("SELECT * FROM " + HistoryEntity.HISTORY + " WHERE historyParentUid=:historyParentUid ")
    Maybe<List<HistoryEntity>> getHistoryList(int historyParentUid);
    @Query("select * from " + FavoritesEntity.FAVORITES)
    Maybe<List<FavoritesEntity>> getFavoritesEntityALL();//搜尋所有我的最愛資料(FavoritesEntity)



    @Query("select * from " + UserProfileEntity.USER_PROFILE + " where uid = :uid")
    Maybe<UserProfileEntity> getUserProfileEntity(int uid);

    @Query("select * from " + HistoryEntity.HISTORY + " where historyParentUid = :historyParentUid")
    Maybe<List<HistoryEntity>> getHistoryEntity(int historyParentUid);

    @Query("select * from " + FavoritesEntity.FAVORITES + " where favoriteParentUid = :favoriteParentUid")
    Maybe<List<FavoritesEntity>> getFavoritesEntity(int favoriteParentUid);



    @Query("select * from " + HistoryEntity.HISTORY + " where uid = :uid")
    Maybe<HistoryEntity> getHistoryEntity_Uid(int uid);

    @Query("select * from " + FavoritesEntity.FAVORITES + " where favoriteParentUid = :favoriteParentUid" +
            " AND favoriteType = :favoriteType")
    Maybe<FavoritesEntity> getFavoritesEntity(int favoriteParentUid, int favoriteType);



    //取得Favorites的列表
    @Transaction
    @Query("SELECT * FROM " + UserProfileEntity.USER_PROFILE + " WHERE uid=:id")
    Maybe<List<UserProfileAndFavorites>> getFavoriteForUserProfile(int id);
    /**
     * Template
     */

    @Insert(onConflict = REPLACE)
    long insertUserProfileEntity(UserProfileEntity userProfileEntity);
    @Insert(onConflict = REPLACE)
    long insertHistoryEntity(HistoryEntity historyEntity);
    @Insert(onConflict = REPLACE)
    long insertFavoritesEntity(FavoritesEntity favorites);

    @Update
    void updateUserProfile(UserProfileEntity userProfileEntity);
    @Update
    void updateHistoryEntity(HistoryEntity historyEntity);
    @Update
    void updateFavoritesEntity(FavoritesEntity favorites);

    @Delete
    void deleteUserProfile(UserProfileEntity userProfileEntity);
    @Delete
    void deleteHistoryEntity(HistoryEntity historyEntity);
    @Delete
    void deleteFavoritesEntity(FavoritesEntity favorites);



}
