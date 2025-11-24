package com.dicoding.picodiploma.loginwithanimation.view.story

import android.graphics.pdf.LoadParams
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import retrofit2.HttpException
import java.io.IOException

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: 1 
            val size = params.loadSize 
            val response = apiService.getStories("Bearer $token", page, size)

            if (response.error == true) {
                LoadResult.Error(Exception(response.message))
            } else {
                LoadResult.Page(
                    data = response.listStory,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.listStory.isEmpty()) null else page + 1
                )
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

