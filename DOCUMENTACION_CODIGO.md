# 📚 DOCUMENTACIÓN DEL CÓDIGO - ARTELAB

> Documentación completa del código para el equipo - Explicado paso a paso

**Autor**: Grupo 10
**Fecha**: 2025-11-01
**Proyecto**: ArteLab - EP3

---

## 📑 ÍNDICE

1. [Arquitectura General](#1-arquitectura-general)
2. [Capa de Datos (Data Layer)](#2-capa-de-datos-data-layer)
3. [Capa de Dominio (Domain Layer)](#3-capa-de-dominio-domain-layer)
4. [Capa de Presentación (UI Layer)](#4-capa-de-presentación-ui-layer)
5. [Navegación](#5-navegación)
6. [Inyección de Dependencias](#6-inyección-de-dependencias)
7. [Flujos de Usuario](#7-flujos-de-usuario)
8. [Casos de Uso Específicos](#8-casos-de-uso-específicos)

---

## 1. ARQUITECTURA GENERAL

### ¿Qué es MVVM?

Usamos el patrón **MVVM (Model-View-ViewModel)** porque es lo que recomienda Google para Android:

```
┌─────────────────────────────────────────┐
│              UI LAYER (View)             │  ← Lo que ve el usuario
│  - LoginScreen.kt, ProfileScreen.kt     │
└──────────────┬──────────────────────────┘
               │ observa StateFlow
┌──────────────▼──────────────────────────┐
│         VIEWMODEL LAYER                  │  ← Lógica de presentación
│  - LoginViewModel.kt                     │
│  - Maneja estados (Loading/Success/Error)│
└──────────────┬──────────────────────────┘
               │ llama métodos
┌──────────────▼──────────────────────────┐
│         DOMAIN LAYER                     │  ← Modelos de negocio
│  - UiState.kt (estados)                  │
└──────────────┬──────────────────────────┘
               │ usa
┌──────────────▼──────────────────────────┐
│         DATA LAYER (Model)               │  ← Fuentes de datos
│  - Repository, Room, Retrofit            │
└─────────────────────────────────────────┘
```

### ¿Por qué MVVM?

1. **Separación de responsabilidades**: La UI no sabe de base de datos, el ViewModel no sabe de botones
2. **Testeable**: Podemos probar la lógica sin necesidad de Android
3. **Reactivo**: Los datos fluyen automáticamente cuando cambian
4. **Recomendado por Google**: Es el estándar oficial

---

## 2. CAPA DE DATOS (DATA LAYER)

### 2.1 Room Database (Persistencia Local)

#### 📄 **Usuario.kt** - Entidad de Usuario

```kotlin
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,  // ← IMPORTANTE: Guarda la foto de perfil

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

**¿Qué hace?**
- Define cómo se guarda un usuario en SQLite
- `@Entity` = Es una tabla de base de datos
- `@PrimaryKey(autoGenerate = true)` = SQLite genera IDs automáticamente (1, 2, 3...)
- `@ColumnInfo` = Nombre de la columna en la tabla
- `avatarUri: String?` = Guarda la ruta de la foto (puede ser null si no tiene foto)

**Ejemplo de uso:**
```kotlin
val usuario = Usuario(
    id = 0,  // Se auto-genera
    nombre = "Juan Pérez",
    email = "juan@ejemplo.com",
    avatarUri = "content://com.duoc.artelab_grupo10.fileprovider/temp_photos/foto.jpg"
)
```

---

#### 📄 **UsuarioDao.kt** - Operaciones de Base de Datos

```kotlin
@Dao
interface UsuarioDao {

    // Obtener todos los usuarios (Flow = se actualiza automáticamente)
    @Query("SELECT * FROM usuarios ORDER BY created_at DESC")
    fun getAllUsuarios(): Flow<List<Usuario>>

    // Buscar por ID (suspend = se ejecuta en segundo plano)
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioById(id: Int): Usuario?

    // CRÍTICO: Actualizar solo la foto de perfil
    @Query("UPDATE usuarios SET avatar_uri = :avatarUri WHERE id = :id")
    suspend fun updateAvatarUri(id: Int, avatarUri: String?)

    // Insertar nuevo usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long
}
```

**¿Qué hace cada método?**

1. **getAllUsuarios()**
   - Devuelve `Flow<List<Usuario>>` = Lista que se actualiza sola cuando cambia la BD
   - Ordenados por fecha (más recientes primero)

2. **getUsuarioById(id)**
   - `suspend` = No bloquea la app mientras busca
   - Devuelve `Usuario?` = Puede ser null si no existe

3. **updateAvatarUri(id, avatarUri)** ⭐ IMPORTANTE
   - Solo actualiza la columna `avatar_uri`
   - Usado cuando el usuario toma/selecciona una foto

4. **insertUsuario(usuario)**
   - `OnConflictStrategy.REPLACE` = Si el ID ya existe, lo reemplaza
   - Devuelve el ID del usuario insertado

**Ejemplo de uso en ViewModel:**
```kotlin
// Actualizar foto de perfil
viewModelScope.launch {
    usuarioDao.updateAvatarUri(
        id = 1,
        avatarUri = "content://...foto.jpg"
    )
}
```

---

#### 📄 **AppDatabase.kt** - Singleton de Room

```kotlin
@Database(
    entities = [Usuario::class, Obra::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun obraDao(): ObraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artelab_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**¿Qué hace?**

1. **@Database**
   - `entities = [Usuario::class, Obra::class]` = Tablas que tiene la BD
   - `version = 1` = Versión de la estructura (si cambias la estructura, aumenta esto)

2. **abstract fun usuarioDao()**
   - Room genera la implementación automáticamente

3. **Singleton Pattern**
   - `@Volatile` = Asegura que todos los hilos vean la misma instancia
   - `synchronized(this)` = Solo un hilo puede crear la instancia a la vez
   - **¿Por qué?** Para que toda la app use la MISMA base de datos

**Cómo se usa:**
```kotlin
val db = AppDatabase.getDatabase(context)
val usuarioDao = db.usuarioDao()
val usuarios = usuarioDao.getAllUsuarios()
```

---

### 2.2 DataStore (Preferencias)

#### 📄 **PreferencesManager.kt**

```kotlin
// Extension property: Cada Context tiene su propio DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "artelab_preferences"
)

// Keys: Así identificamos cada preferencia
object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = intPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_NAME = stringPreferencesKey("user_name")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
}

class PreferencesManager(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    // Guardar token JWT
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    // Leer token JWT (como Flow - se actualiza automáticamente)
    fun getAuthToken(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.AUTH_TOKEN]
            }
    }

    // Guardar sesión completa (cuando el usuario hace login)
    suspend fun saveUserSession(
        userId: Int,
        email: String,
        name: String,
        token: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }

    // Cerrar sesión (borrar todo)
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }
}
```

**¿Qué es DataStore?**
- El reemplazo moderno de `SharedPreferences`
- Guarda datos clave-valor (como un diccionario)
- Es **asíncrono** (no bloquea la app)
- Retorna **Flow** (se actualiza automáticamente)

**¿Cuándo se usa?**
- Guardar el token JWT después de login
- Guardar datos básicos del usuario (id, email, nombre)
- Verificar si hay sesión activa (para el Splash)
- Cerrar sesión (borrar todo)

**Ejemplo de uso:**
```kotlin
// Guardar sesión después de login exitoso
preferencesManager.saveUserSession(
    userId = 123,
    email = "user@ejemplo.com",
    name = "Usuario",
    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
)

// Leer token para enviarlo al API
val token = preferencesManager.getAuthToken().first()
```

---

### 2.3 Retrofit (API REST)

#### 📄 **ArtelabApi.kt** - Definición de Endpoints

```kotlin
interface ArtelabApi {
    companion object {
        const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/"
    }

    // Registro de nuevo usuario
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    // Inicio de sesión
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Obtener datos del usuario autenticado (CRÍTICO para EP3)
    @GET("auth/me")
    suspend fun getMe(): UserResponse
}
```

**¿Qué hace cada endpoint?**

1. **POST /auth/signup**
   - Envía: `{ name, email, password }`
   - Recibe: `{ authToken, user: { id, email, name } }`
   - Úsalo en: RegisterScreen

2. **POST /auth/login**
   - Envía: `{ email, password }`
   - Recibe: `{ authToken, user: { id, email, name } }`
   - Úsalo en: LoginScreen

3. **GET /auth/me** ⭐ OBLIGATORIO
   - Envía: Header `Authorization: Bearer <token>`
   - Recibe: `{ id, email, name }`
   - Úsalo en: ProfileScreen (para mostrar datos del usuario)

**¿Qué es `suspend`?**
- Funciones que se ejecutan en segundo plano
- No bloquean la UI
- Se llaman desde `viewModelScope.launch { }`

---

#### 📄 **AuthInterceptor.kt** - Agregar Token Automáticamente

```kotlin
class AuthInterceptor(
    private val tokenProvider: () -> String?  // ← Función que devuelve el token
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()  // Obtener token de DataStore

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")  // ← Agregar header
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
```

**¿Qué hace?**
- **ANTES** de cada petición HTTP, agrega el header `Authorization: Bearer <token>`
- Así no tienes que agregarlo manualmente en cada llamada
- Solo afecta a las peticiones que NECESITAN token (GET /auth/me)

**¿Cómo funciona?**
```
1. Retrofit va a hacer una petición GET /auth/me
2. AuthInterceptor intercepta la petición
3. Pregunta: "¿Hay token guardado?"
4. Si SÍ: Agrega header "Authorization: Bearer abc123"
5. Si NO: Deja la petición como está
6. Continúa con la petición
```

---

#### 📄 **RetrofitClient.kt** - Configuración de Retrofit

```kotlin
object RetrofitClient {

    fun create(tokenProvider: () -> String?): ArtelabApi {

        // OkHttp: Cliente HTTP con configuraciones
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))  // ← Agregar token
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY  // ← Log completo (útil para debug)
            })
            .connectTimeout(30, TimeUnit.SECONDS)  // ← 30s para conectar
            .readTimeout(30, TimeUnit.SECONDS)     // ← 30s para leer respuesta
            .writeTimeout(30, TimeUnit.SECONDS)    // ← 30s para enviar datos
            .build()

        // Retrofit: Convierte JSON en objetos Kotlin
        val retrofit = Retrofit.Builder()
            .baseUrl(ArtelabApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())  // ← JSON → Kotlin
            .build()

        return retrofit.create(ArtelabApi::class.java)
    }
}
```

**¿Qué hace cada parte?**

1. **AuthInterceptor**
   - Agrega el token a cada petición

2. **HttpLoggingInterceptor**
   - Muestra en Logcat qué se envía y qué se recibe
   - Útil para debuggear

3. **Timeouts**
   - Si el servidor no responde en 30s, se cancela
   - Evita que la app se quede colgada

4. **GsonConverterFactory**
   - Convierte JSON automáticamente
   - Ejemplo: `{"id": 1, "name": "Juan"}` → `User(id=1, name="Juan")`

---

#### 📄 **AuthRepository.kt** - Lógica de Negocio de API

```kotlin
class AuthRepository(
    private val api: ArtelabApi,
    private val preferencesManager: PreferencesManager
) {

    // Login
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            // 1. Llamar al API
            val response = api.login(LoginRequest(email, password))

            // 2. Guardar sesión en DataStore
            preferencesManager.saveUserSession(
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                token = response.authToken
            )

            // 3. Retornar éxito
            Result.success(response)

        } catch (e: IOException) {
            // Error de conexión (sin internet)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))

        } catch (e: HttpException) {
            // Error del servidor (400, 500, etc.)
            val errorMessage = when (e.code()) {
                401 -> "Email o contraseña incorrectos."
                404 -> "Usuario no encontrado."
                else -> "Error del servidor. Intenta más tarde."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Obtener datos del usuario autenticado (CRÍTICO)
    suspend fun getMe(): Result<UserResponse> {
        return try {
            // 1. Verificar que haya token
            val token = preferencesManager.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No hay sesión activa."))
            }

            // 2. Llamar al API (AuthInterceptor agrega el token automáticamente)
            val response = api.getMe()

            // 3. Retornar datos
            Result.success(response)

        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.failure(Exception("Sesión expirada. Inicia sesión nuevamente."))
                else -> Result.failure(Exception("Error del servidor."))
            }
        }
    }

    // Cerrar sesión
    suspend fun logout() {
        preferencesManager.clearSession()
    }

    // Verificar si hay sesión activa
    suspend fun hasActiveSession(): Boolean {
        val token = preferencesManager.getAuthToken().first()
        return !token.isNullOrEmpty()
    }
}
```

**¿Por qué usamos Result<T>?**
- `Result.success(data)` = Todo salió bien, aquí está la data
- `Result.failure(error)` = Algo salió mal, aquí está el error
- Es más claro que lanzar excepciones

**¿Cómo se maneja cada error?**

| Error | Código HTTP | Mensaje |
|-------|-------------|---------|
| Sin internet | IOException | "Error de conexión. Verifica tu internet." |
| Credenciales incorrectas | 401 | "Email o contraseña incorrectos." |
| Usuario no encontrado | 404 | "Usuario no encontrado." |
| Error del servidor | 500 | "Error del servidor. Intenta más tarde." |
| Token expirado | 401 (en getMe) | "Sesión expirada. Inicia sesión nuevamente." |

---

## 3. CAPA DE DOMINIO (DOMAIN LAYER)

### 📄 **UiState.kt** - Estados de la UI

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()           // Estado inicial
    object Loading : UiState<Nothing>()         // Cargando
    data class Success<T>(val data: T) : UiState<T>()  // Éxito con datos
    data class Error(val message: String) : UiState<Nothing>()  // Error con mensaje
    object Empty : UiState<Nothing>()           // Sin datos (lista vacía)
}
```

**¿Qué es una Sealed Class?**
- Es como un `enum` pero más poderoso
- Solo puede tener estos 5 estados (no puedes crear otros)
- Puedes usar `when` y el compilador te obliga a manejar todos los casos

**¿Para qué sirve cada estado?**

| Estado | Cuándo usarlo | Ejemplo en UI |
|--------|---------------|---------------|
| `Idle` | Formulario vacío inicial | Botón "Iniciar Sesión" habilitado |
| `Loading` | Esperando respuesta del servidor | CircularProgressIndicator |
| `Success<T>` | Todo salió bien | Navegar a HomeScreen |
| `Error(message)` | Algo salió mal | Mostrar Snackbar con error |
| `Empty` | Lista sin elementos | "No hay obras, agrega una" |

**Ejemplo de uso en ViewModel:**
```kotlin
class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading  // ← Mostrar loading

            val result = authRepository.login(email, password)

            _uiState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },  // ← Navegar a Home
                onFailure = { UiState.Error(it.message ?: "Error") }  // ← Mostrar error
            )
        }
    }
}
```

**Ejemplo de uso en Screen:**
```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is UiState.Idle -> { /* Formulario normal */ }
        is UiState.Loading -> { CircularProgressIndicator() }
        is UiState.Success -> { /* Navegar a Home */ }
        is UiState.Error -> {
            Text("Error: ${(uiState as UiState.Error).message}")
        }
        is UiState.Empty -> { /* No aplica en login */ }
    }
}
```

---

## 4. CAPA DE PRESENTACIÓN (UI LAYER)

### 4.1 ViewModels

#### 📄 **LoginViewModel.kt**

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado de UI (privado para que solo el ViewModel lo modifique)
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    // Estado de UI (público para que la UI lo observe)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Estados de validación de campos
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Validar email
    fun validateEmail(email: String): Boolean {
        _emailError.value = when {
            email.isBlank() -> "El email es obligatorio"
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Formato de email inválido"
            else -> null
        }
        return _emailError.value == null
    }

    // Validar password
    fun validatePassword(password: String): Boolean {
        _passwordError.value = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        return _passwordError.value == null
    }

    // Realizar login
    fun login(email: String, password: String) {
        // 1. Validar campos
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        if (!isEmailValid || !isPasswordValid) {
            _uiState.value = UiState.Error("Completa todos los campos")
            return
        }

        // 2. Iniciar loading
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // 3. Llamar al repositorio
            val result = authRepository.login(email, password)

            // 4. Procesar resultado
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    // Resetear estado (después de navegar)
    fun resetState() {
        _uiState.value = UiState.Idle
        _emailError.value = null
        _passwordError.value = null
    }
}
```

**¿Por qué StateFlow?**
- Es un "observable" = La UI se entera automáticamente cuando cambia
- La UI lee con `collectAsState()`
- Es "type-safe" = El compilador te avisa si usas mal los tipos

**¿Por qué viewModelScope?**
- Es un `CoroutineScope` que se cancela automáticamente cuando se destruye el ViewModel
- Evita memory leaks

---

#### 📄 **ProfileViewModel.kt**

```kotlin
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    // Estado de datos del perfil
    private val _profileState = MutableStateFlow<UiState<UserData>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserData>> = _profileState.asStateFlow()

    // Cargar datos del usuario (CRÍTICO: llama a GET /auth/me)
    fun loadUserData() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            // 1. Llamar al API
            val result = authRepository.getMe()

            _profileState.value = result.fold(
                onSuccess = { userResponse ->
                    // 2. Buscar foto guardada en Room
                    val localUser = usuarioDao.getUsuarioById(userResponse.id)
                    val avatarUri = localUser?.avatarUri

                    // 3. Combinar datos del API + foto local
                    UiState.Success(
                        UserData(
                            id = userResponse.id,
                            email = userResponse.email,
                            name = userResponse.name,
                            avatarUri = avatarUri  // ← Foto de Room
                        )
                    )
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Error al cargar perfil")
                }
            )
        }
    }

    // Actualizar foto de perfil (CRÍTICO para persistencia)
    fun updateAvatar(userId: Int, avatarUri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Guardar URI en Room Database
                usuarioDao.updateAvatarUri(userId, avatarUri.toString())

                // 2. Actualizar estado de perfil
                val currentState = _profileState.value
                if (currentState is UiState.Success) {
                    _profileState.value = UiState.Success(
                        currentState.data.copy(avatarUri = avatarUri.toString())
                    )
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Cerrar sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _profileState.value = UiState.Idle
        }
    }
}
```

**Flujo de datos del perfil:**
```
1. ProfileScreen llama loadUserData()
2. ProfileViewModel llama authRepository.getMe()
3. AuthRepository llama api.getMe() (AuthInterceptor agrega token)
4. API devuelve { id, email, name }
5. ProfileViewModel busca avatarUri en Room
6. ProfileViewModel combina datos API + foto local
7. ProfileViewModel emite UiState.Success(UserData(...))
8. ProfileScreen observa el cambio y muestra los datos
```

---

### 4.2 Screens (Pantallas)

#### 📄 **LoginScreen.kt**

```kotlin
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginClick: suspend (String, String) -> Result<Unit>
) {
    // Estados del formulario (locales a esta pantalla)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Estados de validación
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Validar email cuando el usuario escribe
    LaunchedEffect(email) {
        emailError = when {
            email.isBlank() -> "El email es obligatorio"
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Formato de email inválido"
            else -> null
        }
    }

    // Validar password cuando el usuario escribe
    LaunchedEffect(password) {
        passwordError = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
    }

    // Formulario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Campo de email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = {
                emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Contraseña")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón con animaciones
        AnimatedLoginButton(
            onClick = { /* lanzar login */ },
            enabled = emailError == null && passwordError == null && !isLoading,
            isLoading = isLoading,
            text = if (isLoading) "Iniciando sesión..." else "Iniciar Sesión"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para ir a registro
        TextButton(
            onClick = { navController.navigate(Screen.Register.route) }
        ) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}
```

**¿Qué hace cada parte?**

1. **remember { mutableStateOf() }**
   - Guarda el estado durante recomposiciones
   - Sin `remember`, el estado se resetearía cada vez que Compose redibuja

2. **LaunchedEffect(email)**
   - Se ejecuta cada vez que `email` cambia
   - Útil para validaciones en tiempo real

3. **OutlinedTextField**
   - `isError = emailError != null` = Borde rojo si hay error
   - `supportingText` = Mensaje de error debajo del campo
   - `keyboardOptions` = Tipo de teclado (Email, Number, Password)
   - `imeAction` = Qué botón mostrar (Next, Done, Search)

4. **PasswordVisualTransformation()**
   - Convierte "password" en "••••••••"

5. **AnimatedLoginButton**
   - Botón personalizado con animaciones
   - Deshabilitado si hay errores o está cargando

---

#### 📄 **ProfileScreen.kt** - Cámara y Galería

```kotlin
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: Int,
    profileState: UiState<UserData>,
    onUpdateAvatar: suspend (Uri) -> Result<Unit>,
    onLogout: suspend () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados derivados del profileState
    val isLoading = profileState is UiState.Loading
    val userData = (profileState as? UiState.Success)?.data
    val errorMessage = (profileState as? UiState.Error)?.message

    var showImagePicker by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Función para crear URI temporal para la cámara
    fun createTempPhotoUri(context: Context): Uri {
        val tempFile = File(
            context.filesDir,  // ← Directorio interno de la app
            "temp_photo_${System.currentTimeMillis()}.jpg"  // ← Nombre único con timestamp
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",  // ← Authority del FileProvider
            tempFile
        )
    }

    // Launcher para permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido: crear URI y abrir cámara
            tempPhotoUri = createTempPhotoUri(context)
            tempPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            // Permiso denegado: mostrar mensaje
            errorMessage = "Permiso de cámara denegado"
        }
    }

    // Launcher para permiso de galería (solo Android 13+)
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            galleryLauncher.launch("image/*")
        } else {
            errorMessage = "Permiso de galería denegado"
        }
    }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            // Guardar en Room Database
            scope.launch {
                onUpdateAvatar(it)
            }
        }
    }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            avatarUri = tempPhotoUri
            // Guardar en Room Database
            scope.launch {
                tempPhotoUri?.let { uri ->
                    onUpdateAvatar(uri)
                }
            }
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
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
                    },
                    modifier = Modifier.clickable {
                        showImagePicker = false
                        // Verificar versión de Android
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Android 13+: pedir permiso
                            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            // Android 12 y anteriores: abrir directamente
                            galleryLauncher.launch("image/*")
                        }
                    }
                )
            }
        }
    }

    // UI del perfil
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            userData != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Foto de perfil con AsyncImage (Coil)
                    Box(contentAlignment = Alignment.BottomEnd) {
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
                            onClick = { showImagePicker = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = userData.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = userData.email,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón de cerrar sesión
                    Button(
                        onClick = {
                            scope.launch {
                                onLogout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    }
}
```

**¿Cómo funciona la cámara/galería?**

1. **Usuario hace clic en botón de editar foto**
   - Se abre BottomSheet

2. **Usuario elige "Tomar foto"**
   - Se solicita permiso CAMERA
   - Si se concede:
     - Se crea URI temporal: `content://...fileprovider/temp_photos/temp_photo_123456.jpg`
     - Se abre app de cámara
     - Usuario toma foto
     - Foto se guarda en URI temporal
     - `cameraLauncher` recibe `success = true`
     - Se llama `onUpdateAvatar(uri)` → ProfileViewModel → Room Database

3. **Usuario elige "Elegir de galería"**
   - Android 13+: Se solicita permiso READ_MEDIA_IMAGES
   - Android 12-: Se abre directamente
   - Si se concede:
     - Se abre selector de imágenes
     - Usuario elige imagen
     - `galleryLauncher` recibe URI
     - Se llama `onUpdateAvatar(uri)` → ProfileViewModel → Room Database

4. **Persistencia**
   - URI se guarda como String en tabla `usuarios`, columna `avatar_uri`
   - `AsyncImage` de Coil carga la imagen desde el URI
   - Funciona offline (no necesita internet)

---

### 4.3 Componentes Animados

#### 📄 **AnimatedLoginButton.kt**

```kotlin
@Composable
private fun AnimatedLoginButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    // Animación de escala al presionar
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,  // ← Escala a 95% al presionar
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,  // ← Efecto "rebote"
            stiffness = Spring.StiffnessLow  // ← Velocidad suave
        ),
        label = "button_scale"
    )

    // Animación de pulso infinito
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),  // ← 1.5 segundos
            repeatMode = RepeatMode.Reverse  // ← Va y viene
        ),
        label = "pulse_alpha"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)  // ← Aplicar escala
            .graphicsLayer {
                alpha = if (enabled && !isLoading) pulseAlpha else 1f  // ← Aplicar pulso
            }
    ) {
        // Transición suave entre Loading y Texto
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "button_content"
        ) { loading ->
            if (loading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text)
                }
            } else {
                Text(text, fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

**¿Qué hace cada animación?**

1. **animateFloatAsState (escala)**
   - Anima de 1f (100%) a 0.95f (95%) cuando se presiona
   - `spring()` = Efecto de rebote suave

2. **rememberInfiniteTransition (pulso)**
   - Anima entre 0.8f y 1f infinitamente
   - Solo se activa cuando el botón está habilitado
   - Da sensación de "botón vivo"

3. **AnimatedContent (loading ↔ texto)**
   - Transición con fadeIn/fadeOut
   - Cambia entre "CircularProgressIndicator + Texto" y "Solo Texto"

---

## 5. NAVEGACIÓN

### 📄 **Screen.kt** - Definición de Rutas

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: Int) = "profile/$userId"
    }
}
```

**¿Cómo se usa?**

```kotlin
// Navegar sin parámetros
navController.navigate(Screen.Login.route)

// Navegar con parámetros
navController.navigate(Screen.Profile.createRoute(userId = 123))
// Resultado: "profile/123"
```

---

### 📄 **MainActivity.kt** - NavHost

```kotlin
@Composable
fun ArtelabApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val factory = ViewModelFactory(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // SPLASH
        composable(route = Screen.Splash.route) {
            val viewModel: SplashViewModel = viewModel(factory = factory)
            val sessionState by viewModel.sessionState.collectAsState()

            // Navegar automáticamente según si hay sesión
            LaunchedEffect(sessionState) {
                when (sessionState) {
                    is UiState.Success -> {
                        val hasSession = (sessionState as UiState.Success).data
                        val destination = if (hasSession)
                            Screen.Home.route
                        else
                            Screen.Login.route

                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    else -> { /* Mantener en splash */ }
                }
            }

            SplashScreen(navController)
        }

        // LOGIN
        composable(route = Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsState()

            // Navegar a Home si login exitoso
            LaunchedEffect(uiState) {
                if (uiState is UiState.Success) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }

            LoginScreen(navController, onLoginClick = { email, password ->
                viewModel.login(email, password)
                Result.success(Unit)
            })
        }

        // PROFILE (con parámetro userId)
        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            val viewModel: ProfileViewModel = viewModel(factory = factory)
            val profileState by viewModel.profileState.collectAsState()

            // Cargar datos al entrar
            LaunchedEffect(Unit) {
                viewModel.loadUserData()
            }

            ProfileScreen(
                navController = navController,
                userId = userId,
                profileState = profileState,
                onUpdateAvatar = { uri ->
                    viewModel.updateAvatar(userId, uri)
                    Result.success(Unit)
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        }
    }
}
```

**¿Qué hace cada parte?**

1. **composable(route = "login")**
   - Define una pantalla en la navegación

2. **LaunchedEffect(uiState)**
   - Se ejecuta cada vez que `uiState` cambia
   - Si el estado es Success → Navegar a Home

3. **popUpTo(Screen.Login.route) { inclusive = true }**
   - Borra del backstack todas las pantallas hasta Login (inclusive)
   - Evita que el usuario pueda volver atrás con el botón físico

4. **navArgument("userId")**
   - Define que esta ruta acepta un parámetro
   - `type = NavType.IntType` = El parámetro es un número entero

5. **backStackEntry.arguments?.getInt("userId")**
   - Obtiene el valor del parámetro
   - Ejemplo: "profile/123" → userId = 123

---

## 6. INYECCIÓN DE DEPENDENCIAS

### 📄 **AppModule.kt** - DI Manual

```kotlin
object AppModule {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var preferencesManager: PreferencesManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    // Singleton de Database
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            database = instance
            instance
        }
    }

    // Singleton de PreferencesManager
    fun providePreferencesManager(context: Context): PreferencesManager {
        return preferencesManager ?: synchronized(this) {
            val instance = PreferencesManager(context)
            preferencesManager = instance
            instance
        }
    }

    // Singleton de AuthRepository
    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            val prefs = providePreferencesManager(context)

            // Crear API con token provider
            val api = RetrofitClient.create {
                runBlocking {
                    prefs.getAuthToken().first()
                }
            }

            val instance = AuthRepository(api, prefs)
            authRepository = instance
            instance
        }
    }
}
```

**¿Qué es Singleton?**
- Solo existe UNA instancia en toda la app
- Se crea la primera vez que se pide
- Las siguientes veces se devuelve la misma instancia

**¿Por qué Singleton?**
- **Database**: Solo debe haber una conexión a SQLite
- **PreferencesManager**: Evita conflictos de lectura/escritura
- **AuthRepository**: Comparte el mismo cliente HTTP

---

### 📄 **ViewModelFactory.kt**

```kotlin
class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                LoginViewModel(authRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                val database = AppModule.provideDatabase(context)
                val usuarioDao = database.usuarioDao()
                ProfileViewModel(authRepository, usuarioDao) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
```

**¿Para qué sirve?**
- Los ViewModels necesitan dependencias (Repository, DAOs)
- El Factory las inyecta al crear el ViewModel
- Evita tener que pasarlas manualmente

**Cómo se usa:**
```kotlin
val factory = ViewModelFactory(context)
val viewModel: LoginViewModel = viewModel(factory = factory)
```

---

## 7. FLUJOS DE USUARIO

### 7.1 Flujo de Login

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Usuario abre la app                                       │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. SplashScreen verifica si hay token en DataStore          │
│    - SplashViewModel.checkSession()                         │
│    - preferencesManager.getAuthToken()                      │
└─────────────┬───────────────────────────────────────────────┘
              │
       ┌──────┴──────┐
       │             │
       ▼             ▼
   No Token      Sí Token
       │             │
       │             └──> Navegar a HomeScreen
       │
       └──> Navegar a LoginScreen
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. LoginScreen                                               │
│    - Usuario escribe email/password                         │
│    - Validaciones en tiempo real                            │
│    - Botón habilitado si no hay errores                     │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼ Usuario hace clic en "Iniciar Sesión"
┌─────────────────────────────────────────────────────────────┐
│ 4. LoginViewModel.login(email, password)                    │
│    - Cambia estado a UiState.Loading                        │
│    - Muestra CircularProgressIndicator                      │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. AuthRepository.login(email, password)                    │
│    - api.login(LoginRequest(email, password))               │
│    - Envía POST https://.../auth/login                      │
└─────────────┬───────────────────────────────────────────────┘
              │
       ┌──────┴──────┐
       │             │
       ▼             ▼
    Error         Success
       │             │
       │             └──> Guarda token en DataStore
       │                  preferencesManager.saveUserSession(...)
       │
       └──> UiState.Error("Credenciales incorrectas")
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. LoginScreen observa el cambio de estado                  │
│    - LaunchedEffect(uiState)                                │
│    - if (uiState is UiState.Success)                        │
│      → navController.navigate(Screen.Home.route)            │
└─────────────────────────────────────────────────────────────┘
```

---

### 7.2 Flujo de Foto de Perfil

```
┌─────────────────────────────────────────────────────────────┐
│ 1. ProfileScreen se monta                                    │
│    - LaunchedEffect(Unit) { viewModel.loadUserData() }     │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. ProfileViewModel.loadUserData()                          │
│    - authRepository.getMe()                                 │
│    - GET /auth/me (con Bearer token automático)            │
│    - Recibe { id, email, name }                             │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. ProfileViewModel busca foto en Room                      │
│    - usuarioDao.getUsuarioById(id)                          │
│    - localUser?.avatarUri                                   │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. ProfileViewModel combina datos                           │
│    - UserData(id, email, name, avatarUri)                   │
│    - UiState.Success(userData)                              │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. ProfileScreen muestra datos                              │
│    - AsyncImage(model = avatarUri, ...)                     │
│    - Foto se carga desde URI local                          │
└─────────────────────────────────────────────────────────────┘
              │
              ▼ Usuario hace clic en botón de editar foto
┌─────────────────────────────────────────────────────────────┐
│ 6. Se abre ModalBottomSheet                                 │
│    - "Tomar foto" o "Elegir de galería"                     │
└─────────────┬───────────────────────────────────────────────┘
              │
       ┌──────┴──────┐
       │             │
   Cámara        Galería
       │             │
       ▼             ▼
┌───────────┐  ┌────────────┐
│ Permiso?  │  │ Permiso?   │
└─────┬─────┘  └──────┬─────┘
      │               │
   Sí │            Sí │
      │               │
      ▼               ▼
┌──────────────┐  ┌──────────────┐
│ Crear URI    │  │ Abrir        │
│ temporal     │  │ selector     │
│ Abrir cámara │  │ de imágenes  │
└─────┬────────┘  └──────┬───────┘
      │                  │
      │ Foto tomada      │ Imagen seleccionada
      │                  │
      └────────┬─────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Guardar URI en Room Database                             │
│    - ProfileViewModel.updateAvatar(userId, uri)             │
│    - usuarioDao.updateAvatarUri(userId, uri.toString())     │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. ProfileScreen se actualiza automáticamente               │
│    - LaunchedEffect(userData?.avatarUri) detecta cambio     │
│    - AsyncImage carga nueva foto                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. CASOS DE USO ESPECÍFICOS

### 8.1 ¿Cómo agregar una nueva pantalla?

1. **Crear la ruta en Screen.kt**
```kotlin
sealed class Screen(val route: String) {
    // ... existentes
    object MiNuevaPantalla : Screen("mi_nueva_pantalla")
}
```

2. **Crear el Screen (UI)**
```kotlin
@Composable
fun MiNuevaPantalla(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Nueva Pantalla") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Contenido de mi pantalla")
        }
    }
}
```

3. **Agregar al NavHost en MainActivity**
```kotlin
composable(route = Screen.MiNuevaPantalla.route) {
    MiNuevaPantalla(navController)
}
```

4. **Navegar desde otra pantalla**
```kotlin
Button(onClick = {
    navController.navigate(Screen.MiNuevaPantalla.route)
}) {
    Text("Ir a Mi Nueva Pantalla")
}
```

---

### 8.2 ¿Cómo agregar un nuevo endpoint al API?

1. **Agregar método en ArtelabApi.kt**
```kotlin
interface ArtelabApi {
    // ... existentes

    @GET("obras")
    suspend fun getObras(): List<ObraResponse>

    @POST("obras")
    suspend fun createObra(@Body request: ObraRequest): ObraResponse
}
```

2. **Crear DTOs**
```kotlin
data class ObraRequest(
    val titulo: String,
    val autor: String,
    val descripcion: String
)

data class ObraResponse(
    val id: Int,
    val titulo: String,
    val autor: String,
    val descripcion: String
)
```

3. **Agregar método en Repository**
```kotlin
class ObraRepository(
    private val api: ArtelabApi
) {
    suspend fun getObras(): Result<List<ObraResponse>> {
        return try {
            val obras = api.getObras()
            Result.success(obras)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

4. **Usar en ViewModel**
```kotlin
class ObrasViewModel(
    private val obraRepository: ObraRepository
) : ViewModel() {

    private val _obrasState = MutableStateFlow<UiState<List<ObraResponse>>>(UiState.Idle)
    val obrasState: StateFlow<UiState<List<ObraResponse>>> = _obrasState.asStateFlow()

    fun cargarObras() {
        viewModelScope.launch {
            _obrasState.value = UiState.Loading

            val result = obraRepository.getObras()

            _obrasState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error") }
            )
        }
    }
}
```

---

### 8.3 ¿Cómo debuggear problemas?

#### **Problema: "No navega después de login"**

1. **Verificar que el estado cambie**
```kotlin
// En LoginViewModel.login()
Log.d("LoginViewModel", "Estado actual: ${_uiState.value}")
```

2. **Verificar que LaunchedEffect se ejecute**
```kotlin
// En LoginScreen
LaunchedEffect(uiState) {
    Log.d("LoginScreen", "Estado cambió a: $uiState")
    if (uiState is UiState.Success) {
        Log.d("LoginScreen", "Navegando a Home...")
        navController.navigate(Screen.Home.route)
    }
}
```

3. **Verificar logs de Retrofit**
```
D/OkHttp: --> POST /auth/login
D/OkHttp: {"email":"test@test.com","password":"123456"}
D/OkHttp: <-- 200 OK (234ms)
D/OkHttp: {"authToken":"abc123","user":{...}}
```

---

#### **Problema: "Foto no se guarda"**

1. **Verificar que se llame updateAvatar**
```kotlin
// En ProfileScreen, cameraLauncher
Log.d("ProfileScreen", "Foto capturada: $tempPhotoUri")
```

2. **Verificar que se guarde en Room**
```kotlin
// En ProfileViewModel.updateAvatar
Log.d("ProfileViewModel", "Guardando URI: $avatarUri")
usuarioDao.updateAvatarUri(userId, avatarUri.toString())
Log.d("ProfileViewModel", "URI guardado exitosamente")
```

3. **Verificar en Database Inspector (Android Studio)**
```
View → Tool Windows → App Inspection → Database Inspector
→ Seleccionar tabla "usuarios"
→ Ver columna "avatar_uri"
```

---

#### **Problema: "App se crashea al abrir cámara"**

**Posibles causas:**

1. **Falta permiso en AndroidManifest.xml**
```xml
<uses-permission android:name="android.permission.CAMERA" />
```

2. **Falta FileProvider**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    ...
/>
```

3. **file_paths.xml incorrecto**
```xml
<files-path name="temp_photos" path="." />
```

4. **URI mal formado**
```kotlin
// INCORRECTO
val uri = Uri.parse("/data/user/0/...")

// CORRECTO
val uri = FileProvider.getUriForFile(context, authority, file)
```

---

## 🎓 GLOSARIO DE TÉRMINOS

| Término | Significado |
|---------|-------------|
| **ViewModel** | Clase que maneja la lógica de UI y sobrevive a rotaciones de pantalla |
| **StateFlow** | Observable que emite estados y se puede observar desde la UI |
| **Coroutine** | Hilo ligero para operaciones asíncronas (no bloquea la UI) |
| **suspend** | Función que se puede pausar y reanudar (usada en coroutines) |
| **Flow** | Stream de datos que se actualiza automáticamente |
| **@Composable** | Función que dibuja UI en Jetpack Compose |
| **LaunchedEffect** | Ejecuta código cuando cambia una variable |
| **remember** | Guarda estado durante recomposiciones |
| **mutableStateOf** | Estado que cuando cambia, redibuja la UI |
| **Singleton** | Solo existe una instancia en toda la app |
| **DAO** | Data Access Object - Interfaz para operaciones de BD |
| **Entity** | Tabla de base de datos |
| **DTO** | Data Transfer Object - Objeto para enviar/recibir del API |
| **Repository** | Capa que abstrae el acceso a datos (API + DB) |
| **Interceptor** | Código que se ejecuta antes/después de cada petición HTTP |
| **Sealed Class** | Clase con un conjunto fijo de subclases (como enum mejorado) |
| **URI** | Uniform Resource Identifier - Dirección de un recurso (archivo, imagen) |
| **FileProvider** | Comparte archivos de forma segura entre apps |

---

## 📞 CONTACTO Y SOPORTE

Si tienes dudas sobre el código, contacta a:
- **Email del equipo**: [tu-email@ejemplo.com]
- **GitHub**: [link al repositorio]

---

**Última actualización:** 2025-11-01
**Versión:** 1.0
**Estado:** Completo y listo para EP3

