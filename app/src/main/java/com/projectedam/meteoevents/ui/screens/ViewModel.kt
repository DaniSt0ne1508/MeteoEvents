package com.projectedam.meteoevents.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.projectedam.meteoevents.network.ApiClient
import com.projectedam.meteoevents.network.ApiService
import com.projectedam.meteoevents.network.CipherUtil
import com.projectedam.meteoevents.network.Esdeveniment
import com.projectedam.meteoevents.network.LoginResponse
import com.projectedam.meteoevents.network.Mesura
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
                val response = ApiClient.apiService.login(username, password)

                if (response.isSuccessful && response.body() != null) {
                    val encryptedResponse = response.body()!!.string()

                    val decryptedResponse = try {
                        CipherUtil.decrypt(encryptedResponse) // Desencriptar string
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
                    onFailure("Login fallit. Codi de resposta: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("Login", "Error de connexió: ${e.localizedMessage}")
                onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
            } catch (e: HttpException) {
                onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.logout("Bearer $currentToken")
                    if (response.isSuccessful) {
                        token = null
                        funcionalId = null
                        onSuccess()
                    } else {
                        onFailure("Logout fallit. Codi de resposta: ${response.code()}")
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
                        onFailure("No s'ha pogut obtenir la llista d'usuaris. Codi de resposta: ${response.code()}")
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
                        user = user  // Pasar el objeto user directamente
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
                        onFailure("No s'ha pogut eliminar l'usuari. Codi de resposta: ${response.code()}")
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

    fun createUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val response = ApiClient.apiService.createUser(
                        authToken = "Bearer $currentToken",
                        user = user
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
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot crear l'usuari.")
        }
    }

    fun seeEvents(onSuccess: (List<Esdeveniment>) -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val response = ApiClient.apiService.getEsdeveniments("Bearer $currentToken")
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onFailure("No s'ha pogut obtenir el llistat d'esdeveniments. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.getEsdeveniment("Bearer $currentToken", id)
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onFailure("No s'ha pogut obtenir l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
    fun createEvent(esdeveniment: Esdeveniment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val response = ApiClient.apiService.createEsdeveniment("Bearer $currentToken", esdeveniment)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut crear l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
    fun updateEvent(id: Int, esdeveniment: Esdeveniment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val response = ApiClient.apiService.updateEsdeveniment("Bearer $currentToken", id, esdeveniment)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar l'esdeveniment. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.deleteEsdeveniment("Bearer $currentToken", id)
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
                    val response = ApiClient.apiService.getMesures("Bearer $currentToken")
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onFailure("No s'ha pogut obtenir el llistat de mesures. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.getMesura("Bearer $currentToken", id)
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onFailure("No s'ha pogut obtenir la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.createMesura("Bearer $currentToken", mesura)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut crear la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
    fun updateMesura(id: Int, mesura: Mesura, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentToken = token
        if (currentToken != null) {
            viewModelScope.launch {
                try {
                    val response = ApiClient.apiService.updateMesura("Bearer $currentToken", id, mesura)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut actualitzar la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
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
                    val response = ApiClient.apiService.deleteMesura("Bearer $currentToken", id)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("No s'ha pogut eliminar la mesura. Codi de resposta: ${response.code()}")
                    }
                } catch (e: IOException) {
                    onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
                } catch (e: HttpException) {
                    onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
                }
            }
        } else {
            onFailure("Token no vàlid, no es pot eliminar la mesura.")
        }
    }
}

