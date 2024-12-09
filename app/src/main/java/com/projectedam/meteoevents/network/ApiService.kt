package com.projectedam.meteoevents.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
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
 * Data class que representa la informació completa d'un usuari.
 *
 * @param id Identificador de l'usuari.
 * @param nomC Nom complet de l'usuari.
 * @param funcionalId ID funcional de l'usuari.
 * @param nomUsuari Nom d'usuari.
 * @param contrasenya Contrasenya de l'usuari.
 * @param dataNaixement Data de naixement de l'usuari.
 * @param sexe Sexe de l'usuari.
 * @param poblacio Població de l'usuari.
 * @param email Correu electrònic de l'usuari.
 * @param telefon Número de telèfon de l'usuari.
 * @param descripcio Descripció o bio de l'usuari.
 */
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("nom_c") val nomC: String,
    @SerializedName("nomUsuari") val nomUsuari: String,
    @SerializedName("contrasenya") val contrasenya: String,
    @SerializedName("data_naixement") val dataNaixement: String?,
    @SerializedName("sexe") val sexe: String?,
    @SerializedName("poblacio") val poblacio: String?,
    @SerializedName("email") val email: String,
    @SerializedName("telefon") val telefon: String?,
    @SerializedName("descripcio") val descripcio: String?,
    @SerializedName("funcional_id") val funcionalId: String
)

/**
 * Data class que representa un esdeveniment.
 *
 * @param id Identificador de l'esdeveniment.
 * @param nom Nom de l'esdeveniment.
 * @param descripcio Descripció de l'esdeveniment.
 * @param organitzador Nom de l'organitzador.
 * @param direccio Direcció de l'esdeveniment.
 * @param codiPostal Codi postal de l'esdeveniment.
 * @param poblacio Població on se celebra l'esdeveniment.
 * @param aforament Nombre màxim d'assistents.
 * @param horari Horari de l'esdeveniment.
 */
data class Esdeveniment(
    val id: Int? = null,
    val nom: String,
    val descripcio: String,
    val organitzador: String,
    val direccio: String,
    @SerializedName("codi_postal") val codiPostal: String,
    val poblacio: String,
    val aforament: String,
    val hora_inici: String,
    val hora_fi: String,
    @SerializedName("data_esde") val data_desde: String
)

/**
 * Data class que representa una mesura de seguretat.
 *
 * @param id Identificador de la mesura.
 * @param condicio Condició associada a la mesura.
 * @param valor Valor numèric de la mesura.
 * @param valorUm Unitat de mesura del valor.
 * @param accio Acció a prendre en cas que es compleixi la condició.
 */
data class Mesura(
    val id: Int? = null,
    val condicio: String,
    val valor: Double,
    val valorUm: String,
    val accio: String
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
    ): Response<ResponseBody>

    /**
     * Mètode per realitzar el logout de l'usuari.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Resposta de la sol·licitud de logout.
     */
    @POST("api/usuaris/logout")
    suspend fun logout(
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    /**
     * Mètode per obtenir la llista d'usuaris.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Llistat d'usuaris.
     */
    @GET("api/usuaris")
    suspend fun getUsers(
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    /**
     * Mètode per obtenir el llistat d'esdeveniments.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Llistat d'esdeveniments.
     */
    @GET("api/esdeveniments")
    suspend fun getEsdeveniments(
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    /**
     * Mètode per obtenir el llistat de mesures de seguretat.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @return Llistat de mesures de seguretat.
     */
    @GET("api/mesures")
    suspend fun getMesures(
        @Header("Authorization") authToken: String
    ): Response<ResponseBody>

    /**
     * Mètode per obtenir un esdeveniment per ID.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param esdevenimentId ID de l'esdeveniment.
     * @return Esdeveniment amb l'ID especificat.
     */
    @GET("api/esdeveniments/{esdevenimentId}")
    suspend fun getEsdeveniment(
        @Header("Authorization") authToken: String,
        @Path("esdevenimentId") esdevenimentId: Int
    ): Response<ResponseBody>

    /**
     * Mètode per obtenir una mesura de seguretat per ID.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param mesuraId ID de la mesura de seguretat.
     * @return Mesura de seguretat amb l'ID especificat.
     */
    @GET("api/mesures/{mesuraId}")
    suspend fun getMesura(
        @Header("Authorization") authToken: String,
        @Path("mesuraId") mesuraId: Int
    ): Response<ResponseBody>

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

    @PUT("api/usuaris/{userId}")
    suspend fun updateUser(
        @Header("Authorization") authToken: String,
        @Path("userId") userId: String,
        @Body user: String
    ): Response<ResponseBody>

    /**
     * Mètode per actualitzar un esdeveniment existent.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param esdevenimentId ID de l'esdeveniment a actualitzar.
     * @param esdeveniment Esdeveniment amb els canvis.
     * @return Resposta de l'actualització.
     */
    @PUT("api/esdeveniments/{esdevenimentId}")
    suspend fun updateEsdeveniment(
        @Header("Authorization") authToken: String,
        @Path("esdevenimentId") esdevenimentId: Int,
        @Body esdeveniment: String
    ): Response<ResponseBody>

    /**
     * Mètode per actualitzar una mesura de seguretat existent.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param mesuraId ID de la mesura de seguretat a actualitzar.
     * @param mesura Mesura de seguretat amb els canvis.
     * @return Resposta de l'actualització.
     */
    @PUT("api/mesures/{mesuraId}")
    suspend fun updateMesura(
        @Header("Authorization") authToken: String,
        @Path("mesuraId") mesuraId: Int,
        @Body mesura: String
    ): Response<ResponseBody>

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
    ): Response<ResponseBody>

    /**
     * Mètode per eliminar un esdeveniment.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param esdevenimentId ID de l'esdeveniment a eliminar.
     * @return Resposta de l'eliminació.
     */
    @DELETE("api/esdeveniments/{esdevenimentId}")
    suspend fun deleteEsdeveniment(
        @Header("Authorization") authToken: String,
        @Path("esdevenimentId") esdevenimentId: Int
    ): Response<ResponseBody>

    /**
     * Mètode per eliminar una mesura de seguretat.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param mesuraId ID de la mesura de seguretat a eliminar.
     * @return Resposta de l'eliminació.
     */
    @DELETE("api/mesures/{mesuraId}")
    suspend fun deleteMesura(
        @Header("Authorization") authToken: String,
        @Path("mesuraId") mesuraId: Int
    ): Response<ResponseBody>

    @POST("/api/usuaris")
    suspend fun createUser(
        @Header("Authorization") authToken: String,
        @Body user: String
    ): Response<Unit>

    /**
     * Mètode per crear un nou esdeveniment.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param esdeveniment Esdeveniment a crear.
     * @return Resposta de la creació.
     */
    @POST("api/esdeveniments")
    suspend fun createEsdeveniment(
        @Header("Authorization") authToken: String,
        @Body esdeveniment: String
    ): Response<ResponseBody>

    /**
     * Mètode per crear una nova mesura de seguretat.
     *
     * @param authToken Token d'autenticació de l'usuari.
     * @param mesura Mesura de seguretat a crear.
     * @return Resposta de la creació.
     */
    @POST("api/mesures")
    suspend fun createMesura(
        @Header("Authorization") authToken: String,
        @Body mesura: String
    ): Response<ResponseBody>
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




