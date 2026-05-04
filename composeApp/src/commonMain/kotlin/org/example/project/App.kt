package org.example.project // Verifique se o seu pacote é esse mesmo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.repository.PostRepository
import org.example.project.ui.PostScreen
import org.example.project.ui.PostViewModel

@Composable
fun App() {
    MaterialTheme {
        // 1. Mantemos a inicialização do Ktor
        val httpClient = remember {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                    })
                }
            }
        }

        // 2. Mantemos o repositório
        val repository = remember { PostRepository(httpClient) }

        // 3. NOVO: Inicializamos a ViewModel usando a função oficial do Compose
        val viewModel = viewModel { PostViewModel(repository) }

        // 4. Chamamos a tela passando a ViewModel em vez do repositório
        PostScreen(viewModel = viewModel)
    }
}