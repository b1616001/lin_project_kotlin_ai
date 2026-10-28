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
import com.example.lin_new_project_kotlin_ai.popupwindow.PopupWindowManager
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

        // 點擊列表項目選中
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

        // 列表中單獨刪除單一資料
        userAdapter?.setOnDeleteClickListener(object : UserAdapter.OnDeleteClickListener {
            override fun onDeleteClick(userProfile: UserProfileEntity?, position: Int) {
                val target = userProfile ?: return
                val userName = target.userData?.userName ?: "未命名"
                PopupWindowManager.showDeleteConfirmPopupWindow(
                    requireActivity(),
                    "確認刪除帳號",
                    "確定要刪除第 ${position + 1} 項帳號嗎？\nID: ${target.uid}\n名稱: $userName"
                ) {
                    performDeleteUserProfile(target)
                }
            }
        })

        getBinding()?.recyclerView?.adapter = userAdapter

        // 新增帳號：點擊跳出 PopupWindow，繼承 BasePopupWindow，透過 PopupWindowManager 跳出
        getBinding()?.btnInsert?.setOnClickListener {
            PopupWindowManager.showAddAccountPopupWindow(requireActivity()) { userData ->
                insertUserProfile(userData)
            }
        }

        getBinding()?.btnQuery?.setOnClickListener { getUserProfileEntityALL() }

        // 刪除選中的帳號：跳出 PopupWindow 確認
        getBinding()?.btnDelete?.setOnClickListener {
            val target = userProfileEntity ?: run {
                showToast("請先點選欲刪除之使用者")
                return@setOnClickListener
            }
            val userName = target.userData?.userName ?: "未命名"
            PopupWindowManager.showDeleteConfirmPopupWindow(
                requireActivity(),
                "確認刪除帳號",
                "確定要刪除選中的帳號資料嗎？\nID: ${target.uid}\n名稱: $userName"
            ) {
                performDeleteUserProfile(target)
            }
        }

        // 清空資料庫：也跳出確認 PopupWindow 確保安全
        getBinding()?.btnClearTable?.setOnClickListener {
            PopupWindowManager.showDeleteConfirmPopupWindow(
                requireActivity(),
                "確認清空資料庫",
                "確定要清空所有資料庫帳號資料嗎？此操作無法復原！"
            ) {
                clearTable()
            }
        }

        getBinding()?.btnInsertFavorites?.setOnClickListener { insertFavoritesEntity() }
        getBinding()?.btnInsertHistory?.setOnClickListener { insertHistoryEntity() }
    }

    private fun insertUserProfile(userData: UserData) {
        val entity = UserProfileEntity().apply {
            this.userData = userData
            this.createTime = System.currentTimeMillis()
            this.note = "AutoMigration 建立"
            this.textname = "TN_${userData.userName}"
        }
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

    private fun performDeleteUserProfile(target: UserProfileEntity) {
        mDatabaseManager().deleteUserProfile(target, object : DatabaseCallback<UserProfileEntity?>() {
            override fun onDeleted() {
                super.onDeleted()
                if (userProfileEntity?.uid == target.uid) {
                    userProfileEntity = null
                    getBinding()?.apply {
                        btnDelete.visibility = View.GONE
                        btnInsertFavorites.visibility = View.GONE
                        btnInsertHistory.visibility = View.GONE
                        tvUser.visibility = View.GONE
                        tvUser.text = ""
                    }
                }
                setLog("onDeleted, target ID: ${target.uid}")
                showToast("刪除成功 (ID: ${target.uid})")
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
        PopupWindowManager.dismissCurrentPopup()
        mDatabaseManager().roomClear()
        super.onDestroyView()
    }
}