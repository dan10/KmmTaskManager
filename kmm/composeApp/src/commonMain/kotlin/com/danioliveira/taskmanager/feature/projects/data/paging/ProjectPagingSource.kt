package com.danioliveira.taskmanager.feature.projects.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danioliveira.taskmanager.core.domain.model.Project
import com.danioliveira.taskmanager.feature.projects.data.mapper.toProject
import com.danioliveira.taskmanager.feature.projects.data.network.ProjectApiService

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

            val projects = response.items.map { it.toProject() }

            LoadResult.Page(
                data = projects,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (page < response.totalPages - 1) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
