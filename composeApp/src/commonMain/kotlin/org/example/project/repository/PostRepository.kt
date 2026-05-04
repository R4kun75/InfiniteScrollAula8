package org.example.project.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.utils.io.errors.*
import org.example.project.data.Post

class PostRepository(private val httpClient: HttpClient) {

    suspend fun getPosts(page: Int, limit: Int, userId: Int? = null): Result<List<Post>> {
        return try {
            val response = httpClient.get("https://jsonplaceholder.typicode.com/posts") {
                parameter("_page", page)
                parameter("_limit", limit)
                if (userId != null) {
                    parameter("userId", userId)
                }
            }
            // Retorna sucesso se a requisição deu certo
            Result.success(response.body())

        } catch (e: RedirectResponseException) {
            // Erros da família 3xx
            Result.failure(Exception("Redirecionamento inesperado: ${e.response.status.description}"))
        } catch (e: ClientRequestException) {
            // Erros da família 4xx (ex: 404 Not Found)
            Result.failure(Exception("Erro na requisição: Verifique os dados enviados (Código ${e.response.status.value})"))
        } catch (e: ServerResponseException) {
            // Erros da família 5xx (ex: 500 Internal Server Error)
            Result.failure(Exception("Erro no servidor: Tente novamente mais tarde (Código ${e.response.status.value})"))
        } catch (e: IOException) {
            // Falha de conectividade (sem internet, timeout, etc)
            Result.failure(Exception("Sem conexão com a internet. Verifique sua rede e tente novamente."))
        } catch (e: Exception) {
            // Qualquer outro erro genérico
            Result.failure(Exception("Ocorreu um erro desconhecido: ${e.message}"))
        }
    }
}