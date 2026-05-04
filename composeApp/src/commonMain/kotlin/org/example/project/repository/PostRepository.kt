package org.example.project.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.example.project.data.Post // Importe a classe Post que você acabou de criar

class PostRepository(private val httpClient: HttpClient) {

    // Função exatamente como pedida na atividade
    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): List<Post> {
        return httpClient.get("https://jsonplaceholder.typicode.com/posts") {

            // Adicionando a Paginação nativa da API
            parameter("_page", page)
            parameter("_limit", limit)

            // Filtro por ID do usuário (se não for nulo)
            if (userId != null) {
                parameter("userId", userId)
            }
        }.body()
    }
}