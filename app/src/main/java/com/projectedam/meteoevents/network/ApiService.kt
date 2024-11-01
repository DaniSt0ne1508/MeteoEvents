package com.projectedam.meteoevents.network

import retrofit2.http.POST
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path


/**
 * Data class que representa la resposta del login.
 *
 * @param userType Tipus d'usuari.
 * @param token Token generat durant el login.
 * @param funcionalId Identificador funcional de l'usuari.
 */
data class LoginResponse(
    val userType: String,
    val token: String,
    val funcionalId: String
)

/**
 * Data class que representa la resposta del logout, de moment no es fa servir.
 *
 * @param message Missatge de confirmació.
 */
data class LogoutResponse(
    val message: String
)

/**
 * Data class que representa la informació completa d'un usuari.
 *
 * @param id Identificador de l'usuari.
 * @param funcional Objecte amb informació funcional de l'usuari.
 * @param nomC Nom complet de l'usuari.
 * @param funcionalId ID funcional de l'usuari.
 * @param nomUsuari Nom d'usuari.
 * @param contrasenya Contrasenya de l'usuari.
 * @param ultimaConexio Data de l'última connexió de l'usuari.
 * @param dataNaixement Data de naixement de l'usuari.
 * @param sexe Sexe de l'usuari.
 * @param poblacio Població de l'usuari.
 * @param email Correu electrònic de l'usuari.
 * @param telefon Número de telèfon de l'usuari.
 * @param descripcio Descripció o bio de l'usuari.
 */
data class User(
    val id: String,
    val funcional: Funcional,
    val nomC: String,
    val funcionalId: String,
    val nomUsuari: String,
    val contrasenya: String,
    val ultimaConexio: String,
    val dataNaixement: String,
    val sexe: String,
    val poblacio: String,
    val email: String,
    val telefon: String,
    val descripcio: String
)

/**
 * Data class que representa l'objecte Funcional amb informació funcional de l'usuari.
 *
 * @param id ID funcional de l'usuari.
 * @param nom Nom funcional.
 */
data class Funcional(
    val id: String,
    val nom: String
)


/**
 * Interface que defineix els serveis d'API.
 */
interface ApiService {
    /**
     * Mètode per realitzar el login de l'usuari.
     *
     * @param username Nom d'usuari.
     * @param password Contrasenya de l'usuari.
     * @return Resposta de la sol·licitud de login.
     */
    @FormUrlEncoded
    @POST("api/usuaris/login")
    suspend fun login(
        @Field("nomUsuari") username: String,
        @Field("contrasenya") password: String
    ): Response<LoginResponse>

    /**
     * Mètode per realitzar el logout de l'usuari.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Resposta de la sol·licitud de logout.
     */
    @POST("api/usuaris/logout")
    suspend fun logout(
        @Header("Authorization") authToken: String
    ): Response<Unit>

    /**
     * Mètode per obtenir la llista d'usuaris.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Llistat d'usuaris.
     */
    @GET("api/usuaris")
    suspend fun getUsers(
        @Header("Authorization") authToken: String
    ): Response<List<User>>


    /**
     * Mètode per actualitzar la informació d'un usuari.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param userId ID de l'usuari a actualitzar.
     * @param nomC Nou nom complet de l'usuari.
     * @param nomUsuari Nou nom d'usuari.
     * @param contrasenya Nova contrasenya.
     * @param dataNaixement Nova data de naixement.
     * @param sexe Nou sexe.
     * @param poblacio Nova població.
     * @param email Nou email.
     * @param telefon Nou telèfon.
     * @param descripcio Nova descripció.
     * @return Resposta de la sol·licitud d'actualització.
     */
    @FormUrlEncoded
    @PUT("api/usuaris/{userId}")
    suspend fun updateUser(
        @Header("Authorization") authToken: String,
        @Path("userId") userId: String,
        @Field("nomC") nomC: String,
        @Field("nomUsuari") nomUsuari: String,
        @Field("contrasenya") contrasenya: String,
        @Field("dataNaixement") dataNaixement: String,
        @Field("sexe") sexe: String,
        @Field("poblacio") poblacio: String,
        @Field("email") email: String,
        @Field("telefon") telefon: String,
        @Field("descripcio") descripcio: String
    ): Response<Unit>

    /**
     * Mètode per eliminar un usuari.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param userId ID de l'usuari a eliminar.
     * @return Resposta de la sol·licitud d'eliminació.
     */
    @DELETE("api/usuaris/{userId}")
    suspend fun deleteUser(
        @Header("Authorization") authToken: String,
        @Path("userId") userId: String
    ): Response<Unit>
}



/**
 * Objecte que gestiona la configuració de l'API.
 */
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


