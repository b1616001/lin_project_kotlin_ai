package com.example.lin_new_project_kotlin_ai.room

 open class DatabaseCallback<T> {
     open fun onDataLoadedList(lists: MutableList<T?>?) {
    }

    fun onDataLoadedList(testEntity: T?) {
    }

    open fun onAdded(rowId: Long) {
    }

    fun onQueryAll() {
    }

    open fun onDeleted() {
    }

    fun onUpdated() {
    }

    fun onError(e: Throwable) {
    }

    open fun onError(e: String?) {
    }

    fun onCount(i: Int?) {
    }

    fun onNoData() {
    }
}
