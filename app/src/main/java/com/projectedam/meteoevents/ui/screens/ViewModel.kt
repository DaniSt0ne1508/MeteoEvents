package com.projectedam.meteoevents.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.projectedam.meteoevents.network.ApiClient
import com.projectedam.meteoevents.network.ApiService
import com.projectedam.meteoevents.network.CipherUtil
import com.projectedam.meteoevents.network.Esdeveniment
import com.projectedam.meteoevents.network.LoginResponse
import com.projectedam.meteoevents.network.Mesura
import com.projectedam.meteoevents.network.MeteoDetails
import com.projectedam.meteoevents.network.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

/**
 * Model de dades de l'usuari que gestiona l'estat del login i logout.
 */
class UserViewModel(private val apiService: ApiService) : ViewModel() {
    var token: String? = null
    var funcionalId: String? = null
    var currentUserName: String? = null

    /**
     * Mètode per gestionar el login de l'usuari.
     *
     * @param username Nom d'usuari.
     * @param password Contrasenya de l'usuari.
     * @param onSuccess Funció que s'executa quan el login és exitós.
     * @param onFailure Funció que s'executa en cas d'error durant el login.
     */
    fun login(
        username: String,
        password: String,
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val timestamp = java.time.Instant.now().toString()

                val passwordWithTimestamp = "$password|$timestamp"

                val encryptedPassword = try {
                    CipherUtil.encrypt(passwordWithTimestamp)
                } catch (e: Exception) {
                    onFailure("Error al xifrar la contrasenya: ${e.message}")
                    return@launch
                }

                val base64Password = android.util.Base64.encodeToString(
                    encryptedPassword.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                )

                val encryptedUsername = try {
                    CipherUtil.encrypt(username)
                } catch (e: Exception) {
                    onFailure("Error al xifrar el nom d'usuari: ${e.message}")
                    return@launch
                }

                val base64Username = android.util.Base64.encodeToString(
                    encryptedUsername.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                )

                Log.d("Login", "Nom d'usuari xifrat i codificat en Base64: $base64Username")
                Log.d("Login", "Contrasenya xifrada i codificada en Base64: $base64Password")

                val response = ApiClient.apiService.login(base64Username, base64Password)

                if (response.isSuccessful && response.body() != null) {
                    val encryptedResponse = response.body()!!.string()

                    val decryptedResponse = try {
                        CipherUtil.decrypt(encryptedResponse)
                    } catch (e: Exception) {
                        onFailure("Error al desxifrar la resposta del servidor: ${e.message}")
                        return@launch
                    }

                    val loginResponse = try {
                        Gson().fromJson(decryptedResponse, LoginResponse::class.java)
                    } catch (e: Exception) {
                        onFailure("Error al parsejar la resposta desxifrada: ${e.message}")
                        return@launch
                    }

                    token = loginResponse.token
                    funcionalId = loginResponse.funcionalId
                    currentUserName = username

                    onSuccess(loginResponse.token, loginResponse.funcionalId)
                } else {
                    if (response.code() == 401) {
                        onFailure("Usuari o contrasenya incorrectes")
                    } else {
                        onFailure("Error en l'inici de sessió. Codi de resposta: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                Log.e("Login", "Error de connexió: ${e.localizedMessage}")
                onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
            } catch (e: HttpException) {
                onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
            } catch (e: Exception) {
                onFailure("Error inesperat: ${e.message}")
            }
        }
    }

    /**
     * Mètode per gestionar el logout de l'usuari.
     *
     * @param onSuccess Funció que s'executa quan el logout és exitós.
     * @param onFailure Funció que s'executa en cas d'error durant el logout.
     */
    fun logout(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.logout("Bearer $encryptedToken")
                    if (response.isSuccessful) {
                        token = null
                        funcionalId = null
                        onSuccess()
                    } else {
                        onFailure("Logout fallit. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no s'ha pogut tancar sessió.")
        }
    }

    /**
     * Mètode per obtenir la llista d'usuaris.
     *
     * @param onSuccess Funció que s'executa quan la recuperació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la recuperació.
     */
    fun seeUsers(onSuccess: (List<User>) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.getUsers("Bearer $encryptedToken")

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error al desxifrar la resposta del servidor: ${e.message}")
                            return@launch
                        }

                        val usersList = try {
                            Gson().fromJson(decryptedResponse, Array<User>::class.java).toList()
                        } catch (e: Exception) {
                            onFailure("Error al parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(usersList)
                    } else {
                        onFailure("Error en la resposta del servidor: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir la llista d'usuaris.")
        }
    }

    /**
     * Mètode per actualitzar la informació d'un usuari.
     *
     * @param user Usuari amb les dades actualitzades.
     * @param onSuccess Funció que s'executa quan l'actualització és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'actualització.
     */
    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val userWithEncryptedPassword = try {
                        if (!user.contrasenya.isNullOrEmpty()) {
                            val encryptedPassword = CipherUtil.encrypt(user.contrasenya)
                            user.copy(contrasenya = encryptedPassword)
                        } else {
                            user
                        }
                    } catch (e: Exception) {
                        onFailure("Error en xifrar la contrasenya de l'usuari: ${e.message}")
                        return@launch
                    }

                    val requestBody = try {
                        val userJson = Gson().toJson(userWithEncryptedPassword)
                        val encryptedUserJson = CipherUtil.encrypt(userJson)
                        encryptedUserJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } catch (e: Exception) {
                        onFailure("Error en xifrar les dades de l'usuari: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.updateUser(
                        authToken = "Bearer $encryptedToken",
                        userId = user.id,
                        user = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar l'usuari. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot actualitzar l'usuari.")
        }
    }

    /**
     * Mètode per eliminar un usuari.
     *
     * @param userId ID de l'usuari a eliminar.
     * @param onSuccess Funció que s'executa quan l'eliminació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'eliminació.
     */
    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.deleteUser("Bearer $encryptedToken", userId)

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut eliminar l'usuari. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar l'usuari.")
        }
    }

    /**
     * Mètode per crear un usuari.
     *
     * @param user L'usuari a crear, representat com un objecte User.
     * @param onSuccess Funció que s'executa quan l'usuari es crea correctament.
     * @param onFailure Funció que s'executa si hi ha un error, passant un missatge d'error.
     */
    fun createUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token

        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = CipherUtil.encrypt(currentToken)
                    val userWithEncryptedPassword = if (!user.contrasenya.isNullOrEmpty()) {
                        val encryptedPassword = CipherUtil.encrypt(user.contrasenya)
                        user.copy(contrasenya = encryptedPassword)
                    } else {
                        user
                    }

                    val userJson = Gson().toJson(userWithEncryptedPassword)
                    val encryptedUserJson = CipherUtil.encrypt(userJson)
                    val requestBody =
                        encryptedUserJson.toRequestBody("application/json; charset=utf-8".toMediaType())

                    val response = apiService.createUser(
                        authToken = "Bearer $encryptedToken",
                        user = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut crear l'usuari. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot crear l'usuari.")
        }
    }

    /**
     * Mètode per veure els esdeveniments.
     *
     * @param onSuccess Funció que s'executa quan la crida a l'API és exitosa i es recupera la llista d'esdeveniments.
     * @param onFailure Funció que s'executa quan hi ha un error, passant un missatge d'error.
     */
    fun seeEvents(onSuccess: (List<Esdeveniment>) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.getEsdeveniments("Bearer $encryptedToken")

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error en desxifrar la resposta: ${e.message}")
                            return@launch
                        }

                        val eventsList = try {
                            Gson().fromJson(decryptedResponse, Array<Esdeveniment>::class.java)
                                .toList()
                        } catch (e: Exception) {
                            onFailure("Error en parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(eventsList)
                    } else {
                        onFailure("No s'ha pogut obtenir el llistat d'esdeveniments. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir el llistat d'esdeveniments.")
        }
    }

    /**
     * Mètode per obtenir un esdeveniment per ID.
     *
     * @param id ID de l'esdeveniment.
     * @param onSuccess Funció que s'executa quan la recuperació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la recuperació.
     */
    fun getEventById(id: Int, onSuccess: (Esdeveniment) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response =
                        ApiClient.apiService.getEsdeveniment("Bearer $encryptedToken", id)

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error al descifrar la resposta del servidor: ${e.message}")
                            return@launch
                        }

                        //Implementat amb IA amb prompt: "Parseo de JSON en Kotlin: extraer un objeto 'body' de la respuesta descifrada y mapearlo usando Gson."
                        val event = try {
                            val jsonObject =
                                Gson().fromJson(decryptedResponse, JsonObject::class.java)
                            val bodyJson = jsonObject.getAsJsonObject("body")
                            Gson().fromJson(bodyJson, Esdeveniment::class.java)
                        } catch (e: Exception) {
                            onFailure("Error al parsejar la resposta descifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(event)
                    } else {
                        onFailure("No s'ha pogut obtenir l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir l'esdeveniment.")
        }
    }

    /**
     * Mètode per crear un nou esdeveniment.
     *
     * @param esdeveniment Esdeveniment a crear.
     * @param onSuccess Funció que s'executa quan la creació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la creació.
     */
    fun createEvent(
        esdeveniment: Esdeveniment,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val requestBody = try {
                        val esdevenimentJson = Gson().toJson(esdeveniment)
                        val encryptedEsdeveniment = CipherUtil.encrypt(esdevenimentJson)
                        encryptedEsdeveniment.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } catch (e: Exception) {
                        onFailure("Error al encriptar l'esdeveniment: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.createEsdeveniment(
                        authToken = "Bearer $encryptedToken",
                        esdeveniment = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut crear l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot crear l'esdeveniment.")
        }
    }

    /**
     * Mètode per actualitzar un esdeveniment.
     *
     * @param id ID de l'esdeveniment a actualitzar.
     * @param esdeveniment Esdeveniment amb les dades actualitzades.
     * @param onSuccess Funció que s'executa quan l'actualització és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'actualització.
     */
    fun updateEvent(
        esdevenimentId: Int,
        esdeveniment: Esdeveniment,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val requestBody = try {
                        val esdevenimentJson = Gson().toJson(esdeveniment)
                        val encryptedEsdeveniment = CipherUtil.encrypt(esdevenimentJson)
                        encryptedEsdeveniment.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } catch (e: Exception) {
                        onFailure("Error al encriptar l'esdeveniment: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.updateEsdeveniment(
                        authToken = "Bearer $encryptedToken",
                        esdevenimentId = esdevenimentId,
                        esdeveniment = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot actualitzar l'esdeveniment.")
        }
    }

    /**
     * Mètode per eliminar un esdeveniment.
     *
     * @param id ID de l'esdeveniment a eliminar.
     * @param onSuccess Funció que s'executa quan l'eliminació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'eliminació.
     */
    fun deleteEvent(id: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response =
                        ApiClient.apiService.deleteEsdeveniment("Bearer $encryptedToken", id)

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut eliminar l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar l'esdeveniment.")
        }
    }

    /**
     * Mètode per obtenir el llistat de mesures de seguretat.
     *
     * @param onSuccess Funció que s'executa quan la recuperació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la recuperació.
     */
    fun seeMesures(onSuccess: (List<Mesura>) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.getMesures("Bearer $encryptedToken")

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error en desxifrar la resposta: ${e.message}")
                            return@launch
                        }

                        val measuresList = try {
                            Gson().fromJson(decryptedResponse, Array<Mesura>::class.java).toList()
                        } catch (e: Exception) {
                            onFailure("Error en parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(measuresList)
                    } else {
                        onFailure("No s'ha pogut obtenir el llistat de mesures. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir el llistat de mesures.")
        }
    }

    /**
     * Mètode per obtenir una mesura de seguretat per ID.
     *
     * @param id ID de la mesura.
     * @param onSuccess Funció que s'executa quan la recuperació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la recuperació.
     */
    fun getMesuraById(id: Int, onSuccess: (Mesura) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.getMesura("Bearer $encryptedToken", id)

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error en desxifrar la resposta: ${e.message}")
                            return@launch
                        }

                        val mesure = try {
                            val jsonObject =
                                Gson().fromJson(decryptedResponse, JsonObject::class.java)
                            val bodyJson = jsonObject.getAsJsonObject("body")
                            Gson().fromJson(bodyJson, Mesura::class.java)
                        } catch (e: Exception) {
                            onFailure("Error en parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(mesure)
                    } else {
                        onFailure("No s'ha pogut obtenir la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir la mesura.")
        }
    }

    /**
     * Mètode per crear una nova mesura de seguretat.
     *
     * @param mesura Mesura a crear.
     * @param onSuccess Funció que s'executa quan la creació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant la creació.
     */
    fun createMesura(mesura: Mesura, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val requestBody = try {
                        val mesuraJson = Gson().toJson(mesura)
                        val encryptedMesura = CipherUtil.encrypt(mesuraJson)
                        encryptedMesura.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } catch (e: Exception) {
                        onFailure("Error en convertir o xifrar la mesura: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.createMesura(
                        authToken = "Bearer $encryptedToken",
                        mesura = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut crear la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot crear la mesura.")
        }
    }

    /**
     * Mètode per actualitzar una mesura de seguretat.
     *
     * @param id ID de la mesura a actualitzar.
     * @param mesura Mesura amb les dades actualitzades.
     * @param onSuccess Funció que s'executa quan l'actualització és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'actualització.
     */
    fun updateMesura(
        mesuraId: Int,
        mesura: Mesura,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val requestBody = try {
                        val mesuraJson = Gson().toJson(mesura)
                        val encryptedMesura = CipherUtil.encrypt(mesuraJson)
                        encryptedMesura.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } catch (e: Exception) {
                        onFailure("Error al encriptar la mesura: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.updateMesura(
                        authToken = "Bearer $encryptedToken",
                        mesuraId = mesuraId,
                        mesura = requestBody
                    )

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot actualitzar la mesura.")
        }
    }

    /**
     * Mètode per eliminar una mesura de seguretat.
     *
     * @param id ID de la mesura a eliminar.
     * @param onSuccess Funció que s'executa quan l'eliminació és exitosa.
     * @param onFailure Funció que s'executa en cas d'error durant l'eliminació.
     */
    fun deleteMesura(id: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error en xifrar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.deleteMesura("Bearer $encryptedToken", id)

                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut eliminar la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar la mesura.")
        }
    }

    /**
     * Mètode per obtenir la llista d'usuaris assignats a un esdeveniment.
     *
     * @param eventId ID de l'esdeveniment.
     * @param onSuccess Funció que s'executa amb la llista d'usuaris obtinguda.
     * @param onFailure Funció que s'executa en cas d'error durant l'obtenció.
     */
    fun getUsersEvents(
        eventId: Int,
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response =
                        ApiClient.apiService.getUsersByEvent("Bearer $encryptedToken", eventId)

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error al desxifrar la resposta: ${e.message}")
                            return@launch
                        }

                        val usersList = try {
                            Gson().fromJson(decryptedResponse, Array<User>::class.java).toList()
                        } catch (e: Exception) {
                            onFailure("Error al parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(usersList)
                    } else {
                        onFailure("No s'han pogut obtenir els usuaris. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir els usuaris.")
        }
    }

    /**
     * Afegeix un usuari a un esdeveniment específic.
     *
     * @param eventId L'ID de l'esdeveniment al qual s'ha d'afegir l'usuari.
     * @param userId L'ID de l'usuari que s'ha d'afegir a l'esdeveniment.
     * @param onSuccess Callback que es crida quan l'operació s'ha completat amb èxit, passant el missatge de resposta del servidor.
     * @param onFailure Callback que es crida quan hi ha un error, passant el missatge d'error corresponent.
     */
    fun addUserEvent(
        eventId: Int,
        userId: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.addUserToEvent(
                        authToken = "Bearer $encryptedToken",
                        esdevenimentId = eventId,
                        usuariId = userId
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!.string()
                        onSuccess(responseBody)
                    } else {
                        when (response.code()) {
                            401 -> onFailure("Token invàlid o inactiu.")
                            404 -> onFailure("Esdeveniment o Usuari no trobats.")
                            400 -> onFailure("Token no proporcionat.")
                            else -> onFailure("Error inesperat. Codi de resposta: ${response.code()}.")
                        }
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot afegir l'usuari a l'esdeveniment.")
        }
    }

    /**
     * Elimina un usuari d'un esdeveniment.
     *
     * @param eventId ID de l'esdeveniment del qual es vol eliminar l'usuari.
     * @param userId ID de l'usuari que es vol eliminar de l'esdeveniment.
     * @param onSuccess Funció callback que s'executa quan l'operació és exitosa. Rep un missatge com a paràmetre.
     * @param onFailure Funció callback que s'executa en cas d'error. Rep un missatge d'error com a paràmetre.
     */
    fun deleteUserEvent(
        eventId: Int,
        userId: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.deleteUserFromEvent(
                        authToken = "Bearer $encryptedToken",
                        esdevenimentId = eventId,
                        usuariId = userId
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!.string()
                        onSuccess(responseBody)
                    } else {
                        when (response.code()) {
                            401 -> onFailure("Token invàlid o inactiu.")
                            404 -> onFailure("Esdeveniment o Usuari no trobats.")
                            400 -> onFailure("Token no proporcionat.")
                            else -> onFailure("Error inesperat. Codi de resposta: ${response.code()}.")
                        }
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar l'usuari de l'esdeveniment.")
        }
    }

    /**
     * Mètode per obtenir la llista de mesures assignades a un esdeveniment.
     *
     * @param eventId ID de l'esdeveniment.
     * @param onSuccess Funció que s'executa amb la llista de mesures obtinguda.
     * @param onFailure Funció que s'executa en cas d'error durant l'obtenció.
     */
    fun getMesuresEvent(
        eventId: Int,
        onSuccess: (List<Mesura>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response =
                        ApiClient.apiService.getMeasuresByEvent("Bearer $encryptedToken", eventId)

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error al desxifrar la resposta: ${e.message}")
                            return@launch
                        }

                        val mesuresList = try {
                            Gson().fromJson(decryptedResponse, Array<Mesura>::class.java).toList()
                        } catch (e: Exception) {
                            onFailure("Error al parsejar la resposta desxifrada: ${e.message}")
                            return@launch
                        }

                        onSuccess(mesuresList)
                    } else {
                        onFailure("No s'han pogut obtenir les mesures. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot obtenir les mesures.")
        }
    }

    /**
     * Afegeix una mesura a un esdeveniment específic.
     *
     * @param eventId L'ID de l'esdeveniment al qual s'ha d'afegir la mesura.
     * @param measureId L'ID de la mesura que s'ha d'afegir a l'esdeveniment.
     * @param onSuccess Callback que es crida quan l'operació s'ha completat amb èxit, passant el missatge de resposta del servidor.
     * @param onFailure Callback que es crida quan hi ha un error, passant el missatge d'error corresponent.
     */
    fun addMesuraEvent(
        eventId: Int,
        measureId: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.addMeasureToEvent(
                        authToken = "Bearer $encryptedToken",
                        esdevenimentId = eventId,
                        mesuraId = measureId
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!.string()
                        onSuccess(responseBody)
                    } else {
                        when (response.code()) {
                            401 -> onFailure("Token invàlid o inactiu.")
                            404 -> onFailure("Esdeveniment o Mesura no trobats.")
                            400 -> onFailure("Token no proporcionat.")
                            else -> onFailure("Error inesperat. Codi de resposta: ${response.code()}.")
                        }
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot afegir la mesura a l'esdeveniment.")
        }
    }

    /**
     * Elimina una mesura d'un esdeveniment.
     *
     * @param eventId ID de l'esdeveniment del qual es vol eliminar la mesura.
     * @param measureId ID de la mesura que es vol eliminar de l'esdeveniment.
     * @param onSuccess Funció callback que s'executa quan l'operació és exitosa. Rep un missatge com a paràmetre.
     * @param onFailure Funció callback que s'executa en cas d'error. Rep un missatge d'error com a paràmetre.
     */
    fun deleteMesuraEvent(
        eventId: Int,
        measureId: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error al encriptar el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.deleteMeasureFromEvent(
                        authToken = "Bearer $encryptedToken",
                        esdevenimentId = eventId,
                        mesuraId = measureId
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!.string()
                        onSuccess(responseBody)
                    } else {
                        when (response.code()) {
                            401 -> onFailure("Token invàlid o inactiu.")
                            404 -> onFailure("Esdeveniment o Mesura no trobats.")
                            400 -> onFailure("Token no proporcionat.")
                            else -> onFailure("Error inesperat. Codi de resposta: ${response.code()}.")
                        }
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, intenta-ho més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar la mesura de l'esdeveniment.")
        }
    }

    fun getMeteo(
        eventId: Int,
        onSuccess: (List<String>, MeteoDetails, List<String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val encryptedToken = try {
                        CipherUtil.encrypt(currentToken)
                    } catch (e: Exception) {
                        onFailure("Error encriptant el token: ${e.message}")
                        return@launch
                    }

                    val response = ApiClient.apiService.getMeteo("Bearer $encryptedToken", eventId)

                    if (response.isSuccessful && response.body() != null) {
                        val encryptedResponse = response.body()!!.string()

                        val decryptedResponse = try {
                            CipherUtil.decrypt(encryptedResponse)
                        } catch (e: Exception) {
                            onFailure("Error desxifrant la resposta: ${e.message}")
                            return@launch
                        }

                        try {
                            if (decryptedResponse.contains("No s'ha pogut generar el JSON")) {
                                onFailure("El servidor no ha pogut generar les dades meteorològiques.")
                                return@launch
                            }

                            val jsonResponse = Gson().fromJson(decryptedResponse, Map::class.java)

                            val usuariosList =
                                (jsonResponse["Usuaris participants"] as? List<*>)?.map { it.toString() }
                                    ?: emptyList()

                            val meteoDataMap =
                                jsonResponse.filterKeys { it != "Usuaris participants" }

                            val accionesList = meteoDataMap.flatMap { (_, value) ->
                                if (value is Map<*, *>) {
                                    val actions = extractActionsFromSubmap(value)
                                    println("Acciones encontradas: $actions")
                                    actions
                                } else {
                                    emptyList()
                                }
                            }

                            val meteoDetails = meteoDataMap.values
                                .filterIsInstance<Map<*, *>>()
                                .firstOrNull()?.let { subMap ->
                                    Gson().fromJson(Gson().toJson(subMap), MeteoDetails::class.java)
                                }

                            if (meteoDetails != null) {
                                onSuccess(usuariosList, meteoDetails, accionesList)
                            } else {
                                onFailure("No s'han trobat dades meteorològiques vàlides.")
                            }
                        } catch (e: Exception) {
                            onFailure("Error analitzant la resposta desxifrada: ${e.message}")
                            println("Contingut de la resposta que no es pot analitzar: $decryptedResponse")
                        }
                    } else {
                        when (response.code()) {
                            401 -> onFailure("Token invàlid o inactiu.")
                            404 -> onFailure("Token no proporcionat.")
                            500 -> onFailure("Error intern al servidor.")
                            else -> onFailure("Error obtenint les dades meteorològiques: ${response.code()}")
                        }
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Si us plau, comprova la teva connexió amb el servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Si us plau, torna-ho a intentar més tard.")
                } catch (e: Exception) {
                    onFailure("Error inesperat: ${e.message}")
                }
            }
        } else {
            onFailure("Token no vàlid, no es poden obtenir les dades meteorològiques.")
        }
    }

    private fun extractActionsFromSubmap(map: Map<*, *>): List<String> {
        // Adaptat de la funció d'en Miquel
        return map.entries
            .filter { (_, value) -> value is Map<*, *> }
            .map { (_, subMap) ->
                val submapAsString = subMap.toString()
                submapAsString
            }
    }
}

