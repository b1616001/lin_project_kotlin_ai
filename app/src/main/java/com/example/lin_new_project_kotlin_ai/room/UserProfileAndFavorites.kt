package com.example.lin_new_project_kotlin_ai.room

import androidx.room.Embedded
import androidx.room.Relation
import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity

class UserProfileAndFavorites {
    @Embedded
    var userProfileEntity: UserProfileEntity? = null

    @Relation(
        parentColumn = "uid",
        entityColumn = "favoriteParentUid",
        entity = FavoritesEntity::class
    )
    var favoritesEntityList: MutableList<FavoritesEntity?>? = null
}
