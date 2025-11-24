package com.dicoding.picodiploma.loginwithanimation.view.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val apiService: ApiService) {

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return liveData {
            val pagingData = Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { StoryPagingSource(apiService, token) }
            ).flow.collect { pagingData ->
                emit(pagingData) 
            }
        }
    }
}
