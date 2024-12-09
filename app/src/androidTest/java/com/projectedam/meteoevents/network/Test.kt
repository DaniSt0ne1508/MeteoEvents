package com.projectedam.meteoevents.network

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projectedam.meteoevents.MainActivity
import com.projectedam.meteoevents.ui.screens.UserViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @MockK
    lateinit var mockApiService: ApiService

    lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        // Iniciamos el MockK para el ApiService y configuramos el ViewModel
        MockKAnnotations.init(this)

        val mockResponse = LoginResponse("user", "dummyToken", "userFuncId")
        coEvery { mockApiService.login(any(), any()) } returns Response.success(mockResponse)

        userViewModel = UserViewModel()
    }

    private fun coEvery(function: () -> Response<LoginResponse>): Any {

    }

    @Test
    fun testLogin() {
        // Ingresamos un usuario y contraseña
        onView(withId(R.id.username_input))
            .perform(typeText("testUser"), closeSoftKeyboard())

        onView(withId(R.id.password_input))
            .perform(typeText("testPassword"), closeSoftKeyboard())

        // Realizamos el login
        onView(withId(R.id.login_button)).perform(click())

        // Verificamos que se muestra el mensaje de éxito o la transición de actividad
        onView(withText("Login exitoso"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFail() {
        // Ingresamos un usuario y contraseña incorrectos
        onView(withId(R.id.username_input))
            .perform(typeText("wrongUser"), closeSoftKeyboard())

        onView(withId(R.id.password_input))
            .perform(typeText("wrongPassword"), closeSoftKeyboard())

        // Realizamos el login
        onView(withId(R.id.login_button)).perform(click())

        // Verificamos que se muestra el mensaje de error
        onView(withText("Login fallido"))
            .check(matches(isDisplayed()))
    }
}

