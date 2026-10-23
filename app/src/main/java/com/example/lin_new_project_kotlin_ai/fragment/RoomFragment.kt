package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.MyApplication.mDatabaseManager
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.adapter.UserAdapter
import com.example.lin_new_project_kotlin_ai.databinding.FragmentRoomBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.RecyclerViewMethed
import com.example.lin_new_project_kotlin_ai.room.DatabaseCallback
import com.example.lin_new_project_kotlin_ai.room.data.FavoritesData
import com.example.lin_new_project_kotlin_ai.room.data.HistoryData
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.example.lin_new_project_kotlin_ai.room.entity.FavoritesEntity
import com.example.lin_new_project_kotlin_ai.room.entity.HistoryEntity
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.google.gson.Gson

class RoomFragment : BaseBindingFragment<FragmentRoomBinding?>() {
    private var userProfileEntity: UserProfileEntity? = null
    private var userAdapter: UserAdapter? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentRoomBinding {
        return FragmentRoomBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getUserProfileEntityALL()
    }

    private fun initView() {
        RecyclerViewMethed.init_VERTICAL(
            activity, getBinding()?.recyclerView,
            true, 0, CommonUtils.getColorByVERSION(R.color.white),
            true, true
        )
        userAdapter = UserAdapter(context, ArrayList())
        userAdapter?.setOnItemClickListener(object : UserAdapter.OnItemClickListener {
            override fun OnItemClick(userProfile: UserProfileEntity?) {
                userProfileEntity = userProfile
                getBinding()?.apply {
                    btnDelete.visibility = View.VISIBLE
                    btnInsertFavorites.visibility = View.VISIBLE
                    btnInsertHistory.visibility = View.VISIBLE
                    tvUser.visibility = View.VISIBLE
                    tvUser.text = "已選中 ID:${userProfileEntity?.uid} -> ${Gson().toJson(userProfileEntity)}"
                    recyclerView.visibility = View.VISIBLE
                }
            }
        })
        getBinding()?.recyclerView?.adapter = userAdapter
        getBinding()?.btnInsert?.setOnClickListener { insertUserProfileEntity() }
        getBinding()?.btnQuery?.setOnClickListener { getUserProfileEntityALL() }
        getBinding()?.btnDelete?.setOnClickListener { deleteUserProfile() }
        getBinding()?.btnClearTable?.setOnClickListener { clearTable() }
        getBinding()?.btnInsertFavorites?.setOnClickListener { insertFavoritesEntity() }
        getBinding()?.btnInsertHistory?.setOnClickListener { insertHistoryEntity() }
    }

    private fun insertUserProfileEntity() {
        val entity = UserProfileEntity()
        val userData = UserData()
        val randomNum = (100..999).random()
        userData.userName = "User_$randomNum"
        userData.userType = (0..2).random()
        userData.imageName = "img_$randomNum"
        entity.userData = userData
        mDatabaseManager().insertUserProfileEntity(
            entity,
            object : DatabaseCallback<UserProfileEntity?>() {
                override fun onAdded(rowId: Long) {
                    super.onAdded(rowId)
                    setLog("新增使用者資料,onAdded,rowId:$rowId")
                    showToast("新增使用者成功 (ID: $rowId)")
                    getUserProfileEntityALL()
                }

                override fun onError(e: String?) {
                    super.onError(e)
                    setLog("insertUserProfileEntity onError: $e")
                    showToast("新增失敗: $e")
                }
            })
    }

    private fun getUserProfileEntityALL() {
        mDatabaseManager().getUserProfilesALL(object : DatabaseCallback<UserProfileEntity?>() {
            override fun onDataLoadedList(lists: MutableList<UserProfileEntity?>?) {
                super.onDataLoadedList(lists)
                val count = lists?.size ?: 0
                setLog("讀取UserProfileEntityALL,onDataLoadedList,size:$count")
                if (lists != null) {
                    for (i in lists.indices) {
                        val userProfile = lists[i]
                        setLog("使用者資料:" + Gson().toJson(userProfile))
                    }
                    userAdapter?.setArray(lists)
                } else {
                    userAdapter?.setArray(ArrayList())
                }
                getBinding()?.recyclerView?.visibility = View.VISIBLE
                showToast("查詢完成，共 $count 筆資料")
            }

            override fun onError(e: String?) {
                super.onError(e)
                setLog("getUserProfileEntityALL onError: $e")
                showToast("查詢失敗: $e")
            }
        })
    }

