package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.User

@Composable
fun UserManagementScreen(userViewModel: UserViewModel = viewModel()) {
    val userList = remember { mutableStateOf<List<User>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isEditDialogOpen = remember { mutableStateOf(false) }
    val userToEdit = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        userViewModel.seeUsers(
            onSuccess = { users ->
                userList.value = users
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestió d'usuaris", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 16.dp))

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (errorMessage.value != null) {
                Text(errorMessage.value!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(userList.value) { user ->
                        UserItem(user, onEditClick = {
                            userToEdit.value = user
                            isEditDialogOpen.value = true
                        }, onDeleteClick = {
                            userViewModel.deleteUser(
                                userId = user.id,
                                onSuccess = {
                                    userList.value = userList.value.filter { it.id != user.id }
                                },
                                onFailure = { error ->
                                    errorMessage.value = error
                                }
                            )
                        })
                    }
                }
            }
        }
    }

    // Dialog for editing user information
    if (isEditDialogOpen.value && userToEdit.value != null) {
        EditUserDialog(user = userToEdit.value!!, onDismiss = { isEditDialogOpen.value = false }) { updatedUser ->
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

@Composable
fun UserItem(user: User, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nom: ${user.nomC}")
            Text("Usuari: ${user.nomUsuari}")
            Text("Email: ${user.email}")
            Text("Telèfon: ${user.telefon}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onEditClick) {
                    Text("Edita")
                }
                Button(onClick = onDeleteClick, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                    Text("Elimina")
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(user: User, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var nomC by remember { mutableStateOf(TextFieldValue(user.nomC)) }
    var nomUsuari by remember { mutableStateOf(TextFieldValue(user.nomUsuari)) }
    var email by remember { mutableStateOf(TextFieldValue(user.email)) }
    var telefon by remember { mutableStateOf(TextFieldValue(user.telefon)) }
    var descripcio by remember { mutableStateOf(TextFieldValue(user.descripcio)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edita Usuari") },
        text = {
            Column {
                OutlinedTextField(value = nomC, onValueChange = { nomC = it }, label = { Text("Nom complet") })
                OutlinedTextField(value = nomUsuari, onValueChange = { nomUsuari = it }, label = { Text("Nom d'usuari") })
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