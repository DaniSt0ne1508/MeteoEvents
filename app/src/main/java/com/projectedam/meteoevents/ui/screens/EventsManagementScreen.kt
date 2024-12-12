package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.Esdeveniment
import com.projectedam.meteoevents.network.User

/**
 * Pantalla de gestió d'esdeveniments.
 *
 * Aquesta pantalla permet visualitzar, crear, editar i eliminar esdeveniments.
 *
 * @param userViewModel ViewModel que gestiona la lògica d'esdeveniments i usuaris.
 * @param onNavigateBack Callback per tornar a la pantalla anterior.
 */
@Composable
fun EsdevenimentsManagementScreen(
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val esdevenimentsList = remember { mutableStateOf<List<Esdeveniment>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isEditDialogOpen = remember { mutableStateOf(false) }
    val isCreateDialogOpen = remember { mutableStateOf(false) }
    val esdevenimentToEdit = remember { mutableStateOf<Esdeveniment?>(null) }
    val isAdmin = userViewModel.funcionalId == "ADM"
    val eventToView = remember { mutableStateOf<Esdeveniment?>(null) }
    val usersForEvent = remember { mutableStateOf<List<User>?>(null) }
    val isUsersDialogOpen = remember { mutableStateOf(false) }
    val userErrorMessage = remember { mutableStateOf<String?>(null) }

    val onViewUsers: (Int) -> Unit = { eventId ->
        userViewModel.getUsersEvents(
            eventId = eventId,
            onSuccess = { users ->
                usersForEvent.value = users
                isUsersDialogOpen.value = true
            },
            onFailure = { error ->
                userErrorMessage.value = error
                isUsersDialogOpen.value = true
            }
        )
    }


    LaunchedEffect(Unit) {
        isLoading.value = true
        userViewModel.seeEvents(
            onSuccess = { events ->
                esdevenimentsList.value = events
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    val onViewEvent: (Int) -> Unit = { id ->
        userViewModel.getEventById(
            id = id,
            onSuccess = { event ->
                eventToView.value = event
            },
            onFailure = { error ->
                errorMessage.value = error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(16.dp)
        ) {
            Text("Tornar")
        }

        Text(
            "Gestió d'Esdeveniments",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if(isAdmin){
            Button(
                onClick = { isCreateDialogOpen.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Crear Esdeveniment")
            }
        }

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            errorMessage.value?.let { error ->
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) {
                    Text("Tornar a la pantalla principal")
                }
            } ?: LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(esdevenimentsList.value) { esdeveniment ->
                    EsdevenimentItem(
                        esdeveniment = esdeveniment,
                        isAdmin = isAdmin,
                        onEditClick = {
                            esdevenimentToEdit.value = esdeveniment
                            isEditDialogOpen.value = true
                        },
                        onDeleteClick = {
                            userViewModel.deleteEvent(
                                id = esdeveniment.id!!,
                                onSuccess = {
                                    esdevenimentsList.value =
                                        esdevenimentsList.value.filter { it.id != esdeveniment.id }
                                },
                                onFailure = { error -> errorMessage.value = error }
                            )
                        },
                        onViewClick = onViewEvent,
                        onViewUsersClick = onViewUsers
                    )
                }
            }
        }
        eventToView.value?.let { event ->
            AlertDialog(
                onDismissRequest = { eventToView.value = null },
                title = { Text("Detalls de l'Esdeveniment") },
                text = {
                    Column {
                        Text("ID: ${event.id}")
                        Text("Nom: ${event.nom}")
                        Text("Descripció: ${event.descripcio}")
                        Text("Organitzador: ${event.organitzador}")
                        Text("Direccio: ${event.direccio}")
                        Text("Codi Postal: ${event.codiPostal}")
                        Text("Poblacio: ${event.poblacio}")
                        Text("Aforament: ${event.aforament}")
                        Text("Hora Inici: ${event.hora_inici}")
                        Text("Hora Fi: ${event.hora_fi}")
                        Text("Data: ${event.data_esde}")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { eventToView.value = null }
                    ) {
                        Text("Tancar")
                    }
                }
            )
        }
    }

    if (isUsersDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isUsersDialogOpen.value = false },
            title = { Text("Usuaris de l'Esdeveniment") },
            text = {
                usersForEvent.value?.let { users ->
                    LazyColumn {
                        items(users) { user ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "Nom: ${user.nomC}"
                                )
                                Text(
                                    text = "Email: ${user.email}"
                                )
                            }
                        }
                    }
                } ?: Text(userErrorMessage.value ?: "No s'han pogut obtenir els usuaris.")
            },
            confirmButton = {
                Button(onClick = { isUsersDialogOpen.value = false }) {
                    Text("Tancar")
                }
            }
        )
    }

    if (isCreateDialogOpen.value) {
        EditEventDialog(
            esdeveniment = Esdeveniment(
                id = null,
                nom = "",
                descripcio = "",
                organitzador = "",
                direccio = "",
                aforament = "",
                codiPostal = "",
                hora_inici = "",
                hora_fi = "",
                data_esde = "",
                poblacio = ""
            ),
            onDismiss = { isCreateDialogOpen.value = false }
        ) { newEsdeveniment ->
            userViewModel.createEvent(
                esdeveniment = newEsdeveniment,
                onSuccess = {
                    esdevenimentsList.value = esdevenimentsList.value + newEsdeveniment
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

    if (isEditDialogOpen.value && esdevenimentToEdit.value != null) {
        EditEventDialog(
            esdeveniment = esdevenimentToEdit.value!!,
            onDismiss = { isEditDialogOpen.value = false }
        ) { updatedEsdeveniment ->
            val esdevenimentId = updatedEsdeveniment.id
            if (esdevenimentId != null) {
                userViewModel.updateEvent(
                    esdevenimentId = esdevenimentId,
                    esdeveniment = updatedEsdeveniment,
                    onSuccess = {
                        esdevenimentsList.value = esdevenimentsList.value.map {
                            if (it.id == esdevenimentId) updatedEsdeveniment else it
                        }
                        isEditDialogOpen.value = false
                    },
                    onFailure = { error ->
                        errorMessage.value = error
                        isEditDialogOpen.value = false
                    }
                )
            } else {
                errorMessage.value = "El id de l'esdeveniment no és vàlid"
            }
        }
    }
}

/**
 * Representa un element d'esdeveniment dins d'una llista.
 *
 * @param esdeveniment L'esdeveniment a mostrar.
 * @param onEditClick Callback per a editar l'esdeveniment.
 * @param onDeleteClick Callback per a eliminar l'esdeveniment.
 */
@Composable
fun EsdevenimentItem(
    esdeveniment: Esdeveniment,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isAdmin: Boolean,
    onViewClick: (Int) -> Unit,
    onViewUsersClick: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nom: ${esdeveniment.nom}", fontWeight = FontWeight.Bold)
            Text("Descripció: ${esdeveniment.descripcio}")
            Text("Organitzador:: ${esdeveniment.organitzador}")
            Text("Direccio: ${esdeveniment.direccio}")
            Text("Data: ${esdeveniment.data_esde}")
            Button(
                onClick = { onViewClick(esdeveniment.id!!) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Veure")
            }
            Button(
                onClick = { onViewUsersClick(esdeveniment.id!!) }, // Nuevo botón
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Usuaris")
            }
            if(isAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onEditClick) {
                        Text("Edita")
                    }
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
 * Diàleg per editar o crear un esdeveniment.
 *
 * @param esdeveniment L'esdeveniment a editar o base per a crear-ne un de nou.
 * @param onDismiss Callback per tancar el diàleg sense guardar.
 * @param onSave Callback per guardar els canvis de l'esdeveniment.
 */
@Composable
fun EditEventDialog(
    esdeveniment: Esdeveniment,
    onDismiss: () -> Unit,
    onSave: (Esdeveniment) -> Unit
) {
    var nom by remember { mutableStateOf(esdeveniment.nom ?: "") }
    var descripcio by remember { mutableStateOf(esdeveniment.descripcio ?: "") }
    var organitzador by remember { mutableStateOf(esdeveniment.organitzador ?: "") }
    var direccio by remember { mutableStateOf(esdeveniment.direccio ?: "") }
    var codiPostal by remember { mutableStateOf(esdeveniment.codiPostal ?: "") }
    var poblacio by remember { mutableStateOf(esdeveniment.poblacio ?: "") }
    var aforament by remember { mutableStateOf(esdeveniment.aforament ?: "") }
    var horari by remember { mutableStateOf(esdeveniment.hora_inici ?: "") }
    var horariFins by remember { mutableStateOf(esdeveniment.hora_fi ?: "") }
    var data by remember { mutableStateOf(esdeveniment.data_esde ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Esdeveniment") },
        text = {
            Column {
                TextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") }
                )
                TextField(
                    value = descripcio,
                    onValueChange = { descripcio = it },
                    label = { Text("Descripció") }
                )
                TextField(
                    value = organitzador,
                    onValueChange = { organitzador = it },
                    label = { Text("Organitzador") }
                )
                TextField(
                    value = direccio,
                    onValueChange = { direccio = it },
                    label = { Text("Direccio") }
                )
                TextField(
                    value = codiPostal,
                    onValueChange = { codiPostal = it },
                    label = { Text("Codi Postal") }
                )
                TextField(
                    value = poblacio,
                    onValueChange = { poblacio = it },
                    label = { Text("Poblacio") }
                )
                TextField(
                    value = aforament,
                    onValueChange = { aforament = it },
                    label = { Text("Aforament") }
                )
                TextField(
                    value = horari,
                    onValueChange = { horari = it },
                    label = { Text("Horari Inici") }
                )
                TextField(
                    value = horariFins,
                    onValueChange = { horariFins = it },
                    label = { Text("Horari Fi") }
                )
                TextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Data") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedEsdeveniment = esdeveniment.copy(
                        nom = nom,
                        descripcio = descripcio,
                        organitzador = organitzador,
                        direccio = direccio,
                        codiPostal = codiPostal,
                        poblacio = poblacio,
                        aforament = aforament,
                        hora_inici = horari,
                        hora_fi = horariFins,
                        data_esde = data
                    )
                    onSave(updatedEsdeveniment)
                }
            ) {
                Text("Desar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}
