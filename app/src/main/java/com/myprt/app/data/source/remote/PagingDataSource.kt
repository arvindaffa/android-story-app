package com.myprt.app.data.source.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.myprt.app.data.model.Story

class PagingDataSource(
    private val apiService: ApiService
): PagingSource<Int, Story>() {
    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val position = params.key ?: INITIAL_PAGE
            val responseData: List<Story> = apiService.getStories(position, params.loadSize, 0).listStory ?: emptyList()
            LoadResult.Page(
                data = responseData,
                prevKey = if (position == INITIAL_PAGE) null else position - 1,
                nextKey = if (responseData.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    private companion object {
        const val INITIAL_PAGE = 1
    }
}