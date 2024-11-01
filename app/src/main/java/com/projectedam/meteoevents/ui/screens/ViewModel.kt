package com.projectedam.meteoevents.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectedam.meteoevents.network.ApiClient
import com.projectedam.meteoevents.network.ApiService
import com.projectedam.meteoevents.network.User
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Model de dades de l'usuari que gestiona l'estat del login i logout.
 */
class UserViewModel(apiService: ApiService) : ViewModel() {
    var token: String? = null
    var funcionalId: String? = null
    var loginError: String? = null

    /**
     * Mètode per gestionar el login de l'usuari.
     *
     * @param username Nom d'usuari.
     * @param password Contrasenya de l'usuari.
     * @param onSuccess Funció que s'executa quan el login és exitós.
     * @param onFailure Funció que s'executa en cas d'error durant el login.
     */
    fun login(username: String, password: String, onSuccess: (String, String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.login(username, password)
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    token = loginResponse.token
                    funcionalId = loginResponse.funcionalId
                    onSuccess(loginResponse.token, loginResponse.funcionalId)
                } else {
                    onFailure("Login fallit.")
                }
            } catch (e: IOException) {
                onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
            } catch (e: HttpException) {
                onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.logout("Bearer $currentToken")
                    if (response.isSuccessful) {
                        token = null
                        funcionalId = null
                        onSuccess()
                    } else {
                        onFailure("Logout fallit.")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.getUsers("Bearer $currentToken")
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onFailure("No s'ha pogut obtenir la llista d'usuaris.")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.updateUser(
                        authToken = "Bearer $currentToken",
                        userId = user.id,
                        nomC = user.nomC,
                        nomUsuari = user.nomUsuari,
                        contrasenya = user.contrasenya,
                        dataNaixement = user.dataNaixement,
                        sexe = user.sexe,
                        poblacio = user.poblacio,
                        email = user.email,
                        telefon = user.telefon,
                        descripcio = user.descripcio
                    )
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar l'usuari.")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.deleteUser("Bearer $currentToken", userId)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut eliminar l'usuari.")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar l'usuari.")
        }
    }
}
