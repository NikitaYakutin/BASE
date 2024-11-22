package com.example.base3

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndsanVzbW9pZHRjbm5zdGl0ZHFoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzIyNjcxNDEsImV4cCI6MjA0Nzg0MzE0MX0.NROSMZ8Iq46LGA8uACuk_arfjCHgH94GnbxcbcnwvOQ"
const val url = "https://wljusmoidtcnnstitdqh.supabase.co"

val supaClient = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = key
) {
    install(Postgrest)
}

@Serializable
data class User (
    @SerialName("user_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("access_level") val accessLevel: Int
)

suspend fun getUsers(): List<User> {
    return supaClient.from("users").select().decodeList<User>()
}

suspend fun addUserIfNotExists(email: String, password: String, name: String): Boolean {
    return try {
        val existingUser = supaClient.from("users")
            .select {
                filter { eq("email", email) }
            }
            .decodeAs<List<Map<String, String>>>()

        if (!existingUser.isNullOrEmpty()) {
            // Пользователь с таким email уже существует
            return false
        }

        // Добавляем нового пользователя
        supaClient.from("users").insert(
            mapOf(
                "email" to email,
                "password" to password,
                "name" to name,
                "access_level" to 0,
            )
        )
        true
    } catch (e: Exception) {
        // Обрабатываем возможные ошибки
        println("Error: ${e.message}")
        false
    }
}


suspend fun loginUser(email: String, password: String): Boolean {
    return try {
        val users = supaClient
            .from("users")
            .select(Columns.raw("email, password")) {
                filter {
                    eq("email", email);
                }
            }.decodeAs<List<Map<String, String>>>()

        if (users.isNotEmpty()) {
            val storedPassword = users[0]["password"]
            if (storedPassword == password) {
                true
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        println("Ошибка входа: ${e.message}")
        false
    }
}