package com.projectedam.meteoevents.network



import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


//Fet seguint el video https://www.youtube.com/watch?app=desktop&v=F5cRcqeVlRU&ab_channel=CheezyCode

/**
 * Classe de proves d'integració per a l'ApiService.
 */
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
            val loginResponse = apiService.login(username, password)

            if (loginResponse.isSuccessful && loginResponse.body() != null) {
                val loginBody = loginResponse.body()
                authToken = "Bearer ${loginBody?.token}"
                if (authToken != null) {
                    println("Token obtingut correctament.")
                } else {
                    println("Error al obtenir el token.")
                }
            } else {
                println("Login FAILED: Codi de resposta ${loginResponse.code()}")
            }
        }
    }

    /**
     * Verifica que el login i el logout funcionen correctament.
     */
    @Test
    fun testLoginAndLogout() = runBlocking {
        if (authToken == null) {
            println("Login FAILED: No s'ha obtingut el token abans de la prova.")
            assertTrue(false)
            return@runBlocking
        }

        val logoutResponse = apiService.logout(authToken!!)

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
        val response = apiService.login(username, password)

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

        val response = apiService.getUsers(authToken!!)

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

        val response = apiService.getEsdeveniments(authToken!!)

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

        val response = apiService.getMesures(authToken!!)

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
            println("TestCreateUser FAILED: No s'ha obtingut el token")
            assertTrue(false)
            return@runBlocking
        }

        val user = User(
            id = "",
            nomC = "Nou Nom",
            nomUsuari = "nouUsuari",
            contrasenya = "NouPassword123",
            dataNaixement = "2000-01-01",
            sexe = "M",
            poblacio = "Barcelona",
            email = "nouemail@example.com",
            telefon = "123456789",
            descripcio = "Usuari de prova",
            funcionalId = "USR",
            username = "nouUsuari",
            password = "NouPassword123"
        )

        val response = apiService.createUser(authToken!!, user)

        if (response.code() == 201) {
            println("TestCreateUser OK")
            assertTrue(response.isSuccessful)
        } else {
            println("Error en crear l'usuari: Codi de resposta ${response.code()}")
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

        val esdeveniment = Esdeveniment(
            nom = "Esdeveniment de prova",
            descripcio = "Descripció de l'esdeveniment de prova",
            organitzador = "Organitzador Prova",
            direccio = "Carrer de la prova, 123",
            codiPostal = "08001",
            poblacio = "Barcelona",
            aforament = "100",
            horari = "14:00-18:00"
        )

        val response = apiService.createEsdeveniment(authToken!!, esdeveniment)

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

        val mesura = Mesura(
            condicio = "Temperatura",
            valor = 40.0,
            valorUm = "graus",
            accio = "Activar aire acondicionat!!!"
        )

        val response = apiService.createMesura(authToken!!, mesura)

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


