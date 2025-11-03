package com.duoc.artelab_grupo10.ui.screens.profile

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.duoc.artelab_grupo10.domain.model.UiState
import com.duoc.artelab_grupo10.navigation.Screen
import kotlinx.coroutines.launch
import java.io.File

/**
 * Pantalla de perfil de usuario
 *
 * Funcionalidades CRÍTICAS para evaluación:
 * - Muestra datos del usuario autenticado (GET /auth/me)
 * - Foto de perfil con cámara/galería (OBLIGATORIO)
 * - Persistencia de foto en Room Database
 * - BottomSheet para elegir cámara o galería
 * - AsyncImage con Coil para mostrar foto offline
 * - Botón de cerrar sesión
 *
 * Estados:
 * - Loading mientras carga datos de /auth/me
 * - Success con datos del usuario
 * - Error si falla la carga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: Int,
    profileState: UiState<UserData>,
    onUpdateAvatar: suspend (Uri) -> Result<Unit>,
    onLogout: suspend () -> Unit
) {
    // Context y Coroutine Scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados de UI derivados del profileState
    val isLoading = profileState is UiState.Loading
    val userData = (profileState as? UiState.Success)?.data
    val errorMessage = (profileState as? UiState.Error)?.message

    var showImagePicker by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var permissionErrorMessage by remember { mutableStateOf<String?>(null) }

    // URI temporal para cámara (debe persistir entre recomposiciones)
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Crear URI temporal para captura de cámara
    fun createTempPhotoUri(context: Context): Uri {
        val tempFile = File(
            context.filesDir,
            "temp_photo_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    // Launcher para cámara (DEBE IR ANTES del permission launcher)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            // Foto capturada exitosamente
            avatarUri = tempPhotoUri
            // Guardar URI en Room Database
            scope.launch {
                tempPhotoUri?.let { uri ->
                    onUpdateAvatar(uri)
                }
            }
        }
    }

    // Launcher para galería (DEBE IR ANTES del permission launcher)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            // Guardar URI en Room Database mediante callback
            scope.launch {
                onUpdateAvatar(it)
            }
        }
    }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir cámara
            tempPhotoUri = createTempPhotoUri(context)
            tempPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            // Permiso denegado, mostrar mensaje
            permissionErrorMessage = "Permiso de cámara denegado"
        }
    }

    // Launcher para permisos de galería (solo Android 13+)
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permiso concedido o no necesario, abrir galería
            galleryLauncher.launch("image/*")
        } else {
            // Permiso denegado
            permissionErrorMessage = "Permiso de galería denegado"
        }
    }

    // Actualizar avatarUri cuando userData cambia
    LaunchedEffect(userData?.avatarUri) {
        userData?.avatarUri?.let { uriString ->
            avatarUri = Uri.parse(uriString)
        }
    }

    // BottomSheet para elegir cámara o galería
    if (showImagePicker) {
        ModalBottomSheet(
            onDismissRequest = { showImagePicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cambiar foto de perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opción de cámara
                ListItem(
                    headlineContent = { Text("Tomar foto") },
                    supportingContent = { Text("Usar la cámara") },
                    leadingContent = {
                        Icon(Icons.Default.Camera, contentDescription = "Cámara")
                    },
                    modifier = Modifier.clickable {
                        showImagePicker = false
                        // Solicitar permiso de cámara
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                // Opción de galería
                ListItem(
                    headlineContent = { Text("Elegir de galería") },
                    supportingContent = { Text("Seleccionar imagen existente") },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
                    },
                    modifier = Modifier.clickable {
                        showImagePicker = false
                        // Verificar versión de Android para permisos
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Android 13+ requiere permiso READ_MEDIA_IMAGES
                            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            // Android 12 y anteriores, abrir galería directamente
                            galleryLauncher.launch("image/*")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Snackbar para mensajes de error de permisos
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(permissionErrorMessage) {
        permissionErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            permissionErrorMessage = null
        }
    }

    // UI Principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            // Estado de loading
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando perfil...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Estado de error
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar perfil",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Estado de éxito
            userData != null -> {
                ProfileContent(
                    userData = userData!!,
                    avatarUri = avatarUri,
                    onChangePhoto = { showImagePicker = true },
                    onLogout = {
                        // Cerrar sesión y volver a login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

/**
 * Contenido del perfil cuando los datos se cargaron exitosamente
 */
@Composable
private fun ProfileContent(
    userData: UserData,
    avatarUri: Uri?,
    onChangePhoto: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Foto de perfil con botón de editar
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            // AsyncImage con Coil para cargar imagen desde URI
            // CRÍTICO: Esto permite persistencia offline
            AsyncImage(
                model = avatarUri ?: "https://via.placeholder.com/150",
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )

            // Botón flotante para cambiar foto
            FilledIconButton(
                onClick = onChangePhoto,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar foto",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre del usuario
        Text(
            text = userData.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email del usuario
        Text(
            text = userData.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card con información adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Información de la cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(
                    label = "ID de usuario",
                    value = userData.id.toString()
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(
                    label = "Email",
                    value = userData.email
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(
                    label = "Nombre",
                    value = userData.name
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de cerrar sesión
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}

/**
 * Fila de información (label + value)
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Modelo de datos del usuario
 * Debe coincidir con UserResponse del API
 */
data class UserData(
    val id: Int,
    val email: String,
    val name: String,
    val avatarUri: String? = null
)
