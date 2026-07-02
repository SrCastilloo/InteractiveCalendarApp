package com.danielcastillo.calendariointeractivo.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.danielcastillo.calendariointeractivo.data.AppUser
import com.danielcastillo.calendariointeractivo.ui.theme.Coral
import com.danielcastillo.calendariointeractivo.ui.theme.Ink
import com.danielcastillo.calendariointeractivo.ui.theme.Lagoon

@Composable
fun ProfileScreen(
    user: AppUser?,
    loading: Boolean,
    onBack: () -> Unit,
    onSave: (String, String, Uri?, String?) -> Unit
) {
    var name by rememberSaveable(user?.uid) { mutableStateOf(user?.name.orEmpty()) }
    var email by rememberSaveable(user?.uid) { mutableStateOf(user?.email.orEmpty()) }
    var selectedPhotoUri by rememberSaveable(user?.uid) { mutableStateOf<Uri?>(null) }
    var password by rememberSaveable(user?.uid) { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedPhotoUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modificar perfil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Actualiza tus datos personales",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Volver"
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileAvatar(
                    name = name,
                    selectedPhotoUri = selectedPhotoUri,
                    remotePhotoUrl = user?.photoUrl.orEmpty()
                )

                Text(
                    text = name.ifBlank { "Tu perfil" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (selectedPhotoUri == null) {
                            "Cambiar foto desde galería"
                        } else {
                            "Nueva foto seleccionada"
                        }
                    )
                }
            }
        }

        item {
            ProfileTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null
                    )
                },
                keyboardType = KeyboardType.Text
            )
        }

        item {
            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null
                    )
                },
                keyboardType = KeyboardType.Email
            )
        }

        item {
            ProfileTextField(
                value = password,
                onValueChange = { password = it },
                label = "Nueva contraseña",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null
                    )
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation()
            )
        }

        item {
            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        email.trim(),
                        selectedPhotoUri,
                        password.takeIf { it.isNotBlank() }
                    )
                },
                enabled = !loading && name.isNotBlank() && email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text("Guardar cambios")
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    selectedPhotoUri: Uri?,
    remotePhotoUrl: String
) {
    val model: Any? = selectedPhotoUri ?: remotePhotoUrl.takeIf { it.isNotBlank() }

    Surface(
        modifier = Modifier.size(104.dp),
        shape = CircleShape,
        color = Lagoon.copy(alpha = 0.18f),
        shadowElevation = 4.dp
    ) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initialsOf(name),
                    color = Lagoon,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            cursorColor = Lagoon,
            focusedBorderColor = Lagoon,
            unfocusedBorderColor = Ink.copy(alpha = 0.24f),
            focusedLabelColor = Lagoon,
            unfocusedLabelColor = Ink.copy(alpha = 0.68f),
            focusedLeadingIconColor = Lagoon,
            unfocusedLeadingIconColor = Ink.copy(alpha = 0.58f),
            errorBorderColor = Coral
        )
    )
}

private fun initialsOf(name: String): String {
    return name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "U" }
}