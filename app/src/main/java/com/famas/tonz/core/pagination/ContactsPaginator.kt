package com.famas.tonz.core.pagination


/**
 *  private val paginator = ContactsPaginator(
        initialKey = _homeScreenState.value.pageNumber,
        onLoadUpdated = {
            _homeScreenState.value = homeScreenState.value.copy(
                isLoading = it
            )
        },
        onRequest = { nextKey ->
            homeScreenRepository.getContacts(nextKey, pageSize, homeScreenState.value.searchValue)
        },
        getNextKey = {
            _homeScreenState.value.pageNumber + 1
        },
        onError = {
            _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.something_went_wrong)))
        },
        onSuccess = { items, newKey ->
            _homeScreenState.value = homeScreenState.value.copy(
                contacts = if (homeScreenState.value.pageNumber == 0) items else homeScreenState.value.contacts + items,
                                pageNumber = newKey,
                                endReached = items.isEmpty()
                )
        }
    )
 *
 * **/
class ContactsPaginator<Key, Item>(
    private val initialKey: Key,
    private inline val onLoadUpdated: (Boolean) -> Unit,
    private inline val onRequest: suspend (nextKey: Key) -> Result<List<Item>>,
    private inline val getNextKey: suspend (List<Item>) -> Key,
    private inline val onError: suspend (Throwable?) -> Unit,
    private inline val onSuccess: suspend (items: List<Item>, newKey: Key) -> Unit
): Paginator<Key, Item> {

    private var currentKey = initialKey
    private var isMakingRequest = false

    override suspend fun loadNextItems() {
        if (isMakingRequest) {
            return
        }
        isMakingRequest = true
        onLoadUpdated(true)
        val result = onRequest(currentKey)
        isMakingRequest = false
        val items = result.getOrElse {
            onError(it)
            onLoadUpdated(false)
            return
        }
        currentKey = getNextKey(items)
        onSuccess(items, currentKey)
        onLoadUpdated(false)
    }

    override fun reset() {
        currentKey = initialKey
    }
}