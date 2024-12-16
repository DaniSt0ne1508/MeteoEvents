package com.projectedam.meteoevents.network



import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.Base64

class IntegrationTest {
    private lateinit var apiService: ApiService
    var authToken: String? = null

    /**
     * Configura el servidor i el servei d'API abans de cada prova.
     */
    @Before
    fun setUp() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)

        val username = "admin"
        val password = "admin24"

        runBlocking {
            val timestamp = java.time.Instant.now().toString()
            val passwordWithTimestamp = "$password|$timestamp"

            val encryptedPassword = try {
                CipherUtil.encrypt(passwordWithTimestamp)
            } catch (e: Exception) {
                println("Error al xifrar la contrasenya: ${e.message}")
                return@runBlocking
            }

            val base64Password = Base64.getEncoder().encodeToString(
                encryptedPassword.toByteArray(Charsets.UTF_8)
            )

            val encryptedUsername = try {
                CipherUtil.encrypt(username)
            } catch (e: Exception) {
                println("Error al xifrar el nom d'usuari: ${e.message}")
                return@runBlocking
            }

            val base64Username = Base64.getEncoder().encodeToString(
                encryptedUsername.toByteArray(Charsets.UTF_8)
            )

            val loginResponse = apiService.login(base64Username, base64Password)

            if (loginResponse.isSuccessful && loginResponse.body() != null) {
                val encryptedResponse = loginResponse.body()!!.string()

                val decryptedResponse = try {
                    CipherUtil.decrypt(encryptedResponse)
                } catch (e: Exception) {
                    println("Error al desxifrar la resposta del servidor: ${e.message}")
                    return@runBlocking
                }

                try {
                    val loginBody = Gson().fromJson(decryptedResponse, LoginResponse::class.java)
                    authToken = loginBody.token

                    if (authToken != null) {
                        println("Token obtingut correctament.")
                    } else {
                        println("Error al obtenir el token.")
                    }
                } catch (e: Exception) {
                    println("Error al parsejar la resposta desxifrada: ${e.message}")
                }
            } else {
                println("Login FAILED: Codi de resposta ${loginResponse.code()}")
            }
        }
    }

    /**
     * Verifica que el login i el logout funcionin correctament.
     */
    @Test
    fun testLoginAndLogout() = runBlocking {
        if (authToken == null) {
            println("Login FAILED: No s'ha obtingut el token.")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val logoutResponse = apiService.logout("Bearer $encryptedToken")

        if (logoutResponse.isSuccessful) {
            println("Logout OK: Codi de resposta ${logoutResponse.code()}")
            assertTrue(true)
        } else {
            println("Logout FAILED: Codi de resposta ${logoutResponse.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica que el login gestiona errors correctament.
     */
    @Test
    fun testLoginError() = runBlocking {
        val username = "wrongUser"
        val password = "wrongPassword"

        val timestamp = java.time.Instant.now().toString()
        val passwordWithTimestamp = "$password|$timestamp"

        val encryptedPassword = try {
            CipherUtil.encrypt(passwordWithTimestamp)
        } catch (e: Exception) {
            println("Error al xifrar la contrasenya: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val base64Password = Base64.getEncoder().encodeToString(
            encryptedPassword.toByteArray(Charsets.UTF_8)
        )

        val encryptedUsername = try {
            CipherUtil.encrypt(username)
        } catch (e: Exception) {
            println("Error al xifrar el nom d'usuari: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val base64Username = Base64.getEncoder().encodeToString(
            encryptedUsername.toByteArray(Charsets.UTF_8)
        )

        val response = apiService.login(base64Username, base64Password)

        if (response.code() == 401) {
            println("TestLoginError OK")
            assertTrue(true)
        } else {
            println("TestLoginError FAILED: Codi de resposta ${response.code()}")
            assertTrue(false)
        }

        assertEquals(401, response.code())
    }

    /**
     * Verifica que la llista d'usuaris es pot obtenir correctament.
     */
    @Test
    fun testGetUsers() = runBlocking {
        if (authToken == null) {
            println("TestGetUsers FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getUsers("Bearer $encryptedToken")

        if (response.code() == 200) {
            println("TestGetUsers OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en obtenir els usuaris: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica que la llista d'esdeveniments es pot obtenir correctament.
     */
    @Test
    fun testGetEsdeveniments() = runBlocking {
        if (authToken == null) {
            println("TestGetEsdeveniments FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getEsdeveniments("Bearer $encryptedToken")

        if (response.code() == 200) {
            println("TestGetEsdeveniments OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en obtenir els esdeveniments: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica que la llista de mesures es pot obtenir correctament.
     */
    @Test
    fun testGetMesures() = runBlocking {
        if (authToken == null) {
            println("TestGetMesures FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getMesures("Bearer $encryptedToken")

        if (response.code() == 200) {
            println("TestGetMesures OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en obtenir les mesures de seguretat: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica la creació d'un nou usuari.
     */
    @Test
    fun testCreateUser() = runBlocking {
        if (authToken == null) {
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val user = User(
            id = "",
            nomC = "Nou Nom",
            nomUsuari = "nouUsuari",
            contrasenya = "NouPassword123", // Contraseña que se encriptará
            dataNaixement = "2000-01-01",
            sexe = "M",
            poblacio = "Barcelona",
            email = "nouemail@example.com",
            telefon = "123456789",
            descripcio = "Usuari de prova",
            funcionalId = "USR"
        )

        val userWithEncryptedPassword = if (!user.contrasenya.isNullOrEmpty()) {
            val encryptedPassword = try {
                CipherUtil.encrypt(user.contrasenya)
            } catch (e: Exception) {
                println("Error al xifrar la contrasenya: ${e.message}")
                return@runBlocking
            }
            user.copy(contrasenya = encryptedPassword) // Sustituimos la contraseña original por la encriptada
        } else {
            user
        }

        val userJson = Gson().toJson(userWithEncryptedPassword)
        val encryptedUserJson = try {
            CipherUtil.encrypt(userJson)
        } catch (e: Exception) {
            println("Error al xifrar l'usuari: ${e.message}")
            return@runBlocking
        }

        val requestBody = encryptedUserJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val response = apiService.createUser("Bearer $encryptedToken", requestBody)

        if (response.code() == 201) {
            assertTrue(response.isSuccessful)
        } else {
            assertTrue(false)
        }
    }

    /**
     * Verifica la creació d'un nou esdeveniment.
     */
    @Test
    fun testCreateEsdeveniment() = runBlocking {
        if (authToken == null) {
            assertTrue(false)
            return@runBlocking
        }

        val esdeveniment = Esdeveniment(
            nom = "Esdeveniment de prova",
            descripcio = "Descripció de l'esdeveniment de prova",
            organitzador = "Organitzador Prova",
            direccio = "Carrer de la prova, 123",
            codiPostal = "08001",
            poblacio = "Barcelona",
            aforament = "100",
            hora_inici = "14:00",
            hora_fi = "18:00",
            data_esde = "2024-12-31"
        )

        val jsonEsdeveniment = Gson().toJson(esdeveniment)
        val requestBody = jsonEsdeveniment.toRequestBody("application/json; charset=utf-8".toMediaType())

        val response = apiService.createEsdeveniment(authToken!!, requestBody)

        if (response.code() == 201) {
            assertTrue(response.isSuccessful)
        } else {
            assertTrue(false)
        }
    }

    /**
     * Verifica la creació d'una nova mesura.
     */
    @Test
    fun testCreateMesura() = runBlocking {
        if (authToken == null) {
            println("TestCreateMesura FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val mesura = Mesura(
            condicio = "Temperatura",
            valor = 40.0,
            valorUm = "graus",
            accio = "Activar aire acondicionat!!!",
            nivell_mesura = 1
        )

        val jsonMesura = Gson().toJson(mesura)
        val requestBody = jsonMesura.toRequestBody("application/json; charset=utf-8".toMediaType())

        val response = apiService.createMesura(authToken!!, requestBody)

        if (response.code() == 201) {
            println("TestCreateMesura OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en crear la mesura: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'eliminació d'un usuari.
     */
    @Test
    fun testDeleteUser() = runBlocking {
        val userId = "45"

        if (authToken == null) {
            println("TestDeleteUser FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.deleteUser(authToken!!, userId)

        if (response.code() == 200) {
            println("TestDeleteUser OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en eliminar l'usuari: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'eliminació d'un esdeveniment.
     */
    @Test
    fun testDeleteEsdeveniment() = runBlocking {
        val esdevenimentId = 38

        if (authToken == null) {
            println("TestDeleteEsdeveniment FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.deleteEsdeveniment(authToken!!, esdevenimentId)

        if (response.code() == 200) {
            println("TestDeleteEsdeveniment OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en eliminar l'esdeveniment: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'eliminació d'una Mesura.
     */
    @Test
    fun testDeleteMesura() = runBlocking {
        val mesuraId = 40

        if (authToken == null) {
            println("TestDeleteMesura FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.deleteMesura(authToken!!, mesuraId)

        if (response.code() == 200) {
            println("TestDeleteMesura OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en eliminar la mesura: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }
}


