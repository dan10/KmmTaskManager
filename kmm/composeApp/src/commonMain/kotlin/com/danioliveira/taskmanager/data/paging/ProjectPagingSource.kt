package com.danioliveira.taskmanager.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.network.ProjectApiService
import com.danioliveira.taskmanager.domain.Project

/**
 * PagingSource implementation for projects.
 *
 * @property apiService The API service for project operations
 * @property query Optional query to filter projects by name
 */
class ProjectPagingSource(
    private val apiService: ProjectApiService,
    private val query: String?
) : PagingSource<Int, Project>() {

    override fun getRefreshKey(state: PagingState<Int, Project>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val response = apiService.getProjects(page, pageSize, query)

            val projects = response.items.map { it.toDomainModel() }

            LoadResult.Page(
                data = projects,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (page < response.totalPages - 1) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun ProjectResponse.toDomainModel(): Project {
        return Project(
            id = id,
            name = name,
            completed = completed,
            inProgress = inProgress,
            total = total,
            description = description
        )
    }
}
