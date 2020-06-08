package com.android.sanwei.uikit.recyclerview.diff

interface ListChangeListener<T> {
    fun onCurrentListChanged(previousList: List<T>, currentList: List<T>)
}