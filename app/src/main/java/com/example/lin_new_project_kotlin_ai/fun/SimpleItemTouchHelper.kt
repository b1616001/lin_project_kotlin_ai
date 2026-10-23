package com.example.lin_new_project_kotlin_ai.`fun`

import androidx.recyclerview.widget.ItemTouchHelper

class SimpleItemTouchHelper
/**
 * Creates an ItemTouchHelper that will work with the given Callback.
 * 
 * 
 * You can attach ItemTouchHelper to a RecyclerView via
 * [.attachToRecyclerView]. Upon attaching, it will add an item decoration,
 * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
 * 
 * @param callback The Callback which controls the behavior of this touch helper.
 */
    (callback: Callback) : ItemTouchHelper(callback)
