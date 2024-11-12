package com.famas.tonz.core.pagination

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}