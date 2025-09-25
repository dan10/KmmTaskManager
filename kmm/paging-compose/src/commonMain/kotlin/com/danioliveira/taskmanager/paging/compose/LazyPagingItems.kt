package com.danioliveira.taskmanager.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class LazyPagingItems<T : Any>
internal constructor(
    private val flow: Flow<PagingData<T>>
) {
    private val mainDispatcher = UiDispatcher


    private val pagingDataPresenter =
        object :
            PagingDataPresenter<T>(
                mainContext = mainDispatcher,
                cachedPagingData =
                    if (flow is SharedFlow<PagingData<T>>) flow.replayCache.firstOrNull() else null
            ) {
            override suspend fun presentPagingDataEvent(
                event: PagingDataEvent<T>,
            ) {
                updateItemSnapshotList()
            }
        }


    var itemSnapshotList by mutableStateOf(pagingDataPresenter.snapshot())
        private set

    /** The number of items which can be accessed. */
    val itemCount: Int
        get() = itemSnapshotList.size

    private fun updateItemSnapshotList() {
        itemSnapshotList = pagingDataPresenter.snapshot()
    }


    operator fun get(index: Int): T? {
        pagingDataPresenter[index] // this registers the value load
        return itemSnapshotList[index]
    }


    fun peek(index: Int): T? {
        return itemSnapshotList[index]
    }

    /**
     * Retry any failed load requests that would result in a [LoadState.Error] update to this
     * [LazyPagingItems].
     *
     * Unlike [refresh], this does not invalidate [PagingSource], it only retries failed loads
     * within the same generation of [PagingData].
     *
     * [LoadState.Error] can be generated from two types of load requests:
     * * [PagingSource.load] returning [PagingSource.LoadResult.Error]
     * * [RemoteMediator.load] returning [RemoteMediator.MediatorResult.Error]
     */
    fun retry() {
        pagingDataPresenter.retry()
    }

    /**
     * Refresh the data presented by this [LazyPagingItems].
     *
     * [refresh] triggers the creation of a new [PagingData] with a new instance of [PagingSource]
     * to represent an updated snapshot of the backing dataset. If a [RemoteMediator] is set,
     * calling [refresh] will also trigger a call to [RemoteMediator.load] with [LoadType] [REFRESH]
     * to allow [RemoteMediator] to check for updates to the dataset backing [PagingSource].
     *
     * Note: This API is intended for UI-driven refresh signals, such as swipe-to-refresh.
     * Invalidation due repository-layer signals, such as DB-updates, should instead use
     * [PagingSource.invalidate].
     *
     * @see PagingSource.invalidate
     */
    fun refresh() {
        pagingDataPresenter.refresh()
    }

    /** A [CombinedLoadStates] object which represents the current loading state. */
    var loadState: CombinedLoadStates by
    mutableStateOf(
        pagingDataPresenter.loadStateFlow.value
            ?: CombinedLoadStates(
                refresh = InitialLoadStates.refresh,
                prepend = InitialLoadStates.prepend,
                append = InitialLoadStates.append,
                source = InitialLoadStates
            )
    )
        private set

    internal suspend fun collectLoadState() {
        pagingDataPresenter.loadStateFlow.filterNotNull().collect { loadState = it }
    }

    internal suspend fun collectPagingData() {
        flow.collectLatest { pagingDataPresenter.collectFrom(it) }
    }
}

private val IncompleteLoadState = LoadState.NotLoading(false)
private val InitialLoadStates =
    LoadStates(LoadState.Loading, IncompleteLoadState, IncompleteLoadState)


@Composable
public fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
    context: CoroutineContext = EmptyCoroutineContext
): LazyPagingItems<T> {

    val lazyPagingItems = remember(this) { LazyPagingItems(this) }

    LaunchedEffect(lazyPagingItems) {
        if (context == EmptyCoroutineContext) {
            lazyPagingItems.collectPagingData()
        } else {
            withContext(context) { lazyPagingItems.collectPagingData() }
        }
    }

    LaunchedEffect(lazyPagingItems) {
        if (context == EmptyCoroutineContext) {
            lazyPagingItems.collectLoadState()
        } else {
            withContext(context) { lazyPagingItems.collectLoadState() }
        }
    }

    return lazyPagingItems
}