package com.danielcastillo.calendariointeractivo.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danielcastillo.calendariointeractivo.ui.theme.Coral
import com.danielcastillo.calendariointeractivo.ui.theme.Gold
import com.danielcastillo.calendariointeractivo.ui.theme.Ink
import com.danielcastillo.calendariointeractivo.ui.theme.Iris
import com.danielcastillo.calendariointeractivo.ui.theme.Lagoon

@Composable
fun AuthScreen(
    loading: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, Uri?) -> Unit,
    onResetPassword: (String) -> Unit
) {
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    val canSubmit = email.isNotBlank() &&
            password.length >= 6 &&
            (!registerMode || name.isNotBlank()) &&
            !loading

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Ink,
                        Color(0xFF132A3A),
                        Color(0xFF0F766E),
                        Color(0xFF18254A)
                    )
                )
            )
    ) {
        val expanded = maxWidth >= 840.dp

        AuthBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
            contentPadding = PaddingValues(
                horizontal = if (expanded) 48.dp else 20.dp,
                vertical = if (expanded) 36.dp else 22.dp
            ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (expanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(620.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(36.dp)
                    ) {
                        AuthHeroPanel(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        AuthFormCard(
                            registerMode = registerMode,
                            name = name,
                            email = email,
                            password = password,
                            photoUri = photoUri,
                            showPassword = showPassword,
                            loading = loading,
                            canSubmit = canSubmit,
                            onRegisterModeChange = { registerMode = it },
                            onNameChange = { name = it },
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            onPickPhoto = { imagePicker.launch("image/*") },
                            onShowPasswordChange = { showPassword = it },
                            onSubmit = {
                                focusManager.clearFocus()

                                if (registerMode) {
                                    onRegister(
                                        name.trim(),
                                        email.trim(),
                                        password,
                                        photoUri
                                    )
                                } else {
                                    onLogin(
                                        email.trim(),
                                        password
                                    )
                                }
                            },
                            onResetPassword = {
                                focusManager.clearFocus()
                                onResetPassword(email.trim())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 520.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        MiniAuthHeader()

                        AuthFormCard(
                            registerMode = registerMode,
                            name = name,
                            email = email,
                            password = password,
                            photoUri = photoUri,
                            showPassword = showPassword,
                            loading = loading,
                            canSubmit = canSubmit,
                            onRegisterModeChange = { registerMode = it },
                            onNameChange = { name = it },
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            onPickPhoto = { imagePicker.launch("image/*") },
                            onShowPasswordChange = { showPassword = it },
                            onSubmit = {
                                focusManager.clearFocus()

                                if (registerMode) {
                                    onRegister(
                                        name.trim(),
                                        email.trim(),
                                        password,
                                        photoUri
                                    )
                                } else {
                                    onLogin(
                                        email.trim(),
                                        password
                                    )
                                }
                            },
                            onResetPassword = {
                                focusManager.clearFocus()
                                onResetPassword(email.trim())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 520.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Lagoon.copy(alpha = 0.25f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.12f, size.height * 0.12f)
        )

        drawCircle(
            color = Coral.copy(alpha = 0.18f),
            radius = size.minDimension * 0.26f,
            center = Offset(size.width * 0.92f, size.height * 0.22f)
        )

        drawCircle(
            color = Gold.copy(alpha = 0.16f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.72f, size.height * 0.92f)
        )

        drawCircle(
            color = Iris.copy(alpha = 0.18f),
            radius = size.minDimension * 0.18f,
            center = Offset(size.width * 0.18f, size.height * 0.82f)
        )
    }
}

@Composable
private fun MiniAuthHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.16f),
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "31",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            text = "Calendario interactivo",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Organiza eventos, comparte planes y recibe recordatorios sin perderte nada.",
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthHeroPanel(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(36.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFFE8FFFA),
                            Color(0xFFEFF2FF)
                        )
                    )
                )
                .padding(34.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(104.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Ink
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "31",
                            color = Gold,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Tu calendario,\npero con vida.",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )

                    Text(
                        text = "Eventos compartidos, perfiles reales, recordatorios y una experiencia pensada para móvil, tablet y pantallas grandes.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink.copy(alpha = 0.72f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroPill("Eventos")
                    HeroPill("Recordatorios")
                    HeroPill("Perfiles")
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String) {
    Surface(
        shape = CircleShape,
        color = Lagoon.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Ink,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AuthFormCard(
    registerMode: Boolean,
    name: String,
    email: String,
    password: String,
    photoUri: Uri?,
    showPassword: Boolean,
    loading: Boolean,
    canSubmit: Boolean,
    onRegisterModeChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onShowPasswordChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onResetPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(34.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.96f))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (registerMode) "Crear cuenta" else "Bienvenido de nuevo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Ink
                )

                Text(
                    text = if (registerMode) {
                        "Regístrate para empezar a publicar eventos en el calendario."
                    } else {
                        "Inicia sesión para acceder a tus eventos y recordatorios."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink.copy(alpha = 0.68f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = !registerMode,
                    onClick = { onRegisterModeChange(false) },
                    label = { Text("Iniciar sesión") },
                    leadingIcon = if (!registerMode) {
                        {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Ink,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )

                FilterChip(
                    selected = registerMode,
                    onClick = { onRegisterModeChange(true) },
                    label = { Text("Registro") },
                    leadingIcon = if (registerMode) {
                        {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Ink,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }

            AnimatedVisibility(visible = registerMode) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuthTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Nombre",
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null)
                        },
                        keyboardType = KeyboardType.Text
                    )

                    OutlinedButton(
                        onClick = onPickPhoto,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = if (photoUri == null) {
                                "Elegir foto de perfil"
                            } else {
                                "Foto seleccionada"
                            }
                        )
                    }
                }
            }

            AuthTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Correo electrónico",
                leadingIcon = {
                    Icon(Icons.Rounded.Email, contentDescription = null)
                },
                keyboardType = KeyboardType.Email
            )

            AuthTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onShowPasswordChange(!showPassword) }
                    ) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Rounded.VisibilityOff
                            } else {
                                Icons.Rounded.Visibility
                            },
                            contentDescription = null
                        )
                    }
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                }
            )

            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Ink,
                    disabledContainerColor = Gold.copy(alpha = 0.38f),
                    disabledContentColor = Ink.copy(alpha = 0.5f)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Ink
                    )
                } else {
                    Icon(
                        imageVector = if (registerMode) {
                            Icons.Rounded.Check
                        } else {
                            Icons.AutoMirrored.Rounded.ArrowForward
                        },
                        contentDescription = null
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = if (registerMode) "Crear cuenta" else "Entrar",
                    fontWeight = FontWeight.Black
                )
            }

            TextButton(
                onClick = onResetPassword,
                enabled = !loading && email.isNotBlank(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Recuperar contraseña")
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            disabledTextColor = Ink.copy(alpha = 0.45f),
            errorTextColor = Coral,

            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.7f),
            errorContainerColor = Color.White,

            focusedBorderColor = Lagoon,
            unfocusedBorderColor = Ink.copy(alpha = 0.22f),
            disabledBorderColor = Ink.copy(alpha = 0.12f),
            errorBorderColor = Coral,

            focusedLabelColor = Lagoon,
            unfocusedLabelColor = Ink.copy(alpha = 0.64f),
            disabledLabelColor = Ink.copy(alpha = 0.38f),
            errorLabelColor = Coral,

            focusedLeadingIconColor = Lagoon,
            unfocusedLeadingIconColor = Ink.copy(alpha = 0.58f),
            disabledLeadingIconColor = Ink.copy(alpha = 0.32f),

            focusedTrailingIconColor = Lagoon,
            unfocusedTrailingIconColor = Ink.copy(alpha = 0.58f),

            cursorColor = Lagoon
        )
    )
}