package com.example.lin_new_project_kotlin_ai.room

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity
import com.example.lin_new_project_kotlin_ai.room.entity.HistoryEntity
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicLong


class DatabaseManager(context: Context) {
    private val db: DBDatabase
    private val compositeDisposable = CompositeDisposable()
    private var mDisposable: Disposable? = null
    private val TAG = "Room資料庫存取"

    init {
        db = Room.databaseBuilder<DBDatabase>(context, DBDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration() //清空，如果版本升級卻無addMigrations則將會刪除資料庫並重建
            //                .addMigrations(MIGRATION_1_2) //升級
            //                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            //                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            //                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    fun clearTable(databaseCallback: DatabaseCallback<*>) {
        Completable.fromAction(Action { db.clearAllTables() })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onComplete() {
                    databaseCallback.onDeleted()
                }

                override fun onError(e: Throwable) {
                    databaseCallback.onError(e.message)
                }
            })
    }

    fun roomClear() {
        //acivity或Fragment销毁的时候，调用compositeDisposable.dispose()就可以切断所有订阅事件，防止内存泄漏。
        Log.d(TAG, "roomClear,Size:" + compositeDisposable.size())
        compositeDisposable.clear() //一次清除
    }

    fun cancel() {
        Log.d(TAG, "cancel")
        if (mDisposable != null && !mDisposable!!.isDisposed()) {
            mDisposable!!.dispose() //單一清除，比較適用於執行完某件事情後就結束，並不需要回傳資料的
        }
    }


    //這一區塊是處理UserProfiles----------------------------------------------------------------------
    fun getUserProfilesALL(databaseCallback: DatabaseCallback<UserProfileEntity?>) {
        Log.d(TAG, "getUserProfilesALL")
        val d = db.dbDao()!!
            .userProfilesALL
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(
                Consumer { lists: MutableList<UserProfileEntity?>? ->
                    databaseCallback.onDataLoadedList(lists)
                },
                Consumer { throwable: Throwable ->
                    Log.e(TAG, "getUserProfilesALL error: " + throwable.message)
                    databaseCallback.onError(throwable.message)
                }
            )

        compositeDisposable.add(d)
    }

    fun getUserProfiles(uid: Int, databaseCallback: DatabaseCallback<UserProfileEntity?>) {
        Log.d(TAG, "getUserProfiles")
        val d = db.dbDao()!!
            .getUserProfileEntity(uid)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(
                Consumer { testEntity: UserProfileEntity? ->
                    databaseCallback.onDataLoadedList(
                        testEntity
                    )
                },
                Consumer { throwable: Throwable ->
                    Log.e(TAG, "getUserProfiles error: " + throwable.message)
                    databaseCallback.onError(throwable.message)
                }
            )
        compositeDisposable.add(d)
    }

    fun insertUserProfileEntity(
        userProfileEntity: UserProfileEntity?,
        databaseCallback: DatabaseCallback<UserProfileEntity?>) {
        val rowId = AtomicLong()
        Completable
            .fromAction(Action {
                rowId.set(
                    db.dbDao()!!.insertUserProfileEntity(userProfileEntity)
                )
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                    Log.d(TAG, "onSubscribe ")
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete ")
                    databaseCallback.onAdded(rowId.get())
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError " + e.message)
                    databaseCallback.onError(e.message)
                }
            })
    }

    fun updateUserProfile(
        userProfileEntity: UserProfileEntity?,
        callback: DatabaseCallback<UserProfileEntity?>
    ) {
        Completable.fromAction(Action { db.dbDao()!!.updateUserProfile(userProfileEntity) })
            .subscribeOn(
                Schedulers.io()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    mDisposable = d
                    Log.d(TAG, "onSubscribe: ")
                }

                override fun onComplete() {
                    callback.onUpdated()
                    Log.d(TAG, "onComplete: ")
                    cancel()
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onSubscribe: ")
                    callback.onError(e.message)
                }
            })
    }

    fun deleteUserProfile(
        userProfileEntity: UserProfileEntity?,
        databaseCallback: DatabaseCallback<UserProfileEntity?>
    ) {
        Completable
            .fromAction(Action { db.dbDao()!!.deleteUserProfile(userProfileEntity) })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onComplete() {
                    databaseCallback.onDeleted()
                }

                override fun onError(e: Throwable) {
                    databaseCallback.onError(e.message)
                }
            })
    }

    //----------------------------------------------------------------------------------------------
    //這一區塊是處理HistoryEntity----------------------------------------------------------------------
    fun getHistoryEntityALL(databaseCallback: DatabaseCallback<HistoryEntity?>) {
        Log.d(TAG, "getHistoryEntityALL")
        val d = db.dbDao()!!
            .historyEntityALL!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { lists: MutableList<HistoryEntity?>? ->
                databaseCallback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    fun getHistoryList(
        historyParentUid: Int,
        callback: DatabaseCallback<HistoryEntity?>
    ) {
        val d = db.dbDao()!!
            .getHistoryList(historyParentUid)!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(Consumer { lists: MutableList<HistoryEntity?>? ->
                callback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    fun getHistoryEntity(
        historyParentUid: Int,
        databaseCallback: DatabaseCallback<HistoryEntity?>
    ) {
        Log.d(TAG, "getHistoryEntity")
        val d = db.dbDao()!!
            .getHistoryEntity(historyParentUid)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { lists: MutableList<HistoryEntity?>? ->
                databaseCallback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    fun getHistoryEntity_Uid(uid: Int, databaseCallback: DatabaseCallback<HistoryEntity?>) {
        Log.d(TAG, "getHistoryEntity_Uid")
        val d = db.dbDao()!!
            .getHistoryEntity_Uid(uid)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { testEntity: HistoryEntity? ->
                databaseCallback.onDataLoadedList(
                    testEntity
                )
            })

        compositeDisposable.add(d)
    }

    fun insertHistoryEntity(
        historyEntity: HistoryEntity?,
        databaseCallback: DatabaseCallback<HistoryEntity?>
    ) {
        //這裡使用Completable是因為數據庫處理完後不發射數據，只處理onComplete和onError事件
        //Completable
        //適合用在執行的內容沒有回傳值，只要知道成功或失敗就好的時候，
        //例如更新個人資料。在callback有onComplete()和onError(Throwable e)兩個方法，
        //其中onComplete()是沒有參數的表示執行完不會有回傳值。
        //建立我們要執行的內容。
        val rowId = AtomicLong()
        Completable
            .fromAction(Action { rowId.set(db.dbDao()!!.insertHistoryEntity(historyEntity)) })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                    Log.d(TAG, "onSubscribe ")
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete ")
                    databaseCallback.onAdded(rowId.get())
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError ")
                    databaseCallback.onError(e.message)
                }
            })
    }

    fun updateHistoryEntity(
        historyEntity: HistoryEntity?,
        callback: DatabaseCallback<HistoryEntity?>
    ) {
        Completable.fromAction(Action { db.dbDao()!!.updateHistoryEntity(historyEntity) })
            .subscribeOn(
                Schedulers.io()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    mDisposable = d
                    Log.d(TAG, "onSubscribe: ")
                }

                override fun onComplete() {
                    callback.onUpdated()
                    Log.d(TAG, "onComplete: ")
                    cancel()
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onSubscribe: ")
                    callback.onError(e.message)
                }
            })
    }

    fun deleteHistoryEntity(
        historyEntity: HistoryEntity?,
        databaseCallback: DatabaseCallback<HistoryEntity?>
    ) {
        Completable
            .fromAction(Action { db.dbDao()!!.deleteHistoryEntity(historyEntity) })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onComplete() {
                    databaseCallback.onDeleted()
                }

                override fun onError(e: Throwable) {
                    databaseCallback.onError(e.message)
                }
            })
    }

    //----------------------------------------------------------------------------------------------
    //這一區塊是處理FavoritesEntity----------------------------------------------------------------------
    fun getFavoritesEntityALL(databaseCallback: DatabaseCallback<FavoritesEntity?>) {
        Log.d(TAG, "getFavoritesEntityALL")
        val d = db.dbDao()!!
            .favoritesEntityALL!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { lists: MutableList<FavoritesEntity?>? ->
                databaseCallback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    //從User的id對應到Favorite的id,取得Favorite的表
    fun getFavoriteFromUserProfile(
        id: Int,
        callback: DatabaseCallback<UserProfileAndFavorites?>
    ) {
        val d = db.dbDao()!!
            .getFavoriteForUserProfile(id)!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(Consumer { lists: MutableList<UserProfileAndFavorites?>? ->
                callback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    fun getFavoritesEntity(
        favoriteParentUid: Int,
        databaseCallback: DatabaseCallback<FavoritesEntity?>
    ) {
        Log.d(TAG, "getFavoritesEntity")
        val d = db.dbDao()!!
            .getFavoritesEntity(favoriteParentUid)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { lists: MutableList<FavoritesEntity?>? ->
                databaseCallback.onDataLoadedList(
                    lists
                )
            })

        compositeDisposable.add(d)
    }

    fun getFavoritesEntity(
        favoriteParentUid: Int, favoriteType: Int,
        databaseCallback: DatabaseCallback<FavoritesEntity?>
    ) {
        Log.d(TAG, "getFavoritesEntity2")
        val d = db.dbDao()!!
            .getFavoritesEntity(favoriteParentUid, favoriteType)!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Consumer { throwable: Throwable? ->
                Log.d(TAG, "Throwable" + throwable!!.message)
            })
            .subscribe(Consumer { testEntity: FavoritesEntity? ->
                databaseCallback.onDataLoadedList(
                    testEntity
                )
            })

        compositeDisposable.add(d)
    }

    fun insertFavoritesEntity(
        favoritesEntity: FavoritesEntity?,
        databaseCallback: DatabaseCallback<FavoritesEntity?>
    ) {
        //這裡使用Completable是因為數據庫處理完後不發射數據，只處理onComplete和onError事件
        //Completable
        //適合用在執行的內容沒有回傳值，只要知道成功或失敗就好的時候，
        //例如更新個人資料。在callback有onComplete()和onError(Throwable e)兩個方法，
        //其中onComplete()是沒有參數的表示執行完不會有回傳值。
        //建立我們要執行的內容。
        val rowId = AtomicLong()
        Completable
            .fromAction(Action { rowId.set(db.dbDao()!!.insertFavoritesEntity(favoritesEntity)) })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                    Log.d(TAG, "onSubscribe ")
                }

                override fun onComplete() {
                    Log.d(TAG, "onComplete ")
                    databaseCallback.onAdded(rowId.get())
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError ")
                    databaseCallback.onError(e.message)
                }
            })
    }

    fun updateFavoritesEntity(
        favoritesEntity: FavoritesEntity?,
        callback: DatabaseCallback<FavoritesEntity?>
    ) {
        Completable.fromAction(Action { db.dbDao()!!.updateFavoritesEntity(favoritesEntity) })
            .subscribeOn(
                Schedulers.io()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    mDisposable = d
                    Log.d(TAG, "onSubscribe: ")
                }

                override fun onComplete() {
                    callback.onUpdated()
                    Log.d(TAG, "onComplete: ")
                    cancel()
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onSubscribe: ")
                    callback.onError(e.message)
                }
            })
    }

    fun deleteFavoritesEntity(
        favoritesEntity: FavoritesEntity?,
        databaseCallback: DatabaseCallback<FavoritesEntity?>
    ) {
        Completable
            .fromAction(Action { db.dbDao()!!.deleteFavoritesEntity(favoritesEntity) })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onComplete() {
                    databaseCallback.onDeleted()
                }

                override fun onError(e: Throwable) {
                    databaseCallback.onError(e.message)
                }
            })
    } //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------


    companion object {
        //    收合/展開所有程式碼區塊	Control+Shift+減號或 Control+Shift+加號
        private var instance: DatabaseManager? = null
        private const val DB_NAME = "lin_database"

        @JvmStatic
        fun getInstance(context: Context): DatabaseManager {
            if (instance == null) {
                synchronized(DBDatabase::class.java) {
                    if (instance == null) {
                        instance = DatabaseManager(context)
                    }
                }
            }
            return instance!!
        }
    }
}
