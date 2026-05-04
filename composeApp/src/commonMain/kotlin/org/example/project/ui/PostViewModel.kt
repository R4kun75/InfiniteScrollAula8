package org.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.Post
import org.example.project.repository.PostRepository

// 1. Criamos uma classe de dados para guardar TODO o estado da tela
data class PostUiState(
    val posts: List<Post> = emptyList(),
    val page: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePosts: Boolean = true,
    val userIdFilter: String = ""
)

// 2. A ViewModel gerencia esse estado
class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val limit = 10

    init {
        loadPosts(isNewSearch = true)
    }

    fun onUserIdChanged(newId: String) {
        _uiState.update { it.copy(userIdFilter = newId) }
    }

    fun searchPosts() {
        _uiState.update { it.copy(page = 1, posts = emptyList(), hasMorePosts = true, errorMessage = null) }
        loadPosts(isNewSearch = true)
    }

    fun loadMorePosts() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePosts && _uiState.value.errorMessage == null) {
            loadPosts(isNewSearch = false)
        }
    }

    // Tentar novamente após um erro
    fun retry() {
        _uiState.update { it.copy(errorMessage = null) }
        loadPosts(isNewSearch = _uiState.value.posts.isEmpty())
    }

    private fun loadPosts(isNewSearch: Boolean) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val currentState = _uiState.value
            val userId = currentState.userIdFilter.toIntOrNull()

            val result = repository.getPosts(page = currentState.page, limit = limit, userId = userId)

            result.fold(
                onSuccess = { newPosts ->
                    _uiState.update { state ->
                        state.copy(
                            posts = if (isNewSearch) newPosts else state.posts + newPosts,
                            page = state.page + 1,
                            isLoading = false,
                            hasMorePosts = newPosts.size == limit
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Erro desconhecido"
                        )
                    }
                }
            )
        }
    }
}