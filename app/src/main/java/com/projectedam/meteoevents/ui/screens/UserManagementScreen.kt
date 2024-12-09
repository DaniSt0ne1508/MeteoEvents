package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.User

/**
 * Pantalla de gestió d'usuaris.
 *
 * Aquesta pantalla permet als administradors gestionar usuaris (crear, editar i eliminar).
 * Els usuaris no administradors poden veure i editar només la seva pròpia informació.
 *
 * @param userViewModel Instància del ViewModel per gestionar usuaris.
 * @param onNavigateBack Funció per tornar a la pantalla anterior.
 */
@Composable
fun UserManagementScreen(
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val userList = remember { mutableStateOf<List<User>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isEditDialogOpen = remember { mutableStateOf(false) }
    val isCreateDialogOpen = remember { mutableStateOf(false) }
    val userToEdit = remember { mutableStateOf<User?>(null) }
    val isAdmin = userViewModel.funcionalId == "ADM"

    LaunchedEffect(Unit) {
        isLoading.value = true
        userViewModel.seeUsers(
            onSuccess = { users ->
                userList.value = if (isAdmin) users else users.filter { it.nomUsuari == userViewModel.currentUserName }
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onNavigateBack, modifier = Modifier
            .align(Alignment.Start)
            .padding(16.dp),
        ) {
            Text("Tornar")
        }

        Text("Gestió d'usuaris", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))

        if (isAdmin) {
            Button(
                onClick = { isCreateDialogOpen.value = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Crear Usuari")
            }
        }

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            errorMessage.value?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors()) {
                    Text("Tornar a la pantalla principal")
                }
            } ?: LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(userList.value) { user ->
                    UserItem(
                        user = user,
                        isAdmin = isAdmin,
                        onEditClick = {
                            userToEdit.value = user
                            isEditDialogOpen.value = true
                        },
                        onDeleteClick = {
                            userViewModel.deleteUser(
                                userId = user.id,
                                onSuccess = {
                                    userList.value = userList.value.filter { it.id != user.id }
                                },
                                onFailure = { error -> errorMessage.value = error }
                            )
                        }
                    )
                }
            }
        }
    }
    if (isCreateDialogOpen.value) {
        EditUserDialog(
            user = User(id = "", nomC = "", nomUsuari = "", contrasenya = "", dataNaixement = "", sexe = "", poblacio = "", email = "", telefon = "", descripcio = "", funcionalId = ""),
            isAdmin = isAdmin,
            onDismiss = { isCreateDialogOpen.value = false }
        ) { newUser ->
            userViewModel.createUser(
                user = newUser,
                onSuccess = {
                    userList.value = userList.value + newUser
                    isCreateDialogOpen.value = false
                    onNavigateBack()
                },
                onFailure = { error ->
                    errorMessage.value = error
                    isCreateDialogOpen.value = false
                }
            )
        }
    }

    if (isEditDialogOpen.value && userToEdit.value != null) {
        EditUserDialog(
            user = userToEdit.value!!,
            isAdmin = isAdmin,
            onDismiss = { isEditDialogOpen.value = false }
        ) { updatedUser ->
            userViewModel.updateUser(
                user = updatedUser,
                onSuccess = {
                    userList.value = userList.value.map { if (it.id == updatedUser.id) updatedUser else it }
                    isEditDialogOpen.value = false
                },
                onFailure = { error ->
                    errorMessage.value = error
                    isEditDialogOpen.value = false
                }
            )
        }
    }
}

/**
 * Component per mostrar la informació d'un usuari amb opcions per editar o eliminar.
 *
 * @param user L'usuari que es mostra.
 * @param onEditClick Funció que s'executa quan es prem el botó per editar.
 * @param onDeleteClick Funció que s'executa quan es prem el botó per eliminar.
 * @param isAdmin Indica si l'usuari actual és administrador.
 */
@Composable
fun UserItem(user: User, onEditClick: () -> Unit, onDeleteClick: () -> Unit, isAdmin: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nom: ${user.nomC}", fontWeight = FontWeight.Bold)
            Text("Usuari: ${user.nomUsuari}")
            Text("Rol: ${user.funcionalId}")
            Text("Email: ${user.email}")
            Text("Telèfon: ${user.telefon}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onEditClick) {
                    Text("Edita")
                }
                if (isAdmin) {
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                    ) {
                        Text("Elimina")
                    }
                }
            }
        }
    }
}

/**
 * Diàleg per editar o crear un usuari.
 *
 * @param user L'usuari a editar o les dades inicials per a un nou usuari.
 * @param isAdmin Indica si l'usuari actual és administrador.
 * @param onDismiss Funció per tancar el diàleg sense desar.
 * @param onSave Funció per desar els canvis realitzats a l'usuari.
 */
@Composable
fun EditUserDialog(user: User, isAdmin: Boolean, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var nomC by remember { mutableStateOf(TextFieldValue(user.nomC ?: "")) }
    var nomUsuari by remember { mutableStateOf(TextFieldValue(user.nomUsuari ?: "")) }
    var funcionalId by remember { mutableStateOf(TextFieldValue(user.funcionalId ?: "")) }
    var contrasenya by remember { mutableStateOf(TextFieldValue(user.contrasenya ?: "")) }
    var dataNaixement by remember { mutableStateOf(TextFieldValue(user.dataNaixement ?: "")) }
    var sexe by remember { mutableStateOf(TextFieldValue(user.sexe ?: "")) }
    var poblacio by remember { mutableStateOf(TextFieldValue(user.poblacio ?: "")) }
    var email by remember { mutableStateOf(TextFieldValue(user.email ?: "")) }
    var telefon by remember { mutableStateOf(TextFieldValue(user.telefon ?: "")) }
    var descripcio by remember { mutableStateOf(TextFieldValue(user.descripcio ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edita Usuari") },
        text = {
            Column (modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
            ){
                if (isAdmin) {
                    OutlinedTextField(value = funcionalId, onValueChange = { funcionalId = it }, label = { Text("Funcional ID") })
                } else {
                    OutlinedTextField(
                        value = funcionalId,
                        onValueChange = {},
                        label = { Text("Funcional ID") },
                        enabled = false
                    )
                }
                OutlinedTextField(value = nomC, onValueChange = { nomC = it }, label = { Text("Nom complet") })
                OutlinedTextField(value = nomUsuari, onValueChange = { nomUsuari = it }, label = { Text("Nom d'usuari") })
                OutlinedTextField(value = contrasenya, onValueChange = { contrasenya = it }, label = { Text("Contrasenya") })
                OutlinedTextField(value = dataNaixement, onValueChange = { dataNaixement = it }, label = { Text("Data Naixement (YYYY-MM-DD)") })
                OutlinedTextField(value = sexe, onValueChange = { sexe = it }, label = { Text("Sexe") })
                OutlinedTextField(value = poblacio, onValueChange = { poblacio = it }, label = { Text("Població") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = telefon, onValueChange = { telefon = it }, label = { Text("Telèfon") })
                OutlinedTextField(value = descripcio, onValueChange = { descripcio = it }, label = { Text("Descripció") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    user.copy(
                        nomC = nomC.text,
                        nomUsuari = nomUsuari.text,
                        funcionalId = funcionalId.text,
                        contrasenya = contrasenya.text,
                        dataNaixement = dataNaixement.text,
                        sexe = sexe.text,
                        poblacio = poblacio.text,
                        email = email.text,
                        telefon = telefon.text,
                        descripcio = descripcio.text
                    )
                )
            }) {
                Text("Guarda")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel·la")
            }
        }
    )
}
