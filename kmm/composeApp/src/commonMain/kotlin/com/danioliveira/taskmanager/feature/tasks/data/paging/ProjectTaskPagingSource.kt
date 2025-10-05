package com.danioliveira.taskmanager.feature.tasks.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.feature.tasks.data.mapper.toTask
import com.danioliveira.taskmanager.feature.tasks.data.network.TaskApiService
import kotlin.uuid.ExperimentalUuidApi

/**
 * PagingSource implementation for tasks in a specific project.
 *
 * @property apiService The API service for task operations
 * @property projectId The ID of the project
 */
class ProjectTaskPagingSource(
    private val apiService: TaskApiService,
    private val projectId: String
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
            val response = apiService.getTasksByProjectId(projectId, page, pageSize)

            val tasks = response.items.map { it.toTask() }

            LoadResult.Page(
                data = tasks,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (page < response.totalPages - 1) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}