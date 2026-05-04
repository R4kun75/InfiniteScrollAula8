package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.data.Post
import org.example.project.repository.PostRepository

@Composable
fun PostScreen(repository: PostRepository) {
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    var page by remember { mutableStateOf(1) }
    val limit = 10
    var userIdFilter by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasMorePosts by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Função que faz a requisição no repositório
    val loadPosts = { isNewSearch: Boolean ->
        // NOVO: Só entra se não estiver carregando E se ainda houver posts
        if (!isLoading && hasMorePosts) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val userId = userIdFilter.toIntOrNull()
                    val newPosts = repository.getPosts(page = page, limit = limit, userId = userId)

                    // NOVO: Se vieram menos posts do que pedimos, é porque acabou!
                    if (newPosts.size < limit) {
                        hasMorePosts = false
                    }

                    if (isNewSearch) {
                        posts = newPosts
                    } else {
                        posts = posts + newPosts
                    }
                    page++
                } catch (e: Exception) {
                    println("Erro ao carregar posts: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Carrega a primeira página ao abrir a tela
    LaunchedEffect(Unit) {
        loadPosts(true)
    }

    // Interface Visual
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = userIdFilter,
            onValueChange = { newValue ->
                userIdFilter = newValue
                page = 1
                posts = emptyList()
                isLoading = false
                hasMorePosts = true
                loadPosts(true)
            },
            label = { Text("Filtrar por User ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // Trocamos para itemsIndexed para saber qual a posição (index) do item atual
            itemsIndexed(posts) { index, post ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Post #${post.id} (User: ${post.userId})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // A NOVA MÁGICA AQUI:
                // Se a tela acabou de desenhar o último item da lista, manda carregar mais!
                if (index == posts.lastIndex && !isLoading && hasMorePosts) {
                    LaunchedEffect(Unit) {
                        loadPosts(false)
                    }
                }
            }

            // O indicador de carregamento continua aqui no final
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}