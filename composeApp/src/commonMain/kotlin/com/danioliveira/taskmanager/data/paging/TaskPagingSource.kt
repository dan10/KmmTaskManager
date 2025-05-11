package com.danioliveira.taskmanager.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.network.TaskApiService
import com.danioliveira.taskmanager.domain.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * PagingSource implementation for tasks.
 *
 * @property apiService The API service for task operations
 */
class TaskPagingSource(
    private val apiService: TaskApiService,
    private val query: String?
) : PagingSource<Int, Task>() {

    override fun getRefreshKey(state: PagingState<Int, Task>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Task> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val response = apiService.getTasks(page, pageSize, query)

            val tasks = response.items.map { it.toDomainModel() }

            LoadResult.Page(
                data = tasks,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (page < response.totalPages - 1) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun TaskResponse.toDomainModel(): Task {
        return Task(
            id = Uuid.parse(id),
            title = title,
            description = description,
            projectName = projectId,
            status = status,
            priority = priority,
            dueDate = dueDate
        )
    }
}
