package com.projectedam.meteoevents.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectedam.meteoevents.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserViewModel : ViewModel() {
    var token: String? = null
    var funcionalId: String? = null
    var loginError: String? = null

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
                    val errorBody = response.errorBody()?.string()
                    onFailure("Login fallit.")
                }
            } catch (e: IOException) {
                onFailure("Error de connexió. Siusplau, comprova la teva connexió al servidor.")
            } catch (e: HttpException) {
                onFailure("Error al servidor. Siusplau, intenta-ho més tard.")
            }
        }
    }
}