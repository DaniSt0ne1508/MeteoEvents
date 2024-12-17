package com.projectedam.meteoevents.network



import com.google.gson.Gson
import com.google.gson.JsonObject
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

        val requestBody =
            encryptedUserJson.toRequestBody("application/json; charset=utf-8".toMediaType())

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
            println("TestCreateEsdeveniment FAILED: No s'ha obtingut el token")
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
        val encryptedEsdeveniment = try {
            CipherUtil.encrypt(jsonEsdeveniment)
        } catch (e: Exception) {
            println("Error al xifrar l'esdeveniment: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val requestBody =
            encryptedEsdeveniment.toRequestBody("application/json; charset=utf-8".toMediaType())

        val response = apiService.createEsdeveniment("Bearer $encryptedToken", requestBody)

        if (response.code() == 201) {
            println("TestCreateEsdeveniment OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en crear l'esdeveniment: Codi de resposta ${response.code()}")
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

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error al xifrar el token: ${e.message}")
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
        val encryptedMesura = try {
            CipherUtil.encrypt(jsonMesura)
        } catch (e: Exception) {
            println("Error al xifrar la mesura: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val requestBody =
            encryptedMesura.toRequestBody("application/json; charset=utf-8".toMediaType())

        val response = apiService.createMesura("Bearer $encryptedToken", requestBody)

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
        val userId = "41"

        if (authToken == null) {
            println("TestDeleteUser FAILED: No s'ha obtingut el token")
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

        val response = apiService.deleteUser("Bearer $encryptedToken", userId)

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
        val esdevenimentId = 4

        if (authToken == null) {
            println("TestDeleteEsdeveniment FAILED: No s'ha obtingut el token")
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

        val response = apiService.deleteEsdeveniment("Bearer $encryptedToken", esdevenimentId)

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
        val mesuraId = 7

        if (authToken == null) {
            println("TestDeleteMesura FAILED: No s'ha obtingut el token")
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

        val response = apiService.deleteMesura("Bearer $encryptedToken", mesuraId)

        if (response.code() == 200) {
            println("TestDeleteMesura OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en eliminar la mesura: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'obtenció d'un esdeveniment per ID.
     */
    @Test
    fun testGetEsdevenimentById() = runBlocking {
        val esdevenimentId = 5

        if (authToken == null) {
            println("TestGetEsdevenimentById FAILED: No s'ha obtingut el token")
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

        val response = apiService.getEsdeveniment("Bearer $encryptedToken", esdevenimentId)

        if (response.isSuccessful && response.body() != null) {
            val encryptedResponse = response.body()!!.string()

            val decryptedResponse = try {
                CipherUtil.decrypt(encryptedResponse)
            } catch (e: Exception) {
                println("Error al descifrar la resposta: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            val esdeveniment = try {
                val jsonObject = Gson().fromJson(decryptedResponse, JsonObject::class.java)
                val bodyJson = jsonObject.getAsJsonObject("body")
                Gson().fromJson(bodyJson, Esdeveniment::class.java)
            } catch (e: Exception) {
                println("Error al parsejar la resposta descifrada: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            println("TestGetEsdevenimentById OK: Esdeveniment obtingut -> ${esdeveniment.nom}")
            assertTrue(true)
        } else {
            println("Error en obtenir l'esdeveniment: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'obtenció d'una mesura per ID.
     */
    @Test
    fun testGetMesuraById() = runBlocking {
        val mesuraId = 2

        if (authToken == null) {
            println("TestGetMesuraById FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error en xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getMesura("Bearer $encryptedToken", mesuraId)

        if (response.isSuccessful && response.body() != null) {
            val encryptedResponse = response.body()!!.string()

            val decryptedResponse = try {
                CipherUtil.decrypt(encryptedResponse)
            } catch (e: Exception) {
                println("Error en desxifrar la resposta: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            val mesura = try {
                val jsonObject = Gson().fromJson(decryptedResponse, JsonObject::class.java)
                val bodyJson = jsonObject.getAsJsonObject("body")
                Gson().fromJson(bodyJson, Mesura::class.java)
            } catch (e: Exception) {
                println("Error en parsejar la resposta desxifrada: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            println("TestGetMesuraById OK: Mesura obtinguda -> Condició: ${mesura.condicio}, Valor: ${mesura.valor}")
            assertTrue(true)
        } else {
            println("Error en obtenir la mesura: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica l'obtenció de la llista d'usuaris assignats a un esdeveniment.
     */
    @Test
    fun testGetUsersByEvent() = runBlocking {
        val eventId = 1

        if (authToken == null) {
            println("TestGetUsersByEvent FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error en xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getUsersByEvent("Bearer $encryptedToken", eventId)

        if (response.isSuccessful && response.body() != null) {
            val encryptedResponse = response.body()!!.string()

            val decryptedResponse = try {
                CipherUtil.decrypt(encryptedResponse)
            } catch (e: Exception) {
                println("Error en desxifrar la resposta: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            val usersList = try {
                Gson().fromJson(decryptedResponse, Array<User>::class.java).toList()
            } catch (e: Exception) {
                println("Error en parsejar la resposta desxifrada: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            println("TestGetUsersByEvent OK: ${usersList.size} usuaris obtinguts")
            assertTrue(usersList.isNotEmpty())
        } else {
            println("Error en obtenir la llista d'usuaris: Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }

    /**
     * Verifica que s'afegeix un usuari a un esdeveniment de manera correcta.
     */
    @Test
    fun testAddUserToEvent() = runBlocking {
        val eventId = 3
        val userId = 3

        if (authToken == null) {
            println("TestAddUserToEvent FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error en xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.addUserToEvent(
            authToken = "Bearer $encryptedToken",
            esdevenimentId = eventId,
            usuariId = userId
        )

        if (response.isSuccessful && response.body() != null) {
            val responseBody = response.body()!!.string()
            println("TestAddUserToEvent OK: $responseBody")
            assertTrue(true)
        } else {
            when (response.code()) {
                401 -> println("TestAddUserToEvent FAILED: Token invàlid o inactiu.")
                404 -> println("TestAddUserToEvent FAILED: Esdeveniment o Usuari no trobats.")
                400 -> println("TestAddUserToEvent FAILED: Token no proporcionat.")
                else -> println("TestAddUserToEvent FAILED: Error inesperat. Codi de resposta: ${response.code()}")
            }
            assertTrue(false)
        }
    }

    /**
     * Verifica l'eliminació d'un usuari d'un esdeveniment.
     */
    @Test
    fun testDeleteUserFromEvent() = runBlocking {
        val eventId = 3
        val userId = 3

        if (authToken == null) {
            println("TestDeleteUserFromEvent FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error en xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.deleteUserFromEvent(
            authToken = "Bearer $encryptedToken",
            esdevenimentId = eventId,
            usuariId = userId
        )

        if (response.isSuccessful && response.body() != null) {
            val responseBody = response.body()!!.string()
            println("TestDeleteUserFromEvent OK: $responseBody")
            assertTrue(true)
        } else {
            when (response.code()) {
                401 -> println("TestDeleteUserFromEvent FAILED: Token invàlid o inactiu.")
                404 -> println("TestDeleteUserFromEvent FAILED: Esdeveniment o Usuari no trobats.")
                400 -> println("TestDeleteUserFromEvent FAILED: Token no proporcionat.")
                else -> println("TestDeleteUserFromEvent FAILED: Error inesperat. Codi de resposta: ${response.code()}")
            }
            assertTrue(false)
        }
    }

    /**
     * Verifica l'obtenció de la llista de mesures assignades a un esdeveniment.
     */
    @Test
    fun testGetMesuresEvent() = runBlocking {
        val esdevenimentId = 3

        if (authToken == null) {
            println("TestGetMesuresEvent FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val encryptedToken = try {
            CipherUtil.encrypt(authToken!!)
        } catch (e: Exception) {
            println("Error en xifrar el token: ${e.message}")
            assertTrue(false)
            return@runBlocking
        }

        val response = apiService.getMeasuresByEvent(
            authToken = "Bearer $encryptedToken",
            esdevenimentId = esdevenimentId
        )

        if (response.isSuccessful && response.body() != null) {
            val encryptedResponse = response.body()!!.string()

            val decryptedResponse = try {
                CipherUtil.decrypt(encryptedResponse)
            } catch (e: Exception) {
                println("Error al desxifrar la resposta: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            val mesuresList = try {
                Gson().fromJson(decryptedResponse, Array<Mesura>::class.java).toList()
            } catch (e: Exception) {
                println("Error al parsejar la resposta desxifrada: ${e.message}")
                assertTrue(false)
                return@runBlocking
            }

            println("TestGetMesuresEvent OK: Llista de mesures obtinguda amb èxit.")
            assertTrue(true)
        } else {
            println("TestGetMesuresEvent FAILED: No s'han pogut obtenir les mesures. Codi de resposta ${response.code()}")
            assertTrue(false)
        }
    }
}


