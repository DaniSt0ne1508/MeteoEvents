package com.projectedam.meteoevents.network


import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

data class LoginRequest(
    val nomUsuari: String,
    val contrasenya: String
)

data class LoginResponse(
    val userType: String,
    val token: String,
    val funcionalId: String
)

interface ApiService {
    @FormUrlEncoded
    @POST("api/usuaris/login")
    suspend fun login(
        @Field("nomUsuari") username: String,
        @Field("contrasenya") password: String
    ): Response<LoginResponse>
}

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}