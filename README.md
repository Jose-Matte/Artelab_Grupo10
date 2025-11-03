# ArteLab - Plataforma de Arte Digital

**Evaluación:** EP3 - Desarrollo de Aplicaciones Móviles
**Asignatura:** DSY1105
**Grupo:** Grupo 10
**Institución:** DUOC UC

---

## 1. Caso elegido y alcance

### Caso
**ArteLab - Plataforma de Arte Digital**

Aplicación móvil para gestionar y explorar obras de arte. Permite a los usuarios registrarse, iniciar sesión, ver su perfil con foto personalizada y navegar por una galería de obras.

### Alcance EP3
- Diseño visual coherente (Material 3, jerarquía tipográfica)
- Formularios validados (Login y Registro)
- Navegación entre 5 pantallas (Splash, Login, Register, Home, Profile)
- Gestión de estado (UiState sealed class, StateFlow)
- Persistencia local (Room Database para Usuario y Obra, DataStore para preferencias)
- Recursos nativos: Cámara y Galería con manejo de permisos
- Almacenamiento local de imagen de perfil
- Animaciones con propósito (botones, transiciones)
- Consumo de API REST (incluye `/auth/me`)

---

## 2. Requisitos y ejecución

### Stack
- **Lenguaje:** Kotlin 1.9.0
- **UI:** Jetpack Compose 1.5.4, Material 3
- **Persistencia:** Room 2.6.1, DataStore 1.0.0
- **Networking:** Retrofit 2.9.0, OkHttp 4.12.0, Gson 2.10.1
- **Async:** Coroutines 1.7.3, Flow
- **Navegación:** Navigation Compose 2.7.6
- **Imagen:** Coil 2.5.0

### Instalación
```bash
git clone https://github.com/tu-usuario/artelab-grupo10.git
cd artelab-grupo10
# Abrir en Android Studio
# Sync Gradle
# Run (▶)
```

### Ejecución
- **Requisitos:** Android Studio Hedgehog+, JDK 17+, Android SDK 34
- **Target:** Android 7.0 (API 24) o superior
- **Perfiles:** Debug (desarrollo), Release (producción)

---

## 3. Arquitectura y flujo

### Estructura carpetas
```
app/src/main/java/com/duoc/artelab_grupo10/
├── data/
│   ├── local/ (dao, entities, database, PreferencesManager)
│   ├── remote/ (api, dto, interceptor, RetrofitClient)
│   └── repository/
├── di/ (AppModule - DI manual)
├── domain/model/ (UiState sealed class)
├── navigation/ (Screen sealed class)
├── ui/
│   ├── screens/ (splash, login, register, home, profile)
│   ├── viewmodel/ (ViewModels + Factory)
│   └── theme/
└── MainActivity.kt
```

### Gestión de estado
- **Estrategia:** Estado local con StateFlow en cada ViewModel
- **Patrón:** UiState sealed class (Idle, Loading, Success, Error, Empty)
- **Flujo de datos:**
  ```
  UI (Compose) → ViewModel → Repository → Data Source (API/Room)
                     ↓
              StateFlow<UiState<T>>
                     ↓
  UI reacciona con collectAsState()
  ```

### Navegación
- **Tipo:** Navigation Stack (no tabs)
- **Implementación:** NavHost con sealed class Screen
- **Backstack:** `popUpTo` para evitar retorno a Splash/Login después de login exitoso
- **Deep link:** No implementado (no requerido)

---

## 4. Funcionalidades

### Formulario validado
- **Login:** Email (formato válido) + Password (≥6 chars)
- **Registro:** Nombre (≥3 chars), Email, Password (≥6 chars + mayúscula + número), Confirmación, Términos (checkbox)
- Validaciones en tiempo real, mensajes específicos, botón deshabilitado si hay errores

### Navegación y backstack
- Flujo: Splash → Login → Register → Home → Profile
- Splash verifica token → redirige a Home o Login
- Login/Register → Home (con `popUpTo` para limpiar stack)
- Home → Profile (permite retorno)
- Profile → Logout (limpia todo el stack)

### Gestión de estado
- **Estados implementados:**
  - Loading: Durante llamadas API o carga de datos
  - Success: Datos cargados correctamente
  - Error: Errores HTTP (401, 404, 500) o red (IOException)
  - Idle/Empty: Estados iniciales o sin datos
- **Sincronización UI:** `collectAsState()` reacciona automáticamente a cambios en StateFlow

### Persistencia local (CRUD)
- **Room Database:**
  - Usuario: id, nombre, email, avatarUri (imagen de perfil), created_at
  - Obra: id, titulo, artista, descripcion, imageUrl, created_at
- **Operaciones:** insert, getAll, getById, update, delete, Flow para observar cambios
- **DataStore:** Token, userId, email, nombre, isLoggedIn