    private fun deleteUserProfile() {
        val target = userProfileEntity ?: run {
            showToast("請先點選欲刪除之使用者")
            return
        }
        mDatabaseManager().deleteUserProfile(target, object : DatabaseCallback<UserProfileEntity?>() {
            override fun onDeleted() {
                super.onDeleted()
                userProfileEntity = null
                getBinding()?.apply {
                    btnDelete.visibility = View.GONE
                    btnInsertFavorites.visibility = View.GONE
                    btnInsertHistory.visibility = View.GONE
                    tvUser.visibility = View.GONE
                    tvUser.text = ""
                }
                setLog("onDeleted")
                showToast("刪除成功")
                getUserProfileEntityALL()
            }

            override fun onError(e: String?) {
                super.onError(e)
                setLog("deleteUserProfile onError: $e")
                showToast("刪除失敗: $e")
            }
        })
    }

    private fun insertHistoryEntity() {
        val user = userProfileEntity ?: run {
            showToast("請先點選使用者")
            return
        }
        val historyEntity = HistoryEntity()
        historyEntity.historyParentUid = user.uid
        val historyData = HistoryData()
        historyData.historyName = "History_${System.currentTimeMillis() % 1000}"
        historyData.programId = 2026
        historyEntity.historyData = historyData
        mDatabaseManager().insertHistoryEntity(historyEntity, object : DatabaseCallback<HistoryEntity?>() {
            override fun onAdded(rowId: Long) {
                super.onAdded(rowId)
                setLog("新增歷史紀錄,onAdded,rowId:$rowId")
                showToast("新增歷史紀錄成功")
            }

            override fun onError(e: String?) {
                super.onError(e)
                setLog("insertHistoryEntity onError: $e")
                showToast("新增歷史紀錄失敗: $e")
            }
        })
    }

    private fun insertFavoritesEntity() {
        val user = userProfileEntity ?: run {
            showToast("請先點選使用者")
            return
        }
        val favoritesEntity = FavoritesEntity()
        favoritesEntity.favoriteParentUid = user.uid
        favoritesEntity.favoriteName = "Fav_${user.uid}"
        favoritesEntity.favoriteType = (1..100).random()
        val favoritesData = FavoritesData()
        favoritesData.test = "Favorite for User ${user.uid}"
        favoritesEntity.favoritesData = favoritesData
        mDatabaseManager().insertFavoritesEntity(favoritesEntity, object : DatabaseCallback<FavoritesEntity?>() {
            override fun onAdded(rowId: Long) {
                super.onAdded(rowId)
                setLog("新增我的最愛,onAdded,rowId:$rowId")
                showToast("新增我的最愛成功")
            }

            override fun onError(e: String?) {
                super.onError(e)
                setLog("insertFavoritesEntity onError: $e")
                showToast("新增我的最愛失敗: $e")
            }
        })
    }

    private fun clearTable() {
        mDatabaseManager().clearTable(object : DatabaseCallback<Any?>() {
            override fun onDeleted() {
                userProfileEntity = null
                getBinding()?.apply {
                    btnDelete.visibility = View.GONE
                    btnInsertFavorites.visibility = View.GONE
                    btnInsertHistory.visibility = View.GONE
                    tvUser.visibility = View.GONE
                    tvUser.text = ""
                }
                userAdapter?.setArray(ArrayList())
                setLog("清空資料庫資料,onDeleted:清空完成")
                showToast("清空資料庫完成")
            }

            override fun onError(e: String?) {
                super.onError(e)
                setLog("clearTable onError: $e")
                showToast("清空失敗: $e")
            }
        })
    }

    override fun onDestroyView() {
        mDatabaseManager().roomClear()
        super.onDestroyView()
    }
}