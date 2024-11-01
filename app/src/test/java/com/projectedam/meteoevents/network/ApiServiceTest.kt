package com.projectedam.meteoevents.network



import com.google.gson.Gson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//Fet seguint el video https://www.youtube.com/watch?app=desktop&v=F5cRcqeVlRU&ab_channel=CheezyCode

/**
 * Classe de proves d'integració per a l'ApiService.
 */
class IntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    /**
     * Configura el servidor i el servei d'API abans de cada prova.
     */
    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Tanca el servidor després de cada prova.
     */
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    /**
     * Prova per verificar que el login funciona correctament.
     */
    @Test
    fun testLogin() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                    "userType": "ADM",
                    "token": "test_token",
                    "funcionalId": "12345"
                }
                """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse) // Encolar la respuesta

        val username = "testUser"
        val password = "testPassword"

        val response = apiService.login(username, password)

        assertTrue(response.isSuccessful)
        val loginResponse = response.body()
        assertEquals("test_token", loginResponse?.token)
        assertEquals("ADM", loginResponse?.userType)
        assertEquals("12345", loginResponse?.funcionalId)
    }

    /**
     * Prova per verificar que el logout funciona correctament.
     */
    @Test
    fun testLogout() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                    "message": "Logout successful"
                }
                """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)

        val authToken = "Bearer test_token"

        val response: Response<Unit> = apiService.logout(authToken)

        // Assert
        assertTrue(response.isSuccessful)
        assertEquals(Unit, response.body())
    }

    /**
     * Prova per verificar que el login gestiona errors correctament.
     */
    @Test
    fun testLoginError() = runBlocking {

        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody(
                """
                {
                    "error": "Unauthorized" // Missatge d'error
                }
                """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)


        val username = "wrongUser"
        val password = "wrongPassword"


        val response = apiService.login(username, password)


        assertTrue(response.code() == 401)
        assertTrue(response.errorBody()?.string()?.contains("Unauthorized") == true)
    }

    /**
     * Prova per verificar que el logout gestiona errors correctament.
     */
    @Test
    fun testLogoutError() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody(
                """
                {
                    "error": "Internal Server Error" // Missatge d'error
                }
                """.trimIndent()
            )
        mockWebServer.enqueue(mockResponse)

        val authToken = "Bearer test_token"

        val response: Response<Unit> = apiService.logout(authToken)

        assertTrue(response.code() == 500)
        assertTrue(response.errorBody()?.string()?.contains("Internal Server Error") == true)
    }
}
