package com.android.sanwei.uikit.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil

open class CommonDiffCallback<T> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return true
    }

}