### Almacenamiento de imagen de perfil
1. Usuario toca botón cambiar foto
2. BottomSheet: elegir Cámara o Galería
3. Solicitar permiso (CAMERA o READ_MEDIA_IMAGES según Android 13+)
4. Capturar/Seleccionar imagen
5. Guardar URI en Room: `updateAvatarUri(userId, uri.toString())`
6. Renderizar con Coil AsyncImage (funciona offline)
7. Persistencia verificada tras reinicio de app

### Recursos nativos
#### Cámara
- Permiso: `CAMERA`
- Launcher: `ActivityResultContracts.TakePicture()`
- FileProvider para URI temporal
- Fallback: Snackbar "Permiso denegado" + opción de galería disponible

#### Galería
- Permiso: `READ_MEDIA_IMAGES` (Android 13+) o `WRITE_EXTERNAL_STORAGE` (Android ≤12)
- Launcher: `ActivityResultContracts.GetContent()` con tipo `image/*`
- Verificación de versión Android
- Fallback: Snackbar "Permiso denegado" + opción de cámara disponible

### Animaciones
1. **Botones Login/Registro:**
   - Pulso infinito cuando habilitado (atrae atención)
   - Escala reduce cuando deshabilitado (feedback visual)
   - Loading indicator durante API call
2. **Mensajes de error:** Fade in/out + slide vertical
3. **Transiciones de estado:** Crossfade entre Loading/Success/Error

### Consumo de API
- **Endpoints usados:** Ver sección 5
- **Manejo de errores:**
  - 400: Datos inválidos
  - 401: Credenciales incorrectas / Token expirado
  - 404: Usuario no encontrado
  - 409: Email ya registrado
  - 500: Error del servidor
  - IOException: Sin conexión a internet
- **Timeout:** 30 segundos (conexión, lectura, escritura)
- **Seguridad:** Token en DataStore (no hardcodeado), AuthInterceptor agrega Bearer token automáticamente

---

## 5. Endpoints

**Base URL:** `https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/`

| Método | Ruta | Body | Headers | Respuesta |
|--------|------|------|---------|-----------|
| POST | /auth/signup | `{ email, password, name, name_lastname, termsAccepted }` | - | 201 `{ authToken, user: { id, email, name, ... } }` |
| POST | /auth/login | `{ email, password }` | - | 200 `{ authToken, user: { id, email, name, ... } }` |
| GET | /auth/me | - | `Authorization: Bearer <token>` | 200 `{ id, email, name, name_lastname, created_at }` |

### Credenciales de testing
```
Email: test@artelab.com
Password: test123
```

---

## 6. User flows

### Flujo principal
```
1. SPLASH
   ├─ Sin token → LOGIN
   └─ Con token → GET /auth/me
       ├─ Válido → HOME
       └─ Inválido (401) → LOGIN

2. LOGIN
   ├─ Ingresar credenciales
   ├─ Validar formato (email, password ≥6)
   ├─ POST /auth/login
   │   ├─ 200 OK → Guardar token → HOME
   │   ├─ 401 → "Credenciales incorrectas"
   │   └─ IOException → "Sin conexión"
   └─ Click "Registrarse" → REGISTER

3. REGISTER
   ├─ Ingresar datos (validaciones en tiempo real)
   ├─ POST /auth/signup
   │   ├─ 201 → Guardar token → HOME
   │   ├─ 409 → "Email ya registrado"
   │   └─ 400 → "Datos inválidos"
   └─ Retroceder → LOGIN

4. HOME
   ├─ Mostrar nombre usuario
   ├─ Lista de obras (Room)
   └─ Click avatar → PROFILE

5. PROFILE
   ├─ Cargar datos: GET /auth/me + Room (avatarUri)
   │   ├─ Loading → CircularProgressIndicator
   │   ├─ Success → Mostrar datos + foto
   │   └─ Error → Mensaje + botón reintentar
   ├─ Cambiar foto → BottomSheet
   │   ├─ Cámara → Permiso → Capturar → Guardar URI en Room
   │   └─ Galería → Permiso → Seleccionar → Guardar URI en Room
   └─ Cerrar sesión → Limpiar DataStore → LOGIN
```

### Casos de error
- **Sin internet:** IOException → Mostrar mensaje → Botón reintentar
- **Token expirado:** 401 en /auth/me → Limpiar sesión → LOGIN
- **Credenciales incorrectas:** 401 en /auth/login → Mensaje de error
- **Permiso denegado:** Snackbar informativo + opción alternativa disponible

---

## Equipo

**Grupo 10**
José Miguel Matte
Ester Quispe

---

**Fecha de entrega:** [3/11/2025]
**Versión:** 1.0
**Estado:** ✅ Listo para evaluación
