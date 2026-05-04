package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.repository.PostRepository
import org.example.project.ui.PostScreen

@Composable
fun App() {
    MaterialTheme {
        // Inicializamos o Ktor ensinando ele a ler JSON
        val httpClient = remember {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true // Ignora campos da API que não usamos na data class
                    })
                }
            }
        }

        // Criamos o repositório
        val repository = remember { PostRepository(httpClient) }

        // Chamamos a tela principal
        PostScreen(repository = repository)
    }
}