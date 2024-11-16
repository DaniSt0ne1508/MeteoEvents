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

        Button(
            onClick = { isCreateDialogOpen.value = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Crear Esdeveniment")
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
                        }
                    )
                }
            }
        }
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
                horari = "",
                poblacio = ""
            ),
            onDismiss = { isCreateDialogOpen.value = false }
        ) { newEsdeveniment ->
            userViewModel.createEvent(
                esdeveniment = newEsdeveniment,
                onSuccess = {
                    esdevenimentsList.value = esdevenimentsList.value + newEsdeveniment
                    isCreateDialogOpen.value = false
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
                    id = esdevenimentId,
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
fun EsdevenimentItem(esdeveniment: Esdeveniment, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nom: ${esdeveniment.nom}", fontWeight = FontWeight.Bold)
            Text("Descripció: ${esdeveniment.descripcio}")
            Text("Data: ${esdeveniment.organitzador}")
            Text("Lloc: ${esdeveniment.direccio}")
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
    var nom by remember { mutableStateOf(esdeveniment.nom) }
    var descripcio by remember { mutableStateOf(esdeveniment.descripcio) }
    var organitzador by remember { mutableStateOf(esdeveniment.organitzador) }
    var direccio by remember { mutableStateOf(esdeveniment.direccio) }

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
                    label = { Text("Data") }
                )
                TextField(
                    value = direccio,
                    onValueChange = { direccio = it },
                    label = { Text("Lloc") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedEsdeveniment = esdeveniment.copy(nom = nom, descripcio = descripcio, organitzador = organitzador, direccio = direccio)
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