package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.Mesura

/**
 * Pantalla de gestió de mesures de seguretat.
 *
 * Aquesta pantalla permet visualitzar, crear, editar i eliminar mesures de seguretat.
 *
 * @param userViewModel ViewModel que gestiona la lògica de mesures de seguretat i usuaris.
 * @param onNavigateBack Callback per tornar a la pantalla anterior.
 */
@Composable
fun MesuresSeguretatManagementScreen(
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val mesuresList = remember { mutableStateOf<List<Mesura>>(emptyList()) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isEditDialogOpen = remember { mutableStateOf(false) }
    val isCreateDialogOpen = remember { mutableStateOf(false) }
    val mesuraToEdit = remember { mutableStateOf<Mesura?>(null) }
    val isAdmin = userViewModel.funcionalId == "ADM"
    val mesuraToView = remember { mutableStateOf<Mesura?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        userViewModel.seeMesures(
            onSuccess = { mesures ->
                mesuresList.value = mesures
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    val onViewMesura: (Int) -> Unit = { id ->
        userViewModel.getMesuraById(
            id = id,
            onSuccess = { mesura ->
                mesuraToView.value = mesura
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
            "Gestió de Mesures de Seguretat",
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
                Text("Crear Mesura")
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
                items(mesuresList.value) { mesura ->
                    MesuraSeguretatItem(
                        mesura = mesura,
                        isAdmin = isAdmin,
                        onEditClick = {
                            mesuraToEdit.value = mesura
                            isEditDialogOpen.value = true
                        },
                        onDeleteClick = {
                            userViewModel.deleteMesura(
                                id = mesura.id!!,
                                onSuccess = {
                                    mesuresList.value =
                                        mesuresList.value.filter { it.id != mesura.id }
                                },
                                onFailure = { error -> errorMessage.value = error }
                            )
                        },
                        onViewClick = onViewMesura
                    )
                }
            }
        }
        mesuraToView.value?.let { mesura ->
            AlertDialog(
                onDismissRequest = { mesuraToView.value = null },
                title = { Text("Detalls de la Mesura") },
                text = {
                    Column {
                        Text("ID: ${mesura.id}")
                        Text("Condicio: ${mesura.condicio}")
                        Text("Valor: ${mesura.valor}")
                        Text("ValorUm: ${mesura.valorUm}")
                        Text("Acció: ${mesura.accio}")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { mesuraToView.value = null }
                    ) {
                        Text("Tancar")
                    }
                }
            )
        }
    }

    if (isCreateDialogOpen.value) {
        EditMesuraDialog(
            mesura = Mesura(
                id = null,
                condicio = "",
                valor = 0.0,
                valorUm = "",
                accio = ""
            ),
            onDismiss = { isCreateDialogOpen.value = false }
        ) { newMesura ->
            userViewModel.createMesura(
                mesura = newMesura,
                onSuccess = {
                    mesuresList.value = mesuresList.value + newMesura
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

    if (isEditDialogOpen.value && mesuraToEdit.value != null) {
        EditMesuraDialog(
            mesura = mesuraToEdit.value!!,
            onDismiss = { isEditDialogOpen.value = false }
        ) { updatedMesura ->
            val mesuraId = updatedMesura.id
            if (mesuraId != null) {
                userViewModel.updateMesura(
                    id = mesuraId,
                    mesura = updatedMesura,
                    onSuccess = {
                        mesuresList.value = mesuresList.value.map {
                            if (it.id == mesuraId) updatedMesura else it
                        }
                        isEditDialogOpen.value = false
                    },
                    onFailure = { error ->
                        errorMessage.value = error
                        isEditDialogOpen.value = false
                    }
                )
            } else {
                errorMessage.value = "El id de la mesura no és vàlid"
            }
        }
    }
}

/**
 * Representa un element de mesura dins d'una llista.
 *
 * @param mesura La mesura de seguretat a mostrar.
 * @param onEditClick Callback per a editar la mesura.
 * @param onDeleteClick Callback per a eliminar la mesura.
 */
@Composable
fun MesuraSeguretatItem(mesura: Mesura, onEditClick: () -> Unit, onDeleteClick: () -> Unit, isAdmin: Boolean, onViewClick: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Condicio: ${mesura.condicio}", fontWeight = FontWeight.Bold)
            Text("Valor: ${mesura.valor}")
            Text("ValorUm: ${mesura.valorUm}")
            Text("Acció: ${mesura.accio}")
            Button(
                onClick = { onViewClick(mesura.id!!) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Veure")
            }
            if (isAdmin) {
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
 * Diàleg per editar o crear una mesura de seguretat.
 *
 * @param mesura La mesura de seguretat a editar o base per a crear-ne una de nova.
 * @param onDismiss Callback per tancar el diàleg sense guardar.
 * @param onSave Callback per guardar els canvis de la mesura.
 */
@Composable
fun EditMesuraDialog(
    mesura: Mesura,
    onDismiss: () -> Unit,
    onSave: (Mesura) -> Unit
) {
    var condicio by remember { mutableStateOf(mesura.condicio) }
    var valor by remember { mutableStateOf(mesura.valor) }
    var valorUm by remember { mutableStateOf(mesura.valorUm) }
    var accio by remember { mutableStateOf(mesura.accio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Mesura de Seguretat") },
        text = {
            Column {
                TextField(
                    value = condicio,
                    onValueChange = { condicio = it },
                    label = { Text("Condicio") }
                )
                //Implementat amb IA BlackBox, amb prompt: How do I pass a textfield with a double in Kotlin.
                TextField(
                    value = valor.toString(),
                    onValueChange = { input ->
                        valor = input.toDoubleOrNull() ?: valor
                    },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = valorUm,
                    onValueChange = { valorUm = it },
                    label = { Text("ValorUm") }
                )
                TextField(
                    value = accio,
                    onValueChange = { accio = it },
                    label = { Text("Acció") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedMesura = mesura.copy(
                        condicio = condicio,
                        valor = valor,
                        valorUm = valorUm,
                        accio = accio
                    )
                    onSave(updatedMesura)
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