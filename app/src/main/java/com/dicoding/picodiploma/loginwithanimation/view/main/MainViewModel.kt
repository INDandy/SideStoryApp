package com.dicoding.picodiploma.loginwithanimation.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.UploadRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.view.story.StoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest


class MainViewModel(
    private val repository: UploadRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val  _token = MutableLiveData<String>()

    val storyList: LiveData<PagingData<ListStoryItem>> = liveData {
        _token.asFlow()
            .flatMapLatest { token ->
                repository.getStories(token).asFlow().cachedIn(viewModelScope)
            }
            .collect { pagingData ->
                emit(pagingData)
            }
    }

    val detail = repository.detail

    fun login(email: String, password: String) = repository.login(email, password)
    fun register(name: String, email: String, password: String) = repository.register(name, email, password)

    fun getStory(token: String) {
        _token.value = token
    }

    fun getStoryDetail(token: String, id: String) = repository.getStoryDetail(token, id)

    fun uploadImage(token: String, file: File, description: String, latitude: Double?, longitude: Double?) =
        repository.uploadImage(token, file, description, latitude, longitude)



    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> get() = _storiesWithLocation

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}